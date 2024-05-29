package chat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class WaitingRoomPanel extends JPanel implements ActionListener {
	private Client mContext;

	private ImageIcon background = new ImageIcon("images/background_bee.jpg");

	private JPanel userListPanel;
	private JPanel roomListPanel;
	private JPanel secretMsgPanel;

	private JPanel roomBtnPanel;
	private JButton makeRoomBtn;
	private JButton outRoomBtn;
	private JButton enterRoomBtn;

	private JTextField secretMsg;
	private JButton secretMsgBtn;

	public WaitingRoomPanel(Client mContext) {
		this.mContext = mContext;
		initData();
		setInitLayout();
		addEventListener();
	}

	private void initData() {
		userListPanel = new JPanel();
		roomListPanel = new JPanel();
		roomBtnPanel = new JPanel();
		secretMsgPanel = new JPanel();

		makeRoomBtn = new JButton("방 만들기");
		outRoomBtn = new JButton("방 나가기");
		enterRoomBtn = new JButton("입장하기");
		secretMsgBtn = new JButton("쪽지보내기");

		secretMsg = new JTextField(20);

		userListPanel.setBounds(80, 20, 140, 350);
		userListPanel.setBackground(Color.white);
		userListPanel.setBorder(new TitledBorder(new LineBorder(Color.lightGray, 2), "참가자"));

		roomListPanel.setBounds(270, 20, 140, 350);
		roomListPanel.setBackground(Color.white);
		roomListPanel.setBorder(new TitledBorder(new LineBorder(Color.lightGray, 2), "방목록"));

		roomBtnPanel.setBounds(110, 380, 280, 32);
		roomBtnPanel.setBackground(Color.white);

		secretMsgPanel.setBounds(80, 420, 330, 32);
		secretMsgPanel.setBackground(Color.white);

		makeRoomBtn.setEnabled(false);
		outRoomBtn.setEnabled(false);
		enterRoomBtn.setEnabled(false);
		secretMsgBtn.setEnabled(false);
	}

	private void setInitLayout() {
		setLayout(null);
		add(userListPanel);
		userListPanel.add(mContext.getUserList());
		add(roomListPanel);
		roomListPanel.add(mContext.getRoomList());
		add(roomBtnPanel);
		roomBtnPanel.add(makeRoomBtn);
		roomBtnPanel.add(outRoomBtn);
		roomBtnPanel.add(enterRoomBtn);
		add(secretMsgPanel);
		secretMsgPanel.add(secretMsg);
		secretMsgPanel.add(secretMsgBtn);
	}

	private void addEventListener() {
		makeRoomBtn.addActionListener(this);
		outRoomBtn.addActionListener(this);
		enterRoomBtn.addActionListener(this);
		secretMsgBtn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == makeRoomBtn) {
			String roomName = JOptionPane.showInputDialog("[ 방 이름 입력 ]");
			if (roomName.equals("")) {
				JOptionPane.showMessageDialog(null, "방 이름을 입력해주세요");
			} else {
				mContext.clickMakeRoomBtn(roomName);
			}
		} else if (e.getSource() == outRoomBtn) {
			mContext.clickOutRoomBtn();
		} else if (e.getSource() == enterRoomBtn && mContext.getRoomList().getSelectedIndex() != -1) {
			String roomName = mContext.getRoomList().getSelectedValue();
			mContext.clickEnterRoomBtn(roomName);
			mContext.getRoomList().setSelectedValue(null, false);
		} else if (e.getSource() == secretMsgBtn && mContext.getUserList().getSelectedIndex() != -1) {
			String msg = secretMsg.getText();
			if (!msg.equals("")) {
				mContext.clickSecretMsgBtn(msg);
				secretMsg.setText("");
				mContext.getUserList().setSelectedValue(null, false);
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}

	public JButton getMakeRoomBtn() {
		return makeRoomBtn;
	}

	public JButton getOutRoomBtn() {
		return outRoomBtn;
	}

	public JButton getEnterRoomBtn() {
		return enterRoomBtn;
	}

	public JButton getSecretMsgBtn() {
		return secretMsgBtn;
	}

}
