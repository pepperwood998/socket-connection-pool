package com.tuan.exercise.connection_pool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessageIO {
    private static final int MSG_BUFFER = 1024;
    private static byte[] msgBuffer = new byte[MSG_BUFFER];

    public static void sendMessage(DataOutputStream netOut, String message) throws IOException {
        byte[] bytes = message.getBytes();
        netOut.writeLong(bytes.length);
        netOut.write(bytes);
    }

    public static String readMessage(DataInputStream netIn) throws IOException {
        long dataSize = netIn.readLong();
        int bytesRead = 0;
        long totalRead = 0L;
        StringBuilder msgData = new StringBuilder();
        while ((bytesRead = netIn.read(msgBuffer)) > 0) {
            String msgPart = new String(msgBuffer, 0, bytesRead);
            msgData.append(msgPart);

            totalRead += bytesRead;
            if (totalRead >= dataSize)
                break;
        }

        return msgData.toString();
    }
}
