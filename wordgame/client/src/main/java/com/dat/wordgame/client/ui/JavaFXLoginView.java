package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFXLoginView extends Application {
    
    private TextField hostInput;
    private TextField portInput;
    private TextField usernameInput;
    private PasswordField passwordInput;
    private Button connectButton;
    
    @Override
    public void start(Stage primaryStage) {
        createLoginUI(primaryStage);
    }
    
    private void createLoginUI(Stage stage) {
        // Main container with ultra modern gradient
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        mainContainer.getStyleClass().add("game-container");
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(135deg,
                #1a1a2e 0%,
                #16213e 25%,
                #0f3460 50%,
                #e94560 75%,
                #533483 100%);
            """);        // Main title with glow effect
        VBox titleContainer = new VBox(10);
        titleContainer.setAlignment(Pos.CENTER);
        
        Label gameIcon = new Label("🎮");
        gameIcon.setStyle("-fx-font-size: 72px;");
        
        Label gameTitle = new Label("Word Game");
        gameTitle.setStyle("""
            -fx-font-size: 56px;
            -fx-font-weight: 900;
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.4), 20, 0, 0, 0);
            """);
            
        Label subtitle = new Label("Ultra Modern Edition");
        subtitle.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: 600;
            -fx-text-fill: rgba(255,255,255,0.8);
            """);
        
        titleContainer.getChildren().addAll(gameIcon, gameTitle, subtitle);

        // Connection settings card
        VBox connectionCard = new VBox(20);
        connectionCard.setAlignment(Pos.CENTER);
        connectionCard.setMaxWidth(450);
        connectionCard.setStyle("""
            -fx-background-color: rgba(30, 30, 60, 0.8);
            -fx-background-radius: 25px;
            -fx-border-color: rgba(255, 255, 255, 0.3);
            -fx-border-width: 2px;
            -fx-border-radius: 25px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 15);
            -fx-padding: 30px;
            """);

        Label connectionTitle = new Label("🌐 Kết nối Server");
        connectionTitle.setStyle("""
            -fx-font-size: 26px;
            -fx-font-weight: 800;
            -fx-text-fill: #ffffff;
            -fx-alignment: center;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 3, 0, 0, 2);
            """);

        HBox serverBox = new HBox(15);
        serverBox.setAlignment(Pos.CENTER);
        
        VBox hostContainer = new VBox(8);
        Label hostLabel = new Label("Host:");
        hostLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 16px;");
        hostInput = new TextField("127.0.0.1");
        hostInput.setStyle("""
            -fx-background-color: rgba(40, 40, 70, 0.9);
            -fx-background-radius: 12px;
            -fx-padding: 16px 20px;
            -fx-font-size: 14px;
            -fx-text-fill: #ffffff;
            -fx-border-color: rgba(255, 255, 255, 0.2);
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-pref-width: 180px;
            """);
        hostContainer.getChildren().addAll(hostLabel, hostInput);
        
        VBox portContainer = new VBox(8);
        Label portLabel = new Label("Port:");
        portLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 16px;");
        portInput = new TextField("7777");
        portInput.setStyle("""
            -fx-background-color: rgba(40, 40, 70, 0.9);
            -fx-background-radius: 12px;
            -fx-padding: 16px 20px;
            -fx-font-size: 14px;
            -fx-text-fill: #ffffff;
            -fx-border-color: rgba(255, 255, 255, 0.2);
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-pref-width: 120px;
            """);
        portContainer.getChildren().addAll(portLabel, portInput);
        
        serverBox.getChildren().addAll(hostContainer, portContainer);

        // Login card
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(450);
        loginCard.setStyle("""
            -fx-background-color: rgba(30, 30, 60, 0.8);
            -fx-background-radius: 25px;
            -fx-border-color: rgba(255, 255, 255, 0.3);
            -fx-border-width: 2px;
            -fx-border-radius: 25px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 15);
            -fx-padding: 40px;
            """);

        Label loginTitle = new Label("👤 Đăng Nhập");
        loginTitle.setStyle("""
            -fx-font-size: 24px;
            -fx-font-weight: 700;
            -fx-text-fill: #ffffff;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 3, 0, 0, 2);
            -fx-alignment: center;
            """);

        VBox usernameContainer = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập:");
        usernameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 16px;");
        usernameInput = new TextField();
        usernameInput.setPromptText("Nhập tên người chơi...");
        usernameInput.setStyle("""
            -fx-background-color: rgba(40, 40, 70, 0.9);
            -fx-background-radius: 12px;
            -fx-padding: 16px 20px;
            -fx-font-size: 16px;
            -fx-text-fill: #ffffff;
            -fx-border-color: rgba(255, 255, 255, 0.2);
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-pref-width: 350px;
            """);
        usernameContainer.getChildren().addAll(usernameLabel, usernameInput);

        VBox passwordContainer = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu:");
        passwordLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: 700; -fx-font-size: 16px;");
        passwordInput = new PasswordField();
        passwordInput.setPromptText("Để trống nếu tạo tài khoản mới...");
        passwordInput.setStyle("""
            -fx-background-color: rgba(40, 40, 70, 0.9);
            -fx-background-radius: 12px;
            -fx-padding: 16px 20px;
            -fx-font-size: 16px;
            -fx-text-fill: #ffffff;
            -fx-border-color: rgba(255, 255, 255, 0.2);
            -fx-border-width: 1px;
            -fx-border-radius: 12px;
            -fx-pref-width: 350px;
            """);
        passwordContainer.getChildren().addAll(passwordLabel, passwordInput);

        connectButton = new Button("🚀 Kết nối & Chơi");
        connectButton.setStyle("""
            -fx-background-color: linear-gradient(45deg, #e94560, #f38ba8);
            -fx-text-fill: white;
            -fx-background-radius: 15px;
            -fx-padding: 20px 40px;
            -fx-font-weight: 700;
            -fx-font-size: 18px;
            -fx-effect: dropshadow(gaussian, rgba(233, 69, 96, 0.5), 15, 0, 0, 8);
            -fx-pref-width: 250px;
            """);
        connectButton.setOnAction(e -> handleConnect(stage));
        
        usernameInput.setOnAction(e -> handleConnect(stage));
        passwordInput.setOnAction(e -> handleConnect(stage));

        connectionCard.getChildren().addAll(connectionTitle, serverBox);
        loginCard.getChildren().addAll(loginTitle, usernameContainer, passwordContainer, connectButton);

        mainContainer.getChildren().addAll(titleContainer, connectionCard, loginCard);

        Scene scene = new Scene(mainContainer, 800, 900);
        
        // Load modern CSS stylesheet
        String css = getClass().getResource("/styles/modern.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        stage.setTitle("🎮 Word Game - Ultra Modern Edition");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
    
    private void handleConnect(Stage stage) {
        String host = hostInput.getText().trim();
        String portStr = portInput.getText().trim();
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText();
        
        if (username.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên đăng nhập!");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            NetClient netClient = new NetClient(host, port);
            
            // Try to connect and login
            connectButton.setDisable(true);
            connectButton.setText("⏳ Đang kết nối...");
            
            // Simulate connection (in real app, this would be async)
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Simulate connection time
                    
                    javafx.application.Platform.runLater(() -> {
                        // Open main game view
                        Stage gameStage = new Stage();
                        MainGameView gameView = new MainGameView(gameStage, netClient, username);
                        gameView.show();
                    });
                    
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        connectButton.setDisable(false);
                        connectButton.setText("🚀 Kết nối & Chơi");
                        showAlert("Lỗi kết nối", "Không thể kết nối đến server: " + e.getMessage());
                    });
                }
            }).start();
            
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Port phải là số!");
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tạo kết nối: " + e.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}