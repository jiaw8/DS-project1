package server;

public class ServerInfo {
	public String getServerID() {
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	

	public int getPort() {
		return port;
	}

//	public void setPort( port) {
//		this.port = port;
//	}

	public int getManagementPort() {
		return managementPort;
	}

//	public void setManagementPort(String managementPort) {
//		this.managementPort = managementPort;
//	}
//


	private String serverID;
	private String serverAddress;
	 
	 //communicate with client
	private int port;
	 //communicate with other server
	private int managementPort;
	
	public ServerInfo(String serverID,String serverAddress,String clientPort,String coordinatePort){
		
		this.serverID=serverID;
		this.serverAddress=serverAddress;
		this.port=Integer.parseInt(clientPort) ;
		this.managementPort=Integer.parseInt(coordinatePort) ;
	}
}
	