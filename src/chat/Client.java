package chat;

public class Client {

	private ClientFrame clientFrame;

	public Client() {
		clientFrame = new ClientFrame(this);
	}

	public static void main(String[] args) {
		new Client();
	}

}
