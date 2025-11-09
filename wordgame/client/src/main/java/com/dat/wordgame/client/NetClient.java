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
    
   public boolean send(Message m) {
        System.out.println("[NetClient] Sending message: " + m.type + " | Connected: " + !sock.isClosed());
        
        // Kiểm tra xem socket còn mở không
        if (sock.isClosed() || out.checkError()) {
            System.err.println("[NetClient] Send failed: Socket is closed or in error state.");
            return false;
        }

        try {
            out.println(Json.encode(m));
            // Không cần gọi out.flush() nếu bạn đã đặt autoFlush=true
            // Nhưng gọi nó cũng không sao, đảm bảo dữ liệu được gửi đi.
            out.flush(); 
            
            // Kiểm tra lỗi sau khi flush
            if (out.checkError()) {
                 System.err.println("[NetClient] Send failed: PrintWriter encountered an error.");
                 return false;
            }
            
            return true; // Gửi thành công

        } catch (Exception e) {
            // Bắt bất kỳ lỗi nào xảy ra trong quá trình gửi
            System.err.println("[NetClient] Exception during send: " + e.getMessage());
            e.printStackTrace();
            return false; // Gửi thất bại
        }
    }
    
    public void listen(Consumer<Message> on) {
        new Thread(new IncomingLoop(in, on)).start();
    }
    
    @Override
    public void close() throws IOException {
        sock.close();
    }
}