package com.dat.wordgame.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.dat.wordgame.client.NetClient;

public class RoomView extends JFrame {
    private NetClient netClient;
    private String currentUser;
    private String roomId;
    private List<String> players;
    
    // UI Components
    private JLabel roomTitleLabel;
    private JLabel hostLabel;
    private JList<String> playersJList;
    private DefaultListModel<String> playersListModel;
    private JButton inviteButton;
    private JButton startGameButton;
    private JButton leaveRoomButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendChatButton;
    
    // Purple theme gradient background
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Purple gradient like login
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(88, 86, 214),
                0, getHeight(), new Color(133, 89, 215)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public RoomView(NetClient netClient, String username, String roomId, List<String> players) {
        this.netClient = netClient;
        this.currentUser = username;
        this.roomId = roomId;
        this.players = players;
        
        initializeUI();
        setupEventHandlers();
        startListening();
    }

    private void initializeUI() {
        setTitle("🎮 Phòng chờ - " + roomId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Purple gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with room info and chat
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        roomTitleLabel = new JLabel("🏆 Phòng: " + roomId, SwingConstants.CENTER);
        roomTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        roomTitleLabel.setForeground(Color.WHITE);
        
        hostLabel = new JLabel("Host: " + (players.isEmpty() ? currentUser : players.get(0)), SwingConstants.CENTER);
        hostLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        hostLabel.setForeground(new Color(255, 255, 255, 180));
        
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(roomTitleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        textPanel.add(hostLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        
        // Players panel
        JPanel playersPanel = createPlayersPanel();
        panel.add(playersPanel);
        
        // Chat panel
        JPanel chatPanel = createChatPanel();
        panel.add(chatPanel);
        
        return panel;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent white
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("👥 Người chơi trong phòng");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(88, 86, 214));
        
        // Players list
        playersListModel = new DefaultListModel<>();
        for (String player : players) {
            playersListModel.addElement(player);
        }
        playersJList = new JList<>(playersListModel);
        playersJList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        playersJList.setBackground(new Color(250, 250, 250));
        playersJList.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(playersJList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent white
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("💬 Chat phòng");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(88, 86, 214));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(new Color(250, 250, 250));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(300, 200));
        
        // Chat input
        JPanel chatInputPanel = new JPanel(new BorderLayout(10, 0));
        chatInputPanel.setOpaque(false);
        
        chatField = new JTextField();
        chatField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            new EmptyBorder(8, 12, 8, 12)
        ));
        
        sendChatButton = new JButton("Gửi");
        sendChatButton.setBackground(new Color(88, 86, 214));
        sendChatButton.setForeground(Color.WHITE);
        sendChatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendChatButton.setBorderPainted(false);
        sendChatButton.setFocusPainted(false);
        sendChatButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(chatInputPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        // Invite button
        inviteButton = new JButton("📧 Mời bạn bè");
        styleButton(inviteButton, new Color(34, 197, 94)); // Green
        
        // Start game button (only for host)
        startGameButton = new JButton("🚀 Bắt đầu game");
        styleButton(startGameButton, new Color(88, 86, 214)); // Purple
        startGameButton.setEnabled(players.size() >= 2); // Need at least 2 players
        
        // Leave room button
        leaveRoomButton = new JButton("🚪 Rời phòng");
        styleButton(leaveRoomButton, new Color(239, 68, 68)); // Red
        
        panel.add(inviteButton);
        panel.add(startGameButton);
        panel.add(leaveRoomButton);
        
        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(160, 45));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupEventHandlers() {
        inviteButton.addActionListener(e -> handleInvite());
        startGameButton.addActionListener(e -> handleStartGame());
        leaveRoomButton.addActionListener(e -> handleLeaveRoom());
        sendChatButton.addActionListener(e -> handleSendChat());
        
        chatField.addActionListener(e -> handleSendChat());
    }

    private void handleInvite() {
        String friendName = JOptionPane.showInputDialog(this, 
            "Nhập tên người bạn muốn mời:", 
            "Mời bạn bè", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (friendName != null && !friendName.trim().isEmpty()) {
            // TODO: Send invite message to server
            JOptionPane.showMessageDialog(this, 
                "Đã gửi lời mời đến " + friendName + "!", 
                "Mời thành công", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleStartGame() {
        if (players.size() < 2) {
            JOptionPane.showMessageDialog(this, 
                "Cần ít nhất 2 người chơi để bắt đầu!", 
                "Chưa đủ người", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Start the game
        GameView gameView = new GameView(netClient, currentUser, players, roomId);
        gameView.setVisible(true);
        this.dispose();
    }

    private void handleLeaveRoom() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn rời phòng?", 
            "Xác nhận rời phòng", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            // Return to lobby
            LobbyView lobbyView = new LobbyView(netClient, currentUser);
            lobbyView.setVisible(true);
            this.dispose();
        }
    }

    private void handleSendChat() {
        String message = chatField.getText().trim();
        if (!message.isEmpty()) {
            // Add to chat area
            chatArea.append(currentUser + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            
            // TODO: Send chat message to server
            
            chatField.setText("");
        }
    }

    private void startListening() {
        // TODO: Setup message listening for room updates
    }
}