package com.tuan.exercise.connection_pool.server;

import java.util.Iterator;
import java.util.Set;

import com.tuan.exercise.connection_pool.Log;

public class CleanUpThread extends Thread {

//    private static final int WAITING_TIME_OUT = 1000 * 60;
    private static Set<ClientHandler> mHandlers;

    public CleanUpThread(Set<ClientHandler> handlers) {
        mHandlers = handlers;
    }

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
                        Log.line(handler.getName() + " disconnected");
                    }
                }
            }
        }
    }
}
