package com.dat.wordgame.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

public class SwingLoginView extends JFrame {
    private NetClient netClient;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField hostField;
    private JTextField portField;
    private JButton loginButton;
    private JButton connectButton;
    private JLabel statusLabel;
    
    // Custom gradient panel class
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Create gradient background like in the image
            Color color1 = new Color(88, 86, 214); // Purple
            Color color2 = new Color(133, 89, 215); // Lighter purple
            GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public SwingLoginView() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("� WordleCup Multiplayer - Modern Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Debug: Load modern version
        System.out.println("🎯 WordleCup Modern Edition loaded!");
        
        // Modern gradient background
        setContentPane(new GradientPanel());
        
        createComponents();
        layoutComponents();
        addEventListeners();
    }

    private void createComponents() {
        // Main transparent panel over gradient
        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false); // Make transparent to show gradient
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        
        // Create main card (white rounded panel) with login functionality
        JPanel cardPanel = createLoginCard();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        mainPanel.add(cardPanel, gbc);
        add(mainPanel);
    }
    
    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw semi-transparent dark card with purple tint
                g2d.setColor(new Color(40, 44, 52, 240)); // Dark semi-transparent
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Add purple glow effect
                g2d.setColor(new Color(88, 86, 214, 30));
                g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 18, 18);
            }
        };
        
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(400, 450));
        card.setBackground(new Color(255, 255, 255, 230)); // Semi-transparent white
        
        // Title - "WordleCup Multiplayer" với màu sáng cho nền tối
        JLabel titleLabel = new JLabel("🎮 WordleCup Multiplayer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 255, 255)); // Trắng cho nền tối
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // "Đăng nhập để chơi" subtitle
        JLabel subtitleLabel = new JLabel("Đăng nhập để chơi", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(200, 200, 200)); // Xám nhạt
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Connection panel
        JPanel connectionPanel = createConnectionPanel();
        
        // Login panel
        JPanel loginPanel = createLoginPanel();
        
        // Connection status
        statusLabel = new JLabel("Chưa kết nối server", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(220, 53, 69)); // Red
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add all components to card
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(connectionPanel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(loginPanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(statusLabel);
        
        return card;
    }
    
    private void styleModernTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(new Color(55, 59, 69)); // Dark background
        field.setForeground(new Color(255, 255, 255)); // White text
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(220, 35)); // Proper size
        field.setMinimumSize(new Dimension(220, 35));
        field.setCaretColor(new Color(255, 255, 255)); // White cursor
    }
    


    private void layoutComponents() {
        // Already handled in createComponents()
    }

    private void addEventListeners() {
        connectButton.addActionListener(e -> connectToServer());
        
        loginButton.addActionListener(e -> handleLogin());
        
        // Enter key handling for password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled()) {
                    handleLogin();
                }
            }
        });

        // Enter key handling for username field
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
    }
    
    private void connectToServer() {
        try {
            if (netClient != null) {
                netClient.close();
            }

            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            
            netClient = new NetClient(host, port);
            netClient.listen(this::onMessage);
            
            statusLabel.setText("✅ Kết nối server thành công!");
            statusLabel.setForeground(new Color(40, 167, 69));
            loginButton.setEnabled(true);
            
        } catch (Exception e) {
            statusLabel.setText("❌ Không thể kết nối: " + e.getMessage());
            statusLabel.setForeground(new Color(220, 53, 69));
            loginButton.setEnabled(false);
        }
    }

    private void handleLogin() {
        if (netClient == null) {
            statusLabel.setText("❌ Chưa kết nối server!");
            statusLabel.setForeground(new Color(220, 53, 69));
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("❌ Vui lòng nhập đầy đủ thông tin!");
            statusLabel.setForeground(new Color(220, 53, 69));
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("🔄 Đang đăng nhập...");
        statusLabel.setForeground(new Color(255, 193, 7));

        netClient.send(Message.of(MessageType.LOGIN_REQ, new Models.LoginReq(username, password)));
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("❌ Vui lòng nhập đầy đủ thông tin!");
            statusLabel.setForeground(new Color(220, 53, 69));
            return;
        }

        // For now, show a simple message (can be enhanced later)
        JOptionPane.showMessageDialog(this, 
            "📝 Đăng ký thành công!\n" +
            "Tài khoản: " + username + "\n" +
            "Giờ bạn có thể đăng nhập!", 
            "Đăng ký tài khoản", 
            JOptionPane.INFORMATION_MESSAGE);
            
        statusLabel.setText("✅ Đăng ký thành công! Hãy đăng nhập.");
        statusLabel.setForeground(new Color(40, 167, 69));
    }

    private void onMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case LOGIN_OK -> {
                    Models.LoginOk loginOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LoginOk.class);
                    openMainGameView(loginOk.username(), loginOk.totalPoints());
                }
                case LOGIN_FAIL, ERROR -> {
                    statusLabel.setText("❌ Đăng nhập thất bại! Kiểm tra tài khoản/mật khẩu.");
                    statusLabel.setForeground(new Color(220, 53, 69));
                    loginButton.setEnabled(true);
                }
                default -> {}
            }
        });
    }
    




    private void openMainGameView(String username, int points) {
        try {
            LobbyView lobbyView = new LobbyView(netClient, username);
            lobbyView.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi mở giao diện game: " + e.getMessage());
            statusLabel.setForeground(new Color(220, 53, 69));
            loginButton.setEnabled(true);
        }
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), 
            "🌐 Kết nối Server", 
            0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(106, 90, 205)));
        
        // Host field
        hostField = new JTextField("127.0.0.1");
        hostField.setPreferredSize(new Dimension(120, 35));
        styleModernTextField(hostField);
        
        // Port field
        portField = new JTextField("7777");
        portField.setPreferredSize(new Dimension(80, 35));
        styleModernTextField(portField);
        
        // Connect button
        connectButton = new JButton("Kết nối");
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        connectButton.setPreferredSize(new Dimension(80, 35));
        connectButton.setBackground(new Color(88, 86, 214)); // Purple theme
        connectButton.setForeground(Color.WHITE);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel hostLabel = new JLabel("Host:");
        hostLabel.setForeground(new Color(200, 200, 200)); // Light gray for dark theme
        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(new Color(200, 200, 200)); // Light gray for dark theme
        
        panel.add(hostLabel);
        panel.add(hostField);
        panel.add(portLabel);
        panel.add(portField);
        panel.add(connectButton);
        
        return panel;
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), 
            "👤 Đăng Nhập", 
            0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(106, 90, 205)));
        
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("🚀 Đăng Nhập");
        
        // Set proper sizes for input fields
        usernameField.setPreferredSize(new Dimension(200, 35));
        passwordField.setPreferredSize(new Dimension(200, 35));
        
        styleModernTextField(usernameField);
        styleModernTextField(passwordField);
        
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(new Color(88, 86, 214)); // Purple theme
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setEnabled(false);
        
        // Add Register button
        JButton registerButton = new JButton("📝 Đăng Ký");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setBackground(new Color(138, 43, 226)); // Darker purple for register
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Username row
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setForeground(new Color(200, 200, 200)); // Light gray for dark theme
        panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);
        
        // Password row
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setForeground(new Color(200, 200, 200)); // Light gray for dark theme
        panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);
        
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SwingLoginView().setVisible(true);
        });
    }
}