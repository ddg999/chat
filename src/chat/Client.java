package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JList;

public class Client {

	private ClientFrame clientFrame;

	// 연결 서버 정보
	private String ip;
	private int port;

	// 유저 정보
	private String id;
	private String myRoomName;

	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;
	private BufferedReader keyboardReader;

	private Vector<String> userVector = new Vector<>();
	private Vector<String> roomVector = new Vector<>();
	private JList<String> userList = new JList<>();
	private JList<String> roomList = new JList<>();

	// 프로토콜 변수
	private String protocol;
	private String data;

	public Client() {
		clientFrame = new ClientFrame(this);
	}

	public void clickLoginBtn(String ip, int port, String id) {
		this.ip = ip;
		this.port = port;
		this.id = id;

		try {
			socket = new Socket(ip, port);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);

			writer(id + "\n");
			System.out.println("연결완료");
			clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
			clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(true);

			readThread();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writer(String msg) {
		socketWriter.println(msg);
	}

	private void readThread() {
		new Thread(() -> {
			while (true) {
				try {
					String protocol = socketReader.readLine();
					checkProtocol(protocol);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void checkProtocol(String msg) {
		String[] parts = msg.split(":", 2);
		protocol = parts[0];
		data = parts[1];
		if (protocol.equals("NewUser")) {
			newUser();
		} else if (protocol.equals("ConnectedUser")) {
			connectedUser();
		} else if (protocol.equals("MadeRoom")) {
			madeRoom();
		} else if (protocol.equals("MakeRoom")) {
			// 새로운 방 이름 설정
			makeRoom();
		} else if (protocol.equals("NewRoom")) {
			// 기존 유저들에게 새로운 방 갱신
			newRoom();
		} else if (protocol.equals("OutRoom")) {
			outRoom();
		} else if (protocol.equals("EmptyRoom")) {
			emptyRoom();
		}
	}

	public void newUser() {
		if (!data.equals(this.id)) {
			userVector.add(data);
			userList.setListData(userVector);
		}
	}

	public void connectedUser() {
		userVector.add(data);
		userList.setListData(userVector);
	}

	public void madeRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	public void makeRoom() {
		myRoomName = data;
	}

	public void newRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	public void outRoom() {
		myRoomName = null;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(false);
	}

	public void emptyRoom() {
		roomVector.remove(data);
		roomList.setListData(roomVector);
	}

	public void clickMakeRoomBtn(String roomName) {
		writer("MakeRoom:" + roomName);
	}

	public void clickOutRoomBtn(String roomName) {
		writer("OutRoom:" + roomName);
	}

	public JList<String> getUserList() {
		return userList;
	}

	public JList<String> getRoomList() {
		return roomList;
	}

	public Vector<String> getUserVector() {
		return userVector;
	}

	public Vector<String> getRoomVector() {
		return roomVector;
	}

	public static void main(String[] args) {
		new Client();
	}

}
