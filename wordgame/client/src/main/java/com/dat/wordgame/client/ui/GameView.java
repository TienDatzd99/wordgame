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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    
    // UI Components
    private JLabel wordLabel;
    private JLabel timeLabel;
    private JLabel scoreLabel;
    private JPanel lettersPanel;
    private JTextField guessField;
    private JButton submitButton;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendChatButton;
    private JPanel playersPanel;
    private JLabel statusLabel;
    
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
        
        initializeUI();
        setupEventHandlers();
        setupGameTimer();
        startListening();
    }

    private void initializeUI() {
        setTitle("WordGame - Trò chơi đang diễn ra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Create gradient background
        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center panel - Game area
        JPanel centerPanel = createGamePanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - Chat and players
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        // Word display area
        JPanel wordPanel = createWordPanel();
        panel.add(wordPanel, BorderLayout.NORTH);
        
        // Letters area
        lettersPanel = createLettersPanel();
        panel.add(lettersPanel, BorderLayout.CENTER);
        
        // Guess input area
        JPanel guessPanel = createGuessPanel();
        panel.add(guessPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel label = new JLabel("Từ cần đoán:", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(88, 86, 214));
        
        wordLabel = new JLabel(maskedWord, SwingConstants.CENTER);
        wordLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        wordLabel.setForeground(new Color(44, 62, 80));
        wordLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(88, 86, 214), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(wordLabel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createLettersPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 5, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(),
            "🔤 Các chữ cái có sẵn",
            0, 0, new Font("Segoe UI", Font.BOLD, 16), new Color(88, 86, 214)
        ));
        
        return panel;
    }

    private JPanel createGuessPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel label = new JLabel("Nhập từ đoán:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(88, 86, 214));
        
        guessField = new JTextField(20);
        guessField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        guessField.setPreferredSize(new Dimension(250, 35));
        
        submitButton = createModernButton("Gửi đáp án", new Color(46, 204, 113));
        
        panel.add(label);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(guessField);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(submitButton);
        
        return panel;
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
        panel.setPreferredSize(new Dimension(280, 150));
        
        JLabel titleLabel = new JLabel("👥 Người chơi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(88, 86, 214));
        
        JPanel playersListPanel = new JPanel();
        playersListPanel.setLayout(new BoxLayout(playersListPanel, BoxLayout.Y_AXIS));
        playersListPanel.setOpaque(false);
        
        for (String player : players) {
            JLabel playerLabel = new JLabel("• " + player + (player.equals(currentUser) ? " (Bạn)" : ""));
            playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            playerLabel.setForeground(player.equals(currentUser) ? new Color(46, 204, 113) : Color.BLACK);
            playersListPanel.add(playerLabel);
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
        
        JButton exitButton = createModernButton("Thoát game", new Color(231, 76, 60));
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(exitButton, BorderLayout.EAST);
        
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
        // Submit guess button
        submitButton.addActionListener(e -> submitGuess());
        
        // Enter key in guess field
        guessField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitGuess();
                }
            }
        });
        
        // Send chat button
        sendChatButton.addActionListener(e -> sendChat());
        
        // Enter key in chat field
        chatField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChat();
                }
            }
        });
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
                    guessField.setEnabled(false);
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
            var roundStart = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundStart.class);
            
            this.gameId = roundStart.roomId();
            this.round = roundStart.round();
            this.maskedWord = roundStart.maskedWord();
            this.availableLetters = roundStart.shuffledLetters();
            this.timeRemaining = roundStart.totalTimeSec();
            
            // Update UI
            setTitle("WordGame - Vòng " + round);
            wordLabel.setText(maskedWord);
            updateAvailableLetters();
            updateTimeDisplay();
            
            // Enable game controls
            gameActive = true;
            submitButton.setEnabled(true);
            guessField.setEnabled(true);
            guessField.setText("");
            guessField.requestFocus();
            
            statusLabel.setText("🎯 Đoán từ để ghi điểm!");
            
            // Start timer
            gameTimer.start();
            
        } catch (Exception e) {
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
            guessField.setEnabled(false);
            
            String winner = roundEnd.winner();
            String correctWord = roundEnd.correctWord();
            int points = roundEnd.totalAward();
            
            if (winner.equals(currentUser)) {
                currentScore += points;
                scoreLabel.setText("🏆 Điểm: " + currentScore);
                statusLabel.setText("🎉 Bạn thắng vòng này! Từ đúng: " + correctWord + " (+"+points+" điểm)");
            } else {
                statusLabel.setText("😔 Vòng này thắng: " + winner + ". Từ đúng: " + correctWord);
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
            
            addChatMessage("📝 " + player + " đã đoán và có " + correctSlots + " chữ đúng vị trí!");
            
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
                guessField.setText(guessField.getText() + letter);
                guessField.requestFocus();
            });
            
            lettersPanel.add(letterButton);
        }
        
        lettersPanel.revalidate();
        lettersPanel.repaint();
    }

    private void submitGuess() {
        if (!gameActive) return;
        
        String guess = guessField.getText().trim().toUpperCase();
        if (guess.isEmpty()) {
            showError("Vui lòng nhập từ đoán!");
            return;
        }
        
        // Send guess to server
        Models.GuessSubmit guessSubmit = new Models.GuessSubmit(gameId, guess);
        Message message = Message.of(MessageType.GUESS_SUBMIT, guessSubmit);
        
        try {
            netClient.send(message);
            guessField.setText("");
            statusLabel.setText("📤 Đã gửi đáp án: " + guess);
        } catch (Exception e) {
            showError("Lỗi gửi đáp án: " + e.getMessage());
        }
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
            LobbyView lobbyView = new LobbyView(netClient, currentUser);
            lobbyView.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            showError("Lỗi quay về lobby: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}