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

public class Client implements Protocol {

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
			clientFrame.getLoginPanel().getLoginBtn().setEnabled(false);
			clientFrame.getLoginPanel().getHostIp().setEnabled(false);
			clientFrame.getLoginPanel().getServerPort().setEnabled(false);
			clientFrame.getLoginPanel().getNickName().setEnabled(false);
			clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
			clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(true);
			clientFrame.getWaitingRoomPanel().getSecretMsgBtn().setEnabled(true);
			clientFrame.setTitle("[ IP : " + ip + " ]" + "[ PORT : " + port + " ] " + "[ ID : " + id + " ]");

			readThread();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "서버를 확인할 수 없습니다");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "서버를 확인할 수 없습니다");
		}
	}

	// 클라이언트 -> 서버 메세지 전송하기
	private void writer(String msg) {
		socketWriter.println(msg);
	}

	// 클라이언트 측 리드 스레드
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

	// 프로토콜 체크 (구분자 :) protocol:data:message
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
		} else if (protocol.equals("UserOut")) {
			userOut();
		}
	}

	// 유저 벡터, 리스트에 추가하기
	@Override
	public void newUser() {
		// 동일한 아이디가 없을 때 추가
		if (!data.equals(this.id)) {
			userVector.add(data);
			userList.setListData(userVector);
		}
	}

	// 유저 목록 갱신하기
	@Override
	public void connectedUser() {
		userVector.add(data);
		userList.setListData(userVector);
	}

	// 방 목록 갱신하기
	@Override
	public void madeRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	// 방 생성하기
	@Override
	public void makeRoom() {
		myRoomName = data;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(false);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(false);
		clientFrame.getChattingPanel().getMsgBtn().setEnabled(true);
	}

	// 방 목록 추가하기
	@Override
	public void newRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	// 방 나가기
	@Override
	public void outRoom() {
		myRoomName = null;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(false);
		clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(true);
		clientFrame.getChattingPanel().getChatArea().setText("");
	}

	// 방 입장하기
	@Override
	public void enterRoom() {
		myRoomName = data;
		clientFrame.getWaitingRoomPanel().getMakeRoomBtn().setEnabled(false);
		clientFrame.getWaitingRoomPanel().getOutRoomBtn().setEnabled(true);
		clientFrame.getWaitingRoomPanel().getEnterRoomBtn().setEnabled(false);
		clientFrame.getChattingPanel().getMsgBtn().setEnabled(true);
	}

	// 채팅하기
	@Override
	public void chatting() {
		if (id.equals(data)) {
			clientFrame.getChattingPanel().getChatArea().append("[나] \n" + message + "\n");
		} else if (data.equals("입장")) {
			clientFrame.getChattingPanel().getChatArea().append("[" + data + "] " + message + "\n");
		} else if (data.equals("퇴장")) {
			clientFrame.getChattingPanel().getChatArea().append("[" + data + "] " + message + "\n");
		} else {
			clientFrame.getChattingPanel().getChatArea().append("[" + data + "]\n" + message + "\n");
		}
	}

	// 쪽지 창 띄우기
	@Override
	public void secretMsg() {
		JOptionPane.showMessageDialog(null, message, "[" + data + "님의 쪽지]", JOptionPane.PLAIN_MESSAGE);
	}

	// 방 목록 벡터에서 방 삭제하기
	public void emptyRoom() {
		roomVector.remove(data);
		roomList.setListData(roomVector);
	}

	// 유저 목록 벡터에서 유저 삭제하기
	public void userOut() {
		userVector.remove(data);
		userList.setListData(userVector);
	}

	// 방 생성 서버호출
	public void clickMakeRoomBtn(String roomName) {
		writer("MakeRoom:" + roomName + ": ");
	}

	// 방 나가기 서버호출
	public void clickOutRoomBtn() {
		writer("OutRoom:" + myRoomName + ": ");
	}

	// 방 입장 서버호출
	public void clickEnterRoomBtn(String roomName) {
		writer("EnterRoom:" + roomName + ": ");
	}

	// 채팅 전송하기 서버호출
	public void clickMsgBtn(String msg) {
		writer("Chatting:" + myRoomName + ":" + msg);
	}

	// 쪽지 보내기 서버호출
	public void clickSecretMsgBtn(String msg) {
		String user = userList.getSelectedValue();
		if (!user.equals(id)) {
			writer("SecretMsg:" + user + ":" + msg);
		} else {
			JOptionPane.showMessageDialog(null, "자신에게 보낼 수 없습니다");
		}
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
