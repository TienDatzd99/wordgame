package com.dat.wordgame.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.util.Duration;

public class FXGameView {
    private NetClient netClient;
    private String currentUser;
    private List<String> players;
    private String gameId;
    private List<Character> availableLetters;
    private int timeRemaining;
    private Timeline gameTimer;
    private boolean gameActive;
    private int countdownSeconds = 5;
    private Timeline countdownTimer;
    private List<Character> currentGuess = new ArrayList<>();
    private List<Button> answerSlots = new ArrayList<>();
    private FXLobbyView parentLobby; // Tham chiếu đến FXLobbyView

    // UI Components (JavaFX)
    private Label wordLabel;
    private Label timeLabel;
    private Label scoreLabel;
    private Label opponentScoreLabel;
    private GridPane lettersPanel;
    private FlowPane answerSlotsPanel;
    private Label countdownLabel;
    private Button submitButton;
    private TextArea chatArea;
    private TextField chatField;
    private Button sendChatButton;
    private VBox playersPanel; // Đổi thành VBox cho đơn giản
    private Label statusLabel;
    private Label myGuessProgressLabel;
    private Label opponentGuessProgressLabel;

    // Game state
    private int currentScore = 0;
    private int opponentScore = 0;
    private String opponentName = "";
    private int round = 1;
    private String maskedWord = "";
    
    // Root pane
    private BorderPane rootPane;

    // Định nghĩa kiểu cho các ô trả lời
    private final String SLOT_STYLE_EMPTY = "-fx-font-size: 22px !important; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white; -fx-background-color: rgba(255, 255, 255, 0.2); -fx-border-color: rgba(255, 255, 255, 0.7); -fx-border-width: 3; -fx-background-radius: 5; -fx-border-radius: 5; -fx-wrap-text: false; -fx-text-overrun: clip; -fx-alignment: center; -fx-content-display: center;";
    private final String SLOT_STYLE_FILLED = "-fx-font-size: 22px !important; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white; -fx-background-color: rgba(255, 215, 0, 0.8); -fx-border-color: rgba(255, 255, 255, 0.7); -fx-border-width: 3; -fx-background-radius: 5; -fx-border-radius: 5; -fx-wrap-text: false; -fx-text-overrun: clip; -fx-alignment: center; -fx-content-display: center;";
    private final String SLOT_STYLE_FILLED_HOVER = "-fx-font-size: 22px !important; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white; -fx-background-color: rgba(255, 100, 100, 0.8); -fx-border-color: rgba(255, 255, 255, 0.7); -fx-border-width: 3; -fx-background-radius: 5; -fx-border-radius: 5; -fx-wrap-text: false; -fx-text-overrun: clip; -fx-alignment: center; -fx-content-display: center;";
    private final String SLOT_STYLE_CORRECT = "-fx-font-size: 22px !important; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white; -fx-background-color: rgba(46, 204, 113, 0.9); -fx-border-color: rgba(46, 204, 113, 1); -fx-border-width: 3; -fx-background-radius: 5; -fx-border-radius: 5; -fx-wrap-text: false; -fx-text-overrun: clip; -fx-alignment: center; -fx-content-display: center;";
    private final String SLOT_STYLE_INCORRECT = "-fx-font-size: 22px !important; -fx-font-weight: bold; -fx-font-family: 'Arial'; -fx-text-fill: white; -fx-background-color: rgba(231, 76, 60, 0.8); -fx-border-color: rgba(231, 76, 60, 1); -fx-border-width: 3; -fx-background-radius: 5; -fx-border-radius: 5; -fx-wrap-text: false; -fx-text-overrun: clip; -fx-alignment: center; -fx-content-display: center;";


    public FXGameView(NetClient netClient, String username, List<String> players, String gameId, FXLobbyView parentLobby) {
        this.netClient = netClient;
        this.currentUser = username;
        this.players = players;
        this.gameId = gameId;
        this.availableLetters = new ArrayList<>();
        this.parentLobby = parentLobby;

        for (String player : players) {
            if (!player.equals(username)) {
                this.opponentName = player;
                break;
            }
        }

        this.rootPane = new BorderPane();
        this.rootPane.getStyleClass().add("game-view-root");
        initializeUI();
        setupEventHandlers();
        setupGameTimer();
        
        // Bắt đầu phát nhạc nền khi vào game
        SoundPlayer.playBackgroundMusic();
        
        System.out.println("FXGameView created, waiting for ROUND_START...");
    }
    
    /**
     * Trả về node gốc của view này để hiển thị trong Scene.
     */
    public Parent getView() {
        return rootPane;
    }

    public void onMessage(Message message) {
        // Luôn cập nhật UI trên luồng JavaFX
        Platform.runLater(() -> handleMessage(message));
    }

    private void startCountdown() {
        gameActive = false;
        submitButton.setDisable(true);
        countdownSeconds = 5;
        countdownLabel.setText("Bắt đầu sau: 5s");
        countdownLabel.setVisible(true);
        lettersPanel.setVisible(false);
        answerSlotsPanel.setVisible(false);

        countdownTimer = new Timeline();
        countdownTimer.setCycleCount(countdownSeconds);
        countdownTimer.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            countdownSeconds--;
            countdownLabel.setText("Bắt đầu sau: " + countdownSeconds + "s");
        }));

        countdownTimer.setOnFinished(e -> {
            countdownLabel.setVisible(false);
            lettersPanel.setVisible(true);
            answerSlotsPanel.setVisible(true);
            gameActive = true;
            submitButton.setDisable(false);
            gameTimer.play(); // Bắt đầu đếm giờ game
            statusLabel.setText("🎯 Game đã bắt đầu! Hãy tìm từ!");
        });

        countdownTimer.play();
    }

    private void initializeUI() {
        // Gradient background
        rootPane.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #5856D6, #8559D7);");
        rootPane.setPadding(new Insets(30, 30, 30, 30));

        // Header panel
        Node headerPanel = createHeaderPanel();
        rootPane.setTop(headerPanel);
        BorderPane.setMargin(headerPanel, new Insets(0, 0, 20, 0));

        // Center panel - Game area
        Node centerPanel = createGamePanel();
        rootPane.setCenter(centerPanel);

        // Right panel - Players and Chat
        Node rightPanel = createRightPanel();
        rootPane.setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 20));

        // Bottom panel - Status and controls
        Node bottomPanel = createBottomPanel();
        rootPane.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(20, 0, 0, 0));
    }

    private Node createHeaderPanel() {
        BorderPane panel = new BorderPane();
        panel.setBackground(Background.EMPTY);

        // Game info
        Label titleLabel = new Label("🎮 WordGame - Vòng " + round);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        // Timer and score
        timeLabel = new Label("⏰ Thời gian: --:--");
        timeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        timeLabel.setTextFill(Color.YELLOW);

        scoreLabel = new Label("🏆 Tôi: " + currentScore);
        scoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        scoreLabel.setTextFill(Color.WHITE);

        opponentScoreLabel = new Label("🎯 " + (opponentName.isEmpty() ? "Đối thủ" : opponentName) + ": " + opponentScore);
        opponentScoreLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        opponentScoreLabel.setTextFill(Color.LIGHTPINK);

        HBox infoPanel = new HBox(15, timeLabel, scoreLabel, opponentScoreLabel);
        infoPanel.setAlignment(Pos.CENTER_RIGHT);
        infoPanel.setBackground(Background.EMPTY);

        panel.setLeft(titleLabel);
        panel.setRight(infoPanel);

        return panel;
    }

    private Node createGamePanel() {
        BorderPane container = new BorderPane();
        container.setBackground(Background.EMPTY);

        // Glass panel with glassmorphism effect
        VBox glassPanel = new VBox(30);
        glassPanel.setBackground(Background.EMPTY);
        glassPanel.setPadding(new Insets(40, 40, 40, 40));
        glassPanel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.25);" +
                "-fx-background-radius: 30;" +
                "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                "-fx-border-radius: 30;" +
                "-fx-border-width: 2;"
        );

        // Word display area
        Node wordPanel = createWordPanel();

        // Center area with countdown, answer slots, and letters
        VBox centerArea = new VBox(20);
        centerArea.setBackground(Background.EMPTY);
        centerArea.setAlignment(Pos.CENTER);

        // Answer slots panel
        answerSlotsPanel = new FlowPane(10, 10);
        answerSlotsPanel.setBackground(Background.EMPTY);
        answerSlotsPanel.setAlignment(Pos.CENTER);

        // Countdown label
        countdownLabel = new Label("Bắt đầu sau: 5s");
        countdownLabel.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        countdownLabel.setStyle("-fx-font-size: 72px !important; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        countdownLabel.setTextFill(Color.GOLD);
        countdownLabel.setAlignment(Pos.CENTER);

        // Letters panel (26 letters A-Z)
        lettersPanel = createLettersPanel();

        // Stack countdown on top of letters
        StackPane gameAreaPanel = new StackPane();
        gameAreaPanel.setBackground(Background.EMPTY);
        gameAreaPanel.getChildren().addAll(lettersPanel, countdownLabel);
        StackPane.setAlignment(countdownLabel, Pos.CENTER);
        StackPane.setAlignment(lettersPanel, Pos.CENTER);
        
        centerArea.getChildren().addAll(answerSlotsPanel, gameAreaPanel);

        // Buttons panel
        Node buttonsPanel = createGuessPanel();

        glassPanel.getChildren().addAll(wordPanel, centerArea, buttonsPanel);
        container.setCenter(glassPanel);
        return container;
    }

    private Node createWordPanel() {
        BorderPane panel = new BorderPane();
        panel.setBackground(Background.EMPTY);
        panel.setPadding(new Insets(0, 0, 20, 0));

        wordLabel = new Label(maskedWord);
        wordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 56));
        wordLabel.setStyle("-fx-font-size: 56px !important; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        wordLabel.setTextFill(Color.WHITE);
        wordLabel.setAlignment(Pos.CENTER);

        panel.setCenter(wordLabel);
        BorderPane.setAlignment(wordLabel, Pos.CENTER);
        return panel;
    }

    private GridPane createLettersPanel() {
        GridPane panel = new GridPane();
        panel.setBackground(Background.EMPTY);
        panel.setHgap(10);
        panel.setVgap(10);
        panel.setPadding(new Insets(20, 20, 20, 20));
        panel.setAlignment(Pos.CENTER);

        for (char c = 'A'; c <= 'Z'; c++) {
            Button letterBtn = createLetterButton(c);
            final char letter = c;
            letterBtn.setOnAction(e -> {
                if (gameActive && currentGuess.size() < maskedWord.length()) {
                    currentGuess.add(letter);
                    updateAnswerSlots();
                }
            });

            int row = (c - 'A') < 13 ? 0 : 1;
            int col = (c - 'A') % 13;
            panel.add(letterBtn, col, row);
        }
        return panel;
    }

    private void updateLettersPanel() {
        // Chỉ cần đảm bảo nó hiển thị (countdown sẽ ẩn/hiện)
        lettersPanel.setVisible(true);
    }

    private void createAnswerSlots(int wordLength) {
        answerSlotsPanel.getChildren().clear();
        answerSlots.clear();
        currentGuess.clear();

        for (int i = 0; i < wordLength; i++) {
            Button slotBtn = createEmptySlot(i);
            answerSlots.add(slotBtn);
            answerSlotsPanel.getChildren().add(slotBtn);
        }
    }

    private Button createEmptySlot(int index) {
        Button btn = new Button("");
        btn.setPrefSize(60, 60);
        btn.setMinSize(60, 60);
        btn.setMaxSize(60, 60);
        btn.setCursor(Cursor.HAND);
        btn.setWrapText(false); // Không wrap text xuống dòng 2
        btn.setStyle(SLOT_STYLE_EMPTY);

        // Click to remove letter
        btn.setOnAction(e -> {
            if (gameActive && index < currentGuess.size()) {
                currentGuess.remove(index);
                updateAnswerSlots();
            }
        });

        // Hover effect
        btn.setOnMouseEntered(e -> {
            if (!btn.getText().isEmpty()) {
                btn.setStyle(SLOT_STYLE_FILLED_HOVER);
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getText().isEmpty()) {
                btn.setStyle(SLOT_STYLE_FILLED);
            } else {
                btn.setStyle(SLOT_STYLE_EMPTY);
            }
        });

        return btn;
    }

    private void updateAnswerSlots() {
        for (int i = 0; i < answerSlots.size(); i++) {
            Button slot = answerSlots.get(i);
            if (i < currentGuess.size()) {
                slot.setText(currentGuess.get(i).toString());
                slot.setStyle(SLOT_STYLE_FILLED);
            } else {
                slot.setText("");
                slot.setStyle(SLOT_STYLE_EMPTY);
            }
        }
    }

    private Button createLetterButton(Character letter) {
        Button btn = new Button(letter.toString().toUpperCase());
        btn.setPrefSize(60, 60); // Điều chỉnh kích thước
        btn.setCursor(Cursor.HAND);

        String baseStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(88, 86, 214, 0.7); -fx-background-radius: 10;";
        String hoverStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(108, 106, 234, 0.9); -fx-background-radius: 10;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));

        return btn;
    }

    private Node createGuessPanel() {
        HBox panel = new HBox(20);
        panel.setBackground(Background.EMPTY);
        panel.setAlignment(Pos.CENTER);

        submitButton = createStyledButton("✓ Gửi câu trả lời", Color.rgb(46, 204, 113), 220, 50);
        Button clearButton = createStyledButton("✗ Xóa", Color.rgb(231, 76, 60), 220, 50);

        submitButton.setOnAction(e -> submitGuess());
        clearButton.setOnAction(e -> clearGuess());

        panel.getChildren().addAll(submitButton, clearButton);
        return panel;
    }

    private Button createStyledButton(String text, Color bgColor, double prefWidth, double prefHeight) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
        btn.setPrefSize(prefWidth, prefHeight);
        btn.setCursor(Cursor.HAND);

        String baseColor = toWebColor(bgColor);
        String hoverColor = toWebColor(bgColor.brighter());

        String baseStyle = String.format("-fx-background-color: %s; -fx-background-radius: 10;", baseColor);
        String hoverStyle = String.format("-fx-background-color: %s; -fx-background-radius: 10;", hoverColor);

        btn.setStyle(baseStyle + "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle + "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle + "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;"));

        return btn;
    }
    
    private String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    private void clearGuess() {
        currentGuess.clear();
        updateAnswerSlots();
    }

    private Node createRightPanel() {
        BorderPane panel = new BorderPane();
        panel.setPrefWidth(300);
        panel.setBackground(Background.EMPTY);

        Node playersPanelNode = createPlayersPanel();
        panel.setTop(playersPanelNode);
        BorderPane.setMargin(playersPanelNode, new Insets(0, 0, 10, 0));
        
        Node chatPanelNode = createChatPanel();
        panel.setCenter(chatPanelNode);

        return panel;
    }

    private Node createPlayersPanel() {
        // Dùng VBox thay vì JPanel
        playersPanel = new VBox(10);
        playersPanel.setStyle("-fx-background-color: white; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        playersPanel.setPadding(new Insets(15, 15, 15, 15));
        playersPanel.setPrefHeight(180); // Cố định chiều cao

        Label titleLabel = new Label("👥 Người chơi");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.rgb(88, 86, 214));

        VBox playersListPanel = new VBox(5);
        playersListPanel.setBackground(Background.EMPTY);

        for (String player : players) {
            BorderPane playerRow = new BorderPane();
            playerRow.setBackground(Background.EMPTY);
            playerRow.setPadding(new Insets(5, 0, 5, 0));

            Label playerLabel = new Label("• " + player + (player.equals(currentUser) ? " (Bạn)" : ""));
            playerLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            playerLabel.setTextFill(player.equals(currentUser) ? Color.rgb(46, 204, 113) : Color.BLACK);

            Label progressLabel = new Label("0 ô đúng");
            progressLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            progressLabel.setTextFill(Color.rgb(150, 150, 150));

            if (player.equals(currentUser)) {
                myGuessProgressLabel = progressLabel;
            } else {
                opponentGuessProgressLabel = progressLabel;
            }

            playerRow.setLeft(playerLabel);
            playerRow.setRight(progressLabel);
            playersListPanel.getChildren().add(playerRow);
        }

        playersPanel.getChildren().addAll(titleLabel, playersListPanel);
        return playersPanel;
    }

    private Node createChatPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: white; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        panel.setPadding(new Insets(15, 15, 15, 15));
        
        Label titleLabel = new Label("💬 Trò chuyện");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.rgb(88, 86, 214));
        panel.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 10, 0));

        chatArea = new TextArea();
        chatArea.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        
        ScrollPane chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(200);
        panel.setCenter(chatScrollPane);
        BorderPane.setMargin(chatScrollPane, new Insets(0, 0, 10, 0));

        HBox chatInputPanel = new HBox(5);
        chatInputPanel.setBackground(Background.EMPTY);
        
        chatField = new TextField();
        chatField.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        HBox.setHgrow(chatField, Priority.ALWAYS); // Cho phép text field mở rộng

        sendChatButton = createStyledButton("Gửi", Color.rgb(52, 152, 219), 60, 30);
        // Giảm font cho nút nhỏ
        sendChatButton.setStyle(sendChatButton.getStyle().replace("-fx-font-size: 18px", "-fx-font-size: 12px"));


        chatInputPanel.getChildren().addAll(chatField, sendChatButton);
        panel.setBottom(chatInputPanel);

        return panel;
    }

    private Node createBottomPanel() {
        BorderPane panel = new BorderPane();
        panel.setBackground(Background.EMPTY);

        statusLabel = new Label("Đang chờ bắt đầu game...");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        statusLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);
        panel.setCenter(statusLabel);

        HBox buttonPanel = new HBox(10);
        buttonPanel.setBackground(Background.EMPTY);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);

        Button inviteButton = createModernButton("👥 Mời bạn bè", Color.rgb(34, 197, 94));
        inviteButton.setOnAction(e -> showInviteFriendsDialog());

        Button exitButton = createModernButton("Thoát game", Color.rgb(231, 76, 60));
        exitButton.setOnAction(e -> handleSurrender());

        buttonPanel.getChildren().addAll(inviteButton, exitButton);
        panel.setRight(buttonPanel);

        return panel;
    }

    private Button createModernButton(String text, Color backgroundColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setTextFill(Color.WHITE);
        button.setPadding(new Insets(8, 16, 8, 16));
        button.setCursor(Cursor.HAND);

        String baseColor = toWebColor(backgroundColor);
        String hoverColor = toWebColor(backgroundColor.darker());

        String baseStyle = String.format("-fx-background-color: %s; -fx-background-radius: 5;", baseColor);
        String hoverStyle = String.format("-fx-background-color: %s; -fx-background-radius: 5;", hoverColor);

        button.setStyle(baseStyle + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;"));
        button.setOnMouseExited(e -> button.setStyle(baseStyle + "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;"));

        return button;
    }

    private void setupEventHandlers() {
        if (sendChatButton != null) {
            sendChatButton.setOnAction(e -> sendChat());
        }
        if (chatField != null) {
            chatField.setOnAction(e -> sendChat());
        }
    }

    private void setupGameTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (timeRemaining > 0) {
                timeRemaining--;
                updateTimeDisplay();
            } else {
                gameTimer.stop();
                statusLabel.setText("⏰ Hết giờ!");
                submitButton.setDisable(true);
            }
        }));
        gameTimer.setCycleCount(Timeline.INDEFINITE);
    }

    private void updateTimeDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timeLabel.setText(String.format("⏰ Thời gian: %02d:%02d", minutes, seconds));
    }

    // handleMessage được gọi bởi onMessage (đã bọc trong Platform.runLater)
    private void handleMessage(Message message) {
        switch (message.type) {
            case ROUND_START -> handleRoundStart(message);
            case ROUND_TICK -> handleRoundTick(message);
            case ROUND_END -> handleRoundEnd(message);
            case GAME_END -> handleGameEnd(message);
            case GUESS_UPDATE -> handleGuessUpdate(message);
            case CHAT -> handleChat(message);
            default -> System.out.println("Unhandled message type: " + message.type);
        }
    }

    private void handleRoundStart(Message message) {
        try {
            var roundStart = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoundStart.class);
            this.gameId = roundStart.roomId();
            this.round = roundStart.round();
            this.maskedWord = roundStart.maskedWord();
            this.availableLetters = roundStart.shuffledLetters();
            this.timeRemaining = roundStart.totalTimeSec();

            // Cập nhật tiêu đề (nếu GameView quản lý Stage)
            // ((Stage) rootPane.getScene().getWindow()).setTitle("WordleCup - Vòng " + round);
            
            wordLabel.setText(maskedWord.toUpperCase());
            createAnswerSlots(maskedWord.length());
            updateLettersPanel();
            updateTimeDisplay();
            resetGuessProgress();
            
            statusLabel.setText("🎯 Vòng " + round + " - Tìm từ có " + maskedWord.length() + " chữ cái!");
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
            submitButton.setDisable(true);

            String winner = roundEnd.winner();
            String correctWord = roundEnd.correctWord();
            int points = roundEnd.totalAward();

            if (winner != null && winner.equals(currentUser)) {
                currentScore += points;
                scoreLabel.setText("🏆 Tôi: " + currentScore);
                statusLabel.setText("🎉 Bạn thắng vòng này! Từ đúng: " + correctWord + " (+" + points + " điểm)");
            } else if (winner != null) {
                opponentScore += points;
                opponentScoreLabel.setText("🎯 " + opponentName + ": " + opponentScore);
                statusLabel.setText("😔 Vòng này thắng: " + winner + ". Từ đúng: " + correctWord + " (+" + points + " điểm)");
            } else {
                statusLabel.setText("⏱️ Hết giờ! Từ đúng: " + correctWord);
            }
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

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kết thúc Game");
            alert.setHeaderText("🏁 Game kết thúc!");
            alert.setContentText("🏆 Người thắng: " + winner + "\n" +
                                 "📊 Xem chi tiết trong bảng xếp hạng");
            alert.showAndWait();

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

            if (player.equals(currentUser)) {
                if (correctSlots == maskedWord.length()) {
                    showCorrectFeedback();
                    SoundPlayer.playSuccess(); // Phát âm thanh success
                } else {
                    showIncorrectFeedback();
                    SoundPlayer.playWrong(); // Phát âm thanh wrong
                    clearGuess();
                    statusLabel.setText("❌ Sai rồi! Thử lại! (" + correctSlots + "/" + maskedWord.length() + " đúng vị trí)");
                }
                
                if (myGuessProgressLabel != null) {
                    myGuessProgressLabel.setText(correctSlots + "/" + maskedWord.length() + " đúng vị trí");
                    myGuessProgressLabel.setTextFill(correctSlots == maskedWord.length() ? 
                        Color.rgb(46, 204, 113) : Color.rgb(230, 126, 34));
                }
            } else {
                if (opponentGuessProgressLabel != null) {
                    opponentGuessProgressLabel.setText(correctSlots + "/" + maskedWord.length() + " đúng vị trí");
                    opponentGuessProgressLabel.setTextFill(correctSlots == maskedWord.length() ? 
                        Color.rgb(46, 204, 113) : Color.rgb(230, 126, 34));
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

            if (from.contains("System") && text.contains("đầu hàng")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("🏆 Chiến thắng!");
                alert.setHeaderText(text);
                alert.setContentText("Bạn sẽ được chuyển về lobby sau khi nhấn OK.");
                alert.showAndWait();
                
                // Dùng PauseTransition thay vì Timer
                PauseTransition returnTimer = new PauseTransition(Duration.millis(1000));
                returnTimer.setOnFinished(e -> returnToLobby());
                returnTimer.play();
            }
        } catch (Exception e) {
            showError("Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }

    private void resetGuessProgress() {
        if (myGuessProgressLabel != null) {
            myGuessProgressLabel.setText("0/" + maskedWord.length() + " đúng vị trí");
            myGuessProgressLabel.setTextFill(Color.rgb(150, 150, 150));
        }
        if (opponentGuessProgressLabel != null) {
            opponentGuessProgressLabel.setText("0/" + maskedWord.length() + " đúng vị trí");
            opponentGuessProgressLabel.setTextFill(Color.rgb(150, 150, 150));
        }
    }

    private void submitGuess() {
        if (!gameActive) return;

        StringBuilder sb = new StringBuilder();
        for (Character c : currentGuess) {
            sb.append(c);
        }
        String guess = sb.toString().trim().toUpperCase();

        if (guess.isEmpty()) {
            showError("Vui lòng nhập từ đoán!");
            return;
        }

        if (guess.length() != maskedWord.length()) {
            showError("Từ phải có " + maskedWord.length() + " chữ cái!");
            showIncorrectFeedback();
            return;
        }

        Models.GuessSubmit guessSubmit = new Models.GuessSubmit(gameId, guess);
        Message message = Message.of(MessageType.GUESS_SUBMIT, guessSubmit);

        try {
            netClient.send(message);
            statusLabel.setText("📤 Đang kiểm tra: " + guess + "...");
        } catch (Exception e) {
            showError("Lỗi gửi đáp án: " + e.getMessage());
        }
    }

    private void showIncorrectFeedback() {
        for (Button slot : answerSlots) {
            slot.setStyle(SLOT_STYLE_INCORRECT);
        }
        // Reset sau 1 giây
        PauseTransition timer = new PauseTransition(Duration.seconds(1));
        timer.setOnFinished(e -> resetSlotStyles());
        timer.play();
    }

    private void showCorrectFeedback() {
        for (Button slot : answerSlots) {
            slot.setStyle(SLOT_STYLE_CORRECT);
        }
        statusLabel.setText("✅ Chính xác! Đợi đối thủ...");
    }

    private void resetSlotStyles() {
        // Cập nhật lại kiểu dựa trên trạng thái (filled/empty)
        updateAnswerSlots();
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
        chatArea.appendText(message + "\n");
        chatArea.setScrollTop(Double.MAX_VALUE); // Cuộn xuống cuối
    }

    private void returnToLobby() {
        try {
            // Dừng nhạc nền khi thoát game
            SoundPlayer.stopBackgroundMusic();
            
            if (parentLobby != null) {
                // Quay về FXLobbyView
                parentLobby.returnFromGame(); 
            }
        } catch (Exception e) {
            showError("Lỗi quay về lobby: " + e.getMessage());
        }
    }

    private void handleSurrender() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText("Bạn có chắc muốn thoát game?");
        alert.setContentText("Bạn sẽ thua trận này!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Models.Surrender surrender = new Models.Surrender(gameId, currentUser);
                Message msg = Message.of(MessageType.SURRENDER, surrender);
                netClient.send(msg);

                System.out.println("[FXGameView] Sent SURRENDER message");

                PauseTransition delayTimer = new PauseTransition(Duration.millis(1500));
                delayTimer.setOnFinished(e -> returnToLobby());
                delayTimer.play();

            } catch (Exception e) {
                showError("Lỗi khi thoát game: " + e.getMessage());
            }
        }
    }

    private void showInviteFriendsDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mời bạn bè");
        dialog.setHeaderText("🎮 Mời bạn bè chơi");
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // Lấy stage gốc để gán chủ nhân
        if (rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            dialog.initOwner(rootPane.getScene().getWindow());
        }

        // Main panel
        BorderPane mainPanel = new BorderPane();
        mainPanel.setPadding(new Insets(20));
        mainPanel.setPrefSize(400, 500);
        mainPanel.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #5856D6, #8559D7);");

        // Search panel
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm kiếm người chơi...");
        searchField.setStyle("-fx-font-size: 14px; -fx-background-radius: 20; -fx-padding: 8 12 8 12;");
        BorderPane.setMargin(searchField, new Insets(10, 0, 10, 0));
        
        // Players list
        ListView<String> playersList = new ListView<>();
        ObservableList<String> listModel = FXCollections.observableArrayList();
        final List<String> allPlayers = new ArrayList<>();

        if (parentLobby != null) {
            allPlayers.addAll(parentLobby.getOnlinePlayers());
            listModel.addAll(allPlayers);
        } else {
            listModel.add("Không thể tải danh sách");
        }
        playersList.setItems(listModel);

        // Custom cell renderer
        playersList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText("🟢 " + item);
                    setTextFill(Color.BLACK);
                    setFont(Font.font("Arial", 14));
                    setPadding(new Insets(8, 10, 8, 10));
                }
            }
        });
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String searchText = newText.toLowerCase().trim();
            listModel.clear();
            if (searchText.isEmpty()) {
                listModel.addAll(allPlayers);
            } else {
                for (String player : allPlayers) {
                    if (player.toLowerCase().contains(searchText)) {
                        listModel.add(player);
                    }
                }
            }
            if (listModel.isEmpty()) {
                listModel.add("Không tìm thấy người chơi");
            }
        });
        
        VBox centerBox = new VBox(10, searchField, playersList);
        mainPanel.setCenter(centerBox);
        
        dialog.getDialogPane().setContent(mainPanel);

        // Buttons
        ButtonType inviteButtonType = new ButtonType("✉️ Gửi lời mời", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(inviteButtonType, ButtonType.CANCEL);
        
        // Tùy chỉnh nút trong DialogPane
        Node inviteBtn = dialog.getDialogPane().lookupButton(inviteButtonType);
        inviteBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold;");
        Node cancelBtn = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");

        // Validate on invite button click
        inviteBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String selectedPlayer = playersList.getSelectionModel().getSelectedItem();
            if (selectedPlayer == null || selectedPlayer.contains("Không")) {
                showError("Vui lòng chọn một người chơi!");
                event.consume(); // Ngăn dialog đóng
            } else {
                selectedPlayer = selectedPlayer.replace("🟢 ", "").trim();
                if (selectedPlayer.equals(currentUser)) {
                    showError("Bạn không thể mời chính mình!");
                    event.consume(); // Ngăn dialog đóng
                } else {
                    sendInvite(selectedPlayer);
                    // Không consume, dialog sẽ tự đóng
                }
            }
        });

        dialog.showAndWait();
    }
    
    private void sendInvite(String playerName) {
        try {
            Models.InviteSend inviteMsg = new Models.InviteSend(currentUser, playerName);
            Message msg = Message.of(MessageType.INVITE_SEND, inviteMsg);
            netClient.send(msg);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã gửi lời mời đến " + playerName + "!\nChờ đối thủ chấp nhận...");
            alert.showAndWait();

        } catch (Exception ex) {
            showError("Lỗi khi gửi lời mời: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}