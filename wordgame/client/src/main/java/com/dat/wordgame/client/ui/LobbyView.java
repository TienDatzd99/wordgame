package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class LobbyView extends JFrame {
    private NetClient netClient;
    private JLabel welcomeLabel;
    private JTable playersTable;
    private JTable rankingTable;
    private DefaultTableModel playersTableModel;
    private DefaultTableModel rankingTableModel;
    private JButton startGameButton;
    private JButton friendsButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private String currentUser;
    private GameView currentGameView; // Store reference to forward messages
    private RoomView currentRoomView; // Store reference to forward friend messages  
    private List<Message> pendingGameMessages = new ArrayList<>(); // Buffer messages until GameView ready
    private boolean waitingForFriendList = false; // Flag to track when user wants friend dialog

    // Dark theme gradient background panel class
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Create dark gradient from purple to darker purple
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(88, 86, 214),
                0, getHeight(), new Color(55, 48, 163)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public LobbyView(NetClient netClient, String username) {
        this.netClient = netClient;
        this.currentUser = username;
        
        System.out.println("[LobbyView] Constructor called for user: " + username);
        
        initializeUI();
        setupEventHandlers();
        
        // Don't listen here - listener already set in SwingLoginView
        // Just request fresh data
        requestPlayersList();
        requestRankingData();
    }

    private void initializeUI() {
        System.out.println("[LobbyView] initializeUI() called");
        setTitle("WordGame - Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with tables
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
        
        welcomeLabel = new JLabel("Chào mừng, " + currentUser + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Chọn chế độ chơi hoặc xem bảng xếp hạng");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        
        // Players panel
        JPanel playersPanel = createPlayersPanel();
        panel.add(playersPanel);
        
        // Ranking panel
        JPanel rankingPanel = createRankingPanel();
        panel.add(rankingPanel);
        
        return panel;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Glass panel with glassmorphism effect
        JPanel glassPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background - semi-transparent white with blur effect
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Border with gradient
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setLayout(new BorderLayout(0, 15));
        glassPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("🎮 Người chơi đang online");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        glassPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create players table
        String[] playersColumns = {"Tên người chơi", "Trạng thái", "Điểm số"};
        playersTableModel = new DefaultTableModel(playersColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        playersTable = new JTable(playersTableModel);
        styleGlassTable(playersTable);
        
        // Add right-click menu for challenging players
        playersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showChallengeMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showChallengeMenu(e);
                }
            }
            
            private void showChallengeMenu(MouseEvent e) {
                int row = playersTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    playersTable.setRowSelectionInterval(row, row);
                    String playerName = (String) playersTable.getValueAt(row, 0);
                    
                    // Don't show menu if clicking on yourself
                    if (!playerName.equals(currentUser)) {
                        JPopupMenu popupMenu = createChallengeMenu(playerName);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        
        JScrollPane playersScrollPane = new JScrollPane(playersTable);
        playersScrollPane.setOpaque(false);
        playersScrollPane.getViewport().setOpaque(false);
        playersScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        glassPanel.add(playersScrollPane, BorderLayout.CENTER);
        panel.add(glassPanel);
        
        return panel;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Glass panel with glassmorphism effect
        JPanel glassPanel = new JPanel(new BorderLayout()) {
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
        glassPanel.setOpaque(false);
        glassPanel.setLayout(new BorderLayout(0, 15));
        glassPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel titleLabel = new JLabel("🏆 Bảng xếp hạng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        glassPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create ranking table
        String[] rankingColumns = {"Hạng", "Tên người chơi", "Tổng điểm", "Số trận thắng"};
        rankingTableModel = new DefaultTableModel(rankingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        rankingTable = new JTable(rankingTableModel);
        styleGlassTable(rankingTable);
        
        JScrollPane rankingScrollPane = new JScrollPane(rankingTable);
        rankingScrollPane.setOpaque(false);
        rankingScrollPane.getViewport().setOpaque(false);
        rankingScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        glassPanel.add(rankingScrollPane, BorderLayout.CENTER);
        panel.add(glassPanel);
        
        return panel;
    }

    private void styleTable(JTable table) {
        styleGlassTable(table);
    }
    
    private void styleGlassTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Transparent background for table
        table.setOpaque(false);
        table.setBackground(new Color(0, 0, 0, 0));
        
        // Selection colors with glass effect
        table.setSelectionBackground(new Color(255, 255, 255, 60));
        table.setSelectionForeground(Color.WHITE);
        
        // Custom cell renderer for glass effect
        DefaultTableCellRenderer glassCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (isSelected) {
                    setBackground(new Color(255, 255, 255, 80));
                    setForeground(Color.WHITE);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(new Color(255, 255, 255, 20));
                    setForeground(new Color(255, 255, 255, 230));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                
                setOpaque(true);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 30)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                
                return c;
            }
        };
        
        // Apply renderer to all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(glassCellRenderer);
        }
        
        // Style header with glass effect
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setOpaque(false);
        header.setBackground(new Color(88, 86, 214, 200)); // Purple background instead of white
        header.setForeground(Color.WHITE); // White text on purple background
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 255, 255, 100)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        // Custom header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(88, 86, 214, 200)); // Purple background
                setForeground(Color.WHITE); // White text
                setFont(new Font("Arial", Font.BOLD, 16)); // Slightly bigger font
                setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                setHorizontalAlignment(SwingConstants.CENTER);
                setOpaque(true);
                return this;
            }
        };
        
        header.setDefaultRenderer(headerRenderer);
    }
    
    private JPopupMenu createChallengeMenu(String playerName) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(new Color(40, 40, 50, 240));
        popupMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JMenuItem challengeItem = new JMenuItem("⚔️ Gửi thách đấu đến " + playerName);
        challengeItem.setFont(new Font("Arial", Font.BOLD, 14));
        challengeItem.setForeground(Color.WHITE);
        challengeItem.setBackground(new Color(88, 86, 214));
        challengeItem.setOpaque(true);
        challengeItem.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        challengeItem.addActionListener(e -> {
            sendChallenge(playerName);
        });
        
        // Hover effect
        challengeItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                challengeItem.setBackground(new Color(108, 106, 234));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                challengeItem.setBackground(new Color(88, 86, 214));
            }
        });
        
        popupMenu.add(challengeItem);
        return popupMenu;
    }
    
    private void sendChallenge(String playerName) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Bạn có muốn gửi lời thách đấu đến " + playerName + "?",
            "Xác nhận thách đấu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Send INVITE_SEND message to server
                Models.InviteSend inviteMsg = new Models.InviteSend(currentUser, playerName);
                Message msg = Message.of(MessageType.INVITE_SEND, inviteMsg);
                netClient.send(msg);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi lời thách đấu đến " + playerName + "!\n" +
                    "Chờ đối thủ chấp nhận...",
                    "Thách đấu",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi gửi thách đấu: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        startGameButton = createModernButton("🏠 Tạo phòng", new Color(46, 204, 113));
        friendsButton = createModernButton("👥 Bạn bè", new Color(155, 89, 182));
        refreshButton = createModernButton("Làm mới", new Color(52, 152, 219));
        logoutButton = createModernButton("Đăng xuất", new Color(231, 76, 60));
        
        System.out.println("[LobbyView] Creating bottom panel with friends button: " + friendsButton.getText());
        System.out.println("[LobbyView] Friends button visible: " + friendsButton.isVisible());
        System.out.println("[LobbyView] Friends button enabled: " + friendsButton.isEnabled());
        
        panel.add(startGameButton);
        panel.add(friendsButton);
        panel.add(refreshButton);
        panel.add(logoutButton);
        
        System.out.println("[LobbyView] Bottom panel has " + panel.getComponentCount() + " components");
        
        return panel;
    }

    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private void setupEventHandlers() {
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createRoom();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        friendsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("[LobbyView] Friends button clicked!");
                showFriendsDialog();
            }
        });
        

    }

    public void requestPlayersList() { // Changed to public
        // Send request for players list
        Message request = Message.of(MessageType.PLAYER_LIST, currentUser);
        
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy danh sách người chơi: " + e.getMessage());
        }
    }

    public void requestRankingData() { // Changed to public
        // Send request for ranking data
        Message request = Message.of(MessageType.LEADERBOARD, currentUser);
        
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy bảng xếp hạng: " + e.getMessage());
        }
    }
    
    // Get list of online players (excluding current user)
    public List<String> getOnlinePlayers() {
        List<String> players = new ArrayList<>();
        for (int i = 0; i < playersTableModel.getRowCount(); i++) {
            String playerName = (String) playersTableModel.getValueAt(i, 0);
            if (!playerName.equals(currentUser)) {
                players.add(playerName);
            }
        }
        return players;
    }

    public void updatePlayersList(List<Map<String, Object>> players) {
        SwingUtilities.invokeLater(() -> {
            playersTableModel.setRowCount(0);
            for (Map<String, Object> player : players) {
                String name = (String) player.get("name");
                String status = (String) player.get("status");
                Object score = player.get("score");
                
                playersTableModel.addRow(new Object[]{name, status, score});
            }
        });
    }

    public void updateRanking(List<Map<String, Object>> ranking) {
        SwingUtilities.invokeLater(() -> {
            rankingTableModel.setRowCount(0);
            int rank = 1;
            for (Map<String, Object> player : ranking) {
                String name = (String) player.get("name");
                Object totalScore = player.get("totalScore");
                Object wins = player.get("wins");
                
                rankingTableModel.addRow(new Object[]{rank++, name, totalScore, wins});
            }
        });
    }

    private void createRoom() {
        // Create a new room and go to RoomView
        String roomId = "room_" + System.currentTimeMillis(); // Simple room ID generation
        List<String> players = new java.util.ArrayList<>();
        players.add(currentUser); // Host is first player
        
        // Open RoomView and hide (not dispose) this lobby view, pass 'this' to preserve data
        currentRoomView = new RoomView(netClient, currentUser, roomId, players, this);
        currentRoomView.setVisible(true);
        this.setVisible(false); // Hide instead of dispose to preserve data
    }

    public void onGameStarted(List<String> players, String gameId) {
        SwingUtilities.invokeLater(() -> {
            // Open game screen
            GameView gameView = new GameView(netClient, currentUser, players, gameId);
            gameView.setVisible(true);
            this.dispose();
        });
    }

    private void refreshData() {
        requestPlayersList();
        requestRankingData();
    }
    
    public void refresh() {
        refreshData();
    }

    private void logout() {
        try {
            netClient.close();
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
        
        // Return to login screen
        SwingLoginView loginView = new SwingLoginView();
        loginView.setVisible(true);
        this.dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void handleMessage(Message message) {
        System.out.println("LobbyView: handleMessage called with type: " + message.type);
        
        // Check if this is a friend system message
        if (message.type == MessageType.FRIEND_LIST_RESP || message.type == MessageType.FRIEND_INVITE_RESP || 
            message.type == MessageType.ROOM_INVITE_RESP || message.type == MessageType.USER_SEARCH_RESP) {
            System.out.println("LobbyView: Received friend system message: " + message.type);
            System.out.println("LobbyView: currentRoomView != null: " + (currentRoomView != null));
            if (currentRoomView != null) {
                System.out.println("LobbyView: currentRoomView.isVisible(): " + currentRoomView.isVisible());
            }
        }
        
        // Forward game-related messages to GameView if it exists
        if (currentGameView != null) {
            switch (message.type) {
                case ROUND_START, ROUND_TICK, ROUND_END, GAME_END, GUESS_UPDATE, CHAT -> {
                    System.out.println("LobbyView: Forwarding " + message.type + " to GameView");
                    currentGameView.onMessage(message);
                    return; // Don't process in LobbyView
                }
                default -> {} // Continue processing in LobbyView
            }
        } else if (currentRoomView != null && currentRoomView.isVisible()) {
            // Forward chat to RoomView if user is in room (before game starts)
            switch (message.type) {
                case CHAT -> {
                    System.out.println("LobbyView: Forwarding " + message.type + " to RoomView");
                    currentRoomView.handleMessage(message);
                    return; // Don't process in LobbyView
                }
                default -> {} // Continue processing in LobbyView
            }
        } else {
            // Buffer game messages if GameView not ready yet
            switch (message.type) {
                case ROUND_START, ROUND_TICK, ROUND_END, GAME_END, GUESS_UPDATE, CHAT -> {
                    System.out.println("LobbyView: Buffering " + message.type + " until GameView ready");
                    pendingGameMessages.add(message);
                    return;
                }
                default -> {}
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case LOBBY_SNAPSHOT -> {
                    System.out.println("LobbyView: Processing LOBBY_SNAPSHOT");
                    // Parse lobby snapshot
                    Map<String, Object> snapshot = (Map<String, Object>) message.payload;
                    List<Map<String, Object>> onlinePlayers = (List<Map<String, Object>>) snapshot.get("online");
                    List<Map<String, Object>> leaderboard = (List<Map<String, Object>>) snapshot.get("leaderboard");
                    
                    System.out.println("LobbyView: Online players count: " + (onlinePlayers != null ? onlinePlayers.size() : "null"));
                    System.out.println("LobbyView: Leaderboard count: " + (leaderboard != null ? leaderboard.size() : "null"));
                    
                    // Update players table
                    if (onlinePlayers != null) {
                        playersTableModel.setRowCount(0);
                        for (Map<String, Object> player : onlinePlayers) {
                            String name = (String) player.get("name");
                            Object pointsObj = player.get("points");
                            int points = pointsObj instanceof Double ? ((Double) pointsObj).intValue() : (Integer) pointsObj;
                            String status = (String) player.get("status");
                            
                            System.out.println("LobbyView: Adding player: " + name + ", status: " + status + ", points: " + points);
                            playersTableModel.addRow(new Object[]{name, status, points});
                        }
                    }
                    
                    // Update ranking table
                    if (leaderboard != null) {
                        rankingTableModel.setRowCount(0);
                        int rank = 1;
                        for (Map<String, Object> player : leaderboard) {
                            String name = (String) player.get("name");
                            Object pointsObj = player.get("points");
                            int points = pointsObj instanceof Double ? ((Double) pointsObj).intValue() : (Integer) pointsObj;
                            
                            rankingTableModel.addRow(new Object[]{rank++, name, points, "-"});
                        }
                    }
                }
                case PLAYER_LIST -> {
                    List<Map<String, Object>> players = (List<Map<String, Object>>) message.payload;
                    updatePlayersList(players);
                }
                case LEADERBOARD -> {
                    List<Map<String, Object>> ranking = (List<Map<String, Object>>) message.payload;
                    updateRanking(ranking);
                }
                case INVITE_RECEIVE -> {
                    Models.InviteReceive invite = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.InviteReceive.class);
                    handleInviteReceived(invite.from());
                }
                case ROOM_JOINED -> {
                    // Both players receive this when room is created
                    System.out.println("LobbyView: ROOM_JOINED received");
                    Models.RoomState roomState = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomState.class);
                    openGameView(roomState);
                }
                case ROOM_CREATED -> {
                    // When room is created from friend invite, open RoomView
                    System.out.println("LobbyView: ROOM_CREATED received");
                    System.out.println("LobbyView: Raw payload: " + Json.GSON.toJson(message.payload));
                    Models.RoomState roomState = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomState.class);
                    System.out.println("LobbyView: Parsed RoomState - roomId=" + roomState.roomId() + 
                                      ", host=" + roomState.host() + 
                                      ", opponent=" + roomState.opponent());
                    handleRoomCreated(roomState);
                }
                case ROOM_INVITE_RECEIVE -> {
                    System.out.println("[LobbyView] Received ROOM_INVITE_RECEIVE message!");
                    Models.RoomInviteReceive invite = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomInviteReceive.class);
                    System.out.println("[LobbyView] Room invite from: " + invite.from() + ", roomId: " + invite.roomId());
                    handleRoomInviteReceived(invite.from(), invite.roomId());
                }
                case FRIEND_REQUEST_RECEIVE -> {
                    Models.FriendRequestReceive request = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendRequestReceive.class);
                    handleFriendRequestReceived(request.from());
                }
                case FRIEND_LIST_RESP -> {
                    System.out.println("[LobbyView] FRIEND_LIST_RESP received. waitingForFriendList: " + waitingForFriendList + 
                                     ", RoomView null? " + (currentRoomView == null) + 
                                     ", visible? " + (currentRoomView != null ? currentRoomView.isVisible() : "n/a"));
                    
                    if (waitingForFriendList) {
                        // User clicked friends button in LobbyView - show dialog
                        waitingForFriendList = false;
                        System.out.println("[LobbyView] Showing friends dialog from LobbyView button");
                        Models.FriendListResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendListResp.class);
                        showFriendsDialogWithData(response.friends());
                    } else if (currentRoomView != null && currentRoomView.isVisible()) {
                        // Forward to RoomView if it's active
                        System.out.println("[LobbyView] Forwarding FRIEND_LIST_RESP to RoomView");
                        currentRoomView.handleMessage(message);
                    } else {
                        // Handle in LobbyView (legacy)
                        System.out.println("[LobbyView] Handling FRIEND_LIST_RESP in LobbyView (legacy)");
                        Models.FriendListResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendListResp.class);
                        handleFriendListResponse(response.friends());
                    }
                }
                case FRIEND_INVITE_RESP -> {
                    if (currentRoomView != null && currentRoomView.isVisible()) {
                        // Forward to RoomView if it's active
                        currentRoomView.handleMessage(message);
                    } else {
                        // Handle in LobbyView
                        Models.FriendInviteResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendInviteResp.class);
                        if (response.success()) {
                            JOptionPane.showMessageDialog(this, response.message(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, response.message(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                case ROOM_INVITE_RESP -> {
                    if (currentRoomView != null && currentRoomView.isVisible()) {
                        // Forward to RoomView if it's active
                        currentRoomView.handleMessage(message);
                    } else {
                        // Handle in LobbyView
                        Models.RoomInviteResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomInviteResp.class);
                        if (response.success()) {
                            JOptionPane.showMessageDialog(this, response.message(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, response.message(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                case USER_SEARCH_RESP -> {
                    Models.UserSearchResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.UserSearchResp.class);
                    handleUserSearchResponse(response.results());
                }
                case ROOM_LEFT -> {
                    Models.RoomLeft roomLeft = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomLeft.class);
                    System.out.println("LobbyView: Player " + roomLeft.playerLeft() + " left room " + roomLeft.roomId());
                    // Forward to RoomView if it exists
                    if (currentRoomView != null && currentRoomView.isVisible()) {
                        currentRoomView.handlePlayerLeft(roomLeft.playerLeft());
                    }
                }
                default -> {}
            }
        });
    }
    
    private void openGameView(Models.RoomState roomState) {
        System.out.println("LobbyView: Opening GameView for room " + roomState.roomId());
        
        // Create list of players
        List<String> players = new ArrayList<>();
        players.add(roomState.host());
        if (roomState.opponent() != null && !roomState.opponent().isEmpty()) {
            players.add(roomState.opponent());
        }
        
        // Open GameView and store reference
        currentGameView = new GameView(netClient, currentUser, players, roomState.roomId());
        currentGameView.setParentLobby(this); // Set reference to return here
        currentGameView.setVisible(true);
        this.setVisible(false); // Hide lobby, don't dispose
        
        // Apply any pending game messages
        if (!pendingGameMessages.isEmpty()) {
            System.out.println("LobbyView: Applying " + pendingGameMessages.size() + " pending messages to GameView");
            for (Message msg : pendingGameMessages) {
                System.out.println("LobbyView: Forwarding pending " + msg.type);
                currentGameView.onMessage(msg);
            }
            pendingGameMessages.clear();
        }
    }
    
    private void handleInviteReceived(String fromPlayer) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            fromPlayer + " đã gửi lời thách đấu đến bạn!\n\nBạn có chấp nhận không?",
            "Lời thách đấu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        try {
            if (choice == JOptionPane.YES_OPTION) {
                // Accept the invite
                Models.InviteAccept acceptMsg = new Models.InviteAccept(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.INVITE_ACCEPT, acceptMsg);
                netClient.send(msg);
            } else {
                // Reject the invite
                Models.InviteReject rejectMsg = new Models.InviteReject(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.INVITE_REJECT, rejectMsg);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi thách đấu: " + ex.getMessage());
        }
    }
    
    private void handleRoomCreated(Models.RoomState roomState) {
        System.out.println("LobbyView: handleRoomCreated for room " + roomState.roomId());
        System.out.println("LobbyView: host=" + roomState.host() + ", opponent=" + roomState.opponent());
        
        // Create list of players
        List<String> players = new ArrayList<>();
        players.add(roomState.host());
        if (roomState.opponent() != null && !roomState.opponent().isEmpty()) {
            players.add(roomState.opponent());
        }
        System.out.println("LobbyView: players list size: " + players.size() + ", players: " + players);
        
        // Open RoomView (not GameView) so players can chat before starting game
        currentRoomView = new RoomView(netClient, currentUser, roomState.roomId(), players, this);
        currentRoomView.setVisible(true);
        this.setVisible(false); // Hide lobby
    }
    
    private void handleRoomInviteReceived(String fromPlayer, String roomId) {
        System.out.println("[LobbyView] handleRoomInviteReceived called - from: " + fromPlayer + ", roomId: " + roomId);
        int choice = JOptionPane.showConfirmDialog(
            this,
            fromPlayer + " đã mời bạn vào phòng chơi!\n\nBạn có chấp nhận không?",
            "Lời mời vào phòng",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        System.out.println("[LobbyView] User choice: " + choice);
        
        try {
            if (choice == JOptionPane.YES_OPTION) {
                // Accept the room invite
                Models.RoomInviteAccept acceptMsg = new Models.RoomInviteAccept(fromPlayer, currentUser, roomId);
                System.out.println("[LobbyView] Sending ROOM_INVITE_ACCEPT: from=" + fromPlayer + ", to=" + currentUser + ", roomId=" + roomId);
                Message msg = Message.of(MessageType.ROOM_INVITE_ACCEPT, acceptMsg);
                netClient.send(msg);
            } else {
                // Reject the room invite
                Models.RoomInviteReject rejectMsg = new Models.RoomInviteReject(fromPlayer, currentUser, roomId);
                Message msg = Message.of(MessageType.ROOM_INVITE_REJECT, rejectMsg);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi lời mời vào phòng: " + ex.getMessage());
        }
    }
    
    private void showFriendsDialog() {
        // Set flag that we're waiting for friend list
        waitingForFriendList = true;
        
        // Request friend list from server
        Message request = Message.of(MessageType.FRIEND_LIST_REQ, new Models.FriendListReq(currentUser));
        try {
            netClient.send(request);
            System.out.println("[LobbyView] Friend list request sent from showFriendsDialog()");
        } catch (Exception e) {
            waitingForFriendList = false;
            showError("Lỗi khi lấy danh sách bạn bè: " + e.getMessage());
        }
    }
    

    
    private void showFriendsDialogWithData(java.util.List<Models.FriendInfo> friends) {
        JDialog dialog = new JDialog(this, "Danh sách bạn bè", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        // Create main panel with glassmorphism
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(88, 86, 214),
                    0, getHeight(), new Color(55, 48, 163)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Header
        JLabel headerLabel = new JLabel("Danh sách bạn bè (" + friends.size() + ")", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Friends table
        String[] columns = {"Tên", "Trạng thái", "Điểm"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        // Sort friends: online first, then by points
        friends.sort((a, b) -> {
            if (a.isOnline() && !b.isOnline()) return -1;
            if (!a.isOnline() && b.isOnline()) return 1;
            return Integer.compare(b.totalPoints(), a.totalPoints());
        });
        
        for (Models.FriendInfo friend : friends) {
            String status = friend.isOnline() ? "🟢 Online" : "🔴 Offline";
            model.addRow(new Object[]{friend.username(), status, friend.totalPoints()});
        }
        
        JTable table = new JTable(model);
        styleGlassTable(table);
        table.setRowHeight(35);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        
        JLabel searchLabel = new JLabel("🔍 Tìm kiếm:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchLabel.setForeground(Color.WHITE);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JButton searchBtn = createModernButton("Tìm", new Color(52, 152, 219));
        
        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                showUserSearchDialog(searchText);
            }
        });
        
        // Allow search on Enter
        searchField.addActionListener(e -> searchBtn.doClick());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton addFriendBtn = createModernButton("➕ Thêm bạn", new Color(46, 204, 113));
        JButton closeBtn = createModernButton("Đóng", new Color(231, 76, 60));
        
        addFriendBtn.addActionListener(e -> {
            String friendName = JOptionPane.showInputDialog(dialog, "Nhập tên người chơi để kết bạn:");
            if (friendName != null && !friendName.trim().isEmpty()) {
                sendFriendRequest(friendName.trim());
            }
        });
        
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(addFriendBtn);
        buttonPanel.add(closeBtn);
        
        // Add search panel between header and friends list
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showUserSearchDialog(String searchText) {
        // Send search request to server instead of showing dialog immediately
        try {
            Models.UserSearchReq request = new Models.UserSearchReq(searchText, currentUser);
            Message msg = Message.of(MessageType.USER_SEARCH_REQ, request);
            netClient.send(msg);
        } catch (Exception e) {
            showError("Lỗi khi tìm kiếm người chơi: " + e.getMessage());
        }
    }
    
    private void handleUserSearchResponse(java.util.List<Models.UserSearchResult> results) {
        String searchText = ""; // Store search text for UI display if needed
        
        JDialog searchDialog = new JDialog(this, "Kết quả tìm kiếm", true);
        searchDialog.setSize(500, 400);
        searchDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Glass background
        mainPanel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Border
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel headerLabel = new JLabel("🔍 Tìm thấy " + results.size() + " kết quả");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Search results table
        String[] columnNames = {"Tên người chơi", "Điểm số", "Trạng thái", "Thao tác"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All columns read-only
            }
        };
        
        // Add real search results
        for (Models.UserSearchResult result : results) {
            String status = result.isFriend() ? "🟢 Đã kết bạn" : "⚪ Chưa kết bạn";
            String action = result.isFriend() ? "Mời chơi" : "Kết bạn";
            model.addRow(new Object[]{result.username(), result.points(), status, action});
        }
        
        JTable table = new JTable(model);
        styleGlassTable(table);
        table.setRowHeight(40);
        
        // Handle row clicks for actions
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 3) { // Action column
                    Models.UserSearchResult result = results.get(row);
                    if (result.isFriend()) {
                        // Send game invite to friend
                        int confirm = JOptionPane.showConfirmDialog(searchDialog, 
                            "Gửi lời mời chơi đến " + result.username() + "?", 
                            "Xác nhận", 
                            JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            sendGameInviteToFriend(result.username());
                            searchDialog.dispose();
                        }
                    } else {
                        // Send friend request
                        int confirm = JOptionPane.showConfirmDialog(searchDialog, 
                            "Gửi lời mời kết bạn đến " + result.username() + "?", 
                            "Xác nhận", 
                            JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            sendFriendRequest(result.username());
                            searchDialog.dispose();
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton closeBtn = createModernButton("Đóng", new Color(231, 76, 60));
        closeBtn.addActionListener(e -> searchDialog.dispose());
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        searchDialog.add(mainPanel);
        searchDialog.getContentPane().setBackground(new Color(88, 86, 214));
        searchDialog.setVisible(true);
    }
    
    private void sendGameInviteToFriend(String friendName) {
        try {
            String roomId = "room_" + System.currentTimeMillis();
            Models.RoomInviteSend invite = new Models.RoomInviteSend(currentUser, friendName, roomId);
            Message msg = Message.of(MessageType.ROOM_INVITE_SEND, invite);
            netClient.send(msg);
            JOptionPane.showMessageDialog(this, 
                "Đã gửi lời mời chơi đến " + friendName + "!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("Lỗi khi gửi lời mời chơi: " + e.getMessage());
        }
    }

    private void sendFriendRequest(String friendName) {
        try {
            Models.FriendRequest request = new Models.FriendRequest(currentUser, friendName);
            Message msg = Message.of(MessageType.FRIEND_REQUEST_SEND, request);
            netClient.send(msg);
            JOptionPane.showMessageDialog(this, 
                "Đã gửi lời mời kết bạn đến " + friendName + "!", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            showError("Lỗi khi gửi lời mời kết bạn: " + e.getMessage());
        }
    }
    

    

    
    private void handleFriendRequestReceived(String fromPlayer) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            fromPlayer + " muốn kết bạn với bạn!\n\nBạn có chấp nhận không?",
            "Lời mời kết bạn",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        try {
            if (choice == JOptionPane.YES_OPTION) {
                Models.FriendRequestAccept accept = new Models.FriendRequestAccept(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.FRIEND_REQUEST_ACCEPT, accept);
                netClient.send(msg);
                JOptionPane.showMessageDialog(this, 
                    "Đã chấp nhận lời mời kết bạn từ " + fromPlayer + "!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                Models.FriendRequestReject reject = new Models.FriendRequestReject(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.FRIEND_REQUEST_REJECT, reject);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi lời mời kết bạn: " + ex.getMessage());
        }
    }
    
    private void handleFriendListResponse(java.util.List<Models.FriendInfo> friends) {
        showFriendsDialogWithData(friends);
    }
    
    public void clearRoomViewReference() {
        this.currentRoomView = null;
    }
}