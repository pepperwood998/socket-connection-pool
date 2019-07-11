package com.tuan.exercise.connection_pool;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MessageSplitter {
    private List<String> mParts;
    private Iterator<String> it;

    public MessageSplitter(String msg) {
        String[] parts = msg.split(" ");
        mParts = Arrays.asList(parts);
        it = mParts.iterator();
    }

    public String next() {
        return it.next();
    }
}
