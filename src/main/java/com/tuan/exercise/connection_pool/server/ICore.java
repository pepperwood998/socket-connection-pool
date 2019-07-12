package com.tuan.exercise.connection_pool.server;

import java.io.IOException;
import java.util.Map;

public interface ICore {

    public int getConnectionNum();

    public Map<String, Integer> getSocketData();

    public void terminate(String username);

    public void terminateAll();

    // shutdown server
    public void shutdown() throws IOException;
}
