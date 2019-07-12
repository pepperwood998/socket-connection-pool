package com.tuan.exercise.connection_pool.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.tuan.exercise.connection_pool.Constant;
import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;
import com.tuan.exercise.connection_pool.MessageSplitter;

public class ClientApp {
    private static NetworkObject mNetObj = null;

    private static class NetworkObject {
        Socket mSocket;
        DataOutputStream mNetOut;
        DataInputStream mNetIn;

        public NetworkObject(Socket socket, DataOutputStream netOut, DataInputStream netIn) {
            mSocket = socket;
            mNetOut = netOut;
            mNetIn = netIn;
        }
    }

    public static void main(String[] args) throws IOException {

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // login and get network object
        while (mNetObj == null) {
            Log.log("Enter login command: ");
            String loginCmd = stdIn.readLine();

            mNetObj = clientLogin(loginCmd);
        }

        // start listening thread
        ListeningThread listeningThread = new ListeningThread();
        listeningThread.start();

        // sending command section
        try {
            DataOutputStream netOut = mNetObj.mNetOut;
            while (true) {
                Log.log("Enter command: ");
                String cmdMsg = stdIn.readLine();
                MessageIO.sendMessage(netOut, cmdMsg);
                if ("quit".equals(cmdMsg))
                    break;
            }
        } catch (IOException e) {
            Log.error("Network stream closed", e, false);
        }
    }

    private static NetworkObject clientLogin(String loginCmd) {
        NetworkObject netObj = null;
        String[] parts = loginCmd.split(" ");

        if ("login".equals(parts[0])) {
            try {
                Socket socket = new Socket(Constant.HOST_NAME, Constant.SERVER_CONN_PORT);
                DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream netIn = new DataInputStream(socket.getInputStream());

                // send login command
                MessageIO.sendMessage(netOut, loginCmd);

                // read login command
                String loginRes = MessageIO.readMessage(netIn);
                MessageSplitter splitter = new MessageSplitter(loginRes);

                // check login status
                String cmd = splitter.next();
                if ("ddos".equals(cmd)) {
                    Log.line(splitter.next());
                } else if ("login".equals(cmd)) {
                    String loginStat = splitter.next();
                    if ("success".equals(loginStat)) {
                        Log.line("Login Success");
                        netObj = new NetworkObject(socket, netOut, netIn);
                    } else {
                        Log.line("Failed to login");
                    }
                }

                if (netObj == null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.error("Network stream closed", e, false);
            }
        }

        return netObj;
    }

    private static class ListeningThread extends Thread {

        @Override
        public void run() {
            DataInputStream netIn = mNetObj.mNetIn;

            try {
                while (true) {
                    String resMsg = MessageIO.readMessage(netIn);

                    MessageSplitter splitter = new MessageSplitter(resMsg);
                    String cmd = splitter.next();
                    if ("timeout".equals(cmd)) {
                        Log.line("YOU TIMEOUT");
                        break;
                    } else if ("quit".equals(cmd)) {
                        Log.line("QUIT " + splitter.next());
                        break;
                    } else if ("msglen".equals(cmd)) {
                        Log.line(splitter.next());
                    }
                }
            } catch (IOException e) {
                Log.error("Network stream closed", e, false);
            } finally {
                try {
                    mNetObj.mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
