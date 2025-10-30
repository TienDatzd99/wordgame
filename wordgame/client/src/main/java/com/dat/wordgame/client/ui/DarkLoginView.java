package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DarkLoginView extends Application {
    
    private TextField hostInput;
    private TextField portInput;
    private TextField usernameInput;
    private PasswordField passwordInput;
    private Button connectButton;
    
    @Override
    public void start(Stage stage) {
        createDarkLoginUI(stage);
    }
    
    private void createDarkLoginUI(Stage stage) {
        // Main container with SOLID dark background - NO transparency
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        // Purple gradient background like in the image
        mainContainer.setStyle("""
            -fx-background-color: #5856d6;
            """);

        // Title with bright white text
        VBox titleContainer = new VBox(10);
        titleContainer.setAlignment(Pos.CENTER);
        
        Label gameIcon = new Label("🎮");
        gameIcon.setStyle("-fx-font-size: 80px;");
        
        Label gameTitle = new Label("Word Game - DARK EDITION");
        gameTitle.setStyle("""
            -fx-font-size: 36px;
            -fx-font-weight: bold;
            -fx-text-fill: #ffffff;
            """);
        
        Label subtitle = new Label("Giao diện tối hiện đại");
        subtitle.setStyle("""
            -fx-font-size: 18px;
            -fx-text-fill: #ff6b6b;
            -fx-font-weight: bold;
            """);
        
        titleContainer.getChildren().addAll(gameIcon, gameTitle, subtitle);

        // Connection card with SOLID dark background
        VBox connectionCard = new VBox(20);
        connectionCard.setAlignment(Pos.CENTER);
        connectionCard.setMaxWidth(500);
        connectionCard.setStyle("""
            -fx-background-color: #2d2d44;
            -fx-background-radius: 20px;
            -fx-padding: 30px;
            -fx-border-color: #ff6b6b;
            -fx-border-width: 2px;
            -fx-border-radius: 20px;
            """);

        Label connectionTitle = new Label("🌐 Kết nối Server");
        connectionTitle.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #ffffff;
            """);

        HBox serverBox = new HBox(20);
        serverBox.setAlignment(Pos.CENTER);
        
        // Host input with purple theme
        VBox hostContainer = new VBox(8);
        Label hostLabel = new Label("Host:");
        hostLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
        hostInput = new TextField("127.0.0.1");
        hostInput.setStyle("""
            -fx-background-color: #373b4d;
            -fx-text-fill: #ffffff;
            -fx-border-color: #5856d6;
            -fx-border-width: 2px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 12px;
            -fx-font-size: 14px;
            -fx-pref-width: 180px;
            """);
        hostContainer.getChildren().addAll(hostLabel, hostInput);
        
        // Port input with purple theme
        VBox portContainer = new VBox(8);
        Label portLabel = new Label("Port:");
        portLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
        portInput = new TextField("7777");
        portInput.setStyle("""
            -fx-background-color: #373b4d;
            -fx-text-fill: #ffffff;
            -fx-border-color: #5856d6;
            -fx-border-width: 2px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 12px;
            -fx-font-size: 14px;
            -fx-pref-width: 120px;
            """);
        portContainer.getChildren().addAll(portLabel, portInput);
        
        serverBox.getChildren().addAll(hostContainer, portContainer);

        // Login card with dark purple theme
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(500);
        loginCard.setStyle("""
            -fx-background-color: rgba(40, 44, 52, 0.9);
            -fx-background-radius: 20px;
            -fx-padding: 30px;
            -fx-border-color: #5856d6;
            -fx-border-width: 2px;
            -fx-border-radius: 20px;
            """);

        Label loginTitle = new Label("👤 Đăng Nhập");
        loginTitle.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #ffffff;
            """);

        // Username input with dark theme
        VBox usernameContainer = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập:");
        usernameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
        usernameInput = new TextField();
        usernameInput.setPromptText("Nhập tên người chơi...");
        usernameInput.setStyle("""
            -fx-background-color: #373b4d;
            -fx-text-fill: #ffffff;
            -fx-border-color: #5856d6;
            -fx-border-width: 2px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 15px;
            -fx-font-size: 16px;
            -fx-pref-width: 400px;
            """);
        usernameContainer.getChildren().addAll(usernameLabel, usernameInput);

        // Password input with dark theme
        VBox passwordContainer = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu:");
        passwordLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
        passwordInput = new PasswordField();
        passwordInput.setPromptText("Để trống nếu tạo tài khoản mới...");
        passwordInput.setStyle("""
            -fx-background-color: #373b4d;
            -fx-text-fill: #ffffff;
            -fx-border-color: #5856d6;
            -fx-border-width: 2px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-padding: 15px;
            -fx-font-size: 16px;
            -fx-pref-width: 400px;
            """);
        passwordContainer.getChildren().addAll(passwordLabel, passwordInput);

        // Connect button with bright styling
        connectButton = new Button("🚀 Kết nối & Chơi");
        connectButton.setStyle("""
            -fx-background-color: #5856d6;
            -fx-text-fill: #ffffff;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-padding: 15px 40px;
            -fx-background-radius: 12px;
            -fx-border-radius: 12px;
            -fx-cursor: hand;
            """);
        
        // Hover effect for button
        connectButton.setOnMouseEntered(e -> {
            connectButton.setStyle("""
                -fx-background-color: #6b5ce7;
                -fx-text-fill: #ffffff;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-padding: 15px 40px;
                -fx-background-radius: 12px;
                -fx-border-radius: 12px;
                -fx-cursor: hand;
                """);
        });
        
        connectButton.setOnMouseExited(e -> {
            connectButton.setStyle("""
                -fx-background-color: #5856d6;
                -fx-text-fill: #ffffff;
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-padding: 15px 40px;
                -fx-background-radius: 12px;
                -fx-border-radius: 12px;
                -fx-cursor: hand;
                """);
        });
        
        connectButton.setOnAction(e -> handleConnect(stage));
        
        usernameInput.setOnAction(e -> handleConnect(stage));
        passwordInput.setOnAction(e -> handleConnect(stage));

        connectionCard.getChildren().addAll(connectionTitle, serverBox);
        loginCard.getChildren().addAll(loginTitle, usernameContainer, passwordContainer, connectButton);

        mainContainer.getChildren().addAll(titleContainer, connectionCard, loginCard);

        Scene scene = new Scene(mainContainer, 900, 1000);
        stage.setTitle("🎮 Word Game - DARK EDITION");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private void handleConnect(Stage stage) {
        String host = hostInput.getText().trim();
        String portStr = portInput.getText().trim();
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText();
        
        if (username.isEmpty()) {
            showDarkAlert("Lỗi", "Vui lòng nhập tên đăng nhập!");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            
            connectButton.setDisable(true);
            connectButton.setText("🔄 Đang kết nối...");
            
            new Thread(() -> {
                try {
                    NetClient client = new NetClient(host, port);
                    
                    javafx.application.Platform.runLater(() -> {
                        stage.close();
                        Stage lobbyStage = new Stage();
                        FXLobbyView lobbyView = new FXLobbyView(lobbyStage, client, username);
                        lobbyView.show();
                    });
                    
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        connectButton.setDisable(false);
                        connectButton.setText("🚀 Kết nối & Chơi");
                        showDarkAlert("Lỗi kết nối", "Không thể kết nối đến server: " + e.getMessage());
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showDarkAlert("Lỗi", "Port phải là số!");
        } catch (Exception e) {
            showDarkAlert("Lỗi", "Không thể tạo kết nối: " + e.getMessage());
        }
    }
    
    private void showDarkAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog with dark theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: #2d2d44;
            -fx-text-fill: #ffffff;
            """);
        
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}