package chat;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class WaitingRoomPanel extends JPanel {
	private ImageIcon background = new ImageIcon("images/background_bee.jpg");
	private JPanel userListPanel;
	private JPanel roomListPanel;

	private JList<String> userList = new JList<>();
	private Vector<String> userIdList = new Vector<>();

	public WaitingRoomPanel() {
		initData();
		setInitLayout();
	}

	private void initData() {
		userListPanel = new JPanel();
		roomListPanel = new JPanel();
		userListPanel.setBounds(80, 20, 140, 350);
		userListPanel.setBackground(Color.WHITE);
		userListPanel.setBorder(new TitledBorder(new LineBorder(Color.lightGray, 2), "참가자"));
		roomListPanel.setBounds(270, 20, 140, 350);
		roomListPanel.setBackground(Color.WHITE);
		roomListPanel.setBorder(new TitledBorder(new LineBorder(Color.lightGray, 2), "방목록"));

		add(userListPanel);
		add(roomListPanel);
		userIdList.add("석지웅");
		userIdList.add("홍길동");
		userIdList.add("이순신");
		userIdList.add("장보고");
		userListPanel.add(userList);
		userList.setListData(userIdList);
	}

	private void setInitLayout() {
		setLayout(null);
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}
}
