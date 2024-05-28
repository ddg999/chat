package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JOptionPane;

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
	private String message;

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
			clientFrame.getLoginPanel().getLoginBtn().setEnabled(false);
			clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
			clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(true);

			readThread();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "서버가 닫혀 있습니다");
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
		String[] parts = msg.split(":", 3);
		protocol = parts[0];
		data = parts[1];
		message = parts[2];
		if (protocol.equals("NewUser")) {
			newUser();
		} else if (protocol.equals("ConnectedUser")) {
			connectedUser();
		} else if (protocol.equals("MadeRoom")) {
			madeRoom();
		} else if (protocol.equals("MakeRoom")) {
			makeRoom();
		} else if (protocol.equals("NewRoom")) {
			newRoom();
		} else if (protocol.equals("OutRoom")) {
			outRoom();
		} else if (protocol.equals("EnterRoom")) {
			enterRoom();
		} else if (protocol.equals("EmptyRoom")) {
			emptyRoom();
		} else if (protocol.equals("Chatting")) {
			chatting();
		} else if (protocol.equals("SecretMsg")) {
			secretMsg();
		}
	}

	// 유저 목록 추가하기
	public void newUser() {
		if (!data.equals(this.id)) {
			userVector.add(data);
			userList.setListData(userVector);
		}
	}

	// 유저 목록 갱신하기
	public void connectedUser() {
		userVector.add(data);
		userList.setListData(userVector);
	}

	// 방 목록 갱신하기
	public void madeRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	// 내 방 생성하기
	public void makeRoom() {
		myRoomName = data;
	}

	// 방 목록 추가하기
	public void newRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	// 내 방 나가기
	public void outRoom() {
		myRoomName = null;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(false);
		clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(true);
	}

	// 방 목록 삭제하기
	public void emptyRoom() {
		roomVector.remove(data);
		roomList.setListData(roomVector);
	}

	// 방 입장하기
	public void enterRoom() {
		myRoomName = data;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(false);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(false);
	}

	// 채팅하기
	public void chatting() {
		if (id.equals(data)) {
			clientFrame.getChattingPanel().getChatArea().append("[나] \n" + message + "\n");
		} else if (data.equals("입장")) {
			clientFrame.getChattingPanel().getChatArea().append("▶" + data + "◀" + message + "\n");
		} else if (data.equals("퇴장")) {
			clientFrame.getChattingPanel().getChatArea().append("▷" + data + "◁" + message + "\n");
		} else {
			clientFrame.getChattingPanel().getChatArea().append("[" + data + "] \n" + message + "\n");
		}
	}

	// 쪽지 보내기
	public void secretMsg() {
		JOptionPane.showMessageDialog(null, data + "님의 쪽지\n\"" + message + "\"", "[쪽지]", JOptionPane.PLAIN_MESSAGE);
	}

	// 방 생성 서버호출
	public void clickMakeRoomBtn(String roomName) {
		writer("MakeRoom:" + roomName + ": ");
	}

	// 방 나가기 서버호출
	public void clickOutRoomBtn(String roomName) {
		writer("OutRoom:" + roomName + ": ");
	}

	// 방 입장 서버호출
	public void clickEnterRoomBtn(String roomName) {
		writer("EnterRoom:" + roomName + ": ");
	}

	// 쪽지 보내기 서버호출
	public void clickMsgBtn(String msg) {
		writer("Chatting:" + myRoomName + ":" + msg);
	}

	public void clickSecretMsgBtn(String msg) {
		String user = userList.getSelectedValue();
		writer("SecretMsg:" + user + ":" + msg);
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

	public String getMyRoomName() {
		return myRoomName;
	}

	public static void main(String[] args) {
		new Client();
	}

}
