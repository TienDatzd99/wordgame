package com.dat.wordgame.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

public class LobbyManager {
    private static final LobbyManager I = new LobbyManager();
    public static LobbyManager get() { return I; }

    private final Map<String, ClientSession> online = new ConcurrentHashMap<>();
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public void onLogin(String user, ClientSession s) { 
        online.put(user, s); 
        broadcastLobby(); 
    }
    
    public void onDisconnect(String user) { 
        online.remove(user); 
        rooms.values().forEach(r -> r.onDisconnect(user)); 
        broadcastLobby(); 
    }

    void broadcastLobby() {
        var players = online.keySet().stream()
            .map(u -> new Models.PlayerBrief(u, Persistence.totalPoints(u), "online"))
            .toList();
        var leaders = Persistence.topPlayers(10).stream()
            .map(p -> new Models.PlayerBrief(p.name(), p.points(), "top"))
            .toList();
        var roomBriefs = rooms.values().stream()
            .map(GameRoom::brief)
            .toList();
        var snap = new Models.LobbySnapshot(players, leaders, roomBriefs);
        broadcast(Message.of(MessageType.LOBBY_SNAPSHOT, snap));
    }

    void broadcast(Message m) { 
        online.values().forEach(c -> c.send(m)); 
    }

    public void route(String from, Message m) {
        System.out.println("[LobbyManager] Received message type: " + m.type + " from: " + from);
        switch (m.type) {
            case INVITE_SEND -> {
                Models.InviteSend invite = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.InviteSend.class);
                var dst = online.get(invite.to());
                if (dst != null) {
                    Models.InviteReceive inviteReceive = new Models.InviteReceive(invite.from());
                    dst.send(Message.of(MessageType.INVITE_RECEIVE, inviteReceive));
                    System.out.println("INVITE_SEND: " + invite.from() + " -> " + invite.to());
                }
            }
            case INVITE_ACCEPT -> {
                Models.InviteAccept accept = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.InviteAccept.class);
                String host = accept.from(); // The person who sent the original invite
                String opponent = accept.to(); // The person who accepted (current user)
                GameRoom room = new GameRoom(host, opponent, this);
                rooms.put(room.id(), room);
                room.notifyJoin();
                broadcastLobby();
                System.out.println("INVITE_ACCEPT: Room created - " + host + " vs " + opponent);
            }
            case INVITE_REJECT -> { 
                System.out.println("INVITE_REJECT received");
                /* optional feedback */ 
            }
            
            // Friend system handlers
            case FRIEND_REQUEST_SEND -> {
                Models.FriendRequest request = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.FriendRequest.class);
                if (Persistence.userExists(request.to()) && Persistence.sendFriendRequest(request.from(), request.to())) {
                    var dst = online.get(request.to());
                    if (dst != null) {
                        Models.FriendRequestReceive receive = new Models.FriendRequestReceive(request.from());
                        dst.send(Message.of(MessageType.FRIEND_REQUEST_RECEIVE, receive));
                    }
                    System.out.println("FRIEND_REQUEST_SEND: " + request.from() + " -> " + request.to());
                }
            }
            case FRIEND_REQUEST_ACCEPT -> {
                Models.FriendRequestAccept accept = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.FriendRequestAccept.class);
                if (Persistence.acceptFriendRequest(accept.from(), accept.to())) {
                    System.out.println("FRIEND_REQUEST_ACCEPT: " + accept.to() + " accepted " + accept.from());
                }
            }
            case FRIEND_REQUEST_REJECT -> {
                Models.FriendRequestReject reject = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.FriendRequestReject.class);
                if (Persistence.rejectFriendRequest(reject.from(), reject.to())) {
                    System.out.println("FRIEND_REQUEST_REJECT: " + reject.to() + " rejected " + reject.from());
                }
            }
            case FRIEND_LIST_REQ -> {
                System.out.println("[LobbyManager] Processing FRIEND_LIST_REQ from: " + from);
                Models.FriendListReq req = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.FriendListReq.class);
                System.out.println("[LobbyManager] Request username: " + req.username());
                var friends = Persistence.getFriends(req.username(), online.keySet());
                System.out.println("[LobbyManager] Found " + friends.size() + " friends");
                var friendInfos = friends.stream()
                    .map(f -> new Models.FriendInfo(f.username(), f.isOnline(), f.points()))
                    .toList();
                Models.FriendListResp resp = new Models.FriendListResp(friendInfos);
                System.out.println("[LobbyManager] Sending FRIEND_LIST_RESP with " + friendInfos.size() + " friends to: " + from);
                sendTo(from, Message.of(MessageType.FRIEND_LIST_RESP, resp));
            }
            case ROOM_INVITE_SEND -> {
                Models.RoomInviteSend invite = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoomInviteSend.class);
                var dst = online.get(invite.to());
                boolean success = false;
                String message = "Bạn bè offline hoặc không tồn tại";
                
                System.out.println("ROOM_INVITE_SEND: " + invite.from() + " -> " + invite.to() + " (room: " + invite.roomId() + ")");
                System.out.println("Target user '" + invite.to() + "' online? " + (dst != null));
                System.out.println("Online users: " + online.keySet());
                
                if (dst != null) {
                    try {
                        Models.RoomInviteReceive roomInvite = new Models.RoomInviteReceive(invite.from(), invite.roomId());
                        dst.send(Message.of(MessageType.ROOM_INVITE_RECEIVE, roomInvite));
                        success = true;
                        message = "Đã gửi lời mời chơi thành công";
                        System.out.println("Successfully sent ROOM_INVITE_RECEIVE to " + invite.to());
                    } catch (Exception e) {
                        System.out.println("Error sending ROOM_INVITE_RECEIVE to " + invite.to() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                Models.RoomInviteResp resp = new Models.RoomInviteResp(success, message);
                sendTo(from, Message.of(MessageType.ROOM_INVITE_RESP, resp));
            }
            case ROOM_INVITE_ACCEPT -> {
                Models.RoomInviteAccept accept = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoomInviteAccept.class);
                String host = accept.from(); // The person who sent the original invite
                String opponent = accept.to(); // The person who accepted (current user)
                String roomId = accept.roomId();
                
                System.out.println("ROOM_INVITE_ACCEPT received: from=" + accept.from() + ", to=" + accept.to() + ", roomId=" + roomId);
                System.out.println("Creating room with host=" + host + ", opponent=" + opponent);
                
                // Create room and send ROOM_CREATED to both players so they go to RoomView
                Models.RoomState roomState = new Models.RoomState(roomId, host, opponent, 1, "waiting");
                
                sendTo(host, Message.of(MessageType.ROOM_CREATED, roomState));
                sendTo(opponent, Message.of(MessageType.ROOM_CREATED, roomState));
                
                System.out.println("ROOM_INVITE_ACCEPT: Created room " + roomId + " - " + host + " vs " + opponent);
            }
            case ROOM_INVITE_REJECT -> {
                System.out.println("ROOM_INVITE_REJECT received");
                // Optional: notify the inviter that invite was rejected
            }
            case USER_SEARCH_REQ -> {
                Models.UserSearchReq req = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.UserSearchReq.class);
                var searchResults = Persistence.searchUsers(req.searchText(), req.requester());
                var results = searchResults.stream()
                    .map(r -> new Models.UserSearchResult(r.username(), r.points(), r.isFriend()))
                    .toList();
                Models.UserSearchResp resp = new Models.UserSearchResp(results);
                sendTo(from, Message.of(MessageType.USER_SEARCH_RESP, resp));
                System.out.println("USER_SEARCH: " + req.requester() + " searched for '" + req.searchText() + "', found " + results.size() + " results");
            }
            case GUESS_SUBMIT -> {
                Models.GuessSubmit gs = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.GuessSubmit.class);
                GameRoom r = rooms.get(gs.roomId()); 
                if (r != null) r.onGuess(from, gs.guess());
            }
            case SURRENDER -> {
                Models.Surrender surrender = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.Surrender.class);
                GameRoom r = rooms.get(surrender.roomId());
                if (r != null) {
                    r.onSurrender(surrender.player());
                    System.out.println("SURRENDER: " + surrender.player() + " surrendered in room " + surrender.roomId());
                }
            }
            case CHAT -> {
                Models.Chat chat = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.Chat.class);
                System.out.println("[LobbyManager] CHAT received from " + from + " in room " + chat.roomId() + ": " + chat.text());
                
                // Find the room and broadcast chat to both players
                GameRoom room = rooms.get(chat.roomId());
                if (room != null) {
                    // Broadcast chat message to both players in the room
                    sendToBoth(room, Message.of(MessageType.CHAT, chat));
                    System.out.println("[LobbyManager] Chat message broadcasted to both players in room " + chat.roomId());
                } else {
                    System.out.println("[LobbyManager] Room not found for chat message: " + chat.roomId());
                }
            }
            case ROOM_LEFT -> {
                Models.RoomLeft roomLeft = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoomLeft.class);
                System.out.println("[LobbyManager] Player " + roomLeft.playerLeft() + " left room " + roomLeft.roomId());
                
                // Find the room and notify other players
                String roomId = roomLeft.roomId();
                if (rooms.containsKey(roomId)) {
                    GameRoom room = rooms.get(roomId);
                    // Notify other player in the room
                    String otherPlayer = room.host().equals(roomLeft.playerLeft()) ? room.opponent() : room.host();
                    if (otherPlayer != null && !otherPlayer.isEmpty()) {
                        sendTo(otherPlayer, Message.of(MessageType.ROOM_LEFT, roomLeft));
                        System.out.println("[LobbyManager] Notified " + otherPlayer + " that " + roomLeft.playerLeft() + " left room");
                    }
                    // Remove the room since someone left
                    rooms.remove(roomId);
                    System.out.println("[LobbyManager] Removed room " + roomId + " due to player leaving");
                }
            }
            default -> {}
        }
    }

    public void sendTo(String user, Message m) { 
        var c = online.get(user); 
        if (c != null) c.send(m); 
    }
    
    public void sendToBoth(GameRoom r, Message m) { 
        sendTo(r.host(), m); 
        sendTo(r.opponent(), m); 
    }

    public Models.LobbySnapshot snapshot() {
        var players = online.keySet().stream()
            .map(u -> new Models.PlayerBrief(u, Persistence.totalPoints(u), "online"))
            .toList();
        var leaders = Persistence.topPlayers(10).stream()
            .map(p -> new Models.PlayerBrief(p.name(), p.points(), "top"))
            .toList();
        var roomBriefs = rooms.values().stream()
            .map(GameRoom::brief)
            .toList();
        return new Models.LobbySnapshot(players, leaders, roomBriefs);
    }
}