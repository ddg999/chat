package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;
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
			serverMsgWriter("[알림] 서버 소켓 생성, 포트 번호 : " + port);
			connectClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connectClient() {
		new Thread(() -> {
			while (true) {
				try {
					socket = serverSocket.accept();
					serverMsgWriter("[알림] 유저 접속 대기");
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
