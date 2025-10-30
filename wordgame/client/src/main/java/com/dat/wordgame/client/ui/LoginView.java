package com.dat.wordgame.client.ui;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView {
    private Stage stage;
    private NetClient netClient;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label statusLabel;
    private VBox mainContainer;

    public LoginView(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        // Main container with gradient background
        mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        mainContainer.getStyleClass().add("login-container");

        // Title
        Label titleLabel = new Label("🎮 Word Game");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(10, Color.BLACK));

        // Subtitle
        Label subtitleLabel = new Label("Đăng nhập để chơi");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);

        // Login form container
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40));
        formContainer.setMaxWidth(400);
        formContainer.getStyleClass().add("form-container");

        // Username field
        VBox usernameBox = createInputBox("👤 Tên đăng nhập:", usernameField = new TextField());
        usernameField.setPromptText("Nhập tên đăng nhập");

        // Password field
        VBox passwordBox = createInputBox("🔒 Mật khẩu:", passwordField = new PasswordField());
        passwordField.setPromptText("Nhập mật khẩu");

        // Login button
        loginButton = new Button("🚀 Đăng Nhập");
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loginButton.setPrefWidth(200);
        loginButton.setPrefHeight(50);
        loginButton.getStyleClass().add("login-button");
        loginButton.setOnAction(e -> handleLogin());

        // Status label
        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setTextFill(Color.RED);

        // Server connection info
        VBox serverBox = createServerConnectionBox();

        formContainer.getChildren().addAll(
            usernameBox, passwordBox, loginButton, statusLabel
        );

        mainContainer.getChildren().addAll(
            titleLabel, subtitleLabel, formContainer, serverBox
        );

        // Enable Enter key for login
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Scene scene = new Scene(mainContainer, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/login.css").toExternalForm());
        
        stage.setTitle("Word Game - Đăng Nhập");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private VBox createInputBox(String labelText, TextField field) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.WHITE);

        field.setPrefHeight(45);
        field.setFont(Font.font("Arial", 14));
        field.getStyleClass().add("input-field");

        box.getChildren().addAll(label, field);
        return box;
    }

    private VBox createServerConnectionBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Label serverLabel = new Label("🌐 Kết nối server");
        serverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        serverLabel.setTextFill(Color.WHITE);

        HBox serverInfo = new HBox(15);
        serverInfo.setAlignment(Pos.CENTER);

        TextField hostField = new TextField("127.0.0.1");
        hostField.setPrefWidth(150);
        hostField.setPromptText("Host");
        hostField.getStyleClass().add("server-field");

        TextField portField = new TextField("7777");
        portField.setPrefWidth(80);
        portField.setPromptText("Port");
        portField.getStyleClass().add("server-field");

        Button connectButton = new Button("Kết nối");
        connectButton.getStyleClass().add("connect-button");
        connectButton.setOnAction(e -> connectToServer(hostField.getText(), portField.getText()));

        serverInfo.getChildren().addAll(
            new Label("Host:"), hostField,
            new Label("Port:"), portField,
            connectButton
        );

        box.getChildren().addAll(serverLabel, serverInfo);
        return box;
    }

    private void connectToServer(String host, String port) {
        try {
            if (netClient != null) {
                netClient.close();
            }
            
            int portNum = Integer.parseInt(port);
            netClient = new NetClient(host, portNum);
            netClient.listen(this::onMessage);
            
            statusLabel.setText("✅ Kết nối server thành công!");
            statusLabel.setTextFill(Color.GREEN);
            loginButton.setDisable(false);
            
        } catch (Exception e) {
            statusLabel.setText("❌ Không thể kết nối server: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            loginButton.setDisable(true);
        }
    }

    private void handleLogin() {
        if (netClient == null) {
            statusLabel.setText("❌ Chưa kết nối server!");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("❌ Vui lòng nhập đầy đủ thông tin!");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("🔄 Đang đăng nhập...");
        statusLabel.setTextFill(Color.YELLOW);

        netClient.send(Message.of(MessageType.LOGIN_REQ, new Models.LoginReq(username, password)));
    }

    private void onMessage(Message message) {
        javafx.application.Platform.runLater(() -> {
            switch (message.type) {
                case LOGIN_OK -> {
                    Models.LoginOk loginOk = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LoginOk.class);
                    openMainGameView(loginOk.username(), loginOk.totalPoints());
                }
                case LOGIN_FAIL, ERROR -> {
                    statusLabel.setText("❌ Đăng nhập thất bại! Kiểm tra tài khoản/mật khẩu.");
                    statusLabel.setTextFill(Color.RED);
                    loginButton.setDisable(false);
                }
                default -> {}
            }
        });
    }

    private void openMainGameView(String username, int points) {
        try {
            Stage gameStage = new Stage();
            NetClient client = new NetClient("localhost", 7777);
            MainGameView gameView = new MainGameView(gameStage, client, username);
            gameView.show();
        } catch (Exception e) {
            statusLabel.setText("❌ Lỗi mở giao diện game: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            loginButton.setDisable(false);
        }
    }

    public void show() {
        stage.show();
        
        // Auto connect to default server
        connectToServer("127.0.0.1", "7777");
    }
}