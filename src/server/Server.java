package server;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import server.chatRoom.ChatroomInfo;
import server.client.UserInfo;
import server.threads.ClientConnection;
import server.threads.ServerConnection;
import server.util.CmdLineArgs;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class Server {

    private ServerInfo serverInfo;
    private ConcurrentHashMap<String, UserInfo> clientList = new ConcurrentHashMap<>();
    private HashMap<String, ServerInfo> serverList = new HashMap<>();
    private ConcurrentHashMap<String, String> lockedIndetityID = new ConcurrentHashMap<>();

    private ChatroomInfo mainHall;
    private ConcurrentHashMap<String, ChatroomInfo> LocalChatRoomList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> RemoteChatRoomList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> lockedChatroomID = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> authList = new ConcurrentHashMap<>();

    public ThreadPoolExecutor pool;

    private static Server server = new Server();

    private Server() {
    }

    public Collection<ServerInfo> getServerList() {
        return serverList.values();
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public static Server getInstance() {
        return server;
    }

    // connected client
    public void clientConnected(UserInfo client) {
        clientList.put(client.getIdentity(), client);
    }

    public void clientDisconnected(String identity) {
        if (clientList != null && clientList.keySet().contains(identity)) {
            clientList.remove(identity);
        }
    }

    public Set<String> getConnectedClients() {
        return clientList.keySet();
    }

    // locked Indetity
    public void addLockedIndetity(String identity, String serverID) {
        lockedIndetityID.put(identity, serverID);
    }

    public void addLockedIndetity(String identity) {
        lockedIndetityID.put(identity, serverInfo.getServerID());
    }

    public void removeLockedIndetity(String identity) {
        lockedIndetityID.remove(identity);
    }

    public String getLockedIndetityServer(String identity) {
        return lockedIndetityID.get(identity);
    }

    public Set<String> getLockedIndetity() {
        return lockedIndetityID.keySet();
    }

    // local chat room
    public ChatroomInfo getChatroom(String indentity) {
        UserInfo client = clientList.get(indentity);
        ChatroomInfo ChatroomInfo = LocalChatRoomList.get(client
                .getCurrentChatroom());
        return ChatroomInfo;
    }

    public ChatroomInfo getChatroomByRoomID(String roomid) {

        ChatroomInfo ChatroomInfo = LocalChatRoomList.get(roomid);
        return ChatroomInfo;
    }

    public void removeChatroom(String roomID) {

        LocalChatRoomList.remove(roomID);

    }

    public Boolean removeChatroomLegal(String indentity, String roomID) {
//		UserInfo client = clientList.get(indentity);

        ChatroomInfo ChatroomInfo = LocalChatRoomList.get(roomID);
        if (ChatroomInfo != null) {
            if (ChatroomInfo.getCreator().equals(indentity)) {
                // LocalChatRoomList.remove(roomID);
                return true;
            }
        }
        return false;
    }

    public void addLocalChatrooms(String identity,
                                  ChatroomInfo chatroom) {
        LocalChatRoomList.put(identity, chatroom);

    }

    public Set<String> getLocalChatrooms() {
        return LocalChatRoomList.keySet();
    }

    // lock chatroom
    public Set<String> getLockedChatrooms() {
        return lockedChatroomID.keySet();
    }

    public void addLockedChatrooms(String roomID, String serverID) {
        lockedChatroomID.put(roomID, serverID);

    }

    public void addLockedChatrooms(String roomID) {
        lockedChatroomID.put(roomID, serverInfo.getServerID());

    }

    public void removeLockedChatrooms(String roomID,
                                      String serverID) {
        if (lockedChatroomID.get(roomID).equals(serverID)) {
            lockedChatroomID.remove(roomID);
        }
    }

    public void removeLockedChatrooms(String roomID) {

        if (lockedChatroomID.containsKey(roomID)) {
            lockedChatroomID.remove(roomID);
        }

    }

    // remote Chatroom
    public void addRemoteChatrooms(String roomID, String serverID) {
        RemoteChatRoomList.put(roomID, serverID);
    }

    public void removeRemoteChatrooms(String roomID,
                                      String serverID) {

        if (RemoteChatRoomList.get(roomID).equals(serverID)) {
            RemoteChatRoomList.remove(roomID);

        }

    }

    public Set<String> getRemoteChatrooms() {

        return RemoteChatRoomList.keySet();
    }

    public ConcurrentHashMap<String, String> getAuthList() {
        return authList;
    }

    // main hall
    public void moveToMainHall(String identity) {
        mainHall.addClient(identity, clientList.get(identity));
    }

    public void moveToChatroom(String identity, String toChatroom) {

        String fromChatroom = clientList.get(identity).getCurrentChatroom();
        LocalChatRoomList.get(fromChatroom).removeClient(identity);
        clientList.get(identity).setCurrentChatroom(toChatroom);
        LocalChatRoomList.get(toChatroom).addClient(identity,
                clientList.get(identity));
    }

//	public  void moveToChatroom2(String identity, String toChatroom) {
//
//		clientList.get(identity).setCurrentChatroom(toChatroom);
//		LocalChatRoomList.get(toChatroom).addClient(identity,
//				clientList.get(identity));
//	}

    public void removeFromChatroom(String identity,
                                   String chatroomID) {
        LocalChatRoomList.get(chatroomID).removeClient(identity);
    }

    public Boolean isRoomLocal(String roomID) {
        Boolean result = false;
        if (LocalChatRoomList.keySet().contains(roomID)) {
            result = true;
        }
        return result;

    }

    public Boolean isRoomOwner(String indentity) {
        Boolean result = false;
        for (ChatroomInfo room : LocalChatRoomList.values()) {
            if (room.getCreator().equals(indentity)) {
                result = true;
            }
        }
        return result;

    }

    public ServerInfo getRemoteServerInfo(String roomID) {
        String serverID = RemoteChatRoomList.get(roomID);
        return serverList.get(serverID);

    }

    public void broadCast(String roomID, String message) {

        LocalChatRoomList.get(roomID).broadCast(message);
    }

    public void broadCastRoommates(String identity, String message) {

        ChatroomInfo chatroominfo = Server.getInstance().getChatroom(identity);
        chatroominfo.broadCast(identity, message);
    }

    public static void main(String[] args) throws CmdLineException {

        CmdLineArgs cmdLineArgs = new CmdLineArgs();
        CmdLineParser parser = new CmdLineParser(cmdLineArgs);
        parser.parseArgument(args);
        BufferedReader readerObject,readerObject1;
        String serverConfig, authInfo;
        System.out.println("1111");

        BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(20);
        Server.getInstance().pool = new ThreadPoolExecutor(30, 32, 50, TimeUnit.MILLISECONDS, bqueue);

        try {
            readerObject = new BufferedReader(new FileReader(
                    cmdLineArgs.getServerConfig()));

            // load config file
            while ((serverConfig = readerObject.readLine()) != null) {
                String[] serverConfigList = serverConfig.split("\t");

                if (serverConfigList[0].equals(cmdLineArgs.getServerID())) {

                    Server.getInstance().serverInfo = new ServerInfo(
                            serverConfigList[0], serverConfigList[1],
                            serverConfigList[2], serverConfigList[3]);

                } else {
                    ServerInfo serverinfo = new ServerInfo(serverConfigList[0],
                            serverConfigList[1], serverConfigList[2],
                            serverConfigList[3]);
                    Server.getInstance().serverList.put(serverConfigList[0],
                            serverinfo);
                    Server.getInstance().addRemoteChatrooms(
                            "MainHall-" + serverConfigList[0],
                            serverConfigList[0]);
                }
            }

            readerObject1 = new BufferedReader(new FileReader("userlist"));
            while ((authInfo = readerObject1.readLine()) != null) {
                String[] authInfoList = authInfo.split("\t");
                Server.getInstance().authList.put(authInfoList[0],authInfoList[1]);
            }


            Server.getInstance().mainHall = new ChatroomInfo("MainHall-"
                    + Server.getInstance().serverInfo.getServerID(), "");
            Server.getInstance().LocalChatRoomList.put(
                    Server.getInstance().mainHall.getChatroomId(),
                    Server.getInstance().mainHall);

            ServerConnection serverConnection = new ServerConnection();
            Server.getInstance().pool.execute(serverConnection);
            Server.getInstance().initialWithClient();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void initialWithClient() throws IOException {

        SSLServerSocket listeningSocket = null;
        try {
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            listeningSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(serverInfo.getPort());

            while (true) {

                SSLSocket clientSocket = (SSLSocket) listeningSocket.accept();
                System.out.println(Thread.currentThread().getName()
                        + " - Client conection accepted");

                ClientConnection clientConnection = new ClientConnection(
                        clientSocket);
                Server.getInstance().pool.execute(clientConnection);

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (listeningSocket != null) {
                listeningSocket.close();
            }
        }
    }

}
