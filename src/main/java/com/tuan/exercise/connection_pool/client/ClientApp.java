package com.tuan.exercise.connection_pool.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.tuan.exercise.connection_pool.Log;

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
            while (true) {
                // listen to server messages
            }
        }
    }

    public static void main(String[] args) throws IOException {

        NetworkObject netObj = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

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
                String cmd = stdIn.readLine();
                netOut.writeLong(cmd.getBytes().length);
                netOut.write(cmd.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static NetworkObject clientLogin(String loginCmd) {
        NetworkObject netObj = null;
        String[] parts = loginCmd.split(" ");

        if ("login".equals(parts[0])) {
            Socket socket;
            try {
                socket = new Socket(HOST_NAME, SERVER_PORT);
                DataOutputStream netOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream netIn = new DataInputStream(socket.getInputStream());

                // send cmd length
                
                // send cmd message
                
                // receive login response
                
                // check login status
                
                // create NetworkObject if login ok
            } catch (IOException e) {
                // print corresponding error
            }
        }

        return netObj;
    }
}
