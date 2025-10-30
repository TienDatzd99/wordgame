package com.dat.wordgame.server;

import java.sql.DriverManager;

public class SimpleServer {
    public static void main(String[] args) throws Exception {
        System.out.println("Simple server for testing authentication...");
        
        // Load SQLite driver
        Class.forName("org.sqlite.JDBC");
        
        // Initialize database
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
                }
            }
            
            // List users
            System.out.println("Users in database:");
            try (var rs = s.executeQuery("SELECT username, password, points FROM users")) {
                while (rs.next()) {
                    System.out.println("  " + rs.getString("username") + " / " + rs.getString("password") + " / " + rs.getInt("points"));
                }
            }
        }
        
        System.out.println("Database ready. You can now test login with alice/123, bob/123, etc.");
        
        // Test authentication
        System.out.println("\nTesting auth:");
        System.out.println("alice/123: " + auth("alice", "123"));
        System.out.println("alice/wrong: " + auth("alice", "wrong"));
        System.out.println("bob/123: " + auth("bob", "123"));
    }
    
    public static boolean auth(String u, String p) {
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
}