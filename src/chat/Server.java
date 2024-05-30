package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Server {
	private ServerFrame serverFrame;

	private ServerSocket serverSocket;
	private Socket socket;

	private JTextArea serverMsg;

	// 프로토콜 변수
	private String protocol;
	private String data;
	private String message;

	// 접속한 유저 벡터
	private Vector<ConnectedUser> connectedUsers = new Vector<>();

	// 만들어진 방 벡터
	private Vector<MyRoom> madeRooms = new Vector<>();

	// 서버 생성자, 서버프레임 실행
	public Server() {
		serverFrame = new ServerFrame(this);
		serverMsg = serverFrame.getServerMsg();
	}

	// 서버 시작하기 (서버 소켓 생성)
	public void startServer(int port) {
		try {
			serverSocket = new ServerSocket(port);
			serverMsgWriter("[알림] 서버 생성, 포트 번호 : " + port + "\n");
			connectClient();
			serverFrame.getStartBtn().setEnabled(false);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 존재하는 서버입니다");
		}
	}

	// 소켓 accept 대기하는 스레드, accept 되면 연결된 유저 객체 생성
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

	// 서버 -> 연결된 클라이언트 전체에 방송하기
	private void broadCast(String msg) {
		for (ConnectedUser connectedUser : connectedUsers) {
			connectedUser.writer(msg);
		}
	}

	private class ConnectedUser extends Thread implements Protocol {
		private Socket socket;

		// 서버 측 입출력 변수
		private PrintWriter socketWriter;
		private BufferedReader socketReader;

		// 서버 측에서 관리 할 아이디, 방 이름 변수
		private String id;
		private String myRoomName = "";

		// 접속종료 변수
		private boolean logout;

		public ConnectedUser(Socket socket) {
			this.socket = socket;
			connectIO();
		}

		// 서버 측 입출력 스트림 생성
		private void connectIO() {
			try {
				socketWriter = new PrintWriter(socket.getOutputStream(), true);
				socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 서버 -> 클라이언트 메세지 전송하기
		private void writer(String msg) {
			socketWriter.println(msg);
		}

		// 새로운 유저 추가
		@Override
		public void newUser() {
			// 닉네임 중복 구분 변수
			boolean duplicateName = false;
			for (ConnectedUser connectedUser : connectedUsers) {
				if (data.equals(connectedUser.id)) {
					serverMsgWriter("[로그인에러] 중복닉네임_" + data + "\n");
					writer("LoginError:" + data + ": ");
					duplicateName = true;
					logout = true;
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
			if (!duplicateName) {
				id = data;
				serverMsgWriter("[알림] " + id + "님 로그인\n");
				connectedUsers.add(this);
				broadCast("NewUser:" + id + ": ");

				connectedUser();
				madeRoom();
			}
		}

		// 새로운 유저에게 기존 유저 목록 갱신
		@Override
		public void connectedUser() {
			for (int i = 0; i < connectedUsers.size(); i++) {
				ConnectedUser user = connectedUsers.elementAt(i);
				writer("ConnectedUser:" + user.id + ": ");
			}
		}

		// 새로운 유저에게 만들어진 방 목록 갱신
		@Override
		public void madeRoom() {
			for (int i = 0; i < madeRooms.size(); i++) {
				MyRoom myRoom = madeRooms.elementAt(i);
				writer("MadeRoom:" + myRoom.roomName + ": ");
			}
		}

		// 프로토콜 체크 (구분자 :) protocol:data:message
		public void checkProtocol(String msg) {
			try {
				String[] parts = msg.split(":", 3);
				protocol = parts[0];
				data = parts[1];
				message = parts[2];
				if (protocol.equals("NewUser")) {
					newUser();
				} else if (protocol.equals("MakeRoom")) {
					makeRoom();
				} else if (protocol.equals("OutRoom")) {
					outRoom();
				} else if (protocol.equals("EnterRoom")) {
					enterRoom();
				} else if (protocol.equals("Chatting")) {
					chatting();
				} else if (protocol.equals("SecretMsg")) {
					secretMsg();
				} else if (protocol.equals("Logout")) {
					logout();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 새로운 방 생성하기
		@Override
		public void makeRoom() {
			// 방 이름 중복 구분 변수
			boolean duplicateRoom = false;
			for (MyRoom myRoom : madeRooms) {
				if (data.equals(myRoom.roomName)) {
					serverMsgWriter("[방생성에러] 방이름중복_" + data + "\n");
					duplicateRoom = true;
					break;
				}
			}
			if (!duplicateRoom) {
				MyRoom myRoom = new MyRoom(data, this); // 새로운 방 생성(방이름, 현재 연결된유저)
				madeRooms.add(myRoom); // 만들어진 방 벡터에 새로운 방 추가
				myRoomName = data;
				serverMsgWriter("[방 생성] " + id + "_" + data + "\n");
				newRoom();
				writer("Chatting:입장:" + id + "님 입장");
				writer("MakeRoom:" + data + ": ");
				writer("NewChatList:" + id + ": ");
			}
		}

		// 방 목록 갱신
		@Override
		public void newRoom() {
			broadCast("NewRoom:" + data + ": ");
		}

		// 방 퇴장하기
		@Override
		public void outRoom() {
			for (int i = 0; i < madeRooms.size(); i++) {
				MyRoom myRoom = madeRooms.elementAt(i);
				if (myRoom.roomName.equals(data)) {
					myRoomName = null;
					myRoom.roomBroadCast("Chatting:퇴장:" + id + "님 퇴장");
					// 해당 방의 기존 유저에게 새로운 유저를 방참가목록 갱신
					myRoom.roomBroadCast("NewChatList:" + id + ":삭제");
					serverMsgWriter("[방 퇴장] " + id + "_" + data + "\n");
					myRoom.removeRoom(this);
					writer("OutRoom:" + data + ": ");
				}
			}
		}

		// 방 입장하기
		@Override
		public void enterRoom() {
			for (int i = 0; i < madeRooms.size(); i++) { // 해당 이름의 방 찾기
				MyRoom myRoom = madeRooms.elementAt(i);
				if (myRoom.roomName.equals(data)) { // 해당 이름과 클라이언트에서 보낸 방 이름이 같으면
					myRoomName = myRoom.roomName;
					// 해당 방의 기존 유저에게 새로운 유저를 방참가목록 갱신
					myRoom.roomBroadCast("NewChatList:" + id + ": ");

					// 이 유저를 해당 방 ConnectUser 벡터에 추가
					myRoom.addUser(this);

					// 이 유저를 클라이언트 측 방참가목록에 추가
					writer("EnterRoom:" + data + ": ");

					// 새로운 유저에게 해당 방 기존 유저를 방참가목록에 추가
					for (String ChatUser : myRoom.roomUser) {

						writer("EnteredChatList:" + ChatUser + ": ");
					}
					// 해당 방 채팅창에 입장알림
					myRoom.roomBroadCast("Chatting:입장:" + id + "님 입장");
					serverMsgWriter("[방 입장] " + data + " 방_" + id + "\n");
				}
			}
		}

		// 채팅 전송하기
		@Override
		public void chatting() {
			serverMsgWriter("[채팅] " + data + "_" + id + "_" + message + "\n");
			for (MyRoom myRoom : madeRooms) {
				if (myRoom.roomName.equals(data)) {
					myRoom.roomBroadCast("Chatting:" + id + ":" + message);
				}
			}
		}

		// 쪽지 보내기
		@Override
		public void secretMsg() {
			serverMsgWriter("[쪽지] " + id + "ㅡ>" + data + "_" + message + "\n");
			for (int i = 0; i < connectedUsers.size(); i++) {
				ConnectedUser user = connectedUsers.elementAt(i);
				if (user.id.equals(data)) {
					user.writer("SecretMsg:" + id + ":" + message);
				}
			}
		}

		// 로그아웃
		@Override
		public void logout() {
			serverMsgWriter("[알림] " + data + "님 로그아웃\n");
			writer("Logout:" + data + ": ");

			connectedUsers.remove(this);
			broadCast("UserOut:" + data + ": ");

			logout = true;
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (!logout) {
					String msg = socketReader.readLine();
					checkProtocol(msg);
				}
			} catch (IOException e) {
				serverMsgWriter("[에러] " + id + "님 접속 끊김\n");
				outRoom();
				connectedUsers.remove(this);
				broadCast("UserOut:" + id + ": ");
			}
		}
	}

	// 각 방을 관리하기 위한 클래스
	private class MyRoom {

		// 방 이름
		private String roomName;

		// 방에 들어온 ConnectedUser 벡터
		private Vector<ConnectedUser> myRoom = new Vector<>();
		// 방에 들어온 유저명 벡터
		private Vector<String> roomUser = new Vector<>();

		// 방 생성자
		public MyRoom(String roomName, ConnectedUser connectedUser) {
			this.roomName = roomName;
			this.myRoom.add(connectedUser);
			roomUser.add(connectedUser.id);
		}

		// 방에 유저 추가
		private void addUser(ConnectedUser connectedUser) {
			myRoom.add(connectedUser);
			roomUser.add(connectedUser.id);
		}

		// 방에서 유저 나가기(제거), 방에 유저가 없으면 방 제거
		private void removeRoom(ConnectedUser user) {
			roomUser.remove(user.id);
			myRoom.remove(user);
			if (myRoom.isEmpty()) {
				for (int i = 0; i < madeRooms.size(); i++) {
					MyRoom myRoom = madeRooms.elementAt(i);

					if (myRoom.roomName.equals(roomName)) {
						// 해당 방을 만들어진 방 벡터에서 삭제
						madeRooms.remove(this);
						serverMsgWriter("[방 삭제] " + user.id + "_" + data + "\n");
						roomBroadCast("OutRoom:" + data + ": ");
						broadCast("EmptyRoom:" + data + ": ");
						break;
					}
				}
			}
		}

		// 방에 있는 모든 유저에게 전송
		private void roomBroadCast(String msg) {
			for (int i = 0; i < myRoom.size(); i++) {
				ConnectedUser user = myRoom.elementAt(i);
				user.writer(msg);
			}
		}
	}

	// 서버에 메세지 출력
	private void serverMsgWriter(String msg) {
		serverMsg.append(msg);
	}

	public static void main(String[] args) {
		new Server();
	}
}
