package com.tuan.exercise.connection_pool.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;

public class ConnectionThread extends Thread {

    private static final int MAX_POOL_SIZE = 1;
    private static Set<ClientHandler> mHandlers;
    private static ServerSocket mServer;
    private boolean mActive;

    public ConnectionThread(int port, Set<ClientHandler> handlers) throws IOException {
        mServer = new ServerSocket(port);
        mHandlers = handlers;
        mActive = true;
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
                    if (mHandlers.size() >= MAX_POOL_SIZE) {
                        DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                        MessageIO.sendMessage(netOut, "ddos SERVER_FULL");
//                        netOut.close();
//                        socket.close();
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
}
