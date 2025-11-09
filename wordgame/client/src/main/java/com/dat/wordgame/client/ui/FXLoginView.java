package com.dat.wordgame.client.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;
import com.dat.wordgame.common.Json;

public class FXLoginView extends Application {
    
    private NetClient netClient;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField hostField;
    private TextField portField;
    private Button loginButton;
    private Button connectButton;
    private Label statusLabel;
    private Stage primaryStage;
    private FXLobbyView activeLobbyView; // Reference để forward messages
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        // Main container with purple gradient background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #af52de);");
        
        // White card container
        VBox card = createMainCard();
        root.getChildren().add(card);
        
        Scene scene = new Scene(root, 900, 700);
        stage.setTitle("WordleCup Multiplayer - v2.5.1");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (netClient != null) {
                try {
                    netClient.close();
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
            Platform.exit();
        });
        stage.show();
    }
    
    private VBox createMainCard() {
        VBox card = new VBox(25);
        card.setMaxWidth(500);
        card.setMaxHeight(650);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        
        // Title
        Label title = new Label("🎮 WORD GAME");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #5856d6;");
        
        Label subtitle = new Label("Multiplayer Edition");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitle.setStyle("-fx-text-fill: #8a2be2;");
        
        // Connection Panel
        VBox connectionPanel = createConnectionPanel();
        
        // Login Panel
        VBox loginPanel = createLoginPanel();
        
        // Status Label
        statusLabel = new Label("Chưa kết nối server");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setStyle("-fx-text-fill: #dc3545;");
        
        // Version
        Label version = new Label("v2.5.1 - Build 08/11/2025");
        version.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        version.setStyle("-fx-text-fill: #999999;");
        
        card.getChildren().addAll(title, subtitle, connectionPanel, loginPanel, statusLabel, version);
        return card;
    }
    
    private VBox createConnectionPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 20;");
        
        Label panelTitle = new Label("🌐 Kết nối Server");
        panelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        panelTitle.setStyle("-fx-text-fill: #5856d6;");
        
        HBox hostPortBox = new HBox(15);
        hostPortBox.setAlignment(Pos.CENTER);
        
        // Host Field
        VBox hostBox = new VBox(8);
        Label hostLabel = new Label("Host:");
        hostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        hostField = new TextField("127.0.0.1");
        hostField.setPrefWidth(200);
        styleTextField(hostField);
        hostBox.getChildren().addAll(hostLabel, hostField);
        
        // Port Field
        VBox portBox = new VBox(8);
        Label portLabel = new Label("Port:");
        portLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        portField = new TextField("7777");
        portField.setPrefWidth(100);
        styleTextField(portField);
        portBox.getChildren().addAll(portLabel, portField);
        
        hostPortBox.getChildren().addAll(hostBox, portBox);
        
        // Connect Button
        connectButton = createStyledButton("Kết nối", "#5856d6");
        connectButton.setOnAction(e -> connectToServer());
        
        panel.getChildren().addAll(panelTitle, hostPortBox, connectButton);
        return panel;
    }
    
    private VBox createLoginPanel() {
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15; -fx-padding: 20;");
        
        Label panelTitle = new Label("👤 Đăng Nhập");
        panelTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        panelTitle.setStyle("-fx-text-fill: #5856d6;");
        
        // Username
        VBox userBox = new VBox(8);
        Label userLabel = new Label("Tên đăng nhập:");
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        usernameField = new TextField();
        usernameField.setPromptText("Nhập tên người chơi...");
        usernameField.setPrefWidth(400);
        styleTextField(usernameField);
        usernameField.textProperty().addListener((obs, old, newVal) -> updateLoginButtonState());
        userBox.getChildren().addAll(userLabel, usernameField);
        
        // Password
        VBox passBox = new VBox(8);
        Label passLabel = new Label("Mật khẩu:");
        passLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPromptText("Nhập mật khẩu...");
        passwordField.setPrefWidth(400);
        styleTextField(passwordField);
        passwordField.setOnAction(e -> handleLogin());
        passBox.getChildren().addAll(passLabel, passwordField);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        loginButton = createStyledButton("ĐĂNG NHẬP", "#5856d6");
        loginButton.setPrefWidth(180);
        loginButton.setDisable(true);
        loginButton.setOnAction(e -> handleLogin());
        
        Button registerButton = createStyledButton("ĐĂNG KÝ", "#8a2be2");
        registerButton.setPrefWidth(180);
        registerButton.setOnAction(e -> handleRegister());
        
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        panel.getChildren().addAll(panelTitle, userBox, passBox, buttonBox);
        return panel;
    }
    
    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: white; -fx-border-color: #5856d6; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14;");
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setPrefHeight(45);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;");
        
        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;"));
        
        return button;
    }
    
    private void updateLoginButtonState() {
        loginButton.setDisable(netClient == null || usernameField.getText().trim().isEmpty());
    }
    
    private void connectToServer() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        
        if (host.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập địa chỉ host!", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            
            connectButton.setDisable(true);
            connectButton.setText("Đang kết nối...");
            
            new Thread(() -> {
                try {
                    if (netClient != null) {
                        netClient.close();
                    }
                    
                    netClient = new NetClient(host, port);
                    netClient.listen(this::onMessage);
                    
                    Platform.runLater(() -> {
                        connectButton.setDisable(false);
                        connectButton.setText("Kết nối");
                        statusLabel.setText("✓ Đã kết nối đến " + host + ":" + port);
                        statusLabel.setStyle("-fx-text-fill: #28a745;");
                        updateLoginButtonState();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        connectButton.setDisable(false);
                        connectButton.setText("Kết nối");
                        showAlert("Lỗi kết nối", "Không thể kết nối đến server:\n" + e.getMessage(), Alert.AlertType.ERROR);
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Port phải là số!", Alert.AlertType.ERROR);
        }
    }
    
    private void handleLogin() {
        if (netClient == null) {
            showAlert("Lỗi", "Vui lòng kết nối server trước!", Alert.AlertType.WARNING);
            return;
        }
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên đăng nhập!", Alert.AlertType.WARNING);
            return;
        }
        
        loginButton.setDisable(true);
        netClient.send(Message.of(MessageType.LOGIN_REQ, new Models.LoginReq(username, password)));
    }
    
    private void handleRegister() {
        if (netClient == null) {
            showAlert("Lỗi", "Vui lòng kết nối server trước!", Alert.AlertType.WARNING);
            return;
        }
        
        // Create register dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký tài khoản");
        dialog.setHeaderText("Tạo tài khoản mới");
        
        // Dialog content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        TextField regUsername = new TextField();
        regUsername.setPromptText("Tên đăng nhập...");
        styleTextField(regUsername);
        
        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("Mật khẩu...");
        styleTextField(regPassword);
        
        PasswordField regConfirm = new PasswordField();
        regConfirm.setPromptText("Xác nhận mật khẩu...");
        styleTextField(regConfirm);
        
        content.getChildren().addAll(
            new Label("Tên đăng nhập:"), regUsername,
            new Label("Mật khẩu:"), regPassword,
            new Label("Xác nhận mật khẩu:"), regConfirm
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String username = regUsername.getText().trim();
                String password = regPassword.getText();
                String confirm = regConfirm.getText();
                
                if (username.isEmpty() || password.isEmpty()) {
                    showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
                    return;
                }
                
                if (!password.equals(confirm)) {
                    showAlert("Lỗi", "Mật khẩu xác nhận không khớp!", Alert.AlertType.WARNING);
                    return;
                }
                
                netClient.send(Message.of(MessageType.REGISTER_REQ, new Models.RegisterReq(username, password)));
            }
        });
    }
    
    private void onMessage(Message message) {
        Platform.runLater(() -> {
            switch (message.type) {
                case LOGIN_OK -> {
                    Models.LoginOk loginOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LoginOk.class);
                    openLobby(loginOk.username(), loginOk.totalPoints());
                }
                case LOGIN_FAIL, ERROR -> {
                    Models.Err err = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Err.class);
                    showAlert("Đăng nhập thất bại", err.message(), Alert.AlertType.ERROR);
                    loginButton.setDisable(false);
                }
                case REGISTER_OK -> {
                    Models.RegisterOk regOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RegisterOk.class);
                    showAlert("Thành công", 
                        "Đăng ký thành công!\n\n" +
                        "Tên đăng nhập: " + regOk.username() + "\n" +
                        "Bạn có thể đăng nhập ngay bây giờ!",
                        Alert.AlertType.INFORMATION);
                }
                default -> {
                    // Forward messages to LobbyView if it's active
                    if (activeLobbyView != null) {
                        activeLobbyView.handleMessage(message);
                    }
                }
            }
        });
    }
    
    private void openLobby(String username, int points) {
        // Open JavaFX LobbyView
        activeLobbyView = new FXLobbyView(primaryStage, netClient, username);
        activeLobbyView.show();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
