package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class LobbyView extends JFrame {
    private NetClient netClient;
    private JLabel welcomeLabel;
    private JTable playersTable;
    private JTable rankingTable;
    private DefaultTableModel playersTableModel;
    private DefaultTableModel rankingTableModel;
    private JButton startGameButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private String currentUser;

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
        
        initializeUI();
        setupEventHandlers();
        requestPlayersList();
        requestRankingData();
    }

    private void initializeUI() {
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent white
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("🎮 Người chơi đang online");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(88, 86, 214));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create players table
        String[] playersColumns = {"Tên người chơi", "Trạng thái", "Điểm số"};
        playersTableModel = new DefaultTableModel(playersColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        playersTable = new JTable(playersTableModel);
        styleTable(playersTable);
        
        JScrollPane playersScrollPane = new JScrollPane(playersTable);
        playersScrollPane.setPreferredSize(new Dimension(0, 400));
        playersScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(playersScrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createRankingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent white
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("🏆 Bảng xếp hạng");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(88, 86, 214));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create ranking table
        String[] rankingColumns = {"Hạng", "Tên người chơi", "Tổng điểm", "Số trận thắng"};
        rankingTableModel = new DefaultTableModel(rankingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        rankingTable = new JTable(rankingTableModel);
        styleTable(rankingTable);
        
        JScrollPane rankingScrollPane = new JScrollPane(rankingTable);
        rankingScrollPane.setPreferredSize(new Dimension(0, 400));
        rankingScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        panel.add(rankingScrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(88, 86, 214, 50));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(240, 240, 240));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(88, 86, 214));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(88, 86, 214)));
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        startGameButton = createModernButton("🏠 Tạo phòng", new Color(46, 204, 113));
        refreshButton = createModernButton("Làm mới", new Color(52, 152, 219));
        JButton rankingButton = createModernButton("🏆 Xem xếp hạng", new Color(155, 89, 182));
        JButton settingsButton = createModernButton("⚙️ Cài đặt", new Color(241, 196, 15));
        logoutButton = createModernButton("Đăng xuất", new Color(231, 76, 60));
        
        // Add button events
        rankingButton.addActionListener(e -> openRankingView());
        settingsButton.addActionListener(e -> openSettingsView());
        
        panel.add(startGameButton);
        panel.add(refreshButton);
        panel.add(rankingButton);
        panel.add(settingsButton);
        panel.add(logoutButton);
        
        return panel;
    }
    
    private void openRankingView() {
        RankingView rankingView = new RankingView(netClient, currentUser);
        rankingView.setVisible(true);
    }
    
    private void openSettingsView() {
        SettingsView settingsView = new SettingsView(netClient, currentUser);
        settingsView.setVisible(true);
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
    }

    private void requestPlayersList() {
        // Send request for players list
        Message request = Message.of(MessageType.PLAYER_LIST, currentUser);
        
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy danh sách người chơi: " + e.getMessage());
        }
    }

    private void requestRankingData() {
        // Send request for ranking data
        Message request = Message.of(MessageType.LEADERBOARD, currentUser);
        
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy bảng xếp hạng: " + e.getMessage());
        }
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
        
        // Open RoomView instead of starting game directly
        RoomView roomView = new RoomView(netClient, currentUser, roomId, players);
        roomView.setVisible(true);
        this.dispose();
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
}