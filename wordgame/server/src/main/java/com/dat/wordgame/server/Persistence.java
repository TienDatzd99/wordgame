package com.dat.wordgame.server;


import java.sql.*; import java.util.*;


public class Persistence {
public static void init(){ try(var c=conn(); var s=c.createStatement()){
s.executeUpdate("CREATE TABLE IF NOT EXISTS users(username TEXT PRIMARY KEY, password TEXT NOT NULL, points INTEGER DEFAULT 0);");
s.executeUpdate("CREATE TABLE IF NOT EXISTS stats(username TEXT, wins INTEGER DEFAULT 0, losses INTEGER DEFAULT 0, bonus_count INTEGER DEFAULT 0);");
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


private static Connection conn() throws Exception { return DriverManager.getConnection(ServerConfig.DB_URL); }
}