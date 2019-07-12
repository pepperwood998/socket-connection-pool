package com.tuan.exercise.connection_pool.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.tuan.exercise.connection_pool.Log;
import com.tuan.exercise.connection_pool.MessageIO;
import com.tuan.exercise.connection_pool.MessageSplitter;

// Each client talk to server in a separated thread
public class ClientHandler extends Thread {

	private final Socket mSocket;
	private DataOutputStream mNetOut;
	private DataInputStream mNetIn;

	private boolean mAvailable;

	public ClientHandler(Socket socket, String clientName) {
		super(clientName);
		mSocket = socket;
	}

	@Override
	public void run() {
		mAvailable = true;

		try {
			mSocket.setSoTimeout(1000 * 20);
			mNetOut = new DataOutputStream(mSocket.getOutputStream());
			mNetIn = new DataInputStream(mSocket.getInputStream());

			while (true) {
				String msgData = MessageIO.readMessage(mNetIn);
				Log.line(msgData);

				MessageSplitter splitter = new MessageSplitter(msgData);
				String cmd = splitter.next();
				if ("login".equals(cmd)) {
					boolean success = doLogin(splitter.next(), splitter.next());
					if (!success) {
						MessageIO.sendMessage(mNetOut, "login failed");
						break;
					}

					Log.line(getName() + " logged in");
					MessageIO.sendMessage(mNetOut, "login success");

				} else if ("msg".equals(cmd)) {
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
		} catch (SocketTimeoutException e) {
			try {
				MessageIO.sendMessage(mNetOut, "timeout");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Log.error("TIMEOUT, NO ACTION", e, false);
		} catch (IOException e) {
			Log.error("Client stream closed", e, false);
		} finally {
			clean();
		}
	}

	private boolean doLogin(String uname, String pwd) {
		boolean success = false;

		if (ServerApp.db.checkAuthentication(uname, pwd))
			success = true;

		return success;
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
