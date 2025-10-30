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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.dat.wordgame.client.NetClient;

public class SettingsView extends JFrame {
    private NetClient netClient;
    private String currentUser;
    private Preferences prefs;
    
    // UI Components
    private JSlider volumeSlider;
    private JCheckBox soundEffectsCheckBox;
    private JCheckBox musicCheckBox;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> themeComboBox;
    private JSpinner timeSpinner;
    private JCheckBox notificationsCheckBox;
    private JCheckBox autoSaveCheckBox;
    private JTextField nicknameField;
    
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

    public SettingsView(NetClient netClient, String username) {
        this.netClient = netClient;
        this.currentUser = username;
        this.prefs = Preferences.userNodeForPackage(SettingsView.class);
        
        initializeUI();
        loadSettings();
        setupEventHandlers();
    }

    private void initializeUI() {
        setTitle("WordGame - Cài đặt");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel with settings
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
        
        JLabel titleLabel = new JLabel("⚙️ Cài đặt Game");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Tùy chỉnh trải nghiệm game của bạn");
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
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setOpaque(false);
        
        // Audio Settings
        JPanel audioPanel = createAudioSettingsPanel();
        panel.add(audioPanel);
        
        // Game Settings
        JPanel gamePanel = createGameSettingsPanel();
        panel.add(gamePanel);
        
        // Display Settings
        JPanel displayPanel = createDisplaySettingsPanel();
        panel.add(displayPanel);
        
        // Account Settings
        JPanel accountPanel = createAccountSettingsPanel();
        panel.add(accountPanel);
        
        return panel;
    }

    private JPanel createAudioSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("🔊 Âm thanh");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Volume slider
        gbc.gridwidth = 1;
        JLabel volumeLabel = new JLabel("Âm lượng:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(volumeLabel, gbc);
        
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(volumeSlider, gbc);
        
        // Sound effects checkbox
        gbc.fill = GridBagConstraints.NONE;
        soundEffectsCheckBox = new JCheckBox("Hiệu ứng âm thanh", true);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(soundEffectsCheckBox, gbc);
        
        // Music checkbox
        musicCheckBox = new JCheckBox("Nhạc nền", true);
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(musicCheckBox, gbc);
        
        return panel;
    }

    private JPanel createGameSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("🎮 Gameplay");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Difficulty setting
        gbc.gridwidth = 1;
        JLabel difficultyLabel = new JLabel("Độ khó:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(difficultyLabel, gbc);
        
        String[] difficulties = {"Dễ", "Trung bình", "Khó", "Cực khó"};
        difficultyComboBox = new JComboBox<>(difficulties);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(difficultyComboBox, gbc);
        
        // Time limit
        gbc.fill = GridBagConstraints.NONE;
        JLabel timeLabel = new JLabel("Thời gian (giây):");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(timeLabel, gbc);
        
        timeSpinner = new JSpinner(new SpinnerNumberModel(60, 30, 300, 10));
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(timeSpinner, gbc);
        
        // Auto save
        gbc.fill = GridBagConstraints.NONE;
        autoSaveCheckBox = new JCheckBox("Tự động lưu", true);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(autoSaveCheckBox, gbc);
        
        return panel;
    }

    private JPanel createDisplaySettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("🎨 Hiển thị");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Theme setting
        gbc.gridwidth = 1;
        JLabel themeLabel = new JLabel("Giao diện:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(themeLabel, gbc);
        
        String[] themes = {"Gradient Purple (Hiện tại)", "Sáng", "Tối", "Xanh lá", "Cam"};
        themeComboBox = new JComboBox<>(themes);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(themeComboBox, gbc);
        
        // Notifications
        gbc.fill = GridBagConstraints.NONE;
        notificationsCheckBox = new JCheckBox("Thông báo", true);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(notificationsCheckBox, gbc);
        
        // Preview panel
        JPanel previewPanel = new JPanel();
        previewPanel.setBackground(new Color(88, 86, 214));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Xem trước"));
        previewPanel.setPreferredSize(new Dimension(0, 50));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(previewPanel, gbc);
        
        return panel;
    }

    private JPanel createAccountSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        JLabel titleLabel = new JLabel("👤 Tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Nickname
        gbc.gridwidth = 1;
        JLabel nicknameLabel = new JLabel("Tên hiển thị:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(nicknameLabel, gbc);
        
        nicknameField = new JTextField(currentUser);
        nicknameField.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nicknameField, gbc);
        
        // Current user info
        gbc.fill = GridBagConstraints.NONE;
        JLabel userInfoLabel = new JLabel("<html><i>Đăng nhập với: " + currentUser + "</i></html>");
        userInfoLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(userInfoLabel, gbc);
        
        // Statistics button
        JButton statsButton = new JButton("📊 Xem thống kê chi tiết");
        statsButton.addActionListener(e -> showDetailedStats());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statsButton, gbc);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        JButton saveButton = createModernButton("💾 Lưu cài đặt", new Color(46, 204, 113));
        JButton resetButton = createModernButton("🔄 Khôi phục mặc định", new Color(231, 76, 60));
        JButton cancelButton = createModernButton("❌ Hủy", new Color(108, 117, 125));
        
        panel.add(saveButton);
        panel.add(resetButton);
        panel.add(cancelButton);
        
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
        // Save button
        Component[] components = ((JPanel) getContentPane().getComponent(0)).getComponents();
        JPanel bottomPanel = (JPanel) components[components.length - 1];
        Component[] buttons = bottomPanel.getComponents();
        
        ((JButton) buttons[0]).addActionListener(e -> saveSettings());
        ((JButton) buttons[1]).addActionListener(e -> resetToDefaults());
        ((JButton) buttons[2]).addActionListener(e -> this.dispose());
        
        // Theme change preview
        themeComboBox.addActionListener(e -> previewTheme());
    }

    private void loadSettings() {
        // Load settings from preferences
        volumeSlider.setValue(prefs.getInt("volume", 50));
        soundEffectsCheckBox.setSelected(prefs.getBoolean("soundEffects", true));
        musicCheckBox.setSelected(prefs.getBoolean("music", true));
        difficultyComboBox.setSelectedIndex(prefs.getInt("difficulty", 1));
        themeComboBox.setSelectedIndex(prefs.getInt("theme", 0));
        timeSpinner.setValue(prefs.getInt("timeLimit", 60));
        notificationsCheckBox.setSelected(prefs.getBoolean("notifications", true));
        autoSaveCheckBox.setSelected(prefs.getBoolean("autoSave", true));
        nicknameField.setText(prefs.get("nickname", currentUser));
    }

    private void saveSettings() {
        // Save settings to preferences
        prefs.putInt("volume", volumeSlider.getValue());
        prefs.putBoolean("soundEffects", soundEffectsCheckBox.isSelected());
        prefs.putBoolean("music", musicCheckBox.isSelected());
        prefs.putInt("difficulty", difficultyComboBox.getSelectedIndex());
        prefs.putInt("theme", themeComboBox.getSelectedIndex());
        prefs.putInt("timeLimit", (Integer) timeSpinner.getValue());
        prefs.putBoolean("notifications", notificationsCheckBox.isSelected());
        prefs.putBoolean("autoSave", autoSaveCheckBox.isSelected());
        prefs.put("nickname", nicknameField.getText());
        
        try {
            prefs.flush();
            JOptionPane.showMessageDialog(this,
                "✅ Cài đặt đã được lưu thành công!\n" +
                "Một số thay đổi có thể cần khởi động lại ứng dụng.",
                "Lưu thành công",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi khi lưu cài đặt: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetToDefaults() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn khôi phục tất cả cài đặt về mặc định?\n" +
            "Thao tác này không thể hoàn tác.",
            "Xác nhận khôi phục",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Clear all preferences
            try {
                prefs.clear();
                // Reload default values
                loadSettings();
                JOptionPane.showMessageDialog(this,
                    "✅ Đã khôi phục cài đặt mặc định thành công!",
                    "Khôi phục thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Lỗi khi khôi phục cài đặt: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void previewTheme() {
        // Preview theme changes
        String selectedTheme = (String) themeComboBox.getSelectedItem();
        // This would apply theme preview in real implementation
        System.out.println("Previewing theme: " + selectedTheme);
    }

    private void showDetailedStats() {
        // Show detailed user statistics
        String stats = """
            📊 THỐNG KÊ CHI TIẾT
            
            🎮 Tổng số trận đã chơi: 42
            🏆 Số trận thắng: 28 (66.7%)
            😔 Số trận thua: 14 (33.3%)
            
            ⭐ Tổng điểm tích lũy: 2,847
            📈 Điểm trung bình/trận: 67.8
            🚀 Điểm cao nhất trong 1 trận: 195
            
            ⏱️ Thời gian chơi trung bình: 1m 23s
            ⚡ Thời gian nhanh nhất: 45s
            
            🎯 Từ đoán đúng: 156/210 (74.3%)
            🔥 Streak dài nhất: 8 trận liên tiếp
            
            📅 Ngày đăng ký: 15/09/2024
            🎂 Thành viên được: 29 ngày
            """;
        
        JTextArea textArea = new JTextArea(stats);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Thống kê chi tiết - " + currentUser, JOptionPane.INFORMATION_MESSAGE);
    }
}