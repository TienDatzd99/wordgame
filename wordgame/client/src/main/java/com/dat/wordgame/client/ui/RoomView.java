package com.dat.wordgame.client.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.dat.wordgame.client.NetClient;

public class RoomView extends JFrame {
    private NetClient netClient;
    private String currentUser;
    private String roomId;
    private List<String> players;
    
    // UI Components
    private JLabel roomIdLabel;
    private JLabel hostLabel;
    private JLabel statusLabel;
    private DefaultListModel<String> playersListModel;
    private JList<String> playersList;
    private JButton startGameButton;
    private JButton inviteButton;
    private JButton leaveButton;
    private JTextArea chatArea;
    private JTextField chatField;
    
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
    }

    private void initializeUI() {
        setTitle("Word Game - Phòng chờ");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        
        // Main gradient panel
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(25, 25));
        mainPanel.setBorder(new EmptyBorder(35, 35, 35, 35));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center - players and chat
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom - buttons
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Room title
        JLabel titleLabel = new JLabel("� PHÒNG CHỜ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Room ID
        roomIdLabel = new JLabel("Mã phòng: " + roomId);
        roomIdLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        roomIdLabel.setForeground(new Color(255, 255, 255, 200));
        roomIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Host info
        String hostName = players.isEmpty() ? currentUser : players.get(0);
        hostLabel = new JLabel("👑 Host: " + hostName);
        hostLabel.setFont(new Font("Arial", Font.BOLD, 16));
        hostLabel.setForeground(new Color(255, 215, 0)); // Gold color
        hostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(roomIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(hostLabel);
        
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 25, 0));
        panel.setOpaque(false);
        
        // Left - Players panel
        JPanel playersPanel = createPlayersPanel();
        panel.add(playersPanel);
        
        // Right - Chat panel
        JPanel chatPanel = createChatPanel();
        panel.add(chatPanel);
        
        return panel;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Title
        JLabel titleLabel = new JLabel("👥 Người chơi trong phòng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Status (waiting/ready count)
        statusLabel = new JLabel(players.size() + "/2 người");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setForeground(new Color(255, 255, 255, 180));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(statusLabel, BorderLayout.EAST);
        
        // Players list
        playersListModel = new DefaultListModel<>();
        for (String player : players) {
            playersListModel.addElement(player);
        }
        
        playersList = new JList<>(playersListModel);
        playersList.setFont(new Font("Arial", Font.PLAIN, 16));
        playersList.setBackground(new Color(255, 255, 255, 200));
        playersList.setBorder(new EmptyBorder(15, 15, 15, 15));
        playersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                
                String playerName = (String) value;
                if (playerName.equals(currentUser)) {
                    label.setText("🎮 " + playerName + " (Bạn)");
                    label.setForeground(new Color(88, 86, 214));
                    label.setFont(new Font("Arial", Font.BOLD, 16));
                } else {
                    label.setText("👤 " + playerName);
                }
                
                label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                if (isSelected) {
                    label.setBackground(new Color(88, 86, 214, 100));
                } else {
                    label.setBackground(new Color(255, 255, 255, 150));
                }
                
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(playersList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2));
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Title
        JLabel titleLabel = new JLabel("💬 Chat phòng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(255, 255, 255, 200));
        chatArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setOpaque(false);
        chatScrollPane.getViewport().setOpaque(false);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2));
        
        // Chat input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        
        chatField = new JTextField();
        chatField.setFont(new Font("Arial", Font.PLAIN, 14));
        chatField.setBackground(new Color(255, 255, 255, 200));
        chatField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JButton sendButton = createGlassButton("📤 Gửi", new Color(88, 86, 214));
        sendButton.addActionListener(e -> handleSendChat());
        
        inputPanel.add(chatField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Status message
        JLabel waitingLabel = new JLabel("⏳ Chờ host bắt đầu game...");
        waitingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        waitingLabel.setForeground(Color.WHITE);
        waitingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        
        inviteButton = createGlassButton("� Mời bạn bè", new Color(34, 197, 94));
        inviteButton.addActionListener(e -> handleInvite());
        
        startGameButton = createGlassButton("🚀 Bắt đầu game", new Color(88, 86, 214));
        startGameButton.addActionListener(e -> handleStartGame());
        startGameButton.setEnabled(players.size() >= 2);
        
        // Only host can start game
        String hostName = players.isEmpty() ? currentUser : players.get(0);
        if (!currentUser.equals(hostName)) {
            startGameButton.setVisible(false);
        }
        
        leaveButton = createGlassButton("🚪 Rời phòng", new Color(239, 68, 68));
        leaveButton.addActionListener(e -> handleLeaveRoom());
        
        buttonsPanel.add(inviteButton);
        buttonsPanel.add(startGameButton);
        buttonsPanel.add(leaveButton);
        
        panel.add(waitingLabel, BorderLayout.NORTH);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createGlassButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setPreferredSize(new Dimension(180, 50));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void setupEventHandlers() {
        chatField.addActionListener(e -> handleSendChat());
        
        // Window closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLeaveRoom();
            }
        });
    }

    private void handleInvite() {
        String friendName = JOptionPane.showInputDialog(
            this,
            "Nhập tên người bạn muốn mời:",
            "Mời bạn bè",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (friendName != null && !friendName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Đã gửi lời mời đến " + friendName + "!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );
            chatArea.append("📧 Đã gửi lời mời đến " + friendName + "\n");
        }
    }

    private void handleStartGame() {
        if (players.size() < 2) {
            JOptionPane.showMessageDialog(
                this,
                "Cần ít nhất 2 người chơi để bắt đầu!",
                "Chưa đủ người",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        chatArea.append("🎮 Bắt đầu game...\n");
        
        // Start game
        SwingUtilities.invokeLater(() -> {
            GameView gameView = new GameView(netClient, currentUser, players, roomId);
            gameView.setVisible(true);
            this.dispose();
        });
    }

    private void handleLeaveRoom() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn rời phòng?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );
        
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
            chatArea.append(currentUser + ": " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            chatField.setText("");
        }
    }

    // Public methods for updating UI from network events
    public void addPlayer(String playerName) {
        if (!playersListModel.contains(playerName)) {
            playersListModel.addElement(playerName);
            statusLabel.setText(playersListModel.size() + "/2 người");
            chatArea.append("✅ " + playerName + " đã vào phòng\n");
            
            // Enable start button if enough players
            if (playersListModel.size() >= 2) {
                startGameButton.setEnabled(true);
            }
        }
    }

    public void removePlayer(String playerName) {
        playersListModel.removeElement(playerName);
        statusLabel.setText(playersListModel.size() + "/2 người");
        chatArea.append("❌ " + playerName + " đã rời phòng\n");
        
        // Disable start button if not enough players
        if (playersListModel.size() < 2) {
            startGameButton.setEnabled(false);
        }
    }

    public void addChatMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}