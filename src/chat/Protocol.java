package chat;

public interface Protocol {
	void newUser();

	void connectedUser();

	void madeRoom();

	void makeRoom();

	void newRoom();

	void outRoom();

	void enterRoom();

	void chatting();

	void secretMsg();
	
	void logout();
}
