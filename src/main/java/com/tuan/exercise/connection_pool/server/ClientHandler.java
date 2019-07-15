package com.tuan.exercise.connection_pool.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;
import com.tuan.exercise.connection_pool.MessageSplitter;

// Each client talk to server in a separated thread
public class ClientHandler extends Thread {

    private final Socket mSocket;
    private DataOutputStream mNetOut;
    private DataInputStream mNetIn;

    private boolean mAvailable;
    private long mLastIOTime;
    private String mUsername;

    public ClientHandler(Socket socket, String clientName) {
        super(clientName);
        mSocket = socket;
        mUsername = null;
    }

    @Override
    public void run() {
        mAvailable = true;
        mLastIOTime = System.currentTimeMillis();

        try {
            mNetOut = new DataOutputStream(mSocket.getOutputStream());
            mNetIn = new DataInputStream(mSocket.getInputStream());

            while (true) {
                String msgData = MessageIO.readMessage(mNetIn);
                mLastIOTime = System.currentTimeMillis();

                MessageSplitter splitter = new MessageSplitter(msgData);
                String cmd = splitter.next();
                if ("msg".equals(cmd)) {
                    StringBuilder sentence = new StringBuilder();
                    String word;
                    while ((word = splitter.next()) != null)
                        sentence.append(word).append(" ");
                    String message = sentence.toString().trim();
                    Log.line(">>>" + getName() + ":" + message);
                    MessageIO.sendMessage(mNetOut, "msglen " + message.length());
                } else if ("quit".equals(cmd)) {
                    MessageIO.sendMessage(mNetOut, "quit ok");
                    break;
                }
            }
        } catch (IOException e) {
            Log.error("Client stream closed", e, false);
        } finally {
            Log.line(getName() + " disconnected");
            clean();
        }
    }

    public void responseTimeOut() {
        try {
            MessageIO.sendMessage(mNetOut, "timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.line("TIMEOUT, NO ACTION");
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public long getLastIOTime() {
        return mLastIOTime;
    }

    public String getUsername() {
        return mUsername;
    }

    public Socket getSocket() {
        return mSocket;
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
