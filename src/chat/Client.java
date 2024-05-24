package chat;

public class Client {

	private ClientFrame clientFrame;

	private String ip;
	private int port;
	private String id;

	public Client() {
		clientFrame = new ClientFrame(this);

	}

	public void clickLoginBtn(String ip, int port, String id) {
		this.ip = ip;
		this.port = port;
		this.id = id;

	}

	public static void main(String[] args) {
		new Client();
	}

}
