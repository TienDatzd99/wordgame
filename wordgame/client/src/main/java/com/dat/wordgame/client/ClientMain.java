package com.dat.wordgame.client;

import javax.swing.SwingUtilities;
import com.dat.wordgame.client.ui.SwingLoginView;

public class ClientMain {
    
    public static void main(String[] args) {
        // Always run Swing GUI mode
        SwingUtilities.invokeLater(() -> {
            try {
                new SwingLoginView().setVisible(true);
            } catch (Exception e) {
                System.err.println("Lỗi khởi động GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}