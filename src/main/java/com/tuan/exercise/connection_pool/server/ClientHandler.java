package com.tuan.exercise.connection_pool.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.tuan.exercise.connection_pool.Log;

// Each client talk to server in a separated thread
public class ClientHandler extends Thread {

    private final Socket mSocket;
    private DataOutputStream mNetOut;
    private DataInputStream mNetIn;

    private boolean mAvailable;

    private Thread readChannel;

    public ClientHandler(Socket socket, String clientName) {
        super(clientName);
        mSocket = socket;
    }

    @Override
    public void run() {
        mAvailable = true;

        final int MSG_BUFFER = 1024;
        byte[] msgBuffer = new byte[MSG_BUFFER];
        try {
            mSocket.setSoTimeout(1000 * 2);
            mNetOut = new DataOutputStream(mSocket.getOutputStream());
            mNetIn = new DataInputStream(mSocket.getInputStream());

            while (true) {
                long dataSize = mNetIn.readLong();
                // read message data
                int bytesRead = 0;
                long totalRead = 0L;
                StringBuilder msgData = new StringBuilder();
                while ((bytesRead = mNetIn.read(msgBuffer)) > 0) {
                    String msgPart = new String(msgBuffer, 0, bytesRead);
                    msgData.append(msgPart);

                    totalRead += bytesRead;
                    if (totalRead >= dataSize)
                        break;
                }
                Log.line(msgData.toString());

                String[] parts = msgData.toString().split(" ");
                String cmd = parts[0];
                if ("login".equals(cmd)) {
                    boolean failed = doLogin(parts[1], parts[2]);
                    if (failed)
                        break;

                    Log.line("A client logged in");
                } else if ("msg".equals(cmd)) {

                } else if ("quit".equals(cmd)) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            Log.error("Client killed due to no active action", e, false);
        } catch (IOException e) {
            Log.error("Client stream closed", e, false);
        } finally {
            clean();
        }

        if (readChannel != null) {
            readChannel.start();
        }
    }

    private boolean doLogin(String uname, String pwd) {
        boolean failed = true;

        if ("abc".equals(uname) && "123".equals(pwd))
            failed = false;

        return failed;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void clean() {
        try {
            mAvailable = false;
            mNetOut.close();
            mNetIn.close();
            mSocket.close();
        } catch (IOException e) {
            Log.error("Socket resource clean up interrupted", e, false);
        }
    }
}
