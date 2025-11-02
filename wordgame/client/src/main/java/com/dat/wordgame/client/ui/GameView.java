package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class GameView extends JFrame {
    private NetClient netClient;
    private String currentUser;
    private List<String> players;
    private String gameId;
    private List<Character> availableLetters;
    private int timeRemaining;
    private Timer gameTimer;
    private boolean gameActive;
    private int countdownSeconds = 5; // Countdown before game starts
    private Timer countdownTimer;
    private List<Character> currentGuess = new ArrayList<>(); // Current word being built as list
    private List<JButton> answerSlots = new ArrayList<>(); // Empty slots for answer
    private LobbyView parentLobby; // Reference to return to lobby
    
    // UI Components
    private JLabel wordLabel;
    private JLabel timeLabel;
    private JLabel scoreLabel;
    private JPanel lettersPanel; // 26 letters A-Z
    private JPanel answerSlotsPanel; // Empty slots based on word length
    private JLabel countdownLabel; // Countdown label
    private JButton submitButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendChatButton;
    private JPanel playersPanel;
    private JLabel statusLabel;
    private JLabel myGuessProgressLabel; // Show my correct letters count
    private JLabel opponentGuessProgressLabel; // Show opponent's correct letters count
    
    // Game state
    private int currentScore = 0;
    private int round = 1;
    private String maskedWord = "";

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

    public GameView(NetClient netClient, String username, List<String> players, String gameId) {
        this.netClient = netClient;
        this.currentUser = username;
        this.players = players;
        this.gameId = gameId;
        this.availableLetters = new ArrayList<>();
        this.parentLobby = null; // Will be set by LobbyView
        
        initializeUI();
        setupEventHandlers();
        setupGameTimer();
        // Don't call startListening() - LobbyView will forward messages
        
        setVisible(true);
        
        System.out.println("GameView created, waiting for ROUND_START...");
    }
    
    // Setter for parent lobby reference
    public void setParentLobby(LobbyView lobby) {
        this.parentLobby = lobby;
    }
    
    // Public method for LobbyView to forward messages
    public void onMessage(Message message) {
        handleMessage(message);
    }
    
    private void startCountdown() {
        gameActive = false;
        submitButton.setEnabled(false);
        countdownSeconds = 5; // Reset countdown
        countdownLabel.setText("Bắt đầu sau: 5s");
        countdownLabel.setVisible(true);
        lettersPanel.setVisible(false);
        answerSlotsPanel.setVisible(false);
        
        countdownTimer = new Timer(1000, e -> {
            countdownSeconds--;
            if (countdownSeconds > 0) {
                countdownLabel.setText("Bắt đầu sau: " + countdownSeconds + "s");
            } else {
                countdownTimer.stop();
                countdownLabel.setVisible(false);
                lettersPanel.setVisible(true);
                answerSlotsPanel.setVisible(true);
                gameActive = true;
                submitButton.setEnabled(true);
                gameTimer.start(); // Start game timer AFTER countdown
                statusLabel.setText("🎯 Game đã bắt đầu! Hãy tìm từ!");
            }
        });
        countdownTimer.start();
    }

    private void initializeUI() {
        setTitle("WordGame - Trò chơi đang diễn ra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Create gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel - Game area
        JPanel centerPanel = createGamePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - Players and Chat
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        // Bottom panel - Status and controls
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Game info
        JLabel titleLabel = new JLabel("🎮 WordGame - Vòng " + round);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        // Timer and score
        timeLabel = new JLabel("⏰ Thời gian: --:--");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLabel.setForeground(Color.YELLOW);
        
        scoreLabel = new JLabel("🏆 Điểm: " + currentScore);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.setOpaque(false);
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(scoreLabel);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createGamePanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        // Glass panel with glassmorphism effect
        JPanel glassPanel = new JPanel(new BorderLayout(0, 30)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        glassPanel.setOpaque(false);
        glassPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Word display area
        JPanel wordPanel = createWordPanel();
        glassPanel.add(wordPanel, BorderLayout.NORTH);
        
        // Center area with countdown, answer slots, and letters
        JPanel centerArea = new JPanel(new BorderLayout(0, 20));
        centerArea.setOpaque(false);
        
        // Answer slots panel (empty boxes based on word length)
        answerSlotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        answerSlotsPanel.setOpaque(false);
        
        // Countdown label (initially visible)
        countdownLabel = new JLabel("Bắt đầu sau: 5s", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 72));
        countdownLabel.setForeground(new Color(255, 215, 0));
        
        // Letters panel (26 letters A-Z)
        lettersPanel = createLettersPanel();
        
        // Countdown wrapper panel to overlay
        JPanel countdownWrapper = new JPanel(new BorderLayout());
        countdownWrapper.setOpaque(false);
        countdownWrapper.add(countdownLabel, BorderLayout.CENTER);
        
        // Stack countdown on top of letters using OverlayLayout
        JPanel gameAreaPanel = new JPanel();
        gameAreaPanel.setOpaque(false);
        gameAreaPanel.setLayout(new OverlayLayout(gameAreaPanel));
        gameAreaPanel.add(countdownWrapper);
        gameAreaPanel.add(lettersPanel);
        
        centerArea.add(answerSlotsPanel, BorderLayout.NORTH);
        centerArea.add(gameAreaPanel, BorderLayout.CENTER);
        
        glassPanel.add(centerArea, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = createGuessPanel();
        glassPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        container.add(glassPanel);
        return container;
    }

    private JPanel createWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        wordLabel = new JLabel(maskedWord, SwingConstants.CENTER);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 48));
        wordLabel.setForeground(Color.WHITE);
        
        panel.add(wordLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createLettersPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 13, 10, 10)); // 2 rows, 13 columns for 26 letters
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        System.out.println("[GameView] Creating letters panel with 26 letters A-Z");
        
        // Create all 26 letters A-Z
        for (char c = 'A'; c <= 'Z'; c++) {
            JButton letterBtn = createLetterButton(c);
            final char letter = c;
            letterBtn.addActionListener(e -> {
                if (gameActive && currentGuess.size() < maskedWord.length()) {
                    currentGuess.add(letter);
                    updateAnswerSlots();
                }
            });
            panel.add(letterBtn);
        }
        
        System.out.println("[GameView] Letters panel created with " + panel.getComponentCount() + " buttons");
        return panel;
    }
    
    private void updateLettersPanel() {
        // Make sure letters panel is visible and repainted
        System.out.println("[GameView] updateLettersPanel called - showing all 26 letters");
        lettersPanel.setVisible(true);
        lettersPanel.revalidate();
        lettersPanel.repaint();
    }
    
    private void createAnswerSlots(int wordLength) {
        answerSlotsPanel.removeAll();
        answerSlots.clear();
        currentGuess.clear();
        
        for (int i = 0; i < wordLength; i++) {
            JButton slotBtn = createEmptySlot(i);
            answerSlots.add(slotBtn);
            answerSlotsPanel.add(slotBtn);
        }
        
        answerSlotsPanel.revalidate();
        answerSlotsPanel.repaint();
    }
    
    private JButton createEmptySlot(int index) {
        JButton btn = new JButton("");
        btn.setFont(new Font("Arial", Font.BOLD, 36));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 255, 255, 50));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 150), 3, true));
        btn.setPreferredSize(new Dimension(60, 60));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Click to remove letter from this slot
        btn.addActionListener(e -> {
            if (gameActive && index < currentGuess.size()) {
                currentGuess.remove(index);
                updateAnswerSlots();
            }
        });
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getText().isEmpty()) {
                    btn.setBackground(new Color(255, 100, 100, 100));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getText().isEmpty()) {
                    btn.setBackground(new Color(255, 215, 0, 200));
                }
            }
        });
        
        return btn;
    }
    
    private void updateAnswerSlots() {
        for (int i = 0; i < answerSlots.size(); i++) {
            JButton slot = answerSlots.get(i);
            if (i < currentGuess.size()) {
                slot.setText(currentGuess.get(i).toString());
                slot.setBackground(new Color(255, 215, 0, 200)); // Gold for filled
            } else {
                slot.setText("");
                slot.setBackground(new Color(255, 255, 255, 50)); // Transparent for empty
            }
        }
    }
    
    private JButton createLetterButton(Character letter) {
        JButton btn = new JButton(letter.toString().toUpperCase());
        btn.setFont(new Font("Arial", Font.BOLD, 18)); // Further reduced to 18
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(88, 86, 214, 150));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(80, 80)); // Increased to 80x80
        btn.setMinimumSize(new Dimension(80, 80));
        btn.setMaximumSize(new Dimension(80, 80));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0)); // Remove internal padding
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(108, 106, 234, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(88, 86, 214, 150));
            }
        });
        
        return btn;
    }

    private JPanel createGuessPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        
        submitButton = createGlassButton("✓ Gửi câu trả lời", new Color(46, 204, 113));
        JButton clearButton = createGlassButton("✗ Xóa", new Color(231, 76, 60));
        
        submitButton.addActionListener(e -> submitGuess());
        clearButton.addActionListener(e -> clearGuess());
        
        panel.add(submitButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private JButton createGlassButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void clearGuess() {
        currentGuess.clear();
        updateAnswerSlots();
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setOpaque(false);
        
        // Players panel
        playersPanel = createPlayersPanel();
        panel.add(playersPanel, BorderLayout.NORTH);
        
        // Chat panel
        JPanel chatPanel = createChatPanel();
        panel.add(chatPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(280, 180));
        
        JLabel titleLabel = new JLabel("👥 Người chơi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        
        JPanel playersListPanel = new JPanel();
        playersListPanel.setLayout(new BoxLayout(playersListPanel, BoxLayout.Y_AXIS));
        playersListPanel.setOpaque(false);
        
        // Show each player with their guess progress
        for (String player : players) {
            JPanel playerRow = new JPanel(new BorderLayout(5, 0));
            playerRow.setOpaque(false);
            playerRow.setBorder(new EmptyBorder(5, 0, 5, 0));
            
            JLabel playerLabel = new JLabel("• " + player + (player.equals(currentUser) ? " (Bạn)" : ""));
            playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            playerLabel.setForeground(player.equals(currentUser) ? new Color(46, 204, 113) : Color.BLACK);
            
            JLabel progressLabel = new JLabel("0 ô đúng");
            progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            progressLabel.setForeground(new Color(150, 150, 150));
            
            // Store references to progress labels
            if (player.equals(currentUser)) {
                myGuessProgressLabel = progressLabel;
            } else {
                opponentGuessProgressLabel = progressLabel;
            }
            
            playerRow.add(playerLabel, BorderLayout.WEST);
            playerRow.add(progressLabel, BorderLayout.EAST);
            playersListPanel.add(playerRow);
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(playersListPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("💬 Trò chuyện");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        
        chatArea = new JTextArea();
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setPreferredSize(new Dimension(0, 200));
        
        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 0));
        chatInputPanel.setOpaque(false);
        
        chatField = new JTextField();
        chatField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        sendChatButton = createModernButton("Gửi", new Color(52, 152, 219));
        sendChatButton.setPreferredSize(new Dimension(60, 30));
        
        chatInputPanel.add(chatField, BorderLayout.CENTER);
        chatInputPanel.add(sendChatButton, BorderLayout.EAST);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(chatInputPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        statusLabel = new JLabel("Đang chờ bắt đầu game...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Button panel for multiple buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton inviteButton = createModernButton("👥 Mời bạn bè", new Color(34, 197, 94));
        inviteButton.addActionListener(e -> showInviteFriendsDialog());
        
        JButton exitButton = createModernButton("Thoát game", new Color(231, 76, 60));
        exitButton.addActionListener(e -> handleSurrender());
        
        buttonPanel.add(inviteButton);
        buttonPanel.add(exitButton);
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
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
        // Send chat button
        if (sendChatButton != null) {
            sendChatButton.addActionListener(e -> sendChat());
        }
        
        // Chat field enter key
        if (chatField != null) {
            chatField.addActionListener(e -> sendChat());
        }
    }

    private void setupGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    updateTimeDisplay();
                } else {
                    // Time's up
                    gameTimer.stop();
                    statusLabel.setText("⏰ Hết giờ!");
                    submitButton.setEnabled(false);
                    // guessField removed - using letter buttons now
                }
            }
        });
    }

    private void updateTimeDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timeLabel.setText(String.format("⏰ Thời gian: %02d:%02d", minutes, seconds));
    }

    private void startListening() {
        // Listen for server messages
        netClient.listen(this::handleMessage);
    }

    private void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case ROUND_START -> handleRoundStart(message);
                case ROUND_TICK -> handleRoundTick(message);
                case ROUND_END -> handleRoundEnd(message);
                case GAME_END -> handleGameEnd(message);
                case GUESS_UPDATE -> handleGuessUpdate(message);
                case CHAT -> handleChat(message);
                default -> System.out.println("Unhandled message type: " + message.type);
            }
        });
    }

    private void handleRoundStart(Message message) {
        try {
            System.out.println("ROUND_START received!");
            
            var roundStart = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundStart.class);
            
            this.gameId = roundStart.roomId();
            this.round = roundStart.round();
            this.maskedWord = roundStart.maskedWord();
            this.availableLetters = roundStart.shuffledLetters();
            this.timeRemaining = roundStart.totalTimeSec();
            
            System.out.println("Masked word: " + maskedWord);
            System.out.println("Available letters: " + availableLetters);
            System.out.println("Time: " + timeRemaining + "s");
            
            // Update UI
            setTitle("WordleCup - Vòng " + round);
            wordLabel.setText(maskedWord.toUpperCase()); // Show the actual word from server
            createAnswerSlots(maskedWord.length()); // Create empty slots based on word length
            updateLettersPanel(); // Show all 26 letters
            updateTimeDisplay();
            
            statusLabel.setText("🎯 Vòng " + round + " - Tìm từ có " + maskedWord.length() + " chữ cái!");
            
            // Start 5-second countdown before enabling game
            startCountdown();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xử lý bắt đầu vòng: " + e.getMessage());
        }
    }

    private void handleRoundTick(Message message) {
        try {
            Models.RoundTick roundTick = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundTick.class);
            this.timeRemaining = roundTick.remainSec();
            updateTimeDisplay();
        } catch (Exception e) {
            showError("Lỗi cập nhật thời gian: " + e.getMessage());
        }
    }

    private void handleRoundEnd(Message message) {
        try {
            Models.RoundEnd roundEnd = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundEnd.class);
            
            gameTimer.stop();
            gameActive = false;
            submitButton.setEnabled(false);
            // guessField removed - using letter buttons now
            
            String winner = roundEnd.winner();
            String correctWord = roundEnd.correctWord();
            int points = roundEnd.totalAward();
            
            if (winner != null && winner.equals(currentUser)) {
                currentScore += points;
                scoreLabel.setText("🏆 Điểm: " + currentScore);
                statusLabel.setText("🎉 Bạn thắng vòng này! Từ đúng: " + correctWord + " (+"+points+" điểm)");
            } else if (winner != null) {
                statusLabel.setText("😔 Vòng này thắng: " + winner + ". Từ đúng: " + correctWord);
            } else {
                statusLabel.setText("⏱️ Hết giờ! Từ đúng: " + correctWord);
            }
            
            // Show correct word
            wordLabel.setText(correctWord);
            
        } catch (Exception e) {
            showError("Lỗi xử lý kết thúc vòng: " + e.getMessage());
        }
    }

    private void handleGameEnd(Message message) {
        try {
            Models.GameEnd gameEnd = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.GameEnd.class);
            
            gameTimer.stop();
            gameActive = false;
            
            String winner = gameEnd.winner();
            
            JOptionPane.showMessageDialog(this,
                "🏁 Game kết thúc!\n" +
                "🏆 Người thắng: " + winner + "\n" +
                "📊 Xem chi tiết trong bảng xếp hạng",
                "Kết thúc Game",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Return to lobby
            returnToLobby();
            
        } catch (Exception e) {
            showError("Lỗi xử lý kết thúc game: " + e.getMessage());
        }
    }

    private void handleGuessUpdate(Message message) {
        try {
            Models.GuessUpdate guessUpdate = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.GuessUpdate.class);
            String player = guessUpdate.player();
            int correctSlots = guessUpdate.correctSlots();
            
            // Check if it's my guess feedback
            if (player.equals(currentUser)) {
                // Check if answer is fully correct
                if (correctSlots == maskedWord.length()) {
                    // Correct answer!
                    showCorrectFeedback();
                    // Don't disable game - can still play until round ends
                } else {
                    // Wrong answer - show red feedback
                    showIncorrectFeedback();
                    clearGuess(); // Clear and try again
                    statusLabel.setText("❌ Sai rồi! Thử lại! (" + correctSlots + "/" + maskedWord.length() + " đúng vị trí)");
                }
                
                // Update my progress
                if (myGuessProgressLabel != null) {
                    myGuessProgressLabel.setText(correctSlots + "/" + maskedWord.length() + " đúng vị trí");
                    myGuessProgressLabel.setForeground(correctSlots == maskedWord.length() ? 
                        new Color(46, 204, 113) : new Color(230, 126, 34));
                }
            } else {
                // Opponent's guess
                if (opponentGuessProgressLabel != null) {
                    opponentGuessProgressLabel.setText(correctSlots + "/" + maskedWord.length() + " đúng vị trí");
                    opponentGuessProgressLabel.setForeground(correctSlots == maskedWord.length() ? 
                        new Color(46, 204, 113) : new Color(230, 126, 34));
                }
                
                if (correctSlots == maskedWord.length()) {
                    addChatMessage("🎉 " + player + " đã trả lời đúng!");
                } else {
                    addChatMessage("📝 " + player + " đoán: " + correctSlots + "/" + maskedWord.length() + " đúng vị trí");
                }
            }
            
        } catch (Exception e) {
            showError("Lỗi xử lý cập nhật đoán: " + e.getMessage());
        }
    }

    private void handleChat(Message message) {
        try {
            Models.Chat chat = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Chat.class);
            String from = chat.from();
            String text = chat.text();
            
            addChatMessage(from + ": " + text);
            
            // If it's a system message about surrender, show popup and return to lobby
            if (from.contains("System") && text.contains("đầu hàng")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        this,
                        text + "\n\nBạn sẽ được chuyển về lobby sau khi nhấn OK.",
                        "🏆 Chiến thắng!",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // After user clicks OK, return to lobby
                    // Wait a bit for GAME_END message to arrive
                    Timer returnTimer = new Timer(1000, e -> {
                        returnToLobby();
                    });
                    returnTimer.setRepeats(false);
                    returnTimer.start();
                });
            }
            
        } catch (Exception e) {
            showError("Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }

    private void updateAvailableLetters() {
        lettersPanel.removeAll();
        
        for (Character letter : availableLetters) {
            JButton letterButton = new JButton(letter.toString().toUpperCase());
            letterButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            letterButton.setPreferredSize(new Dimension(50, 50));
            letterButton.setBackground(new Color(230, 230, 250));
            letterButton.setForeground(new Color(88, 86, 214));
            letterButton.setBorder(BorderFactory.createLineBorder(new Color(88, 86, 214), 2));
            letterButton.setFocusPainted(false);
            letterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            letterButton.addActionListener(e -> {
                // Now handled in createLetterButton() - this code is old
                // Should not reach here as updateLettersPanel() creates new buttons
            });
            
            lettersPanel.add(letterButton);
        }
        
        lettersPanel.revalidate();
        lettersPanel.repaint();
    }

    private void submitGuess() {
        if (!gameActive) return;
        
        // Convert List<Character> to String
        StringBuilder sb = new StringBuilder();
        for (Character c : currentGuess) {
            sb.append(c);
        }
        String guess = sb.toString().trim().toUpperCase();
        
        if (guess.isEmpty()) {
            showError("Vui lòng nhập từ đoán!");
            return;
        }
        
        // Check length match
        if (guess.length() != maskedWord.length()) {
            showError("Từ phải có " + maskedWord.length() + " chữ cái!");
            showIncorrectFeedback(); // Red border
            return;
        }
        
        // Send guess to server
        Models.GuessSubmit guessSubmit = new Models.GuessSubmit(gameId, guess);
        Message message = Message.of(MessageType.GUESS_SUBMIT, guessSubmit);
        
        try {
            netClient.send(message);
            // DON'T clear guess yet - wait for server response
            statusLabel.setText("📤 Đang kiểm tra: " + guess + "...");
        } catch (Exception e) {
            showError("Lỗi gửi đáp án: " + e.getMessage());
        }
    }
    
    private void showIncorrectFeedback() {
        // Flash red border around answer slots
        answerSlotsPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
        
        // Reset border after 1 second
        Timer timer = new Timer(1000, e -> {
            answerSlotsPanel.setBorder(null);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showCorrectFeedback() {
        // Flash green border around answer slots
        answerSlotsPanel.setBorder(BorderFactory.createLineBorder(new Color(46, 204, 113), 3));
        statusLabel.setText("✅ Chính xác! Đợi đối thủ...");
    }

    private void sendChat() {
        String text = chatField.getText().trim();
        if (text.isEmpty()) return;
        
        Models.Chat chat = new Models.Chat(gameId, currentUser, text);
        Message message = Message.of(MessageType.CHAT, chat);
        
        try {
            netClient.send(message);
            chatField.setText("");
        } catch (Exception e) {
            showError("Lỗi gửi tin nhắn: " + e.getMessage());
        }
    }

    private void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void returnToLobby() {
        try {
            if (parentLobby != null) {
                // Reuse existing lobby
                parentLobby.setVisible(true);
                
                // Wait a bit for server to update database and broadcast snapshot
                Timer refreshTimer = new Timer(500, e -> {
                    parentLobby.requestPlayersList(); // Refresh data
                    parentLobby.requestRankingData();
                });
                refreshTimer.setRepeats(false);
                refreshTimer.start();
                
                this.dispose();
            } else {
                // Fallback: create new lobby (should not happen)
                LobbyView lobbyView = new LobbyView(netClient, currentUser);
                lobbyView.setVisible(true);
                this.dispose();
            }
        } catch (Exception e) {
            showError("Lỗi quay về lobby: " + e.getMessage());
        }
    }

    private void handleSurrender() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn thoát game?\nBạn sẽ thua trận này!",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                // Send surrender message to server
                Models.Surrender surrender = new Models.Surrender(gameId, currentUser);
                Message msg = Message.of(MessageType.SURRENDER, surrender);
                netClient.send(msg);
                
                System.out.println("[GameView] Sent SURRENDER message");
                
                // Return to lobby immediately
                returnToLobby();
            } catch (Exception e) {
                showError("Lỗi khi thoát game: " + e.getMessage());
            }
        }
    }

    private void showInviteFriendsDialog() {
        // Create custom dialog
        JDialog dialog = new JDialog(this, "Mời bạn bè", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(88, 86, 214),
                    0, getHeight(), new Color(133, 89, 215)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("🎮 Mời bạn bè chơi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setBackground(new Color(255, 255, 255, 200));
        searchField.setForeground(Color.BLACK);
        
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 16));
        searchIcon.setForeground(Color.WHITE);
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Players list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> playersList = new JList<>(listModel);
        playersList.setFont(new Font("Arial", Font.PLAIN, 14));
        playersList.setBackground(new Color(255, 255, 255, 200));
        playersList.setForeground(Color.BLACK);
        playersList.setSelectionBackground(new Color(88, 86, 214));
        playersList.setSelectionForeground(Color.WHITE);
        playersList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Custom cell renderer to show online status
        playersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                label.setText("🟢 " + value);
                label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(playersList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Request and populate players list from parent lobby
        if (parentLobby != null) {
            // Get online players from lobby
            List<String> onlinePlayers = parentLobby.getOnlinePlayers();
            
            if (onlinePlayers.isEmpty()) {
                listModel.addElement("Không có người chơi online");
            } else {
                for (String player : onlinePlayers) {
                    listModel.addElement(player);
                }
            }
        } else {
            listModel.addElement("Không thể tải danh sách");
        }
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton inviteButton = createStyledButton("✉️ Gửi lời mời", new Color(34, 197, 94));
        JButton cancelButton = createStyledButton("❌ Hủy", new Color(231, 76, 60));
        
        inviteButton.addActionListener(e -> {
            String selectedPlayer = playersList.getSelectedValue();
            if (selectedPlayer != null && !selectedPlayer.equals("Đang tải...") 
                    && !selectedPlayer.equals("Lỗi tải danh sách")) {
                // Remove emoji prefix
                selectedPlayer = selectedPlayer.replace("🟢 ", "").trim();
                
                if (!selectedPlayer.equals(currentUser)) {
                    sendInvite(selectedPlayer);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Bạn không thể mời chính mình!", 
                        "Lỗi", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Vui lòng chọn một người chơi!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(inviteButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store original player list for filtering
        final List<String> allPlayers = new ArrayList<>();
        if (parentLobby != null) {
            allPlayers.addAll(parentLobby.getOnlinePlayers());
        }
        
        // Search functionality with document listener for real-time search
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterPlayers();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterPlayers();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterPlayers();
            }
            
            private void filterPlayers() {
                String searchText = searchField.getText().toLowerCase().trim();
                listModel.clear();
                
                if (searchText.isEmpty()) {
                    // Show all players
                    for (String player : allPlayers) {
                        listModel.addElement(player);
                    }
                } else {
                    // Filter by search text
                    for (String player : allPlayers) {
                        if (player.toLowerCase().contains(searchText)) {
                            listModel.addElement(player);
                        }
                    }
                }
                
                // Show message if no results
                if (listModel.isEmpty()) {
                    listModel.addElement("Không tìm thấy người chơi");
                }
            }
        });
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void sendInvite(String playerName) {
        try {
            Models.InviteSend inviteMsg = new Models.InviteSend(currentUser, playerName);
            Message msg = Message.of(MessageType.INVITE_SEND, inviteMsg);
            netClient.send(msg);
            
            JOptionPane.showMessageDialog(
                this,
                "Đã gửi lời mời đến " + playerName + "!\nChờ đối thủ chấp nhận...",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            showError("Lỗi khi gửi lời mời: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}