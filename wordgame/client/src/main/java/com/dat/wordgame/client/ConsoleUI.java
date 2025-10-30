package com.dat.wordgame.client;

import java.util.Map;
import java.util.Scanner;

import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

public class ConsoleUI {
    private final NetClient net;
    private final Scanner sc = new Scanner(System.in);
    private String roomId;
    private int remain;
    
    public ConsoleUI(NetClient net) {
        this.net = net;
    }

    public void start() {
        net.listen(this::onMsg);
        System.out.print("Tên đăng nhập: ");
        String u = sc.nextLine();
        System.out.print("Mật khẩu: ");
        String p = sc.nextLine();
        net.send(Message.of(MessageType.LOGIN_REQ, new Models.LoginReq(u, p)));
        
        menuLoop();
    }

    void menuLoop() {
        while (true) {
            System.out.println("Commands: /invite <user>, /accept <host>, /chat <msg>, /guess <word>");
            String line = sc.nextLine();
            if (line.startsWith("/invite ")) {
                String to = line.substring(8).trim();
                net.send(Message.of(MessageType.INVITE_SEND, Map.of("to", to)));
            } else if (line.startsWith("/accept ")) {
                String host = line.substring(8).trim();
                net.send(Message.of(MessageType.INVITE_ACCEPT, Map.of("host", host)));
            } else if (line.startsWith("/chat ")) {
                String text = line.substring(6);
                net.send(Message.of(MessageType.CHAT, new Models.Chat(roomId, "me", text)));
            } else if (line.startsWith("/guess ")) {
                String g = line.substring(7).trim();
                net.send(Message.of(MessageType.GUESS_SUBMIT, new Models.GuessSubmit(roomId, g)));
            }
        }
    }

    private void onMsg(Message m) {
        switch (m.type) {
            case LOGIN_OK -> System.out.println("Đăng nhập OK: " + Json.GSON.toJson(m.payload));
            case LOBBY_SNAPSHOT -> System.out.println("Lobby: " + Json.GSON.toJson(m.payload));
            case INVITE_RECEIVE -> System.out.println("Có lời mời: " + Json.GSON.toJson(m.payload) + " -> dùng /accept <host>");
            case ROOM_JOINED -> {
                var rs = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoomState.class);
                roomId = rs.roomId();
                System.out.println("Vào phòng " + roomId);
            }
            case ROUND_START -> {
                var rs = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoundStart.class);
                remain = rs.totalTimeSec();
                System.out.println("ROUND " + rs.round() + " | " + rs.maskedWord() + " | letters=" + rs.shuffledLetters() + " | time=" + rs.totalTimeSec());
            }
            case ROUND_TICK -> {
                var t = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RoundTick.class);
                remain = t.remainSec();
                System.out.println("Time: " + remain);
            }
            case GUESS_UPDATE -> System.out.println("Đúng vị trí: " + Json.GSON.toJson(m.payload));
            case ROUND_END -> System.out.println("Kết thúc round: " + Json.GSON.toJson(m.payload));
            case GAME_END -> System.out.println("Kết thúc trận: " + Json.GSON.toJson(m.payload));
            case ERROR -> System.out.println("Lỗi: " + Json.GSON.toJson(m.payload));
            default -> {}
        }
    }
}