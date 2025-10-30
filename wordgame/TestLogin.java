import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestLogin {
    public static void main(String[] args) {
        String dbUrl = "jdbc:h2:./wordgame;AUTO_SERVER=TRUE";
        
        try (Connection c = DriverManager.getConnection(dbUrl)) {
            // Test database connection
            System.out.println("Database connected successfully!");
            
            // Check users table
            try (Statement stmt = c.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT username, password, points FROM users");
                System.out.println("=== Users in database ===");
                while (rs.next()) {
                    System.out.println("User: " + rs.getString("username") + 
                                     ", Password: " + rs.getString("password") + 
                                     ", Points: " + rs.getInt("points"));
                }
            }
            
            // Test auth function
            System.out.println("\n=== Testing authentication ===");
            testAuth(c, "alice", "123");
            testAuth(c, "bob", "123");
            testAuth(c, "alice", "wrong");
            testAuth(c, "nonexistent", "123");
            
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static void testAuth(Connection c, String username, String password) {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                boolean result = rs.next();
                System.out.println("Auth test: " + username + "/" + password + " -> " + result);
            }
        } catch (Exception e) {
            System.err.println("Auth test error for " + username + ": " + e.getMessage());
        }
    }
}