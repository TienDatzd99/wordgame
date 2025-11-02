package com.dat.wordgame.server;


import java.io.*; import java.net.*; import java.util.concurrent.*;


public class ServerMain {
private final ExecutorService pool = Executors.newCachedThreadPool();
public static void main(String[] args) throws Exception { new ServerMain().start(); }
public void start() throws Exception {
Persistence.init();
// Preload words on startup
System.out.println("Preloading word database...");
WordService.pickByRound(1); // This will trigger static initialization
System.out.println("Word database loaded successfully!");
try (ServerSocket ss = new ServerSocket(ServerConfig.PORT, 50, InetAddress.getByName("0.0.0.0"))){
System.out.println("Server started on port " + ServerConfig.PORT);
System.out.println("Server listening on all network interfaces (0.0.0.0)");
while(true){
Socket s = ss.accept();
s.setTcpNoDelay(true);
pool.submit(new ClientSession(s));
}
}
}
}