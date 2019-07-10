package com.tuan.exercise.connection_pool.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.tuan.exercise.connection_pool.Log;

public class ServerApp {

    private static final int PORT;
    private static Set<ClientHandler> mHandlers;

    static {
        PORT = 6969;
        mHandlers = new HashSet<>();
    }

    public static void main(String[] args) throws IOException {
        Log.line("--- SERVER ---");

        ConnectionThread conThread = new ConnectionThread(PORT, mHandlers);
        CleanUpThread cleanUpThread = new CleanUpThread(mHandlers);
        
        conThread.start();
        cleanUpThread.start();
    }
}
