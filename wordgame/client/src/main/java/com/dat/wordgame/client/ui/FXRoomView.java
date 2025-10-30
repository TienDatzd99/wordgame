package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FXRoomView {
    private Stage stage;
    private NetClient client;
    private String username;
    private String roomName;
    private ListView<String> playerList;
    private TextArea chatArea;
    private TextField chatInput;
    private Button startGameBtn;
    private Label roomStatusLabel;
    
    public FXRoomView(Stage stage, NetClient client, String username, String roomName) {
        this.stage = stage;
        this.client = client;
        this.username = username;
        this.roomName = roomName;
        initialize();
    }
    
    private void initialize() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Left - Players
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);
        
        // Center - Chat
        VBox centerPanel = createCenterPanel();
        root.setCenter(centerPanel);
        
        // Right - Game Settings
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);
        
        // Bottom - Controls
        HBox controls = createControls();
        root.setBottom(controls);
        
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Word Game - " + roomName);
        stage.setScene(scene);
        
        loadPlayers();
        setupChat();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25));
        header.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        
        Label titleLabel = new Label("🏠 " + roomName);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        roomStatusLabel = new Label("Đang chờ người chơi...");
        roomStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        roomStatusLabel.setTextFill(Color.LIGHTBLUE);
        
        header.getChildren().addAll(titleLabel, roomStatusLabel);
        return header;
    }
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setPrefWidth(200);
        leftPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05);");
        
        Label playerTitle = new Label("👥 NGƯỜI CHƠI");
        playerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerTitle.setTextFill(Color.WHITE);
        
        playerList = new ListView<>();
        playerList.setPrefHeight(350);
        playerList.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                           "-fx-text-fill: white; -fx-background-radius: 15; " +
                           "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; " +
                           "-fx-border-radius: 15;");
        
        // Custom cell factory
        playerList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    String displayText;
                    String style;
                    if (item.equals(username)) {
                        displayText = "👑 " + item + " (Bạn)";
                        style = "-fx-background-color: rgba(255,215,0,0.3); " +
                               "-fx-text-fill: gold; -fx-padding: 10; " +
                               "-fx-background-radius: 10; -fx-font-weight: bold;";
                    } else if (item.contains("(Host)")) {
                        displayText = "🏆 " + item;
                        style = "-fx-background-color: rgba(255,0,0,0.2); " +
                               "-fx-text-fill: orange; -fx-padding: 10; " +
                               "-fx-background-radius: 10;";
                    } else {
                        displayText = "🟢 " + item;
                        style = "-fx-background-color: rgba(255,255,255,0.1); " +
                               "-fx-text-fill: lightgreen; -fx-padding: 10; " +
                               "-fx-background-radius: 10;";
                    }
                    setText(displayText);
                    setStyle(style);
                }
            }
        });
        
        // Add/Remove player buttons
        Button inviteBtn = new Button("➕ MỜI BẠN");
        inviteBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        inviteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                          "-fx-background-radius: 20; -fx-padding: 8 16; " +
                          "-fx-border-radius: 20; -fx-cursor: hand;");
        
        Button kickBtn = new Button("❌ KICK");
        kickBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        kickBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                        "-fx-background-radius: 20; -fx-padding: 8 16; " +
                        "-fx-border-radius: 20; -fx-cursor: hand;");
        
        HBox playerButtons = new HBox(5);
        playerButtons.setAlignment(Pos.CENTER);
        playerButtons.getChildren().addAll(inviteBtn, kickBtn);
        
        leftPanel.getChildren().addAll(playerTitle, playerList, playerButtons);
        return leftPanel;
    }
    
    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(20));
        centerPanel.setPrefWidth(400);
        
        Label chatTitle = new Label("💬 CHAT PHÒNG");
        chatTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        chatTitle.setTextFill(Color.WHITE);
        
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(350);
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                         "-fx-text-fill: white; -fx-background-radius: 15; " +
                         "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; " +
                         "-fx-border-radius: 15; -fx-font-size: 14;");
        
        // Chat input
        chatInput = new TextField();
        chatInput.setPromptText("Nhập tin nhắn (Enter để gửi)...");
        chatInput.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                          "-fx-text-fill: white; -fx-prompt-text-fill: gray; " +
                          "-fx-background-radius: 25; -fx-padding: 12; " +
                          "-fx-font-size: 14;");
        chatInput.setOnAction(e -> sendChat());
        
        Button sendBtn = new Button("📤 Gửi");
        sendBtn.setStyle("-fx-background-color: #5856d6; -fx-text-fill: white; " +
                        "-fx-background-radius: 20; -fx-padding: 12 20; " +
                        "-fx-cursor: hand; -fx-font-weight: bold;");
        sendBtn.setOnAction(e -> sendChat());
        
        HBox chatInputBox = new HBox(10);
        chatInputBox.getChildren().addAll(chatInput, sendBtn);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        
        centerPanel.getChildren().addAll(chatTitle, chatArea, chatInputBox);
        return centerPanel;
    }
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(250);
        rightPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05);");
        
        Label settingsTitle = new Label("⚙️ CÀI ĐẶT GAME");
        settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        settingsTitle.setTextFill(Color.WHITE);
        
        VBox settingsBox = new VBox(12);
        settingsBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                            "-fx-background-radius: 15; -fx-padding: 20;");
        
        // Game mode
        Label modeLabel = new Label("🎯 Chế độ:");
        modeLabel.setTextFill(Color.WHITE);
        modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        ComboBox<String> modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("Dễ (60s/từ)", "Trung bình (45s/từ)", "Khó (30s/từ)", "Siêu khó (15s/từ)");
        modeCombo.setValue("Trung bình (45s/từ)");
        modeCombo.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                          "-fx-text-fill: white;");
        
        // Max players
        Label maxPlayersLabel = new Label("👥 Số người tối đa:");
        maxPlayersLabel.setTextFill(Color.WHITE);
        maxPlayersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Spinner<Integer> maxPlayersSpinner = new Spinner<>(2, 8, 4);
        maxPlayersSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.2);");
        
        // Password protection
        CheckBox passwordCheck = new CheckBox("🔒 Bảo vệ mật khẩu");
        passwordCheck.setTextFill(Color.WHITE);
        passwordCheck.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        TextField passwordField = new TextField();
        passwordField.setPromptText("Nhập mật khẩu...");
        passwordField.setStyle("-fx-background-color: rgba(255,255,255,0.2); " +
                              "-fx-text-fill: white; -fx-prompt-text-fill: gray;");
        passwordField.setDisable(true);
        
        passwordCheck.setOnAction(e -> passwordField.setDisable(!passwordCheck.isSelected()));
        
        settingsBox.getChildren().addAll(
            modeLabel, modeCombo,
            maxPlayersLabel, maxPlayersSpinner,
            passwordCheck, passwordField
        );
        
        // Game rules
        Label rulesTitle = new Label("📋 LUẬT CHƠI");
        rulesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rulesTitle.setTextFill(Color.WHITE);
        
        TextArea rulesArea = new TextArea();
        rulesArea.setEditable(false);
        rulesArea.setPrefHeight(120);
        rulesArea.setWrapText(true);
        rulesArea.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                          "-fx-text-fill: white; -fx-background-radius: 10; " +
                          "-fx-font-size: 12;");
        rulesArea.setText("• Sắp xếp các chữ cái thành từ có nghĩa\n" +
                         "• Mỗi từ đúng được điểm = thời gian còn lại × 10\n" +
                         "• Người có điểm cao nhất thắng\n" +
                         "• Có thể chat trong lúc chơi\n" +
                         "• Tối đa 5 round/game");
        
        rightPanel.getChildren().addAll(settingsTitle, settingsBox, rulesTitle, rulesArea);
        return rightPanel;
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20));
        
        startGameBtn = new Button("🎮 BẮT ĐẦU GAME");
        startGameBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        startGameBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                             "-fx-background-radius: 30; -fx-padding: 15 40; " +
                             "-fx-border-radius: 30; -fx-cursor: hand; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        startGameBtn.setOnAction(e -> startGame());
        
        Button readyBtn = new Button("✅ SẴN SÀNG");
        readyBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        readyBtn.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white; " +
                         "-fx-background-radius: 25; -fx-padding: 12 24; " +
                         "-fx-border-radius: 25; -fx-cursor: hand;");
        readyBtn.setOnAction(e -> toggleReady());
        
        Button leaveBtn = new Button("🚪 RỜI PHÒNG");
        leaveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        leaveBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                         "-fx-background-radius: 25; -fx-padding: 12 24; " +
                         "-fx-border-radius: 25; -fx-cursor: hand;");
        leaveBtn.setOnAction(e -> leaveRoom());
        
        controls.getChildren().addAll(startGameBtn, readyBtn, leaveBtn);
        return controls;
    }
    
    private void loadPlayers() {
        playerList.getItems().clear();
        playerList.getItems().addAll(
            username,
            "Player1",
            "WordMaster",
            "FastTyper"
        );
        updateRoomStatus();
    }
    
    private void setupChat() {
        chatArea.appendText("🎮 Chào mừng bạn vào phòng!\n");
        chatArea.appendText("💡 Hãy chat với mọi người trong khi chờ game bắt đầu.\n");
        chatArea.appendText("📝 Sử dụng /ready để báo sẵn sàng\n\n");
        chatArea.appendText("👤 " + username + " đã vào phòng!\n");
    }
    
    private void sendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            if (message.equals("/ready")) {
                chatArea.appendText("✅ " + username + " đã sẵn sàng!\n");
            } else if (message.equals("/unready")) {
                chatArea.appendText("❌ " + username + " chưa sẵn sàng!\n");
            } else {
                chatArea.appendText("👤 " + username + ": " + message + "\n");
            }
            chatInput.clear();
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
    }
    
    private void updateRoomStatus() {
        int playerCount = playerList.getItems().size();
        roomStatusLabel.setText("Số người chơi: " + playerCount + "/4 - Đang chờ...");
        
        // Enable start game if enough players
        startGameBtn.setDisable(playerCount < 2);
    }
    
    private void startGame() {
        chatArea.appendText("🎮 Game bắt đầu trong 3 giây...\n");
        chatArea.appendText("⏰ 3... 2... 1... GO!\n");
        
        // Navigate to game
        Stage gameStage = new Stage();
        MainGameView gameView = new MainGameView(gameStage, client, username);
        gameView.show();
        stage.close();
    }
    
    private void toggleReady() {
        chatArea.appendText("✅ " + username + " đã báo sẵn sàng!\n");
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private void leaveRoom() {
        // Back to lobby
        Stage lobbyStage = new Stage();
        FXLobbyView lobbyView = new FXLobbyView(lobbyStage, client, username);
        lobbyView.show();
        stage.close();
    }
    
    public void show() {
        stage.show();
    }
}