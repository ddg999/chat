package chat;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
	private Client mContext;
	private JButton loginBtn;
	private JButton exitBtn;
	private JTextField hostIp;
	private JTextField serverPort;
	private JTextField nickName;
	private ImageIcon background = new ImageIcon("images/backgroundClient.png");

	public LoginPanel(Client mContext) {
		this.mContext = mContext;
		initData();
		setInitLayout();
		addEventListener();
	}

	private void initData() {
		loginBtn = new JButton("로그인");
		loginBtn.setBounds(120, 450, 100, 30);
		exitBtn = new JButton("나가기");
		exitBtn.setBounds(250, 450, 100, 30);
		hostIp = new JTextField();
		hostIp.setBounds(180, 255, 200, 30);
		hostIp.setText("localhost");
		serverPort = new JTextField();
		serverPort.setBounds(180, 325, 200, 30);
		serverPort.setText("5000");
		nickName = new JTextField();
		nickName.setBounds(180, 395, 200, 30);
		nickName.setText("홍길동");
	}

	private void setInitLayout() {
		setLayout(null);
		add(loginBtn);
		add(exitBtn);
		add(hostIp);
		add(serverPort);
		add(nickName);
	}

	private void login() {
		if (!hostIp.getText().equals(null) && !serverPort.getText().equals(null) && !nickName.getText().equals(null)) {
			String ip = hostIp.getText();
			int port = Integer.valueOf(serverPort.getText());
			String name = nickName.getText();
			mContext.clickLoginBtn(ip, port, name);
		} else {
			System.out.println("올바른 입력이 아닙니다");
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}

	private void addEventListener() {
		loginBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (loginBtn.isEnabled()) {
					System.out.println("클라이언트 로그인");
					login();
					loginBtn.setEnabled(false);
				}
			}
		});

		exitBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("클라이언트 종료");
				System.exit(0);
			}
		});

	}
}
