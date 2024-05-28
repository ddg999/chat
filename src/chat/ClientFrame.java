package chat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ClientFrame extends JFrame {

	private Client mContext;

	// 탭
	private JTabbedPane tab;

	// 로그인창
	private LoginPanel loginPanel;

	// 대기창
	private WaitingRoomPanel waitingRoomPanel;

	// 채팅창
	private ChattingPanel chattingPanel;

	public ClientFrame(Client mContext) {
		this.mContext = mContext;
		initData();
		setInitLayout();
	}

	private void initData() {
		setTitle("Client Page");
		setSize(500, 630);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		loginPanel = new LoginPanel(mContext);
		waitingRoomPanel = new WaitingRoomPanel(mContext);
		chattingPanel = new ChattingPanel(mContext);

		tab = new JTabbedPane();
		tab.setBounds(0, 0, 485, 590);

		tab.addTab("로그인", loginPanel);
		tab.addTab("대기실", waitingRoomPanel);
		tab.addTab("채팅방", chattingPanel);
	}

	private void setInitLayout() {
		setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		add(tab);
	}

	public LoginPanel getLoginPanel() {
		return loginPanel;
	}

	public WaitingRoomPanel getWaitingRoomPanel() {
		return waitingRoomPanel;
	}

	public ChattingPanel getChattingPanel() {
		return chattingPanel;
	}

}
