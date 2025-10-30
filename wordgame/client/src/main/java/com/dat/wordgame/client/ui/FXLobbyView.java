package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FXLobbyView {
    private Stage stage;
    private NetClient client;
    private String username;
    private ListView<String> roomList;
    private ListView<String> playerList;
    
    public FXLobbyView(Stage stage, NetClient client, String username) {
        this.stage = stage;
        this.client = client;
        this.username = username;
        initialize();
    }
    
    private void initialize() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #af52de);");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Center - Room list
        VBox centerPanel = createCenterPanel();
        root.setCenter(centerPanel);
        
        // Right - Player list
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);
        
        // Bottom - Controls
        HBox controls = createControls();
        root.setBottom(controls);
        
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Word Game - Lobby");
        stage.setScene(scene);
        
        loadRooms();
        loadPlayers();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30));
        header.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        
        Label titleLabel = new Label("🎮 WORD GAME LOBBY");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        Label welcomeLabel = new Label("Chào mừng, " + username + "!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        welcomeLabel.setTextFill(Color.LIGHTBLUE);
        
        header.getChildren().addAll(titleLabel, welcomeLabel);
        return header;
    }
    
    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(20));
        centerPanel.setPrefWidth(400);
        
        Label roomTitle = new Label("📋 DANH SÁCH PHÒNG");
        roomTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        roomTitle.setTextFill(Color.WHITE);
        
        roomList = new ListView<>();
        roomList.setPrefHeight(300);
        roomList.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                         "-fx-text-fill: white; -fx-background-radius: 15; " +
                         "-fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; " +
                         "-fx-border-radius: 15;");
        
        // Custom cell factory for purple theme
        roomList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                            "-fx-text-fill: white; -fx-padding: 10; " +
                            "-fx-background-radius: 10;");
                }
            }
        });
        
        Button joinBtn = new Button("🚪 VÀO PHÒNG");
        joinBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        joinBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-background-radius: 25; -fx-padding: 12 30; " +
                        "-fx-border-radius: 25; -fx-cursor: hand;");
        joinBtn.setOnAction(e -> joinRoom());
        
        Button refreshBtn = new Button("🔄 LÀM MỚI");
        refreshBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                          "-fx-background-radius: 25; -fx-padding: 12 30; " +
                          "-fx-border-radius: 25; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> loadRooms());
        
        HBox roomButtons = new HBox(10);
        roomButtons.setAlignment(Pos.CENTER);
        roomButtons.getChildren().addAll(joinBtn, refreshBtn);
        
        centerPanel.getChildren().addAll(roomTitle, roomList, roomButtons);
        return centerPanel;
    }
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(250);
        rightPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05);");
        
        Label playerTitle = new Label("👥 NGƯỜI CHƠI ONLINE");
        playerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerTitle.setTextFill(Color.WHITE);
        
        playerList = new ListView<>();
        playerList.setPrefHeight(300);
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
                    setText("🟢 " + item);
                    setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                            "-fx-text-fill: lightgreen; -fx-padding: 8; " +
                            "-fx-background-radius: 8;");
                }
            }
        });
        
        Label statsLabel = new Label("📊 THỐNG KÊ");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statsLabel.setTextFill(Color.WHITE);
        
        VBox statsBox = new VBox(8);
        statsBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                         "-fx-background-radius: 15; -fx-padding: 15;");
        
        Label roomCount = new Label("🏠 Phòng: 3");
        roomCount.setTextFill(Color.WHITE);
        Label playerCount = new Label("👤 Người chơi: 12");
        playerCount.setTextFill(Color.WHITE);
        Label gameCount = new Label("🎯 Game đang chạy: 2");
        gameCount.setTextFill(Color.WHITE);
        
        statsBox.getChildren().addAll(roomCount, playerCount, gameCount);
        
        rightPanel.getChildren().addAll(playerTitle, playerList, statsLabel, statsBox);
        return rightPanel;
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20));
        
        Button createRoomBtn = new Button("➕ TẠO PHÒNG");
        createRoomBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        createRoomBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; " +
                             "-fx-background-radius: 25; -fx-padding: 15 30; " +
                             "-fx-border-radius: 25; -fx-cursor: hand;");
        createRoomBtn.setOnAction(e -> createRoom());
        
        Button quickGameBtn = new Button("⚡ CHƠI NHANH");
        quickGameBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        quickGameBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; " +
                            "-fx-background-radius: 25; -fx-padding: 15 30; " +
                            "-fx-border-radius: 25; -fx-cursor: hand;");
        quickGameBtn.setOnAction(e -> quickGame());
        
        Button exitBtn = new Button("🚪 THOÁT");
        exitBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        exitBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; " +
                        "-fx-background-radius: 25; -fx-padding: 15 30; " +
                        "-fx-border-radius: 25; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> stage.close());
        
        controls.getChildren().addAll(createRoomBtn, quickGameBtn, exitBtn);
        return controls;
    }
    
    private void loadRooms() {
        roomList.getItems().clear();
        roomList.getItems().addAll(
            "🏠 Phòng Newbie (2/4 người)",
            "🔥 Phòng Pro (1/4 người)", 
            "⭐ Phòng VIP (3/4 người)",
            "🎯 Phòng Thách Đấu (0/6 người)",
            "🌟 Phòng Siêu Tốc (4/4 người - Đầy)"
        );
    }
    
    private void loadPlayers() {
        playerList.getItems().clear();
        playerList.getItems().addAll(
            username,
            "Player1",
            "GameMaster",
            "WordLover", 
            "Speedster",
            "ChillGamer",
            "ProGamer99",
            "QuickThink"
        );
    }
    
    private void joinRoom() {
        String selectedRoom = roomList.getSelectionModel().getSelectedItem();
        if (selectedRoom != null && !selectedRoom.contains("Đầy")) {
            // Navigate to room view
            Stage roomStage = new Stage();
            FXRoomView roomView = new FXRoomView(roomStage, client, username, selectedRoom);
            roomView.show();
            stage.close();
        } else if (selectedRoom != null && selectedRoom.contains("Đầy")) {
            showAlert("Thông báo", "Phòng này đã đầy!", Alert.AlertType.WARNING);
        } else {
            showAlert("Thông báo", "Vui lòng chọn một phòng!", Alert.AlertType.INFORMATION);
        }
    }
    
    private void createRoom() {
        // Show create room dialog
        TextInputDialog dialog = new TextInputDialog("Phòng của " + username);
        dialog.setTitle("Tạo Phòng Mới");
        dialog.setHeaderText("Tạo phòng chơi mới");
        dialog.setContentText("Tên phòng:");
        
        dialog.showAndWait().ifPresent(roomName -> {
            Stage roomStage = new Stage();
            FXRoomView roomView = new FXRoomView(roomStage, client, username, "🏠 " + roomName + " (1/4 người)");
            roomView.show();
            stage.close();
        });
    }
    
    private void quickGame() {
        // Direct to game
        Stage gameStage = new Stage();
        MainGameView gameView = new MainGameView(gameStage, client, username);
        gameView.show();
        stage.close();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        stage.show();
    }
}