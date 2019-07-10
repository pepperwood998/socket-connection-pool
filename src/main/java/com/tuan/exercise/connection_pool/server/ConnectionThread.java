package com.tuan.exercise.connection_pool.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.tuan.exercise.connection_pool.Log;

public class ConnectionThread extends Thread {

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
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        while (mActive) {
            try {
                // accept the client
                Socket socket;
                socket = mServer.accept();

                // create handling thread for each client
                String clientName = "Client-" + mHandlers.size();
                ClientHandler clientHandler = new ClientHandler(socket, clientName);

                // save the client to a collection
                synchronized (mHandlers) {
                    mHandlers.add(clientHandler);
                }
//                clientHandler.start();
                executor.execute(clientHandler);

                Log.line(clientName + " connected and established");

            } catch (IOException e) {
                Log.error("Failed to initiate socket", e, false);
            }
        }
        executor.shutdown();
    }
}
