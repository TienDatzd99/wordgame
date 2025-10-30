package com.dat.wordgame.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

public class SwingMainGameView extends JFrame {
    private NetClient netClient;
    private String username;
    private int totalPoints;
    private String currentRoomId;
    private boolean inGame = false;

    // UI Components
    private JLabel userInfoLabel;
    private DefaultListModel<String> playerListModel;
    private JList<String> playerList;
    private DefaultListModel<String> chatListModel;
    private JList<String> chatList;
    private JTextField chatInput;
    private JTextField inviteInput;
    private JTextField guessInput;
    private JLabel wordDisplayLabel;
    private JPanel lettersPanel;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JProgressBar timeProgressBar;

    public SwingMainGameView(NetClient netClient, String username, int totalPoints) {
        this.netClient = netClient;
        this.username = username;
        this.totalPoints = totalPoints;
        
        initializeUI();
        setupNetworkListener();
    }

    private void initializeUI() {
        setTitle("Word Game - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Using default look and feel

        createComponents();
        layoutComponents();
        addEventListeners();
    }

    private void createComponents() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 147, 251));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left panel - Player info and lobby
        JPanel leftPanel = createLeftPanel();
        
        // Center panel - Game area
        JPanel centerPanel = createCenterPanel();
        
        // Right panel - Chat
        JPanel rightPanel = createRightPanel();

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("🌟 Lobby & Người chơi"));

        // User info
        userInfoLabel = new JLabel("<html>👤 " + username + "<br/>💰 Điểm: " + totalPoints + "</html>");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userInfoLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Player list
        JLabel playersTitle = new JLabel("🌟 Người chơi online");
        playersTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        playerListModel = new DefaultListModel<>();
        playerList = new JList<>(playerListModel);
        playerList.setPreferredSize(new Dimension(280, 200));
        JScrollPane playerScrollPane = new JScrollPane(playerList);

        // Invite section
        JLabel inviteTitle = new JLabel("📨 Mời chơi");
        inviteTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        JPanel invitePanel = new JPanel(new FlowLayout());
        inviteInput = new JTextField(15);
        inviteInput.setToolTipText("Nhập tên người chơi");
        JButton inviteButton = new JButton("Mời");
        styleButton(inviteButton, new Color(76, 175, 80));
        
        invitePanel.add(inviteInput);
        invitePanel.add(inviteButton);

        // Action buttons
        JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton refreshButton = new JButton("🔄 Làm mới");
        JButton leaveRoomButton = new JButton("🚪 Thoát phòng");
        
        styleButton(refreshButton, new Color(33, 150, 243));
        styleButton(leaveRoomButton, new Color(244, 67, 54));
        
        actionsPanel.add(refreshButton);
        actionsPanel.add(leaveRoomButton);

        panel.add(userInfoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(playersTitle);
        panel.add(playerScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(inviteTitle);
        panel.add(invitePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(actionsPanel);

        // Event listeners for left panel
        inviteButton.addActionListener(e -> sendInvite());
        refreshButton.addActionListener(e -> requestLobbyUpdate());
        leaveRoomButton.addActionListener(e -> leaveRoom());

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("🎮 Game Arena"));

        // Game title
        JLabel gameTitle = new JLabel("🎮 Word Game Arena", SwingConstants.CENTER);
        gameTitle.setFont(new Font("Arial", Font.BOLD, 28));
        gameTitle.setForeground(new Color(63, 81, 181));

        // Timer and progress
        JPanel timerPanel = new JPanel(new FlowLayout());
        timerLabel = new JLabel("⏰ 00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.RED);
        
        timeProgressBar = new JProgressBar(0, 100);
        timeProgressBar.setValue(100);
        timeProgressBar.setStringPainted(true);
        timeProgressBar.setString("Sẵn sàng");
        timeProgressBar.setPreferredSize(new Dimension(200, 25));
        
        timerPanel.add(timerLabel);
        timerPanel.add(timeProgressBar);

        // Word display
        wordDisplayLabel = new JLabel("Chờ trận đấu...", SwingConstants.CENTER);
        wordDisplayLabel.setFont(new Font("Courier New", Font.BOLD, 36));
        wordDisplayLabel.setForeground(Color.DARK_GRAY);
        wordDisplayLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            new EmptyBorder(20, 20, 20, 20)));
        wordDisplayLabel.setOpaque(true);
        wordDisplayLabel.setBackground(new Color(248, 249, 250));

        // Letters panel
        lettersPanel = new JPanel(new FlowLayout());
        lettersPanel.setPreferredSize(new Dimension(0, 150));
        lettersPanel.setBorder(BorderFactory.createTitledBorder("Chữ cái gợi ý"));

        // Guess input
        JPanel guessPanel = new JPanel(new FlowLayout());
        guessInput = new JTextField(20);
        guessInput.setFont(new Font("Arial", Font.PLAIN, 16));
        guessInput.setToolTipText("Nhập từ đoán...");
        
        JButton guessButton = new JButton("🎯 Đoán");
        styleButton(guessButton, new Color(76, 175, 80));
        
        guessPanel.add(new JLabel("Từ đoán:"));
        guessPanel.add(guessInput);
        guessPanel.add(guessButton);

        // Status
        statusLabel = new JLabel("🎮 Sẵn sàng chơi! Mời bạn bè để bắt đầu.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.BLUE);

        panel.add(gameTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(timerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(wordDisplayLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(lettersPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(guessPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(statusLabel);

        // Event listeners for center panel
        guessButton.addActionListener(e -> submitGuess());
        guessInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitGuess();
                }
            }
        });

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("💬 Trò chuyện"));

        // Chat messages
        chatListModel = new DefaultListModel<>();
        chatList = new JList<>(chatListModel);
        chatList.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane chatScrollPane = new JScrollPane(chatList);
        chatScrollPane.setPreferredSize(new Dimension(280, 400));

        // Chat input
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        chatInput.setToolTipText("Nhập tin nhắn...");
        JButton sendButton = new JButton("📤");
        styleButton(sendButton, new Color(33, 150, 243));
        
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        panel.add(chatScrollPane, BorderLayout.CENTER);
        panel.add(chatInputPanel, BorderLayout.SOUTH);

        // Event listeners for right panel
        sendButton.addActionListener(e -> sendChatMessage());
        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChatMessage();
                }
            }
        });

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void layoutComponents() {
        // Already handled in createComponents()
    }

    private void addEventListeners() {
        // Already handled in create methods
    }

    private void setupNetworkListener() {
        netClient.listen(this::onMessage);
    }

    private void onMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.type) {
                case LOBBY_SNAPSHOT -> updateLobby(message);
                case INVITE_RECEIVE -> handleInviteReceived(message);
                case ROOM_JOINED -> handleRoomJoined(message);
                case ROUND_START -> handleRoundStart(message);
                case ROUND_TICK -> handleRoundTick(message);
                case ROUND_END -> handleRoundEnd(message);
                case GAME_END -> handleGameEnd(message);
                case GUESS_UPDATE -> handleGuessUpdate(message);
                case CHAT -> handleChatMessage(message);
                case ERROR -> handleError(message);
                default -> {}
            }
        });
    }

    // Message handlers
    private void updateLobby(Message message) {
        try {
            Models.LobbySnapshot lobby = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LobbySnapshot.class);
            
            playerListModel.clear();
            for (Models.PlayerBrief player : lobby.online()) {
                String status = player.status().equals("available") ? "🟢" : "🔴";
                playerListModel.addElement(status + " " + player.name() + " (" + player.points() + "pts)");
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi cập nhật lobby: " + e.getMessage());
        }
    }

    private void handleInviteReceived(Message message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) message.payload;
            String from = (String) payload.get("from");
            
            int result = JOptionPane.showConfirmDialog(
                this,
                "Bạn nhận được lời mời từ " + from + "\nBạn có muốn chấp nhận không?",
                "Lời mời chơi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                netClient.send(Message.of(MessageType.INVITE_ACCEPT, Map.of("host", from)));
            } else {
                netClient.send(Message.of(MessageType.INVITE_REJECT, Map.of("host", from)));
            }
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi xử lý lời mời: " + e.getMessage());
        }
    }

    private void handleRoomJoined(Message message) {
        try {
            Models.RoomState room = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomState.class);
            currentRoomId = room.roomId();
            statusLabel.setText("🎉 Đã vào phòng " + currentRoomId + "! Đang chờ bắt đầu trận...");
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi vào phòng: " + e.getMessage());
        }
    }

    private void handleRoundStart(Message message) {
        try {
            Models.RoundStart round = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundStart.class);
            
            inGame = true;
            wordDisplayLabel.setText(round.maskedWord());
            
            // Create letter buttons
            lettersPanel.removeAll();
            for (Character letter : round.shuffledLetters()) {
                JButton letterBtn = new JButton(letter.toString());
                letterBtn.setFont(new Font("Arial", Font.BOLD, 16));
                letterBtn.setPreferredSize(new Dimension(50, 50));
                styleButton(letterBtn, new Color(33, 150, 243));
                letterBtn.addActionListener(e -> {
                    String current = guessInput.getText();
                    guessInput.setText(current + letter);
                });
                lettersPanel.add(letterBtn);
            }
            lettersPanel.revalidate();
            lettersPanel.repaint();
            
            timerLabel.setText("⏰ " + formatTime(round.totalTimeSec()));
            timeProgressBar.setValue(100);
            timeProgressBar.setString("Time: " + round.totalTimeSec() + "s");
            statusLabel.setText("🎮 Round " + round.round() + " bắt đầu! Hãy đoán từ!");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi bắt đầu round: " + e.getMessage());
        }
    }

    private void handleRoundTick(Message message) {
        try {
            Models.RoundTick tick = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundTick.class);
            timerLabel.setText("⏰ " + formatTime(tick.remainSec()));
            timeProgressBar.setString("Time: " + tick.remainSec() + "s");
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi cập nhật thời gian: " + e.getMessage());
        }
    }

    private void handleRoundEnd(Message message) {
        try {
            Models.RoundEnd end = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundEnd.class);
            statusLabel.setText("🏁 Kết thúc round! Từ đúng: " + end.correctWord() + 
                              " | Người thắng: " + (end.winner() != null ? end.winner() : "Không có"));
            
            lettersPanel.removeAll();
            lettersPanel.revalidate();
            lettersPanel.repaint();
            guessInput.setText("");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi kết thúc round: " + e.getMessage());
        }
    }

    private void handleGameEnd(Message message) {
        try {
            Models.GameEnd end = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.GameEnd.class);
            
            JOptionPane.showMessageDialog(
                this,
                "Người thắng: " + (end.winner() != null ? end.winner() : "Hòa") + "\nCảm ơn bạn đã chơi!",
                "Kết thúc trận đấu",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            inGame = false;
            currentRoomId = null;
            wordDisplayLabel.setText("Chờ trận đấu...");
            
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi kết thúc game: " + e.getMessage());
        }
    }

    private void handleGuessUpdate(Message message) {
        try {
            Models.GuessUpdate update = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.GuessUpdate.class);
            statusLabel.setText("✅ " + update.player() + " đoán đúng " + update.correctSlots() + " vị trí!");
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi cập nhật đoán: " + e.getMessage());
        }
    }

    private void handleChatMessage(Message message) {
        try {
            Models.Chat chat = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Chat.class);
            String chatText = "[" + chat.from() + "]: " + chat.text();
            chatListModel.addElement(chatText);
            
            // Auto scroll to bottom
            SwingUtilities.invokeLater(() -> {
                chatList.ensureIndexIsVisible(chatListModel.getSize() - 1);
            });
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi tin nhắn: " + e.getMessage());
        }
    }

    private void handleError(Message message) {
        try {
            Models.Err error = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Err.class);
            statusLabel.setText("❌ " + error.message());
            statusLabel.setForeground(Color.RED);
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi không xác định");
        }
    }

    // Action methods
    private void sendInvite() {
        String target = inviteInput.getText().trim();
        if (!target.isEmpty()) {
            netClient.send(Message.of(MessageType.INVITE_SEND, Map.of("to", target)));
            inviteInput.setText("");
            statusLabel.setText("📨 Đã gửi lời mời cho " + target);
        }
    }

    private void submitGuess() {
        if (inGame && currentRoomId != null) {
            String guess = guessInput.getText().trim();
            if (!guess.isEmpty()) {
                netClient.send(Message.of(MessageType.GUESS_SUBMIT, new Models.GuessSubmit(currentRoomId, guess)));
                guessInput.setText("");
            }
        }
    }

    private void sendChatMessage() {
        if (currentRoomId != null) {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                netClient.send(Message.of(MessageType.CHAT, new Models.Chat(currentRoomId, username, message)));
                chatInput.setText("");
            }
        }
    }

    private void requestLobbyUpdate() {
        statusLabel.setText("🔄 Đang cập nhật danh sách...");
        // The server will automatically send lobby snapshots
    }

    private void leaveRoom() {
        if (currentRoomId != null) {
            currentRoomId = null;
            inGame = false;
            wordDisplayLabel.setText("Chờ trận đấu...");
            lettersPanel.removeAll();
            lettersPanel.revalidate();
            lettersPanel.repaint();
            statusLabel.setText("🚪 Đã thoát phòng");
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}