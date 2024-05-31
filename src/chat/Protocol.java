package chat;

public interface Protocol {
	void newUser();

	void connectedUser();

	void makeRoom();

	void madeRoom();

	void outRoom();

	void enterRoom();

	void chatting();

	void secretMsg();

	void logout();
}
