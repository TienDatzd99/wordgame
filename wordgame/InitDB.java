import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class InitDB {
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:./wordgame;AUTO_SERVER=TRUE";
        
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            System.out.println("Database connected successfully!");
            
            // Create tables
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(username VARCHAR(255) PRIMARY KEY, password VARCHAR(255) NOT NULL, points INTEGER DEFAULT 0);");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS stats(username VARCHAR(255), wins INTEGER DEFAULT 0, losses INTEGER DEFAULT 0, bonus_count INTEGER DEFAULT 0);");
                System.out.println("Tables created successfully!");
            }
            
            // Check if users exist
            try (Statement stmt = c.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("No users found, seeding default users...");
                    
                    // Seed users
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO users(username,password,points) VALUES(?,?,?)")) {
                        String[] users = {"alice", "bob", "charlie", "dora"};
                        for (String user : users) {
                            ps.setString(1, user);
                            ps.setString(2, "123");
                            ps.setInt(3, 0);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        System.out.println("Default users seeded!");
                    }
                } else {
                    System.out.println("Users already exist in database.");
                }
            }
            
            // List all users
            try (Statement stmt = c.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT username, password, points FROM users");
                System.out.println("\n=== Current users in database ===");
                while (rs.next()) {
                    System.out.println("User: " + rs.getString("username") + 
                                     ", Password: " + rs.getString("password") + 
                                     ", Points: " + rs.getInt("points"));
                }
            }
            
            System.out.println("\nDatabase initialization completed!");
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}