package server.util;

import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import server.Server;
import server.chatRoom.ChatroomInfo;

public class Json {
	@SuppressWarnings("unchecked")
	public static String lockIdentity(String identity){
		
		JSONObject message = new JSONObject();
		message.put("type", "lockindentity");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("identity", identity);
		return message.toJSONString();
	}@SuppressWarnings("unchecked")
	public static String releaseIdentity(String identity){
		
		JSONObject message = new JSONObject();
		message.put("type", "releaseidentity");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("identity", identity);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String newidentityTrue(String identity){
		
		JSONObject message = new JSONObject();
		message.put("type", "newidentity");
		message.put("approved", "true");
		return message.toJSONString();
	}@SuppressWarnings("unchecked")
	public static String newidentityFalse(String identity){
		
		JSONObject message = new JSONObject();
		message.put("type", "newidentity");
		message.put("approved", "false");
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String lockindentityServerTrue(String identity){
		
		JSONObject message = new JSONObject();
		message.put("type", "lockindentity");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("identity", identity);
		message.put("locked", "true");
		return message.toJSONString();

	}
	@SuppressWarnings("unchecked")
	public static String lockindentityServerFalse(String identity){
	
		JSONObject message = new JSONObject();
		message.put("type", "lockindentity");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("identity", identity);
		message.put("locked", "false");
		return message.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public static String who(ChatroomInfo chatroomInfo){
		
		JSONObject message = new JSONObject();
		message.put("type", "roomcontents");
		message.put("roomid", chatroomInfo.getChatroomId());
		JSONArray clients = new JSONArray();
		for (String client:chatroomInfo.getClients()){
			clients.add(client);
		}
		message.put("identities", clients);
		message.put("owner", chatroomInfo.getCreator());
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String lockRoomID(String roomID){
		
		JSONObject message = new JSONObject();
		message.put("type", "lockroomid");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("roomid", roomID);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String lockRoomIDReplay(String roomID,String locked){
		
		JSONObject message = new JSONObject();
		message.put("type", "lockroomid");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("roomid", roomID);
		message.put("locked", locked);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String releaseRoomID(String roomid,String approve){
		
		JSONObject message = new JSONObject();
		message.put("type", "releaseroomid");
		message.put("serverid", Server.getInstance().getServerInfo().getServerID());
		message.put("roomid", roomid);
		message.put("approved", approve);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String createChatroom(String roomid,String approve){
		
		JSONObject message = new JSONObject();
		message.put("type", "createroom");
		message.put("roomid", roomid);
		message.put("approved", approve);
		return message.toJSONString();
	}@SuppressWarnings("unchecked")
	public static String listChatroom(Set<String> remote,Set<String> local){
		
		JSONObject message = new JSONObject();
		message.put("type", "roomlist");
		JSONArray clients = new JSONArray();
		for (String client:remote){
			clients.add(client);
		}
		for (String client:local){
			clients.add(client);
		}
		message.put("rooms", clients);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String changeChatroom(String identity,String former,String roomid){
		
		JSONObject message = new JSONObject();
		message.put("type", "roomchange");
		message.put("identity", identity);
		message.put("former", former);
		message.put("roomid", roomid);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String rout(String roomid,String host,String port){
		JSONObject message = new JSONObject();
		message.put("type", "route");
		message.put("roomid", roomid);
		message.put("host", host);
		message.put("port", port);
		return message.toJSONString();
	}
	@SuppressWarnings("unchecked")
	public static String serverchange(String serverid,String approved){
		
		JSONObject message = new JSONObject();
		message.put("type", "serverchange");
		message.put("approved", approved);
		message.put("serverid", serverid);
	
		return message.toJSONString();
	
	}
	@SuppressWarnings("unchecked")
public static String deletechatroom(String roomid,String approved){
		
		JSONObject message = new JSONObject();
		message.put("type", "deleteroom");
		message.put("roomid", roomid);
		message.put("approved", approved);
	
		return message.toJSONString();
	
	}@SuppressWarnings("unchecked")
public static String deletechatroomToServer(String roomid,String serverid){
	JSONObject message = new JSONObject();
	message.put("type", "deleteroom");
	message.put("serverid", serverid);
	message.put("roomid", roomid);
	return message.toJSONString();

}
	@SuppressWarnings("unchecked")
public static String message(String identity,String content){
	JSONObject message = new JSONObject();
	message.put("type", "message");
	message.put("identity", identity);
	message.put("content", content);
	return message.toJSONString();

}

public static String authFail(String identity, String approved){
	JSONObject message = new JSONObject();
	message.put("type","authorize");
	message.put("identity", identity);
	message.put("approved", approved);
	return message.toJSONString();
}


}
