package com.tuan.exercise.connection_pool.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;
import com.tuan.exercise.connection_pool.MessageSplitter;

public class ClientApp {

    private static final String HOST_NAME;
    private static final int SERVER_PORT;

    static {
        HOST_NAME = "localhost";
        SERVER_PORT = 6969;
    }

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

    private static class ListeningThread extends Thread {

        DataInputStream mNetIn;

        public ListeningThread(DataInputStream netIn) {
            mNetIn = netIn;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String resMsg = MessageIO.readMessage(mNetIn);

                    MessageSplitter splitter = new MessageSplitter(resMsg);
                    String cmd = splitter.next();
                    if ("timeout".equals(cmd)) {
                        Log.line("YOU TIMEOUT");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {

        NetworkObject netObj = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // login and get network object
        while (netObj == null) {
            Log.log("Enter login command: ");
            String loginCmd = stdIn.readLine();

            netObj = clientLogin(loginCmd);
        }

        // start listening thread
        ListeningThread listeningThread = new ListeningThread(netObj.mNetIn);
        listeningThread.start();

        // sending command section
        try {
            DataOutputStream netOut = netObj.mNetOut;
            while (true) {
                Log.log("Enter command: ");
                String cmdMsg = stdIn.readLine();
                MessageIO.sendMessage(netOut, cmdMsg);
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
                Socket socket = new Socket(HOST_NAME, SERVER_PORT);
                DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream netIn = new DataInputStream(socket.getInputStream());

                // send login command
                MessageIO.sendMessage(netOut, loginCmd);

                // read login command
                String loginRes = MessageIO.readMessage(netIn);
                MessageSplitter splitter = new MessageSplitter(loginRes);

                // check login status
                splitter.skip(1);
                String loginStat = splitter.next();
                if ("success".equals(loginStat)) {
                    Log.line("Login Success");
                    netObj = new NetworkObject(socket, netOut, netIn);
                } else {
                    Log.line("Failed to login");
                    netOut.close();
                    netIn.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return netObj;
    }
}
