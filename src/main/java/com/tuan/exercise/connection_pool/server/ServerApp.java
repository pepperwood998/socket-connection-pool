package com.tuan.exercise.connection_pool.server;

import java.io.IOException;

import com.tuan.exercise.connection_pool.Log;

public class ServerApp {

    public static void main(String[] args) throws IOException {
        Log.line("--- SERVER ---");
        CoreSystem conThread = new CoreSystem();
        conThread.start();
    }
}
