package server.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import server.Server;
import server.util.Json;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class ServerConnection implements Runnable {

    public ServerConnection() {

    }

    public void run() {

        SSLServerSocket listeningSocket = null;
        try {
            SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            listeningSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(Server.getInstance().getServerInfo().getManagementPort());


            while (true) {

                SSLSocket clientSocket = (SSLSocket) listeningSocket.accept();
                System.out.println("ask from other server");
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                        clientSocket.getOutputStream(), "UTF-8"));
                String clientMsg = in.readLine();
                System.out.println(clientMsg);

                fromOtherServers(out, clientMsg);
                System.out.println("close");
                clientSocket.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void fromOtherServers(BufferedWriter writter, String input) {

        JSONParser parser = new JSONParser();
        JSONObject clientJson;
        try {
            clientJson = (JSONObject) parser.parse(input);

            String type = (String) clientJson.get("type");

            switch (type) {
                case "lockindentity":

                    String identity = (String) clientJson.get("identity");
                    String serverid = (String) clientJson.get("serverid");
                    String result;
                    if (Server.getInstance().getConnectedClients().contains(identity)) {

                        result = Json.lockindentityServerFalse(identity);
                    } else if (Server.getInstance().getLockedIndetity().contains(identity)) {
                        result = Json.lockindentityServerFalse(identity);
                    } else {
                        Server.getInstance().addLockedIndetity(identity, serverid);
                        result = Json.lockindentityServerTrue(identity);
                    }
                    try {
                        writter.write(result + "\n");
                        writter.flush();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    break;
                case "releaseidentity":

                    String identity2 = (String) clientJson.get("identity");
                    if (clientJson.get("serverid").equals(Server.getInstance().getLockedIndetityServer(identity2))) {
                        Server.getInstance().removeLockedIndetity(identity2);
                    }
                    break;
                case "lockroomid":


                    String roomID = (String) clientJson.get("roomid");
                    String result2;
                    if (Server.getInstance().getLockedChatrooms().contains(roomID)) {

                        result2 = Json.lockRoomIDReplay(roomID, "false");
                    } else if (Server.getInstance().getLocalChatrooms().contains(roomID)) {
                        result2 = Json.lockRoomIDReplay(roomID, "false");
                    } else {
                        Server.getInstance().addLockedChatrooms(roomID);

                        result2 = Json.lockRoomIDReplay(roomID, "true");
                    }
                    try {
                        writter.write(result2 + "\n");
                        writter.flush();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    break;
                case "releaseroomid":

                    String roomId = (String) clientJson.get("roomid");
                    String serverId2 = (String) clientJson.get("serverid");
                    String approved = (String) clientJson.get("approved");
                    if (approved.equals("true")) {
                        Server.getInstance().addRemoteChatrooms(roomId, serverId2);
                    }
                    Server.getInstance().removeLockedChatrooms(roomId);
                    break;
                case "deleteroom":
                    String roomId3 = (String) clientJson.get("roomid");
                    String serverid3 = (String) clientJson.get("serverid");
                    Server.getInstance().removeRemoteChatrooms(roomId3, serverid3);
                    break;
            }

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
