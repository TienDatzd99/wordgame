package com.dat.wordgame.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;

public class NetClient implements Closeable {
    private final Socket sock;
    private final BufferedReader in;
    private final PrintWriter out;
    
    public NetClient(String host, int port) throws Exception {
        sock = new Socket(host, port);
        sock.setTcpNoDelay(true);
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
    }
    
    public void send(Message m) {
        System.out.println("[NetClient] Sending message: " + m.type + " | Connected: " + !sock.isClosed());
        out.println(Json.encode(m));
        out.flush(); // Ensure message is sent immediately
    }
    
    public void listen(Consumer<Message> on) {
        new Thread(new IncomingLoop(in, on)).start();
    }
    
    @Override
    public void close() throws IOException {
        sock.close();
    }
}