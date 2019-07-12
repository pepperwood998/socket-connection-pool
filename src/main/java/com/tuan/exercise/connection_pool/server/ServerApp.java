package com.tuan.exercise.connection_pool.server;

import java.io.IOException;

import com.tuan.exercise.connection_pool.Log;

public class ServerApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        Log.line("--- SERVER ---");
        CoreSystem conThread = new CoreSystem();

//        Thread.sleep(5000);
//        conThread.shutdown();
    }
}
