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
        switch (m.type) {
            case INVITE_SEND -> {
                String to = (String) ((Map<?, ?>) m.payload).get("to");
                var dst = online.get(to);
                if (dst != null) dst.send(Message.of(MessageType.INVITE_RECEIVE, Map.of("from", from)));
            }
            case INVITE_ACCEPT -> {
                String host = (String) ((Map<?, ?>) m.payload).get("host");
                GameRoom room = new GameRoom(host, from, this);
                rooms.put(room.id(), room);
                room.notifyJoin();
                broadcastLobby();
            }
            case INVITE_REJECT -> { /* optional feedback */ }
            case GUESS_SUBMIT -> {
                Models.GuessSubmit gs = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.GuessSubmit.class);
                GameRoom r = rooms.get(gs.roomId()); 
                if (r != null) r.onGuess(from, gs.guess());
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