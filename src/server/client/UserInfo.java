package server.client;


import java.net.Socket;


import server.threads.ClientConnection;



public class UserInfo {
	
	private String identity;
	private Socket socket;
	
	private ClientConnection managingThread;
	
	public ClientConnection getManagingThread() {
		return managingThread;
	}
	public void setManagingThread(ClientConnection managingThread) {
		this.managingThread = managingThread;
	}
	private String currentChatroom;
	
	public String getCurrentChatroom() {
		return currentChatroom;
	}
	public void setCurrentChatroom(String currentChatroom) {
		this.currentChatroom = currentChatroom;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public UserInfo(String identity,Socket socket,ClientConnection managingThread,String currentChatroom){
		this.identity=identity;
		this.socket=socket;
		this.managingThread=managingThread;
		this.currentChatroom=currentChatroom;
	}
	
	

}
