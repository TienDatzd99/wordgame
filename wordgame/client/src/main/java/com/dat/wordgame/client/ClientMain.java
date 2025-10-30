package com.dat.wordgame.client;

import javax.swing.SwingUtilities;

import com.dat.wordgame.client.ui.DarkLoginView;
import com.dat.wordgame.client.ui.SwingLoginView;

import javafx.application.Application;

public class ClientMain {
    
    public static void main(String[] args) {
        // Check for console mode
        if (args.length > 0 && args[0].equals("--console")) {
            // Run in console mode for backward compatibility
            try {
                String host = args.length > 1 ? args[1] : "127.0.0.1";
                int port = args.length > 2 ? Integer.parseInt(args[2]) : 7777;
                new ConsoleUI(new NetClient(host, port)).start();
            } catch (Exception e) {
                System.err.println("Lỗi chạy console mode: " + e.getMessage());
            }
        } else if (args.length > 0 && args[0].equals("--swing")) {
            // Run Swing GUI mode (legacy)
            SwingUtilities.invokeLater(() -> {
                try {
                    new SwingLoginView().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Lỗi khởi động Swing GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else if (args.length > 0 && args[0].equals("--dark")) {
            // Run Dark Swing GUI mode (same as --swing for now)
            SwingUtilities.invokeLater(() -> {
                try {
                    new SwingLoginView().setVisible(true);
                } catch (Exception e) {
                    System.err.println("Lỗi khởi động Dark GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            // Run DARK JavaFX GUI mode (default - ULTRA DARK EDITION)
            System.out.println("🎮 Starting Word Game - DARK EDITION...");
            Application.launch(DarkLoginView.class, args);
        }
    }
}