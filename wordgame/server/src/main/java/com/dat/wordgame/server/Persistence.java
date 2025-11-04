package com.dat.wordgame.server;


import java.sql.*; import java.util.*;


public class Persistence {
public static void init(){ try(var c=conn(); var s=c.createStatement()){
s.executeUpdate("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password TEXT NOT NULL, points INTEGER DEFAULT 0);");
s.executeUpdate("CREATE TABLE IF NOT EXISTS stats(username TEXT, wins INTEGER DEFAULT 0, losses INTEGER DEFAULT 0, bonus_count INTEGER DEFAULT 0);");
s.executeUpdate("CREATE TABLE IF NOT EXISTS friends(user1 TEXT, user2 TEXT, status TEXT DEFAULT 'pending', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(user1, user2));");
seed();
}catch(Exception e){ e.printStackTrace(); throw new RuntimeException(e);} }


private static void seed() throws Exception {
try(var c=conn()){
if(totalUsers(c)==0){
try(var ps=c.prepareStatement("INSERT INTO users(username,password,points) VALUES(?,?,?)")){
for(String u: List.of("alice","bob","charlie","dora")){
ps.setString(1,u); ps.setString(2,"123"); ps.setInt(3,0); ps.addBatch();
}
ps.executeBatch();
}
}
}
}
private static int totalUsers(Connection c) throws Exception { try(var rs=c.createStatement().executeQuery("SELECT COUNT(*) FROM users")){ return rs.getInt(1);} }


public static boolean auth(String u, String p){ 
System.out.println("AUTH: Trying login for user: " + u + " with password: " + p);
try(var c=conn(); var ps=c.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")){
ps.setString(1,u); ps.setString(2,p); try(var rs=ps.executeQuery()){ 
boolean result = rs.next();
System.out.println("AUTH: Login result for " + u + ": " + result);
return result;
}
}catch(Exception e){ 
System.out.println("AUTH: Exception during login: " + e.getMessage());
e.printStackTrace();
return false; 
} }


public static boolean userExists(String u){ 
try(var c=conn(); var ps=c.prepareStatement("SELECT 1 FROM users WHERE username=?")){
ps.setString(1,u); try(var rs=ps.executeQuery()){ return rs.next(); }
}catch(Exception e){ return false; } 
}


public static boolean createUser(String u, String p){ 
System.out.println("CREATE_USER: Attempting to create user: " + u);
if(userExists(u)){
System.out.println("CREATE_USER: User already exists: " + u);
return false;
}
try(var c=conn(); var ps=c.prepareStatement("INSERT INTO users(username,password,points) VALUES(?,?,?)")){
ps.setString(1,u); ps.setString(2,p); ps.setInt(3,0);
int rows = ps.executeUpdate();
System.out.println("CREATE_USER: User created successfully: " + u + " (rows=" + rows + ")");
return rows > 0;
}catch(Exception e){ 
System.out.println("CREATE_USER: Exception during user creation: " + e.getMessage());
e.printStackTrace();
return false; 
} 
}


public static int totalPoints(String u){ try(var c=conn(); var ps=c.prepareStatement("SELECT points FROM users WHERE username=?")){
ps.setString(1,u); try(var rs=ps.executeQuery()){ return rs.next()?rs.getInt(1):0; }
}catch(Exception e){ return 0; } }


public static void addPoints(String u, int pts){ try(var c=conn(); var ps=c.prepareStatement("UPDATE users SET points=points+? WHERE username=?")){
ps.setInt(1, pts); ps.setString(2, u); ps.executeUpdate();
}catch(Exception e){ e.printStackTrace(); } }


public record PlayerRow(String name, int points){}
public static List<PlayerRow> topPlayers(int k){
try(var c=conn(); var ps=c.prepareStatement("SELECT username, points FROM users ORDER BY points DESC LIMIT ?")){
ps.setInt(1,k); try(var rs=ps.executeQuery()){
var out=new ArrayList<PlayerRow>(); while(rs.next()) out.add(new PlayerRow(rs.getString(1), rs.getInt(2))); return out; }
}catch(Exception e){ return List.of(); }
}


// Friend system methods
public static boolean sendFriendRequest(String from, String to) {
    if (from.equals(to)) return false;
    try(var c=conn(); var ps=c.prepareStatement("INSERT OR REPLACE INTO friends(user1, user2, status) VALUES(?, ?, 'pending')")){
        ps.setString(1, from); ps.setString(2, to); 
        return ps.executeUpdate() > 0;
    }catch(Exception e){ e.printStackTrace(); return false; }
}

public static boolean acceptFriendRequest(String from, String to) {
    try(var c=conn(); var ps=c.prepareStatement("UPDATE friends SET status='accepted' WHERE user1=? AND user2=? AND status='pending'")){
        ps.setString(1, from); ps.setString(2, to);
        return ps.executeUpdate() > 0;
    }catch(Exception e){ e.printStackTrace(); return false; }
}

public static boolean rejectFriendRequest(String from, String to) {
    try(var c=conn(); var ps=c.prepareStatement("DELETE FROM friends WHERE user1=? AND user2=? AND status='pending'")){
        ps.setString(1, from); ps.setString(2, to);
        return ps.executeUpdate() > 0;
    }catch(Exception e){ e.printStackTrace(); return false; }
}

public record FriendRow(String username, boolean isOnline, int points){}
public static List<FriendRow> getFriends(String username, Set<String> onlineUsers) {
    try(var c=conn(); var ps=c.prepareStatement(
        "SELECT u.username, u.points FROM friends f " +
        "JOIN users u ON (f.user2 = u.username AND f.user1 = ?) " +
        "WHERE f.status = 'accepted' " +
        "UNION " +
        "SELECT u.username, u.points FROM friends f " +
        "JOIN users u ON (f.user1 = u.username AND f.user2 = ?) " +
        "WHERE f.status = 'accepted' " +
        "ORDER BY u.points DESC")){
        ps.setString(1, username); ps.setString(2, username);
        try(var rs=ps.executeQuery()){
            var out=new ArrayList<FriendRow>(); 
            while(rs.next()) {
                String friendName = rs.getString(1);
                int points = rs.getInt(2);
                boolean isOnline = onlineUsers.contains(friendName);
                out.add(new FriendRow(friendName, isOnline, points));
            }
            // Sort: online first, then by points
            out.sort((a, b) -> {
                if (a.isOnline && !b.isOnline) return -1;
                if (!a.isOnline && b.isOnline) return 1;
                return Integer.compare(b.points, a.points);
            });
            return out;
        }
    }catch(Exception e){ e.printStackTrace(); return List.of(); }
}

public static List<String> getPendingFriendRequests(String username) {
    try(var c=conn(); var ps=c.prepareStatement("SELECT user1 FROM friends WHERE user2=? AND status='pending'")){
        ps.setString(1, username);
        try(var rs=ps.executeQuery()){
            var out=new ArrayList<String>(); 
            while(rs.next()) out.add(rs.getString(1));
            return out;
        }
    }catch(Exception e){ e.printStackTrace(); return List.of(); }
}

private static Connection conn() throws Exception { return DriverManager.getConnection(ServerConfig.DB_URL); }
}