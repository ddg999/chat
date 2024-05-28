package chat;

import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ServerFrame extends JFrame {

	private Server mContext;

	private JLabel background = new JLabel(new ImageIcon("images/background_bee.jpg"));
	private JPanel serverMsgPanel;
	private JPanel portPanel;
	private JButton startBtn;
	private JButton exitBtn;
	private JLabel portNumber;
	private JTextField textPortNumber;
	private JTextArea serverMsg;
	private ScrollPane scrollPane;

	public ServerFrame(Server mContext) {
		this.mContext = mContext;
		initData();
		setInitLayout();
		addEventListener();
	}

	private void initData() {
		setTitle("Server Page");
		setSize(500, 630);
		setContentPane(background);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		serverMsgPanel = new JPanel();
		serverMsgPanel.setBounds(45, 80, 400, 330);
		serverMsgPanel.setBackground(Color.white);
		serverMsgPanel.setBorder(new TitledBorder(new LineBorder(Color.white, 3), "서버메세지"));
		scrollPane = new ScrollPane();
		scrollPane.setBounds(45, 80, 380, 300);

		serverMsg = new JTextArea();

		portPanel = new JPanel();
		portPanel.setBackground(Color.white);
		portPanel.setBounds(80, 420, 330, 32);

		startBtn = new JButton("서버 실행");
		exitBtn = new JButton("서버 종료");

		textPortNumber = new JTextField(7);
		textPortNumber.setText("5000");

		portNumber = new JLabel("포트 번호");
	}

	private void setInitLayout() {
		setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

		add(serverMsgPanel);
		serverMsgPanel.add(scrollPane);
		scrollPane.add(serverMsg);
		serverMsg.setEnabled(false);

		add(portPanel);
		portPanel.add(portNumber);
		portPanel.add(textPortNumber);
		portPanel.add(startBtn);
		portPanel.add(exitBtn);

	}

	private void addEventListener() {
		startBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (startBtn.isEnabled()) {
					System.out.println("서버 실행");
					mContext.startServer(Integer.valueOf(textPortNumber.getText()));
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				startBtn.setEnabled(false);
			}

		});
		exitBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("서버 종료");
				System.exit(0);
			}
		});

	}

	public JButton getStartBtn() {
		return startBtn;
	}

	public JTextArea getServerMsg() {
		return serverMsg;
	}
}
