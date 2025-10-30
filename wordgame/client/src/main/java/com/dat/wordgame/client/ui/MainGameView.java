package com.dat.wordgame.client.ui;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.dat.wordgame.client.NetClient;

import java.util.*;

public class MainGameView {
    private Stage stage;
    private NetClient client;
    private String username;
    
    // UI components  
    private Label targetWordLabel;
    private Label timerLabel;
    private Label scoreLabel;
    private Label roundLabel;
    private Label myProgressLabel;
    private Label opponentProgressLabel;
    private HBox wordSlotsBox;
    private FlowPane lettersBox;
    private TextArea chatArea;
    private Timeline timer;
    
    // Game state
    private String targetWord = "HELLO";
    private String currentInput = "";
    private String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    private int timeLeft = 60;
    private int totalTime = 60; // Total time for current round
    private int score = 0;
    private int round = 0; // Start at 0, will be incremented to 1 in first newWord()
    private int myCorrectLetters = 0;
    private int opponentCorrectLetters = 0;
    
    public MainGameView(Stage stage, NetClient client, String username) {
        this.stage = stage;
        this.client = client;
        this.username = username;
        initialize();
    }
    
    private void initialize() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #af52de);");
        
        // Top panel - Timer, Score, Round
        HBox topPanel = createTopPanel();
        root.setTop(topPanel);
        
        // Game area
        VBox gameArea = new VBox(30);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(30));
        
        // Title
        Label gameTitle = new Label("SẮP XẾP CHỮ CÁI THÀNH TỪ");
        gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gameTitle.setTextFill(Color.WHITE);
        
        // Target word from server
        targetWordLabel = new Label("Từ cần đoán: " + targetWord);
        targetWordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        targetWordLabel.setTextFill(Color.YELLOW);
        targetWordLabel.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 10; -fx-background-radius: 10;");
        
        // Word slots
        Label slotsTitle = new Label("Điền từ vào đây:");
        slotsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        slotsTitle.setTextFill(Color.WHITE);
        
        wordSlotsBox = new HBox(10);
        wordSlotsBox.setAlignment(Pos.CENTER);
        createWordSlots();
        
        // Submit button
        Button submitButton = new Button("XÁC NHẬN TỪ");
        submitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 25;");
        submitButton.setOnAction(e -> checkWord());
        
        // Letters
        Label lettersTitle = new Label("Các chữ cái (click để chọn):");
        lettersTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lettersTitle.setTextFill(Color.WHITE);
        
        lettersBox = new FlowPane(10, 10);
        lettersBox.setAlignment(Pos.CENTER);
        lettersBox.setPrefWrapLength(500);
        createLetterTiles();
        
        gameArea.getChildren().addAll(gameTitle, targetWordLabel, slotsTitle, wordSlotsBox, submitButton, lettersTitle, lettersBox);
        root.setCenter(gameArea);
        
        // Chat area
        VBox chatPanel = new VBox(10);
        chatPanel.setPrefWidth(280);
        chatPanel.setPadding(new Insets(20));
        
        Label chatTitle = new Label("CHAT");
        chatTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        chatTitle.setTextFill(Color.WHITE);
        
        // Chat display area
        chatArea = new TextArea();
        chatArea.setPrefHeight(150);
        chatArea.setEditable(false);
        chatArea.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: black; -fx-font-size: 12;");
        chatArea.setWrapText(true);
        
        // Chat input area
        VBox chatInputArea = new VBox(5);
        Label inputLabel = new Label("Nhập tin nhắn:");
        inputLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        inputLabel.setTextFill(Color.WHITE);
        
        TextField chatInput = new TextField();
        chatInput.setPromptText("Gõ tin nhắn...");
        chatInput.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-font-size: 12;");
        chatInput.setOnAction(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                chatArea.appendText(username + ": " + message + "\n");
                // TODO: Send to server
                chatInput.clear();
            }
        });
        
        Button sendButton = new Button("GỬI");
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 15;");
        sendButton.setOnAction(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                chatArea.appendText(username + ": " + message + "\n");
                // TODO: Send to server
                chatInput.clear();
            }
        });
        
        chatInputArea.getChildren().addAll(inputLabel, chatInput, sendButton);
        chatPanel.getChildren().addAll(chatTitle, chatArea, chatInputArea);
        root.setRight(chatPanel);
        
        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20));
        
        Button leaveRoomButton = new Button("THOÁT PHÒNG");
        leaveRoomButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold;");
        leaveRoomButton.setOnAction(e -> {
            if (timer != null) {
                timer.stop();
            }
            // TODO: Send leave room message to server
            
            try {
                // Close current game and return to lobby
                stage.close();
                
                // Create new lobby window
                Stage lobbyStage = new Stage();
                FXLobbyView lobbyView = new FXLobbyView(lobbyStage, client, username);
                lobbyView.show();
            } catch (Exception ex) {
                // Fallback: Just close the game
                Platform.exit();
            }
        });
        
        Button exitButton = new Button("THOÁT GAME");
        exitButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold;");
        exitButton.setOnAction(e -> {
            if (timer != null) {
                timer.stop();
            }
            Platform.exit();
        });
        
        controls.getChildren().addAll(leaveRoomButton, exitButton);
        root.setBottom(controls);
        
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Word Game - " + username);
        stage.setScene(scene);
        stage.setResizable(true);
        
        // Keyboard support
        scene.setOnKeyPressed(e -> {
            String keyText = e.getText().toUpperCase();
            if (keyText.matches("[A-Z]") && currentInput.length() < targetWord.length()) {
                currentInput += keyText;
                updateWordSlots();
            } else if (e.getCode().toString().equals("BACK_SPACE") && currentInput.length() > 0) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                updateWordSlots();
            } else if (e.getCode().toString().equals("ENTER")) {
                checkWord();
            }
        });
        
        // Initialize first word and start game
        round++; // Start with round 1
        newWord(); // This will start the first round
    }
    
    private void createWordSlots() {
        wordSlotsBox.getChildren().clear();
        for (int i = 0; i < targetWord.length(); i++) {
            TextField slot = new TextField();
            slot.setPrefWidth(50);
            slot.setPrefHeight(50);
            slot.setAlignment(Pos.CENTER);
            slot.setStyle("-fx-background-color: white; -fx-font-size: 20; -fx-font-weight: bold;");
            slot.setEditable(false);
            wordSlotsBox.getChildren().add(slot);
        }
    }
    
    private void createLetterTiles() {
        lettersBox.getChildren().clear();
        
        List<String> shuffledAlphabet = Arrays.asList(alphabet.clone());
        Collections.shuffle(shuffledAlphabet);
        
        for (String letter : shuffledAlphabet) {
            Button letterBtn = new Button(letter);
            letterBtn.setPrefWidth(45);
            letterBtn.setPrefHeight(45);
            letterBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            letterBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 12;");
            
            letterBtn.setOnAction(e -> {
                if (currentInput.length() < targetWord.length()) {
                    currentInput += letter;
                    updateWordSlots();
                }
            });
            
            lettersBox.getChildren().add(letterBtn);
        }
    }
    
    private void updateWordSlots() {
        // Update word slots
        for (int i = 0; i < wordSlotsBox.getChildren().size(); i++) {
            TextField slot = (TextField) wordSlotsBox.getChildren().get(i);
            if (i < currentInput.length()) {
                slot.setText(String.valueOf(currentInput.charAt(i)));
            } else {
                slot.setText("");
            }
        }
        
        // Update my progress
        myCorrectLetters = 0;
        for (int i = 0; i < Math.min(currentInput.length(), targetWord.length()); i++) {
            if (i < currentInput.length() && i < targetWord.length()) {
                if (currentInput.charAt(i) == targetWord.charAt(i)) {
                    myCorrectLetters++;
                }
            }
        }
        myProgressLabel.setText("Tôi: " + myCorrectLetters + "/" + targetWord.length());
    }
    
    private void checkWord() {
        if (currentInput.length() < targetWord.length()) {
            chatArea.appendText("❌ Từ chưa đủ " + targetWord.length() + " chữ cái!\n");
            return;
        }
        
        String word = currentInput.toUpperCase();
        if (word.equals(targetWord.toUpperCase())) {
            // Stop timer and calculate score using new formula
            if (timer != null) {
                timer.stop();
            }
            
            // New scoring system:
            // Base points: +3 for winning round
            // Bonus: (timeLeft/totalTime) × 3
            int basePoints = 3;
            double bonusMultiplier = (double) timeLeft / totalTime;
            double bonus = bonusMultiplier * 3;
            double totalPoints = basePoints + bonus;
            
            score += (int) Math.round(totalPoints);
            scoreLabel.setText(String.valueOf(score));
            
            chatArea.appendText("🎉 Chính xác! +" + String.format("%.1f", totalPoints) + " điểm (còn " + timeLeft + "s)\n");
            chatArea.appendText("   → Cơ bản: +3, Bonus: +" + String.format("%.1f", bonus) + "\n");
            
            // Move to next round
            round++;
            newWord();
        } else {
            chatArea.appendText("❌ Sai rồi! Thử lại nhé.\n");
            currentInput = "";
            updateWordSlots();
        }
    }
    
    // Calculate time limit based on word length
    private int calculateTimeLimit(String word) {
        int length = word.length();
        if (length <= 4) return 10;      // 3-4 letters -> 10s
        else if (length <= 6) return 13; // 5-6 letters -> 13s  
        else if (length <= 8) return 16; // 7-8 letters -> 16s
        else return 19;                  // 9-10 letters -> 19s
    }
    
    // Get word based on round difficulty
    private String getWordByRound(int currentRound) {
        String[] easyWords = {"CAT", "DOG", "HOME", "LOVE"};              // 3-4 letters (Round 1)
        String[] mediumWords = {"HOUSE", "WORLD", "GAMES", "POWER"};      // 5-6 letters (Round 2) 
        String[] hardWords = {"WELCOME", "JOURNEY", "NETWORK"};           // 7-8 letters (Round 3)
        String[] veryHardWords = {"ADVENTURE", "CHALLENGE", "WONDERFUL"}; // 9-10 letters (Round 4+)
        
        if (currentRound == 1) {
            return easyWords[(int)(Math.random() * easyWords.length)];
        } else if (currentRound == 2) {
            return mediumWords[(int)(Math.random() * mediumWords.length)];
        } else if (currentRound == 3) {
            return hardWords[(int)(Math.random() * hardWords.length)];
        } else {
            return veryHardWords[(int)(Math.random() * veryHardWords.length)];
        }
    }
    
    // Get difficulty name for display
    private String getDifficultyName(int currentRound) {
        if (currentRound == 1) return "DỄ";
        else if (currentRound == 2) return "TRUNG BÌNH";
        else if (currentRound == 3) return "KHÓ";
        else return "RẤT KHÓ";
    }
    
    private void newWord() {
        // Get word based on current round difficulty
        targetWord = getWordByRound(round);
        currentInput = "";
        
        // Calculate time based on word length
        totalTime = calculateTimeLimit(targetWord);
        timeLeft = totalTime;
        myCorrectLetters = 0;
        opponentCorrectLetters = (int)(Math.random() * targetWord.length()); // Simulate opponent
        
        // Update UI
        targetWordLabel.setText("Từ cần đoán: " + targetWord);
        roundLabel.setText(String.valueOf(round));
        timerLabel.setText(timeLeft + "s");
        timerLabel.setTextFill(Color.WHITE); // Reset color
        myProgressLabel.setText("Tôi: " + myCorrectLetters + "/" + targetWord.length());
        opponentProgressLabel.setText("Đối thủ: " + opponentCorrectLetters + "/" + targetWord.length());
        
        createWordSlots();
        createLetterTiles();
        
        String difficulty = getDifficultyName(round);
        chatArea.appendText("🎯 Round " + round + " (" + difficulty + ") - " + targetWord.length() + " chữ cái (" + totalTime + "s)\n");
        
        // Start timer for new round
        startTimer();
    }
    
    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }
        
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText(timeLeft + "s");
            
            if (timeLeft <= 10) {
                timerLabel.setTextFill(Color.RED);
            } else {
                timerLabel.setTextFill(Color.ORANGE);
            }
            
            if (timeLeft <= 0) {
                timer.stop();
                chatArea.appendText("⏰ Hết giờ! Từ đúng là: " + targetWord + " (Thua round)\n");
                // Move to next round when time is up (lose round)
                round++;
                newWord();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
    
    private HBox createTopPanel() {
        HBox topPanel = new HBox(50);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(20));
        topPanel.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        
        // Score
        VBox scoreBox = new VBox(5);
        scoreBox.setAlignment(Pos.CENTER);
        Label scoreTitle = new Label("ĐIỂM SỐ");
        scoreTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        scoreTitle.setTextFill(Color.WHITE);
        scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabel.setTextFill(Color.YELLOW);
        scoreBox.getChildren().addAll(scoreTitle, scoreLabel);
        
        // Timer
        VBox timerBox = new VBox(5);
        timerBox.setAlignment(Pos.CENTER);
        Label timerTitle = new Label("THỜI GIAN");
        timerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        timerTitle.setTextFill(Color.WHITE);
        timerLabel = new Label(timeLeft + "s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        timerLabel.setTextFill(Color.ORANGE);
        timerBox.getChildren().addAll(timerTitle, timerLabel);
        
        // Round
        VBox roundBox = new VBox(5);
        roundBox.setAlignment(Pos.CENTER);
        Label roundTitle = new Label("ROUND");
        roundTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roundTitle.setTextFill(Color.WHITE);
        roundLabel = new Label(String.valueOf(round));
        roundLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        roundLabel.setTextFill(Color.LIGHTGREEN);
        roundBox.getChildren().addAll(roundTitle, roundLabel);
        
        // Player Progress
        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        Label progressTitle = new Label("TIẾN ĐỘ");
        progressTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        progressTitle.setTextFill(Color.WHITE);
        
        myProgressLabel = new Label("Tôi: " + myCorrectLetters + "/" + targetWord.length());
        myProgressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        myProgressLabel.setTextFill(Color.LIGHTBLUE);
        
        opponentProgressLabel = new Label("Đối thủ: " + opponentCorrectLetters + "/" + targetWord.length());
        opponentProgressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        opponentProgressLabel.setTextFill(Color.PINK);
        
        progressBox.getChildren().addAll(progressTitle, myProgressLabel, opponentProgressLabel);
        
        topPanel.getChildren().addAll(scoreBox, timerBox, roundBox, progressBox);
        return topPanel;
    }
    
    public void show() {
        stage.show();
    }
}