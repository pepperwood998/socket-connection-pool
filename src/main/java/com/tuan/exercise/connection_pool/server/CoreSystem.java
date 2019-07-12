package com.tuan.exercise.connection_pool.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.tuan.exercise.connection_pool.Constant;
import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;

public class CoreSystem extends Thread {

    private static Set<ClientHandler> mHandlers;
    private static ServerSocket mServer;
    private boolean mActive;

    public CoreSystem() throws IOException {
        mServer = new ServerSocket(Constant.SERVER_CONN_PORT);
        mHandlers = new HashSet<>();
        mActive = true;

        CleanUpThread cleanUpThread = new CleanUpThread();
        cleanUpThread.start();
    }

    @Override
    public void run() {
        Log.line("Waiting for the first client...");
        while (mActive) {
            try {
                // accept the client
                Socket socket;
                socket = mServer.accept();

                synchronized (mHandlers) {
                    // if server capacity is full
                    if (mHandlers.size() >= Constant.MAX_POOL_SIZE) {
                        DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                        MessageIO.sendMessage(netOut, "ddos SERVER_FULL");
                        continue;
                    }
                }

                // create handling thread for each client
                String clientName = "Client-" + mHandlers.size();
                ClientHandler clientHandler = new ClientHandler(socket, clientName);

                // save the client to a collection
                synchronized (mHandlers) {
                    mHandlers.add(clientHandler);
                }
                clientHandler.start();

                Log.line(clientName + " connected and established");

            } catch (IOException e) {
                Log.error("Failed to initiate socket", e, false);
            }
        }
    }

    private class CleanUpThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (mHandlers) {
                    for (Iterator<ClientHandler> it = mHandlers.iterator(); it.hasNext();) {
                        ClientHandler handler = it.next();
                        if (!handler.isAvailable()) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }
}
