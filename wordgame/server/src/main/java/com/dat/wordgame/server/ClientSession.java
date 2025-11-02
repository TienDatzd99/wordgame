package com.dat.wordgame.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

public class ClientSession implements Runnable {
    private final Socket sock;
    private String username = "?";
    private PrintWriter out;
    private BufferedReader in;
    
    public ClientSession(Socket s) {
        this.sock = s;
    }
    
    @Override
    public void run() {
        try (sock) {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
            onConnect();
            String line;
            while ((line = in.readLine()) != null) {
                handle(line);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + username + " -> " + e.getMessage());
        } finally {
            LobbyManager.get().onDisconnect(username);
        }
    }
    
    private void onConnect() {
        send(Message.of(MessageType.LOBBY_SNAPSHOT, LobbyManager.get().snapshot()));
    }
    
    private void handle(String raw) {
        Message m = Json.decode(raw);
        switch (m.type) {
            case LOGIN_REQ -> {
                Models.LoginReq req = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.LoginReq.class);
                System.out.println("LOGIN_REQ received: username=" + req.username() + ", password=" + req.password());
                if (Persistence.auth(req.username(), req.password())) {
                    username = req.username();
                    System.out.println("LOGIN SUCCESS for: " + username);
                    send(Message.of(MessageType.LOGIN_OK, new Models.LoginOk(username, Persistence.totalPoints(username))));
                    LobbyManager.get().onLogin(username, this);
                } else {
                    System.out.println("LOGIN FAILED for: " + req.username());
                    sendErr("AUTH", "Sai tài khoản/mật khẩu");
                }
            }
            case REGISTER_REQ -> {
                Models.RegisterReq req = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.RegisterReq.class);
                System.out.println("REGISTER_REQ received: username=" + req.username());
                
                // Validate username
                if (req.username() == null || req.username().trim().isEmpty()) {
                    System.out.println("REGISTER FAILED: Empty username");
                    sendErr("REGISTER", "Tên đăng nhập không được để trống");
                    return;
                }
                if (req.username().length() < 3) {
                    System.out.println("REGISTER FAILED: Username too short");
                    sendErr("REGISTER", "Tên đăng nhập phải có ít nhất 3 ký tự");
                    return;
                }
                
                // Validate password
                if (req.password() == null || req.password().trim().isEmpty()) {
                    System.out.println("REGISTER FAILED: Empty password");
                    sendErr("REGISTER", "Mật khẩu không được để trống");
                    return;
                }
                if (req.password().length() < 3) {
                    System.out.println("REGISTER FAILED: Password too short");
                    sendErr("REGISTER", "Mật khẩu phải có ít nhất 3 ký tự");
                    return;
                }
                
                // Try to create user
                if (Persistence.createUser(req.username(), req.password())) {
                    System.out.println("REGISTER SUCCESS for: " + req.username());
                    send(Message.of(MessageType.REGISTER_OK, new Models.RegisterOk(req.username())));
                } else {
                    System.out.println("REGISTER FAILED: User already exists: " + req.username());
                    sendErr("REGISTER", "Tên đăng nhập đã tồn tại");
                }
            }
            case CHAT -> {
                Models.Chat chat = Json.GSON.fromJson(Json.GSON.toJson(m.payload), Models.Chat.class);
                ChatService.broadcast(chat);
            }
            case INVITE_ACCEPT, INVITE_REJECT, INVITE_SEND, GUESS_SUBMIT -> 
                LobbyManager.get().route(username, m);
            default -> {}
        }
    }
    
    public void send(Message m) {
        out.println(Json.encode(m));
    }
    
    public void sendErr(String code, String message) {
        send(Message.of(MessageType.ERROR, new Models.Err(code, message)));
    }
    
    public String name() {
        return username;
    }
}