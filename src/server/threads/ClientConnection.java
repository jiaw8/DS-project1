package server.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import server.Server;
import server.ServerInfo;
import server.chatRoom.ChatroomInfo;
import server.client.UserInfo;
import server.util.Common;
import server.util.Json;
import server.util.Message;

public class ClientConnection implements Runnable {

	private Socket clientSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private BlockingQueue<Message> messageQueue;
	private String indentity;
	private String clientNum;
	private String mainHall="MainHall-"+Server.getInstance().getServerInfo().getServerID();
	public String getIndentity() {
		return indentity;
	}


	public void setIndentity(String indentity) {
		this.indentity = indentity;
	}


	public ClientConnection(Socket clientSocket) {
		try {
			this.clientSocket = clientSocket;
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));	
			messageQueue = new LinkedBlockingQueue<Message>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		
		try {
			
			ClientMessage messageReader = new ClientMessage(reader, messageQueue);
			Server.getInstance().pool.execute(messageReader);

			while(true) {

				Message msg = messageQueue.take();
				System.out.println(msg.getMessage());
				if(!msg.isFromClient() && msg.getMessage().equals("exit")) {
					break;
				}
				if(!msg.isFromClient() && msg.getMessage().equals("quit")) {
					 quit();
				}
				
				if(msg.isFromClient()) {
				
					 switch (Common.unmarshalling(msg.getMessage(),"type")) {
						case "newidentity":
							newIdentity( msg.getMessage());
							break;
						case "roomchange":
							Message roomchange=new Message(false,msg.getMessage()); 
							messageQueue.add(roomchange);
							break;
							
						case "list":
							Set<String> remote = Server.getInstance().getRemoteChatrooms();
							Set<String> local = Server.getInstance().getLocalChatrooms();
							Message list= new Message(false,Json.listChatroom(remote,local));
							messageQueue.add(list);
							break;
						case "who":
							ChatroomInfo chatroom =Server.getInstance().getChatroom(this.indentity);
							Message who= new Message(false,Json.who( chatroom));
							messageQueue.add(who);
							break;
							
						case "createroom":
							newRoomID( msg.getMessage());	
							break;

						case "join":
							String roomID = Common.unmarshalling( msg.getMessage(),"roomid");
							String from =Server.getInstance().getChatroom(this.indentity).getChatroomId();
							joinRoom(from, roomID);
							break;
							
						case "movejoin":
							moveJoin( msg.getMessage());
							break;
						case "deleteroom":
							deleteRoom(msg.getMessage());
							break;
						case "message":
							
							String content = Common.unmarshalling( msg.getMessage(),"content");
							Server.getInstance().broadCastRoommates(this.indentity,Json.message(this.indentity, content));
							break;

						case "quit":
							
							quit();
							break;
						}
				} else {
					//If the message is from a thread and it isn't exit, then
					//it is a message that needs to be sent to the client
					write(msg.getMessage());
				} 
			}
			Server.getInstance().pool.remove(messageReader);
			clientSocket.close();
			
			Server.getInstance().clientDisconnected(indentity);
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Client " + clientNum + " disconnected");

		} 
		 catch (SocketException e) {
				e.printStackTrace();
			}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}
	
	public void newIdentity(String msg){
		String identity = Common.unmarshalling(msg,"identity");
	
		String pattern = "^[a-zA-z][a-zA-Z0-9]{2,15}$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(identity);
		if(!m.find()){
			Message message=new Message(false,Json.newidentityFalse(identity)); 
			messageQueue.add(message);
			return;
		}
	
			if(Server.getInstance().getConnectedClients().contains(identity)){
				Message message=new Message(false,Json.newidentityFalse(identity)); 
				messageQueue.add(message);
				return;
			}
			if(Server.getInstance().getLockedIndetity().contains(identity)){
				Message message=new Message(false,Json.newidentityFalse(identity)); 
				messageQueue.add(message);
				return;
			}
		
			Server.getInstance().addLockedIndetity(identity);
			if(messageToOtherServer(Json.lockIdentity(identity))){
			
						UserInfo user=new UserInfo(identity, clientSocket, this,mainHall);
						Server.getInstance().clientConnected(user);
						Server.getInstance().moveToMainHall(identity);
						
						this.indentity=identity;
						Message message=new Message(false,Json.newidentityTrue(identity)); 
						messageQueue.add(message);
						Server.getInstance().broadCast(mainHall,Json.changeChatroom(identity,"",mainHall));
						
			}else{
				Message message=new Message(false,Json.newidentityFalse(identity)); 
				messageQueue.add(message);}
				
		
			Server.getInstance().removeLockedIndetity(identity);
			messageToOtherServer(Json.releaseIdentity(identity));
		}
					
	public void newRoomID(String msg){
		
		String roomid = Common.unmarshalling( msg,"roomid");
		String pattern2 = "^[a-zA-z][a-zA-Z0-9]{2,15}$";
		Pattern p = Pattern.compile(pattern2);
		Matcher match = p.matcher(roomid);
		
		if(!match.find()){
			Message message=new Message(false,Json.createChatroom(roomid, "false"));
			messageQueue.add(message);
			return;
		}
		
		if(Server.getInstance().getLockedChatrooms().contains(roomid)||Server.getInstance().getRemoteChatrooms().contains(roomid)||Server.getInstance().getLocalChatrooms().contains(roomid)){
			Message message=new Message(false,Json.createChatroom(roomid, "false"));
			messageQueue.add(message);
			return;
		}
		if(Server.getInstance().isRoomOwner(this.indentity)){
			Message message=new Message(false,Json.createChatroom(roomid, "false"));
			messageQueue.add(message);
			return;
		}
		
		Server.getInstance().addLockedChatrooms(roomid);
		if(messageToOtherServer(Json.lockRoomID(roomid))){
			
			ChatroomInfo chatroominfo=new ChatroomInfo( roomid,this.indentity);
			Server.getInstance().addLocalChatrooms(roomid,chatroominfo);
			String from =Server.getInstance().getChatroom(this.indentity).getChatroomId();
			
			Message createChatroom=new Message(false,Json.createChatroom(roomid, "true")); 
			messageQueue.add(createChatroom);
			String roomChange=Json.changeChatroom(this.indentity,from,roomid);
			Server.getInstance().broadCast(from,roomChange);
			Server.getInstance().moveToChatroom(this.indentity,roomid);
			messageToOtherServer(Json.releaseRoomID(roomid, "true"));
			
		}else{

			Message message=new Message(false,Json.createChatroom(roomid, "false"));
			messageQueue.add(message);
			messageToOtherServer(Json.releaseRoomID(roomid, "false"));
		}
		Server.getInstance().removeLockedChatrooms(roomid);	
		
	}
	
	public void joinRoom(String from, String roomID){
		if(Server.getInstance().isRoomOwner(this.indentity)){
			Message message=new Message(false,Json.changeChatroom(this.indentity, from, from));
			messageQueue.add(message);
			return;
		}
		if(Server.getInstance().isRoomLocal(roomID)){
				Server.getInstance().broadCastRoommates(this.indentity, Json.changeChatroom(this.indentity, from, roomID));	
				Server.getInstance().broadCast(roomID, Json.changeChatroom(this.indentity, from, roomID));
				Server.getInstance().moveToChatroom(this.indentity,roomID);
				Message message=new Message(false,Json.changeChatroom(this.indentity, from, roomID));
				messageQueue.add(message);
				return;
		}
		if(Server.getInstance().getRemoteChatrooms().contains(roomID)){
			
			Server.getInstance().getChatroom(this.indentity).removeClient(this.indentity);
			Server.getInstance().getConnectedClients().remove(this.indentity);
			Server.getInstance().broadCast(from, Json.changeChatroom(this.indentity, from, roomID));	
			
			ServerInfo serverInfo = Server.getInstance().getRemoteServerInfo(roomID);
			Message message=new Message(false,Json.rout(roomID, serverInfo.getServerAddress(), String.valueOf(serverInfo.getPort())));
			messageQueue.add(message);
			return;
		}
		Message message=new Message(false,Json.changeChatroom(this.indentity, from, from));
		messageQueue.add(message);
		
	}
	
	public void moveJoin(String msg){
		
		String identity = Common.unmarshalling(msg,"identity");
		String roomid = Common.unmarshalling( msg,"roomid");
		String former = Common.unmarshalling( msg,"former");
		String serverID = Server.getInstance().getServerInfo().getServerID();
		
		if(Server.getInstance().getConnectedClients().contains(identity)){
			Message message=new Message(false,Json.serverchange(serverID,"false"));
			messageQueue.add(message);
			return;
		}
		if(Server.getInstance().getLockedIndetity().contains(identity)){
			Message message=new Message(false,Json.serverchange(serverID,"false"));
			messageQueue.add(message);
			return;
		}
	
		Server.getInstance().addLockedIndetity(identity);
		if(messageToOtherServer(Json.lockIdentity(identity))){
			
			if(Server.getInstance().isRoomLocal(roomid)){
				
				UserInfo user=new UserInfo(identity, clientSocket, this,roomid);
				Message message=new Message(false,Json.serverchange(serverID,"true"));
				messageQueue.add(message);
				this.indentity=identity;
				Server.getInstance().clientConnected(user);
				Server.getInstance().moveToChatroom(identity,roomid);
				Server.getInstance().broadCast(roomid,Json.changeChatroom(identity,former,roomid));
				
			}else{

				UserInfo user=new UserInfo(identity, clientSocket, this,mainHall);
				Message message=new Message(false,Json.serverchange(serverID,"true"));
				messageQueue.add(message);
				this.indentity=identity;
				Server.getInstance().clientConnected(user);
				Server.getInstance().moveToChatroom(identity,mainHall);
				Server.getInstance().broadCast(mainHall,Json.changeChatroom(identity,former,mainHall));

			}
		}else{
			Message message=new Message(false,Json.serverchange(serverID,"false"));
			messageQueue.add(message);
			}
		Server.getInstance().removeLockedIndetity(identity);
		messageToOtherServer(Json.releaseIdentity(identity));
	}
	
	public void deleteRoom(String msg){

		String roomid = Common.unmarshalling( msg,"roomid");
		if(Server.getInstance().removeChatroomLegal(this.indentity,roomid)){
			 deletRoomBroadCast(roomid);
		}
		else{
			Message message=new Message(false,Json.deletechatroom(roomid,"false"));
			messageQueue.add(message);
		}
	}
	
	public void deletRoomBroadCast(String room){
		
		messageToOtherServer(Json.deletechatroomToServer(room, Server.getInstance().getServerInfo().getServerID()));
		
		//broad Cast change room previous room
		for(String name :Server.getInstance().getChatroomByRoomID(room).getClients()){
			Server.getInstance().broadCast(room,Json.changeChatroom(name, room, this.mainHall));
			//broad Cast change room mainhall
			Server.getInstance().broadCast(this.mainHall,Json.changeChatroom(name, room, this.mainHall));	
		}
		Message message=new Message(false,Json.deletechatroom(room,"true"));
		messageQueue.add(message);
		ArrayList<String> names=new ArrayList<String>();
		for(String name :Server.getInstance().getChatroomByRoomID(room).getClients()){
			names.add(name);
		}
		for(String name :names){	
			Server.getInstance().moveToChatroom(name,this.mainHall);
		}
		Server.getInstance().removeChatroom(room);
	}

	public void quit(){
		if(Server.getInstance().getConnectedClients()==null||this.indentity==null){
			return;
		}
		System.out.print(this.indentity);
		if(Server.getInstance().getConnectedClients().contains(this.indentity)){
			ChatroomInfo chatroominfo=Server.getInstance().getChatroom(this.indentity);
			if(chatroominfo!=null){
			String room=chatroominfo.getChatroomId();
			//broad cast every in the room quit
			Server.getInstance().broadCast(room,Json.changeChatroom(this.indentity, room, ""));
			chatroominfo.removeClient(this.indentity);
			
			//deletroom
			if(Server.getInstance().isRoomOwner(this.indentity)){
				deletRoomBroadCast(room);
			}
		 }
		}
		Message exit=new Message(false,"exit");
		messageQueue.add(exit);
	}
	
	public Boolean messageToOtherServer(String message){
		System.out.print(message);
		try {
			
			CountDownLatch countDownLatch = new CountDownLatch(Server.getInstance().getServerList().size());
			List<Handler> handlers = new ArrayList<Handler>();

			for( ServerInfo serverinfo : Server.getInstance().getServerList()){
				
					Socket socket = new Socket(serverinfo.getServerAddress(), serverinfo.getManagementPort());
					Handler handler = new Handler(socket,message, countDownLatch);
					handlers.add(handler);
					Server.getInstance().pool.execute(handler);
			}
			countDownLatch.await();

			for(Handler item:handlers){
				System.out.println("result"+item.getResult());
				if(!item.getResult().equals("")){
					if(Common.unmarshalling(item.getResult(),"locked").equals("false")){
						return false;
					}
				}
			}
			
			return true;
		
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public void write(String msg) {
		try {
			writer.write(msg + "\n");
			writer.flush();
			System.out.println(Thread.currentThread().getName() + " - Message sent to client " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
