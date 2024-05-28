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

		private PrintWriter socketWriter;
		private BufferedReader socketReader;

		private String id;
		private String myRoomName;

		//
		public ConnectedUser(Socket socket) {
			this.socket = socket;
			// 입출력 연결
			connectIO();

			newUser();

			connectedUser();

			madeRoom();
		}

		// 서버 측 입/출력 장치 생성
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

		// 서버 -> 클라이언트 메세지 전송하기
		private void writer(String msg) {
			socketWriter.println(msg);
		}

		// 기존 유저에게 새로운 유저를 목록에 추가
		@Override
		public void newUser() {
			connectedUsers.add(this);
			broadCast("NewUser:" + id + ": ");
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

		// 프로토콜 체크
		public void checkProtocol(String msg) {
			if (!msg.equals("")) { // TODO 오류 때문에 조건문 설정해놓음, 오류 안뜨는 다른 방법 찾기
				String[] parts = msg.split(":", 3);
				protocol = parts[0];
				data = parts[1];
				message = parts[2];

				if (protocol.equals("MakeRoom")) {
					makeRoom();
				} else if (protocol.equals("OutRoom")) {
					outRoom();
				} else if (protocol.equals("EnterRoom")) {
					enterRoom();
				} else if (protocol.equals("Chatting")) {
					chatting();
				} else if (protocol.equals("SecretMsg")) {
					secretMsg();
				}
			}
		}

		// 새로운 방 생성하기
		@Override
		public void makeRoom() {
			myRoomName = data;
			MyRoom myRoom = new MyRoom(myRoomName, this);
			madeRooms.add(myRoom);
			serverMsgWriter("[방 생성] " + id + "_" + myRoomName + "\n");

			newRoom();
			writer("MakeRoom:" + data + ": ");
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
					serverMsgWriter("[방 퇴장] " + id + "_" + data + "\n");
					myRoom.removeRoom(this);
					writer("OutRoom:" + data + ": ");
				}
			}
		}

		// 방 입장하기
		@Override
		public void enterRoom() {
			for (int i = 0; i < madeRooms.size(); i++) {
				MyRoom myRoom = madeRooms.elementAt(i);
				if (myRoom.roomName.equals(data)) {
					myRoomName = data;
					myRoom.addUser(this);
					myRoom.roomBroadCast("Chatting:입장:" + id + "님 입장");
					serverMsgWriter("[방 입장] " + data + " 방_" + id + "\n");
					writer("EnterRoom:" + data + ": ");
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

		@Override
		public void run() {
			try {
				while (true) {
					String msg = socketReader.readLine();
					checkProtocol(msg);
				}
			} catch (IOException e) {
				serverMsgWriter("[에러] " + id + "님 접속 끊김\n");
				JOptionPane.showMessageDialog(null, id + "님의 접속이 끊어졌습니다", "[알림]", JOptionPane.ERROR_MESSAGE);
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

		// 방 생성자
		public MyRoom(String roomName, ConnectedUser connectedUser) {
			this.roomName = roomName;
			this.myRoom.add(connectedUser);
		}

		// 방에 유저 추가
		private void addUser(ConnectedUser connectedUser) {
			myRoom.add(connectedUser);
		}

		// 방에서 유저 나가기(제거), 방에 유저가 없으면 방 제거
		private void removeRoom(ConnectedUser user) {
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

		// 방에 있는 모든 유저에게 채팅 전송
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
