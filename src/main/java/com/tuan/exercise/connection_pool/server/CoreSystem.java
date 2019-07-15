package com.tuan.exercise.connection_pool.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tuan.exercise.connection_pool.Constant;
import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;
import com.tuan.exercise.connection_pool.MessageSplitter;

public class CoreSystem implements ICore {

    private static Set<ClientHandler> mHandlers;
    private static ServerSocket mServer;
    private boolean mConnectionActive;
    private boolean mCleanUpActive;

    public CoreSystem() throws IOException {
        mServer = new ServerSocket(Constant.SERVER_CONN_PORT);
        mHandlers = new HashSet<>();
        mConnectionActive = true;
        mCleanUpActive = true;

        CleanUpThread cleanUpThread = new CleanUpThread();
        ConnectionThread connThread = new ConnectionThread();
        cleanUpThread.start();
        connThread.start();
    }

    @Override
    public int getConnectionNum() {
        return mHandlers.size();
    }

    @Override
    public Map<String, Integer> getSocketData() {
        Map<String, Integer> data = new HashMap<>();

        for (ClientHandler handler : mHandlers) {
            String username = handler.getUsername();
            if (username != null)
                data.put(username, handler.getSocket().getPort());
        }

        return data;
    }

    @Override
    public void terminate(String username) {
        for (ClientHandler handler : mHandlers) {
            String cmpUname = handler.getUsername();
            if (username.equals(cmpUname)) {
                handler.clean();
                break;
            }
        }
    }

    @Override
    public void terminateAll() {
        for (ClientHandler handler : mHandlers) {
            handler.clean();
        }
    }

    @Override
    public void shutdown() throws IOException {
        for (ClientHandler handler : mHandlers) {
            handler.clean();
        }

        // shutdown connection
        mConnectionActive = false;
        mServer.close();

        // shutdown clean-up
        while (!mHandlers.isEmpty())
            ;
        mCleanUpActive = false;
    }

    private boolean doLogin(String uname, String pwd) {
        boolean success = false;
        if (SampleDatabase.checkAuthentication(uname, pwd))
            success = true;
        return success;
    }

    private class ConnectionThread extends Thread {

        @Override
        public void run() {
            Log.line("Waiting for the first client...");
            while (mConnectionActive) {
                try {
                    // accept the client
                    Socket socket;
                    socket = mServer.accept();

                    DataInputStream netIn = new DataInputStream(socket.getInputStream());
                    DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                    synchronized (mHandlers) {
                        // if server capacity is full
                        if (mHandlers.size() >= Constant.MAX_POOL_SIZE) {
                            MessageIO.sendMessage(netOut, "ddos SERVER_FULL");
                            continue;
                        }
                    }

                    // get login data
                    String username = "";
                    MessageSplitter splitter = new MessageSplitter(MessageIO.readMessage(netIn));
                    if ("login".equals(splitter.next())) {
                        username = splitter.next();
                        boolean success = doLogin(username, splitter.next());
                        if (!success) {
                            Log.line("A Client failed to login");
                            MessageIO.sendMessage(netOut, "login failed");
                            continue;
                        }
                        MessageIO.sendMessage(netOut, "login success");
                    }

                    // create handling thread for each client
                    ClientHandler clientHandler = new ClientHandler(socket, username);
                    synchronized (mHandlers) {
                        mHandlers.add(clientHandler);
                    }
                    clientHandler.start();

                    Log.line(username + " connected and established");

                } catch (IOException e) {
                    Log.error("Failed to initiate socket or server closed", e, false);
                }
            }
        }
    }

    private class CleanUpThread extends Thread {

        @Override
        public void run() {
            while (mCleanUpActive) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (mHandlers) {
                    for (Iterator<ClientHandler> it = mHandlers.iterator(); it.hasNext();) {
                        ClientHandler handler = it.next();
                        if (System.currentTimeMillis() - handler.getLastIOTime() >= Constant.NO_ACTION_TIMEOUT) {
                            handler.responseTimeOut();
                            handler.clean();
                        }

                        if (!handler.isAvailable()) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }
}
