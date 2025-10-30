package com.dat.wordgame.client;

import java.io.BufferedReader;
import java.util.function.Consumer;

import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;

public class IncomingLoop implements Runnable {
    private final BufferedReader in;
    private final Consumer<Message> on;
    
    public IncomingLoop(BufferedReader in, Consumer<Message> on) {
        this.in = in;
        this.on = on;
    }
    
    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                on.accept(Json.decode(line));
            }
        } catch (Exception ignore) {
            // Connection closed or error occurred
        }
    }
}