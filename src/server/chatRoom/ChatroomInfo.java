package server.chatRoom;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import server.client.UserInfo;
import server.util.Message;

public class ChatroomInfo {

    private String chatroomId;
    private String creator;

    private ConcurrentHashMap<String, UserInfo> clientList = new ConcurrentHashMap<>();

    public ChatroomInfo(String chatroomId, String indentity) {
        this.chatroomId = chatroomId;
        this.creator = indentity;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public void addClient(String identity, UserInfo userInfo) {
        clientList.put(identity, userInfo);
    }

    public void removeClient(String identity) {
        clientList.remove(identity);
    }

    public Set<String> getClients() {
        return clientList.keySet();
    }

    public void broadCast(String message) {
        for (UserInfo item : clientList.values()) {
            Message msg = new Message(false, message);
            item.getManagingThread().getMessageQueue().add(msg);
        }
    }

    public void broadCast(String identity, String message) {

        for (UserInfo item : clientList.values()) {
            if (!item.getIdentity().equals(identity)) {
                Message msg = new Message(false, message);
                item.getManagingThread().getMessageQueue().add(msg);
            }

        }
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
