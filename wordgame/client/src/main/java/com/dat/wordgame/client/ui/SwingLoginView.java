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
    private LobbyView lobbyView; // Reference to lobby view
    private Message pendingLobbySnapshot; // Buffer for lobby snapshot received before LobbyView is created
    
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
        setTitle("WordleCup Multiplayer - v2.5.1 (Build 2025-11-01)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Debug: Load modern version
        System.out.println("WordleCup v2.5.1 - Build 2025-11-01 loaded!");
        
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
        card.setPreferredSize(new Dimension(450, 550));
        card.setMaximumSize(new Dimension(450, 550));
        card.setBackground(new Color(255, 255, 255, 230)); // Semi-transparent white
        
        // Title - "WordleCup Multiplayer" với màu sáng cho nền tối
        JLabel titleLabel = new JLabel("WordleCup Multiplayer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 255, 255)); // Trắng cho nền tối
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // "Đăng nhập để chơi" subtitle
        JLabel subtitleLabel = new JLabel("Đăng nhập để chơi", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(200, 200, 200)); // Xám nhạt
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Connection panel
        JPanel connectionPanel = createConnectionPanel();
        
        // Login panel
        JPanel loginPanel = createLoginPanel();
        
        // Connection status
        statusLabel = new JLabel("Chưa kết nối server", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(220, 53, 69)); // Red
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version label
        JLabel versionLabel = new JLabel("v2.5.1 - Build 01/11/2025", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(150, 150, 150)); // Gray
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add all components to card
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(connectionPanel);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(loginPanel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(versionLabel);
        
        return card;
    }
    
    private void styleModernTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(new Color(55, 59, 69)); // Dark background
        field.setForeground(new Color(255, 255, 255)); // White text
        field.setFont(new Font("Arial", Font.PLAIN, 14));
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
            
            statusLabel.setText("Kết nối server thành công!");
            statusLabel.setForeground(new Color(40, 167, 69));
            loginButton.setEnabled(true);
            
        } catch (Exception e) {
            statusLabel.setText("Không thể kết nối: " + e.getMessage());
            statusLabel.setForeground(new Color(220, 53, 69));
            loginButton.setEnabled(false);
        }
    }

    private void handleLogin() {
        if (netClient == null) {
            statusLabel.setText("Chưa kết nối server!");
            statusLabel.setForeground(new Color(220, 53, 69));
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            statusLabel.setForeground(new Color(220, 53, 69));
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Đang đăng nhập...");
        statusLabel.setForeground(new Color(255, 193, 7));

        netClient.send(Message.of(MessageType.LOGIN_REQ, new Models.LoginReq(username, password)));
    }

    private void handleRegister() {
        // Show registration dialog
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);
        
        // After dialog closes, check if registration was successful
        if (registerDialog.isRegistrationSuccessful()) {
            String username = registerDialog.getRegisteredUsername();
            usernameField.setText(username);
            passwordField.setText("");
            statusLabel.setText("Đăng ký thành công! Vui lòng đăng nhập.");
            statusLabel.setForeground(new Color(40, 167, 69));
            usernameField.requestFocus();
        }
    }
    
    // Inner class for Registration Dialog
    class RegisterDialog extends javax.swing.JDialog {
        private JTextField regUsernameField;
        private JPasswordField regPasswordField;
        private JPasswordField regConfirmPasswordField;
        private JTextField regEmailField;
        private boolean registrationSuccessful = false;
        private String registeredUsername = "";
        
        public RegisterDialog(JFrame parent) {
            super(parent, "Đăng ký tài khoản mới", true);
            initRegisterDialog();
        }
        
        private void initRegisterDialog() {
            setSize(450, 500);
            setLocationRelativeTo(getParent());
            setResizable(false);
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
            mainPanel.setBackground(new Color(40, 44, 52));
            
            // Title
            JLabel titleLabel = new JLabel("Tạo tài khoản mới", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Username field
            JLabel userLabel = new JLabel("Tên đăng nhập:");
            userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            userLabel.setForeground(new Color(200, 200, 200));
            userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            regUsernameField = new JTextField();
            regUsernameField.setMaximumSize(new Dimension(400, 40));
            regUsernameField.setFont(new Font("Arial", Font.PLAIN, 14));
            styleRegisterField(regUsernameField);
            
            // Email field
            JLabel emailLabel = new JLabel("Email (tùy chọn):");
            emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            emailLabel.setForeground(new Color(200, 200, 200));
            emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            regEmailField = new JTextField();
            regEmailField.setMaximumSize(new Dimension(400, 40));
            regEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
            styleRegisterField(regEmailField);
            
            // Password field
            JLabel passLabel = new JLabel("Mật khẩu:");
            passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            passLabel.setForeground(new Color(200, 200, 200));
            passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            regPasswordField = new JPasswordField();
            regPasswordField.setMaximumSize(new Dimension(400, 40));
            regPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
            styleRegisterField(regPasswordField);
            
            // Confirm password field
            JLabel confirmLabel = new JLabel("Xác nhận mật khẩu:");
            confirmLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            confirmLabel.setForeground(new Color(200, 200, 200));
            confirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            regConfirmPasswordField = new JPasswordField();
            regConfirmPasswordField.setMaximumSize(new Dimension(400, 40));
            regConfirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
            styleRegisterField(regConfirmPasswordField);
            
            // Buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonsPanel.setOpaque(false);
            buttonsPanel.setMaximumSize(new Dimension(400, 50));
            
            JButton registerBtn = new JButton("ĐĂNG KÝ");
            registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
            registerBtn.setPreferredSize(new Dimension(150, 45));
            registerBtn.setBackground(new Color(88, 86, 214));
            registerBtn.setForeground(Color.WHITE);
            registerBtn.setBorderPainted(false);
            registerBtn.setFocusPainted(false);
            registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            registerBtn.addActionListener(e -> performRegistration());
            
            JButton cancelBtn = new JButton("HỦY");
            cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
            cancelBtn.setPreferredSize(new Dimension(150, 45));
            cancelBtn.setBackground(new Color(108, 117, 125));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBorderPainted(false);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.addActionListener(e -> dispose());
            
            buttonsPanel.add(registerBtn);
            buttonsPanel.add(cancelBtn);
            
            // Add all components
            mainPanel.add(titleLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            mainPanel.add(userLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            mainPanel.add(regUsernameField);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            mainPanel.add(emailLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            mainPanel.add(regEmailField);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            mainPanel.add(passLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            mainPanel.add(regPasswordField);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            mainPanel.add(confirmLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            mainPanel.add(regConfirmPasswordField);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            mainPanel.add(buttonsPanel);
            
            add(mainPanel);
        }
        
        private void styleRegisterField(JTextField field) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            field.setBackground(new Color(55, 59, 69));
            field.setForeground(Color.WHITE);
            field.setCaretColor(Color.WHITE);
        }
        
        private void performRegistration() {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(regConfirmPasswordField.getPassword());
            
            // Validation
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng nhập tên đăng nhập!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, 
                    "Tên đăng nhập phải có ít nhất 3 ký tự!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng nhập mật khẩu!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 3) {
                JOptionPane.showMessageDialog(this, 
                    "Mật khẩu phải có ít nhất 3 ký tự!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, 
                    "Mật khẩu xác nhận không khớp!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Send registration request to server
            if (netClient == null) {
                JOptionPane.showMessageDialog(this, 
                    "Chưa kết nối server!", 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Save username for later
            registeredUsername = username;
            
            // Send REGISTER_REQ to server
            netClient.send(Message.of(MessageType.REGISTER_REQ, new Models.RegisterReq(username, password)));
            
            // Wait for response (handled in onMessage)
            // We'll set registrationSuccessful in onMessage when REGISTER_OK is received
            dispose();
        }
        
        public boolean isRegistrationSuccessful() {
            return registrationSuccessful;
        }
        
        public String getRegisteredUsername() {
            return registeredUsername;
        }
    }

    private void onMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case LOGIN_OK -> {
                    Models.LoginOk loginOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LoginOk.class);
                    openMainGameView(loginOk.username(), loginOk.totalPoints());
                }
                case LOGIN_FAIL, ERROR -> {
                    Models.Err err = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Err.class);
                    if ("REGISTER".equals(err.code())) {
                        // Registration error
                        JOptionPane.showMessageDialog(this, 
                            err.message(), 
                            "Lỗi đăng ký", 
                            JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Login error
                        statusLabel.setText("Đăng nhập thất bại! " + err.message());
                        statusLabel.setForeground(new Color(220, 53, 69));
                        loginButton.setEnabled(true);
                    }
                }
                case REGISTER_OK -> {
                    Models.RegisterOk regOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RegisterOk.class);
                    JOptionPane.showMessageDialog(this, 
                        "Đăng ký thành công!\n\n" +
                        "Tên đăng nhập: " + regOk.username() + "\n" +
                        "\nBạn có thể đăng nhập ngay bây giờ!", 
                        "Thành công", 
                        JOptionPane.INFORMATION_MESSAGE);
                    usernameField.setText(regOk.username());
                    passwordField.setText("");
                    statusLabel.setText("Đăng ký thành công! Vui lòng đăng nhập.");
                    statusLabel.setForeground(new Color(40, 167, 69));
                }
                case LOBBY_SNAPSHOT -> {
                    // Forward to LobbyView if it exists
                    System.out.println("SwingLoginView: Received LOBBY_SNAPSHOT");
                    if (lobbyView != null) {
                        System.out.println("SwingLoginView: Forwarding to LobbyView");
                        lobbyView.handleMessage(message);
                    } else {
                        System.out.println("SwingLoginView: LobbyView not ready yet, buffering message");
                        pendingLobbySnapshot = message; // Save for later
                    }
                }
                case INVITE_RECEIVE, ROOM_CREATED, ROOM_JOINED, ROOM_LEFT, ROUND_START, ROUND_TICK, ROUND_END, GAME_END, GUESS_UPDATE, CHAT,
                     FRIEND_REQUEST_RECEIVE, FRIEND_LIST_RESP, FRIEND_INVITE_RESP, ROOM_INVITE_RECEIVE, ROOM_INVITE_RESP -> {
                    // Forward all game-related and friend system messages to LobbyView
                    System.out.println("SwingLoginView: Received " + message.type);
                    if (lobbyView != null) {
                        System.out.println("SwingLoginView: Forwarding " + message.type + " to LobbyView");
                        lobbyView.handleMessage(message);
                    } else {
                        System.out.println("SwingLoginView: WARNING - LobbyView is null, cannot forward " + message.type);
                    }
                }
                default -> {}
            }
        });
    }
    




    private void openMainGameView(String username, int points) {
        try {
            lobbyView = new LobbyView(netClient, username);
            
            // If we have a pending lobby snapshot, apply it now
            if (pendingLobbySnapshot != null) {
                System.out.println("SwingLoginView: Applying pending LOBBY_SNAPSHOT to new LobbyView");
                lobbyView.handleMessage(pendingLobbySnapshot);
                pendingLobbySnapshot = null;
            }
            
            lobbyView.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            statusLabel.setText("Lỗi mở giao diện game: " + e.getMessage());
            statusLabel.setForeground(new Color(220, 53, 69));
            loginButton.setEnabled(true);
        }
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), 
            "Kết nối Server", 
            0, 0, new Font("Arial", Font.BOLD, 14), new Color(106, 90, 205)));
        panel.setMaximumSize(new Dimension(400, 120));
        
        // Row 1: Host + Port
        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(400, 45));
        
        JLabel hostLabel = new JLabel("Host:");
        hostLabel.setForeground(new Color(200, 200, 200));
        hostLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        hostField = new JTextField("127.0.0.1");
        hostField.setPreferredSize(new Dimension(150, 35));
        hostField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        hostField.setBackground(new Color(55, 59, 69));
        hostField.setForeground(new Color(255, 255, 255));
        hostField.setFont(new Font("Arial", Font.PLAIN, 14));
        hostField.setCaretColor(new Color(255, 255, 255));
        
        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(new Color(200, 200, 200));
        portLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        portField = new JTextField("7777");
        portField.setPreferredSize(new Dimension(80, 35));
        portField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        portField.setBackground(new Color(55, 59, 69));
        portField.setForeground(new Color(255, 255, 255));
        portField.setFont(new Font("Arial", Font.PLAIN, 14));
        portField.setCaretColor(new Color(255, 255, 255));
        
        inputRow.add(hostLabel);
        inputRow.add(hostField);
        inputRow.add(portLabel);
        inputRow.add(portField);
        
        // Row 2: Connect button
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(400, 45));
        
        connectButton = new JButton("Kết nối");
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.setPreferredSize(new Dimension(120, 38));
        connectButton.setBackground(new Color(88, 86, 214));
        connectButton.setForeground(Color.WHITE);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        buttonRow.add(connectButton);
        
        panel.add(inputRow);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(buttonRow);
        
        return panel;
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), 
            "Đăng Nhập", 
            0, 0, new Font("Arial", Font.BOLD, 14), new Color(106, 90, 205)));
        panel.setMaximumSize(new Dimension(400, 180));
        
        // Username row
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        userRow.setOpaque(false);
        userRow.setMaximumSize(new Dimension(400, 45));
        
        JLabel userLabel = new JLabel("Tên đăng nhập:");
        userLabel.setForeground(new Color(200, 200, 200));
        userLabel.setPreferredSize(new Dimension(110, 35));
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(220, 35));
        styleModernTextField(usernameField);
        
        userRow.add(userLabel);
        userRow.add(usernameField);
        
        // Password row
        JPanel passRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(400, 45));
        
        JLabel passLabel = new JLabel("Mật khẩu:");
        passLabel.setForeground(new Color(200, 200, 200));
        passLabel.setPreferredSize(new Dimension(110, 35));
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(220, 35));
        styleModernTextField(passwordField);
        
        passRow.add(passLabel);
        passRow.add(passwordField);
        
        // Buttons row
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsRow.setOpaque(false);
        buttonsRow.setMaximumSize(new Dimension(400, 60));
        
        loginButton = new JButton("ĐĂNG NHẬP");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.setBackground(new Color(88, 86, 214));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setEnabled(false);
        
        JButton registerButton = new JButton("ĐĂNG KÝ");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(150, 40));
        registerButton.setBackground(new Color(138, 43, 226));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());
        
        buttonsRow.add(loginButton);
        buttonsRow.add(registerButton);
        
        panel.add(userRow);
        panel.add(passRow);
        panel.add(buttonsRow);
        
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SwingLoginView().setVisible(true);
        });
    }
}