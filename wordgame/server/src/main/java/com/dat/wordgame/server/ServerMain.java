package com.dat.wordgame.server;


import java.io.*; import java.net.*; import java.util.concurrent.*;


public class ServerMain {
private final ExecutorService pool = Executors.newCachedThreadPool();
public static void main(String[] args) throws Exception { new ServerMain().start(); }
public void start() throws Exception {
Persistence.init();
try (ServerSocket ss = new ServerSocket(ServerConfig.PORT)){
System.out.println("Server started on port " + ServerConfig.PORT);
while(true){
Socket s = ss.accept();
s.setTcpNoDelay(true);
pool.submit(new ClientSession(s));
}
}
}
}