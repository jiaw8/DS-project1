package server.util;
import org.kohsuke.args4j.Option;

public class CmdLineArgs {
	
//	@Option(name="-h",aliases={"--host"},required=true,usage="hostname")
//	private String host;
//	
//	@Option(name="-p",aliases={"--port"},required=true,usage="port")
//	private int port=4444;
	
	@Option(name="-n",aliases={"--n"},required=true,usage="server ID")
	private String serverID;
	@Option(name="-l",required=true,usage="config file path")
	private String serverConfig;
	
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public String getServerConfig() {
		return serverConfig;
	}
	public void setServerConfig(String serverConfig) {
		this.serverConfig = serverConfig;
	}

}
