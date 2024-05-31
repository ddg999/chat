package chat;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginPanel extends JPanel implements ActionListener {
	private Client mContext;
	private JButton loginBtn;
	private JButton logoutBtn;
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
		loginBtn.setBounds(90, 450, 90, 30);
		logoutBtn = new JButton("로그아웃");
		logoutBtn.setBounds(200, 450, 90, 30);
		logoutBtn.setEnabled(false);
		exitBtn = new JButton("종료");
		exitBtn.setBounds(310, 450, 90, 30);

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
		add(logoutBtn);
		add(exitBtn);
		add(hostIp);
		add(serverPort);
		add(nickName);
	}

	private void login() {
		if (!hostIp.getText().equals("") && !serverPort.getText().equals("") && !nickName.getText().equals("")) {
			String ip = hostIp.getText();
			if (Integer.parseInt(serverPort.getText()) >= 1 && Integer.parseInt(serverPort.getText()) <= 65535) {
				try {
					int port = Integer.parseInt(serverPort.getText());
					String name = nickName.getText();
					mContext.clickLoginBtn(ip, port, name);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "잘못된 입력입니다");
				}
			} else {
				JOptionPane.showMessageDialog(null, "잘못된 포트 번호입니다.\n포트 번호 범위(1~65535)");
			}
		} else {
			JOptionPane.showMessageDialog(null, "빈 칸을 채워주세요");
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}

	private void addEventListener() {
		loginBtn.addActionListener(this);
		logoutBtn.addActionListener(this);
		exitBtn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loginBtn) {
			login();
		} else if (e.getSource() == logoutBtn) {
			mContext.clickLogoutBtn();
		} else if (e.getSource() == exitBtn) {
			System.exit(0);
		}
	}

	public JButton getLoginBtn() {
		return loginBtn;
	}

	public JButton getLogoutBtn() {
		return logoutBtn;
	}

	public JTextField getHostIp() {
		return hostIp;
	}

	public JTextField getServerPort() {
		return serverPort;
	}

	public JTextField getNickName() {
		return nickName;
	}
}
