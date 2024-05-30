package chat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ChattingPanel extends JPanel {
	private Client mContext;

	private ImageIcon background = new ImageIcon("images/background_bee.jpg");
	private JPanel chatPanel;
	private JPanel listPanel;
	private JPanel msgPanel;
	private ScrollPane scrollPane;

	private JTextArea chatArea;
	private JTextField msgBox;
	private JButton msgBtn;

	public ChattingPanel(Client mContext) {
		this.mContext = mContext;
		initData();
		setInitLayout();
		addEventListener();
	}

	private void initData() {
		chatPanel = new JPanel();
		listPanel = new JPanel();
		msgPanel = new JPanel();
		scrollPane = new ScrollPane();
		chatArea = new JTextArea();
		msgBox = new JTextField(25);
		msgBtn = new JButton("전송");

		chatPanel.setBounds(10, 30, 360, 400);
		chatPanel.setBackground(Color.white);
		chatPanel.setBorder(new TitledBorder(new LineBorder(Color.white, 3), "채팅창"));
		scrollPane.setBounds(10, 30, 340, 370);

		listPanel.setBounds(370, 40, 100, 420);
		listPanel.setBackground(Color.white);
		listPanel.setBorder(new TitledBorder(new LineBorder(Color.black, 1), "참가자"));

		// 참가자 갱신
//		chatUserVector.add(data);
//		chatUserList.setListData(chatUserVector);

		msgPanel.setBounds(10, 430, 360, 40);
		msgPanel.setBackground(Color.white);

		msgBtn.setEnabled(false);
	}

	private void setInitLayout() {
		setLayout(null);
		chatArea.setEnabled(false);

		add(chatPanel);
		chatPanel.add(scrollPane);
		scrollPane.add(chatArea);
		add(listPanel);
		listPanel.add(mContext.getChatUserList());
		add(msgPanel);
		msgPanel.add(msgBox);
		msgPanel.add(msgBtn);
	}

	private void addEventListener() {
		msgBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (msgBtn.isEnabled()) {
					sendMessage();
				}
			}
		});
		msgBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && msgBtn.isEnabled()) {
					sendMessage();
				}
			}
		});
	}

	private void sendMessage() {
		if (!msgBox.getText().equals("")) {
			String msg = msgBox.getText();
			mContext.clickMsgBtn(msg);
			msgBox.setText("");
			msgBox.requestFocus();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(background.getImage(), 0, 0, null);
	}

	public JTextArea getChatArea() {
		return chatArea;
	}

	public JButton getMsgBtn() {
		return msgBtn;
	}

}
