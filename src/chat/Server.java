package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JTextArea;

public class Server {
	private ServerFrame serverFrame;

	private ServerSocket serverSocket;
	private Socket socket;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;
	private BufferedReader keyboardReader;

	private JTextArea serverMsg;

	public Server() {
		serverFrame = new ServerFrame(this);
		serverMsg = serverFrame.getServerMsg();
	}

	public void startServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			serverMsgWriter("서버 소켓 생성, 포트 번호 : " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectClient() {
		new Thread(() -> {
			while (true) {
				try {
					socket = serverSocket.accept();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

	}

	private void serverMsgWriter(String msg) {
		serverMsg.append(msg);
	}

	public static void main(String[] args) {
		new Server();
	}
}
