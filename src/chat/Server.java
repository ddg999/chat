package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JTextArea;

public class Server {
	private ServerFrame serverFrame;

	private ServerSocket serverSocket;
	private Socket socket;

	private JTextArea serverMsg;

	// 프로토콜 변수
	private String protocol;
	private String data;

	// 접속한 유저 벡터
	private Vector<ConnectedUser> connectedUsers = new Vector<>();
	// 만들어진 방 벡터
	private Vector<MyRoom> madeRooms = new Vector<>();

	public Server() {
		serverFrame = new ServerFrame(this);
		serverMsg = serverFrame.getServerMsg();
	}

	public void startServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			serverMsgWriter("[알림] 서버 소켓 생성, 포트 번호 : " + port + "\n");
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
					ConnectedUser user = new ConnectedUser(socket);
					user.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void broadCast(String msg) {
		for (int i = 0; i < connectedUsers.size(); i++) {
			ConnectedUser user = connectedUsers.elementAt(i);
			user.writer(msg);
		}
	}

	private class ConnectedUser extends Thread {
		private Socket socket;

		private PrintWriter socketWriter;
		private BufferedReader socketReader;

		private String id;
		private String myRoomName;

		public ConnectedUser(Socket socket) {
			this.socket = socket;
			// 입출력 연결
			connectIO();
			// 기존 유저에게 새로운 유저를 목록에 추가
			newUser();
			// 새로운 유저에게 기존유저 목록 갱신
			connectedUser();
			// 방 목록 갱신
			madeRoom();
		}

		private void connectIO() {
			try {
				socketWriter = new PrintWriter(socket.getOutputStream(), true);
				socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				id = socketReader.readLine();
				serverMsgWriter("[접속] " + id + "님 입장\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void writer(String msg) {
			socketWriter.println(msg);
		}

		public void newUser() {
			connectedUsers.add(this);
			broadCast("NewUser:" + id);
		}

		public void connectedUser() {
			for (int i = 0; i < connectedUsers.size(); i++) {
				ConnectedUser user = connectedUsers.elementAt(i);
				writer("ConnectedUser:" + user.id);
			}
		}

		public void madeRoom() {
			for (int i = 0; i < madeRooms.size(); i++) {
				MyRoom myRoom = madeRooms.elementAt(i);
				writer("MadeRoom:" + myRoom.roomName);
			}
		}

		public void checkProtocol(String msg) {
			if (!msg.equals("")) { // TODO 오류 때문에 조건문 설정해놓음, 오류 안뜨는 다른 방법 찾기
				String[] parts = msg.split(":", 2);
				protocol = parts[0];
				data = parts[1];
				if (protocol.equals("MakeRoom")) {
					makeRoom();
				} else if (protocol.equals("OutRoom")) {
					outRoom();
				}
			}
		}

		public void makeRoom() {
			myRoomName = data;
			MyRoom myRoom = new MyRoom(myRoomName, this);
			madeRooms.add(myRoom);
			serverMsgWriter("[방 생성] " + id + "_" + myRoomName + "\n");

			newRoom();
			writer("MakeRoom:" + data);
		}

		public void newRoom() {
			broadCast("NewRoom:" + data);
		}

		public void outRoom() {
			for (int i = 0; i < madeRooms.size(); i++) {
				MyRoom myRoom = madeRooms.elementAt(i);

				if (myRoom.roomName.equals(data)) {
					myRoomName = null;
//					myRoom.roomBroadCast("Chatting/퇴장/" + id + "님 퇴장");
					serverMsgWriter("[방 퇴장]" + id + "_" + data + "\n");
					myRoom.removeRoom(this);
					writer("OutRoom:" + data);
				}
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					String msg = socketReader.readLine();
					checkProtocol(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class MyRoom {
		// MyRoom에 들어온 유저 정보

		private String roomName;
		private Vector<ConnectedUser> myRoom = new Vector<>();

		public MyRoom(String roomName, ConnectedUser connectedUser) {
			this.roomName = roomName;
			this.myRoom.add(connectedUser);
		}

		private void removeRoom(ConnectedUser user) {
			myRoom.remove(user);
			boolean empty = myRoom.isEmpty();
			if (empty) {
				for (int i = 0; i < madeRooms.size(); i++) {
					MyRoom myRoom = madeRooms.elementAt(i);

					if (myRoom.roomName.equals(roomName)) {
						madeRooms.remove(this);
						serverMsgWriter("[방 삭제]" + user.id + "_" + data + "\n");
//						roomBroadCast("OutRoom/" + data);
						broadCast("EmptyRoom:" + data);
						break;
					}
				}
			}
		}
	}

	private void serverMsgWriter(String msg) {
		serverMsg.append(msg);
	}

	public static void main(String[] args) {
		new Server();
	}
}
