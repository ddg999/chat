package chat;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginPanel extends JPanel {
	private JButton loginBtn;
	private JButton exitBtn;
	private JTextField hostIp;
	private JTextField serverPort;
	private JTextField nickName;
	private ImageIcon background = new ImageIcon("images/backgroundClient.png");

	public LoginPanel() {
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
		serverPort = new JTextField();
		serverPort.setBounds(180, 325, 200, 30);
		nickName = new JTextField();
		nickName.setBounds(180, 395, 200, 30);
	}

	private void setInitLayout() {
		setLayout(null);
		add(loginBtn);
		add(exitBtn);
		add(hostIp);
		add(serverPort);
		add(nickName);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}

	private void addEventListener() {
	}
}
