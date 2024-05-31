package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client implements Protocol {

	private ClientFrame clientFrame;
	private JButton loginBtn;
	private JButton logoutBtn;
	private JTextField ipTextField;
	private JTextField portTextField;
	private JTextField idTextField;
	private JButton makeRoomBtn;
	private JButton outRoomBtn;
	private JButton enterRoomBtn;
	private JButton secretMsgBtn;
	private JTextArea chatArea;
	private JButton msgBtn;

	// 클라이언트 측 서버 정보
	private String ip;
	private int port;

	// 클라이언트 소켓
	private Socket socket;
	private PrintWriter socketWriter;
	private BufferedReader socketReader;

	// 클라이언트 유저 정보
	private String id;
	private String myRoomName;

	// WaitingRoom에 나타낼 유저 목록, 방 목록
	private Vector<String> userVector = new Vector<>();
	private Vector<String> roomVector = new Vector<>();
	private JList<String> userList = new JList<>();
	private JList<String> roomList = new JList<>();

	// ChattingPanel에 나타낼 방에 참가한 유저 목록
	private Vector<String> chatUserVector = new Vector<>();
	private JList<String> chatUserList = new JList<>();

	// 프로토콜 변수
	private String protocol;
	private String data;
	private String message;

	// 접속 종료, 로그아웃 상태 변수
	private boolean logout;

	public Client() {
		clientFrame = new ClientFrame(this);

		// 클라이언트 측 프레임의 컴퍼넌트 가져오기
		loginBtn = clientFrame.getLoginPanel().getLoginBtn();
		logoutBtn = clientFrame.getLoginPanel().getLogoutBtn();
		ipTextField = clientFrame.getLoginPanel().getHostIp();
		portTextField = clientFrame.getLoginPanel().getServerPort();
		idTextField = clientFrame.getLoginPanel().getNickName();
		makeRoomBtn = clientFrame.getWaitingRoomPanel().getMakeRoomBtn();
		outRoomBtn = clientFrame.getWaitingRoomPanel().getOutRoomBtn();
		secretMsgBtn = clientFrame.getWaitingRoomPanel().getSecretMsgBtn();
		enterRoomBtn = clientFrame.getWaitingRoomPanel().getEnterRoomBtn();
		chatArea = clientFrame.getChattingPanel().getChatArea();
		msgBtn = clientFrame.getChattingPanel().getMsgBtn();
	}

	// 로그인 버튼 클릭, 소켓 생성, reaThread 생성
	public void clickLoginBtn(String ip, int port, String id) {
		logout = false;
		this.ip = ip;
		this.port = port;
		this.id = id;
		try {
			socket = new Socket(ip, port);
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);

			writer("NewUser:" + id + ": ");

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

	// 클라이언트 측 readThread
	private void readThread() {
		new Thread(() -> {
			while (!logout) {
				try {
					String msg = socketReader.readLine();
					checkProtocol(msg);
				} catch (IOException e) {
					e.printStackTrace();
					logout = true;
					System.out.println("서버가 종료되었습니다");
				}
			}
		}).start();
	}

	// 프로토콜 체크 (구분자 :) protocol:data:message
	private void checkProtocol(String msg) {
		try {
			String[] parts = msg.split(":", 3);
			protocol = parts[0];
			data = parts[1];
			message = parts[2];

			if (protocol.equals("NewUser")) {
				newUser();
			} else if (protocol.equals("ConnectedUser")) {
				connectedUser();
			} else if (protocol.equals("MakeRoom")) {
				makeRoom();
			} else if (protocol.equals("MadeRoom")) {
				madeRoom();
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
			} else if (protocol.equals("LoginError")) {
				loginError();
			} else if (protocol.equals("Logout")) {
				logout();
			} else if (protocol.equals("NewChatList")) {
				newChatList();
			} else if (protocol.equals("EnteredChatList")) {
				enteredChatList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 새로운 유저를 벡터, 리스트에 추가하기
	@Override
	public void newUser() {
		userVector.add(data);
		userList.setListData(userVector);

		if (id.equals(data)) {
			loginBtn(false);

			makeRoomBtn.setEnabled(true);
			outRoomBtn.setEnabled(false);
			enterRoomBtn.setEnabled(true);
			secretMsgBtn.setEnabled(true);
			clientFrame.setTitle("[ IP : " + ip + " ]" + "[ PORT : " + port + " ] " + "[ ID : " + id + " ]");
		}
	}

	// 중복 닉네임 일 때 소켓 종료
	private void loginError() {
		JOptionPane.showMessageDialog(null, "이미 존재하는 닉네임입니다");
		logout = true;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 로그아웃
	@Override
	public void logout() {
		logout = true;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		userVector.removeAllElements();
		userList.setListData(userVector);
		roomVector.removeAllElements();
		roomList.setListData(roomVector);
		loginBtn(true);
		waitingRoomBtn(false);

		secretMsgBtn.setEnabled(false);
		msgBtn.setEnabled(false);
		chatArea.setText("");
		clientFrame.setTitle("Client Page");
	}

	// 유저 목록 갱신하기
	@Override
	public void connectedUser() {
		if (!data.equals(id)) {
			userVector.add(data);
			userList.setListData(userVector);
		}
	}

	// 방 생성하기
	@Override
	public void makeRoom() {
		if (message.equals("RoomNameError")) {
			JOptionPane.showMessageDialog(null, "이미 존재하는 방 이름입니다");
		} else {
			System.out.println("버그");
			myRoomName = data;
			waitingRoomBtn(false);
			msgBtn.setEnabled(true);
		}
	}

	// 방 목록 갱신하기
	@Override
	public void madeRoom() {
		roomVector.add(data);
		roomList.setListData(roomVector);
	}

	// 방 나가기
	@Override
	public void outRoom() {
		myRoomName = null;
		chatUserVector.removeAllElements();
		chatUserList.setListData(chatUserVector);
		waitingRoomBtn(true);
		chatArea.setText("");
	}

	// 방 입장하기
	@Override
	public void enterRoom() {
		myRoomName = data;
		chatUserVector.add(id);
		chatUserList.setListData(chatUserVector); // 나를 참가목록에 추가
		waitingRoomBtn(false);
		msgBtn.setEnabled(true);
	}

	// 채팅하기
	@Override
	public void chatting() {
		if (id.equals(data)) {
			chatArea.append("[나] \n" + message + "\n");
		} else if (data.equals("입장")) {
			chatArea.append("[" + data + "] " + message + "\n");
		} else if (data.equals("퇴장")) {
			chatArea.append("[" + data + "] " + message + "\n");
		} else {
			chatArea.append("[" + data + "]\n" + message + "\n");
		}
	}

	//
	private void newChatList() {
		if (message.equals("삭제")) {
			chatUserVector.remove(data);
			chatUserList.setListData(chatUserVector);
		} else {
			chatUserVector.add(data);
			chatUserList.setListData(chatUserVector);
		}
	}

	private void enteredChatList() {
		if (!data.equals(id)) {
			chatUserVector.add(data);
			chatUserList.setListData(chatUserVector);
		}
	}

	// 쪽지 창 띄우기
	@Override
	public void secretMsg() {
		JOptionPane.showMessageDialog(null, message, "[" + data + "님의 쪽지]", JOptionPane.PLAIN_MESSAGE);
	}

	// 방 목록 벡터에서 방 삭제하기
	private void emptyRoom() {
		roomVector.remove(data);
		roomList.setListData(roomVector);
	}

	// 유저 목록 벡터에서 유저 삭제하기
	private void userOut() {
		userVector.remove(data);
		userList.setListData(userVector);
	}

	// 로그아웃 서버호출
	public void clickLogoutBtn() {
		writer("OutRoom:" + myRoomName + ": ");
		writer("Logout:" + id + ": ");
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

	private void loginBtn(boolean flag) {
		loginBtn.setEnabled(flag);
		logoutBtn.setEnabled(!flag);
		ipTextField.setEnabled(flag);
		portTextField.setEnabled(flag);
		idTextField.setEnabled(flag);
	}

	private void waitingRoomBtn(boolean flag) {
		makeRoomBtn.setEnabled(flag);
		outRoomBtn.setEnabled(!flag);
		enterRoomBtn.setEnabled(flag);
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

	public Vector<String> getChatUserVector() {
		return chatUserVector;
	}

	public JList<String> getChatUserList() {
		return chatUserList;
	}

	public static void main(String[] args) {
		new Client();
	}

}
