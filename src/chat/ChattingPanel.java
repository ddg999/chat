package chat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ChattingPanel extends JPanel {
	private Client mContext;

	private ImageIcon background = new ImageIcon("images/background_bee.jpg");
	private JPanel chatPanel;
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
		msgPanel = new JPanel();
		scrollPane = new ScrollPane();
		chatArea = new JTextArea();
		msgBox = new JTextField(30);
		msgBtn = new JButton("전송");

		chatPanel.setBounds(30, 30, 420, 400);
		chatPanel.setBackground(Color.white);
		chatPanel.setBorder(new TitledBorder(new LineBorder(Color.white, 3), "채팅창"));
		scrollPane.setBounds(30, 30, 400, 370);

		msgPanel.setBounds(30, 430, 420, 40);
		msgPanel.setBackground(Color.white);

		msgBtn.setEnabled(false);
	}

	private void setInitLayout() {
		setLayout(null);
		chatArea.setEnabled(false);

		add(chatPanel);
		chatPanel.add(scrollPane);
		scrollPane.add(chatArea);
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
