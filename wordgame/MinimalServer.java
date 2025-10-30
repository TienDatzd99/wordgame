// package com.dat.wordgame.server;

import com.dat.wordgame.common.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class MinimalServer {
    public static void main(String[] args) throws Exception {
        // Initialize database
        System.out.println("Initializing database...");
        Class.forName("org.sqlite.JDBC");
        initDatabase();
        
        System.out.println("Starting minimal server on port 7777...");
        
        try (ServerSocket ss = new ServerSocket(7777)) {
            while (true) {
                Socket client = ss.accept();
                System.out.println("Client connected: " + client.getRemoteSocketAddress());
                new Thread(() -> handleClient(client)).start();
            }
        }
    }
    
    static void initDatabase() throws Exception {
        try (var c = DriverManager.getConnection("jdbc:sqlite:wordgame.db"); var s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password TEXT NOT NULL, points INTEGER DEFAULT 0);");
            
            // Check if users exist, if not, seed them
            try (var rs = s.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Seeding default users...");
                    try (var ps = c.prepareStatement("INSERT INTO users(username,password,points) VALUES(?,?,?)")) {
                        for (String u : new String[]{"alice", "bob", "charlie", "dora"}) {
                            ps.setString(1, u);
                            ps.setString(2, "123");
                            ps.setInt(3, 0);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                    System.out.println("Default users seeded!");
                } else {
                    System.out.println("Users already exist in database.");
                }
            }
        }
    }
    
    static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
            
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);
                
                try {
                    Message msg = Json.decode(line);
                    System.out.println("Message type: " + msg.type());
                    
                    if (msg.type() == MessageType.LOGIN_REQ) {
                        Models.LoginReq req = Json.GSON.fromJson(Json.GSON.toJson(msg.payload()), Models.LoginReq.class);
                        System.out.println("Login request: " + req.username() + " / " + req.password());
                        
                        if (auth(req.username(), req.password())) {
                            System.out.println("Login SUCCESS for: " + req.username());
                            Message response = Message.of(MessageType.LOGIN_OK, new Models.LoginOk(req.username(), getPoints(req.username())));
                            out.println(Json.encode(response));
                        } else {
                            System.out.println("Login FAILED for: " + req.username());
                            Message response = Message.of(MessageType.ERROR, new Models.Err("AUTH", "Sai tài khoản/mật khẩu"));
                            out.println(Json.encode(response));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (Exception e) {}
        }
    }
    
    static boolean auth(String u, String p) {
        try (var c = DriverManager.getConnection("jdbc:sqlite:wordgame.db");
             var ps = c.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")) {
            ps.setString(1, u);
            ps.setString(2, p);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    static int getPoints(String u) {
        try (var c = DriverManager.getConnection("jdbc:sqlite:wordgame.db");
             var ps = c.prepareStatement("SELECT points FROM users WHERE username=?")) {
            ps.setString(1, u);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}