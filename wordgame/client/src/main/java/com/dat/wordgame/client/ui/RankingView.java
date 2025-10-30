package com.dat.wordgame.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;

public class RankingView extends JFrame {
    private NetClient netClient;
    private String currentUser;
    
    private JTable dailyTable;
    private JTable weeklyTable;
    private JTable monthlyTable;
    private JTable allTimeTable;
    
    private DefaultTableModel dailyTableModel;
    private DefaultTableModel weeklyTableModel;
    private DefaultTableModel monthlyTableModel;
    private DefaultTableModel allTimeTableModel;
    
    private JTabbedPane tabbedPane;
    private JButton refreshButton;
    private JButton backButton;

    // Gradient background panel class
    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Create gradient from purple to lighter purple
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(88, 86, 214),
                0, getHeight(), new Color(133, 89, 215)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public RankingView(NetClient netClient, String username) {
        this.netClient = netClient;
        this.currentUser = username;
        
        initializeUI();
        setupEventHandlers();
        requestRankingData();
    }

    private void initializeUI() {
        setTitle("WordGame - Bảng Xếp Hạng");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with tabbed pane
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
        
        JLabel titleLabel = new JLabel("🏆 Bảng Xếp Hạng WordGame");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Theo dõi thành tích của bạn qua các thời kỳ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        
        // Daily rankings tab
        JScrollPane dailyScrollPane = createRankingTable("daily");
        tabbedPane.addTab("📅 Hôm nay", dailyScrollPane);
        
        // Weekly rankings tab
        JScrollPane weeklyScrollPane = createRankingTable("weekly");
        tabbedPane.addTab("📆 Tuần này", weeklyScrollPane);
        
        // Monthly rankings tab
        JScrollPane monthlyScrollPane = createRankingTable("monthly");
        tabbedPane.addTab("📊 Tháng này", monthlyScrollPane);
        
        // All-time rankings tab
        JScrollPane allTimeScrollPane = createRankingTable("alltime");
        tabbedPane.addTab("🌟 Mọi thời đại", allTimeScrollPane);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createRankingTable(String period) {
        String[] columns = {"Hạng", "Tên người chơi", "Tổng điểm", "Số trận", "Tỷ lệ thắng", "Điểm TB/trận", "Cấp độ"};
        
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModel);
        styleRankingTable(table);
        
        // Store table models for updates
        switch (period) {
            case "daily" -> {
                dailyTable = table;
                dailyTableModel = tableModel;
            }
            case "weekly" -> {
                weeklyTable = table;
                weeklyTableModel = tableModel;
            }
            case "monthly" -> {
                monthlyTable = table;
                monthlyTableModel = tableModel;
            }
            case "alltime" -> {
                allTimeTable = table;
                allTimeTableModel = tableModel;
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        return scrollPane;
    }

    private void styleRankingTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setSelectionBackground(new Color(88, 86, 214, 50));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(88, 86, 214));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 45));
        
        // Custom cell renderer for ranking
        table.setDefaultRenderer(Object.class, new RankingCellRenderer());
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Hạng
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Điểm
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Trận
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Tỷ lệ thắng
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Điểm TB
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Cấp độ
    }

    private static class RankingCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Reset styles
            setIcon(null);
            setHorizontalAlignment(SwingConstants.CENTER);
            
            if (!isSelected) {
                // Alternate row colors
                if (row % 2 == 0) {
                    c.setBackground(new Color(248, 249, 250));
                } else {
                    c.setBackground(Color.WHITE);
                }
                
                // Special styling for top 3
                if (column == 0 && row < 3) { // Ranking column
                    switch (row) {
                        case 0 -> {
                            c.setBackground(new Color(255, 215, 0, 100)); // Gold
                            setText("🥇 " + value);
                        }
                        case 1 -> {
                            c.setBackground(new Color(192, 192, 192, 100)); // Silver
                            setText("🥈 " + value);
                        }
                        case 2 -> {
                            c.setBackground(new Color(205, 127, 50, 100)); // Bronze
                            setText("🥉 " + value);
                        }
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                
                // Highlight current user
                if (column == 1) { // Name column
                    // This would need to be passed from the parent component
                    // For now, we'll use a simple approach
                }
            }
            
            return c;
        }
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        refreshButton = createModernButton("🔄 Làm mới", new Color(52, 152, 219));
        backButton = createModernButton("← Quay lại Lobby", new Color(108, 117, 125));
        
        // User stats panel
        JPanel statsPanel = createUserStatsPanel();
        
        panel.add(refreshButton);
        panel.add(backButton);
        panel.add(Box.createHorizontalStrut(50));
        panel.add(statsPanel);
        
        return panel;
    }

    private JPanel createUserStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            "📈 Thống kê của bạn",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE
        ));
        
        // Add user stats labels (will be updated with real data)
        JLabel totalGamesLabel = new JLabel("Tổng trận: --");
        totalGamesLabel.setForeground(Color.WHITE);
        totalGamesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel winRateLabel = new JLabel("Tỷ lệ thắng: --%");
        winRateLabel.setForeground(Color.WHITE);
        winRateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel avgScoreLabel = new JLabel("Điểm TB: --");
        avgScoreLabel.setForeground(Color.WHITE);
        avgScoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel levelLabel = new JLabel("Cấp độ: --");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(totalGamesLabel);
        panel.add(winRateLabel);
        panel.add(avgScoreLabel);
        panel.add(levelLabel);
        
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
        refreshButton.addActionListener(e -> requestRankingData());
        
        backButton.addActionListener(e -> {
            LobbyView lobbyView = new LobbyView(netClient, currentUser);
            lobbyView.setVisible(true);
            this.dispose();
        });
    }

    private void requestRankingData() {
        // Request ranking data for all periods
        requestRankingForPeriod("daily");
        requestRankingForPeriod("weekly");
        requestRankingForPeriod("monthly");
        requestRankingForPeriod("alltime");
    }

    private void requestRankingForPeriod(String period) {
        Message request = Message.of(MessageType.LEADERBOARD, period);
        
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy dữ liệu xếp hạng " + period + ": " + e.getMessage());
        }
    }

    public void updateRankingData(String period, List<Map<String, Object>> rankings) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel targetModel = switch (period) {
                case "daily" -> dailyTableModel;
                case "weekly" -> weeklyTableModel;
                case "monthly" -> monthlyTableModel;
                case "alltime" -> allTimeTableModel;
                default -> null;
            };
            
            if (targetModel == null) return;
            
            targetModel.setRowCount(0);
            int rank = 1;
            
            for (Map<String, Object> player : rankings) {
                String name = (String) player.get("name");
                int totalScore = (Integer) player.getOrDefault("totalScore", 0);
                int totalGames = (Integer) player.getOrDefault("totalGames", 0);
                int wins = (Integer) player.getOrDefault("wins", 0);
                
                double winRate = totalGames > 0 ? (double) wins / totalGames * 100 : 0;
                double avgScore = totalGames > 0 ? (double) totalScore / totalGames : 0;
                String level = calculateLevel(totalScore);
                
                targetModel.addRow(new Object[]{
                    rank++,
                    name,
                    totalScore,
                    totalGames,
                    String.format("%.1f%%", winRate),
                    String.format("%.1f", avgScore),
                    level
                });
            }
        });
    }

    private String calculateLevel(int totalScore) {
        if (totalScore >= 10000) return "Thầy phù thủy";
        if (totalScore >= 5000) return "Cao thủ";
        if (totalScore >= 2000) return "Chuyên gia";
        if (totalScore >= 1000) return "Thành thạo";
        if (totalScore >= 500) return "Trung cấp";
        if (totalScore >= 100) return "Sơ cấp";
        return "Tập sự";
    }

    public void updateUserStats(Map<String, Object> stats) {
        // Update user statistics panel with real data
        // This would be called when receiving user stats from server
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}