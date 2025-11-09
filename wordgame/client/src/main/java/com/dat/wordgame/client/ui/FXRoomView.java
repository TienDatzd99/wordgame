package com.dat.wordgame.client.ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dat.wordgame.client.NetClient;
import com.dat.wordgame.common.Json;
import com.dat.wordgame.common.Message;
import com.dat.wordgame.common.MessageType;
import com.dat.wordgame.common.Models;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXRoomView {

    private NetClient netClient;
    private String currentUser;
    private String roomId;
    private List<String> players; // Lưu ý: Danh sách này có thể cần được đồng bộ hóa nếu thay đổi
    private FXLobbyView parentLobby; // Tham chiếu đến LOBBY JavaFX

    // UI Components (JavaFX)
    private BorderPane rootPane;
    private Label roomIdLabel;
    private Label hostLabel;
    private Label statusLabel;
    private ObservableList<String> playersListModel;
    private ListView<String> playersList;
    private Button startGameButton;
    private Button inviteButton;
    private Button leaveButton;
    private TextArea chatArea;
    private TextField chatField;

    // Friend invite dialog state
    private boolean isInviteDialogPending = false;

    // CSS cho hiệu ứng glassmorphism
    private final String GLASS_STYLE = "-fx-background-color: rgba(255, 255, 255, 0.25);" +
                                     "-fx-background-radius: 20;" +
                                     "-fx-border-color: rgba(255, 255, 255, 0.4);" +
                                     "-fx-border-width: 2;" +
                                     "-fx-border-radius: 20;";

    public FXRoomView(NetClient netClient, String username, String roomId, List<String> players, FXLobbyView parentLobby) {
        System.out.println("FXRoomView constructor called:");
        System.out.println("  username: " + username);
        System.out.println("  roomId: " + roomId);
        System.out.println("  players: " + players);

        this.netClient = netClient;
        this.currentUser = username;
        this.roomId = roomId;
        this.players = players; // Lưu ý: Đây là một tham chiếu, không phải bản sao
        this.parentLobby = parentLobby;

        initializeUI();
        setupEventHandlers();
    }

    /**
     * Trả về node gốc của view này để hiển thị trong Scene.
     */
    public Parent getView() {
        return rootPane;
    }

    private void initializeUI() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(35, 35, 35, 35));
        rootPane.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #5856D6, #8559D7);");

        // Header
        Node headerPanel = createHeaderPanel();
        rootPane.setTop(headerPanel);
        BorderPane.setMargin(headerPanel, new Insets(0, 0, 25, 0));

        // Center - players and chat
        Node centerPanel = createCenterPanel();
        rootPane.setCenter(centerPanel);

        // Bottom - buttons
        Node bottomPanel = createBottomPanel();
        rootPane.setBottom(bottomPanel);
        BorderPane.setMargin(bottomPanel, new Insets(25, 0, 0, 0));
    }

    private Node createHeaderPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);
        panel.setBackground(Background.EMPTY);

        Label titleLabel = new Label("PHÒNG CHỜ");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);

        roomIdLabel = new Label("Mã phòng: " + roomId);
        roomIdLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        roomIdLabel.setTextFill(Color.web("rgba(255,255,255,0.8)"));

        String hostName = players.isEmpty() ? currentUser : players.get(0);
        hostLabel = new Label("👑 Host: " + hostName);
        hostLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        hostLabel.setTextFill(Color.GOLD);

        panel.getChildren().addAll(titleLabel, roomIdLabel, hostLabel);
        return panel;
    }

    private Node createCenterPanel() {
        HBox panel = new HBox(25);
        panel.setBackground(Background.EMPTY);
        panel.setAlignment(Pos.CENTER);

        Node playersPanel = createPlayersPanel();
        HBox.setHgrow(playersPanel, Priority.ALWAYS);

        Node chatPanel = createChatPanel();
        HBox.setHgrow(chatPanel, Priority.ALWAYS);

        panel.getChildren().addAll(playersPanel, chatPanel);
        return panel;
    }

    private Node createPlayersPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(GLASS_STYLE);
        panel.setPadding(new Insets(25));

        // Title and Status
        Label titleLabel = new Label("👥 Người chơi trong phòng");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        statusLabel = new Label(players.size() + "/2 người");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        statusLabel.setTextFill(Color.web("rgba(255,255,255,0.9)"));

        BorderPane titlePanel = new BorderPane();
        titlePanel.setLeft(titleLabel);
        titlePanel.setRight(statusLabel);
        titlePanel.setBackground(Background.EMPTY);

        // Players list
        playersListModel = FXCollections.observableArrayList(players);
        playersList = new ListView<>(playersListModel);
        playersList.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-background-radius: 10;");
        playersList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    if (item.equals(currentUser)) {
                        setText("🎮 " + item + " (Bạn)");
                        setTextFill(Color.web("#5856D6"));
                        setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    } else {
                        setText("👤 " + item);
                        setTextFill(Color.BLACK);
                        setFont(Font.font("Arial", FontWeight.NORMAL, 16));
                    }
                    setPadding(new Insets(10));
                    setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-background-radius: 5;");
                }
            }
        });

        VBox.setVgrow(playersList, Priority.ALWAYS);
        panel.getChildren().addAll(titlePanel, playersList);
        return panel;
    }

    private Node createChatPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(GLASS_STYLE);
        panel.setPadding(new Insets(25));

        Label titleLabel = new Label("💬 Chat phòng");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-control-inner-background: rgba(255, 255, 255, 0.8); -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-border-radius: 10;");
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Chat input
        HBox inputPanel = new HBox(10);
        inputPanel.setBackground(Background.EMPTY);

        chatField = new TextField();
        chatField.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        chatField.setPromptText("Nhập tin nhắn...");
        chatField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2;");
        HBox.setHgrow(chatField, Priority.ALWAYS);

        Button sendButton = createGlassButton("📤 Gửi", Color.web("#5856D6"));
        sendButton.setOnAction(e -> handleSendChat());

        inputPanel.getChildren().addAll(chatField, sendButton);
        panel.getChildren().addAll(titleLabel, chatArea, inputPanel);
        return panel;
    }

    private Node createBottomPanel() {
        VBox panel = new VBox(15);
        panel.setBackground(Background.EMPTY);
        panel.setAlignment(Pos.CENTER);

        Label waitingLabel = new Label("⏳ Chờ host bắt đầu game...");
        waitingLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        waitingLabel.setTextFill(Color.WHITE);

        // Buttons panel
        HBox buttonsPanel = new HBox(15);
        buttonsPanel.setBackground(Background.EMPTY);
        buttonsPanel.setAlignment(Pos.CENTER);

        inviteButton = createGlassButton("👥 Mời bạn bè", Color.web("#22C55E"));
        inviteButton.setOnAction(e -> handleInvite());

        startGameButton = createGlassButton("🚀 Bắt đầu game", Color.web("#5856D6"));
        startGameButton.setOnAction(e -> handleStartGame());
        startGameButton.setDisable(players.size() < 2);

        String hostName = players.isEmpty() ? currentUser : players.get(0);
        if (!currentUser.equals(hostName)) {
            startGameButton.setVisible(false);
        }

        leaveButton = createGlassButton("🚪 Rời phòng", Color.web("#EF4444"));
        leaveButton.setOnAction(e -> handleLeaveRoom());

        buttonsPanel.getChildren().addAll(inviteButton, startGameButton, leaveButton);
        panel.getChildren().addAll(waitingLabel, buttonsPanel);
        return panel;
    }

    private Button createGlassButton(String text, Color bgColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefSize(180, 50);
        button.setCursor(Cursor.HAND);

        String baseColor = toWebColor(bgColor);
        String hoverColor = toWebColor(bgColor.brighter());

        String baseStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 10;", baseColor);
        String hoverStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 10;", hoverColor);

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));

        return button;
    }

    private String toWebColor(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    private void setupEventHandlers() {
        chatField.setOnAction(e -> handleSendChat());
    }

    private void handleInvite() {
        System.out.println("[RoomView] handleInvite called - requesting friend list for: " + currentUser);
        isInviteDialogPending = true;
        Message request = Message.of(MessageType.FRIEND_LIST_REQ, new Models.FriendListReq(currentUser));
        try {
            netClient.send(request);
        } catch (Exception e) {
            isInviteDialogPending = false;
            showError("Lỗi khi lấy danh sách bạn bè: " + e.getMessage());
        }
    }

    private void handleStartGame() {
        if (players.size() < 2) {
            showAlert("Chưa đủ người", "Cần ít nhất 2 người chơi để bắt đầu!", Alert.AlertType.WARNING);
            return;
        }

        chatArea.appendText("🎮 Bắt đầu game...\n");

        // Chuyển sang FXGameView
        Platform.runLater(() -> {
            FXGameView gameView = new FXGameView(netClient, currentUser, players, roomId, parentLobby);
            parentLobby.showGameView(gameView.getView());
        });
    }

    private void handleLeaveRoom() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc muốn rời phòng?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Models.RoomLeft roomLeft = new Models.RoomLeft(roomId, currentUser, "player_left");
                Message msg = Message.of(MessageType.ROOM_LEFT, roomLeft);
                netClient.send(msg);
            } catch (Exception ex) {
                System.err.println("Error sending ROOM_LEFT: " + ex.getMessage());
            }
            
            // Quay lại lobby
            parentLobby.returnFromRoom();
        }
    }

    private void handleSendChat() {
        String message = chatField.getText().trim();
        if (!message.isEmpty()) {
            try {
                Models.Chat chatMessage = new Models.Chat(roomId, currentUser, message);
                Message msg = Message.of(MessageType.CHAT, chatMessage);
                netClient.send(msg);
                chatField.setText("");
            } catch (Exception e) {
                System.err.println("[RoomView] Error sending chat message: " + e.getMessage());
                chatArea.appendText(currentUser + ": " + message + " (Chỉ bạn thấy - lỗi mạng)\n");
                chatArea.positionCaret(chatArea.getLength());
                chatField.setText("");
            }
        }
    }

    // Public methods for updating UI from network events

    public void addPlayer(String playerName) {
        if (!playersListModel.contains(playerName)) {
            players.add(playerName); // Cập nhật danh sách logic
            playersListModel.add(playerName);
            statusLabel.setText(playersListModel.size() + "/2 người");
            chatArea.appendText("✅ " + playerName + " đã vào phòng\n");

            if (playersListModel.size() >= 2) {
                startGameButton.setDisable(false);
            }
        }
    }

    public void removePlayer(String playerName) {
        players.remove(playerName); // Cập nhật danh sách logic
        playersListModel.remove(playerName);
        statusLabel.setText(playersListModel.size() + "/2 người");
        chatArea.appendText("❌ " + playerName + " đã rời phòng\n");

        if (playersListModel.size() < 2) {
            startGameButton.setDisable(true);
        }
    }

    public void handleMessage(Message message) {
        System.out.println("[FXRoomView] handleMessage called with type: " + message.type);
        Platform.runLater(() -> {
            switch (message.type) {
                case CHAT -> {
                    Models.Chat chatMessage = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.Chat.class);
                    chatArea.appendText(chatMessage.from() + ": " + chatMessage.text() + "\n");
                    chatArea.positionCaret(chatArea.getLength());
                }
                case FRIEND_LIST_RESP -> {
                    if (isInviteDialogPending) {
                        isInviteDialogPending = false;
                        Models.FriendListResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendListResp.class);
                        showInviteFriendsDialogWithData(response.friends());
                    }
                }
                case ROOM_INVITE_RESP -> {
                    Models.RoomInviteResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomInviteResp.class);
                    if (response.success()) {
                        showAlert("Thông báo", response.message(), Alert.AlertType.INFORMATION);
                        chatArea.appendText("📧 " + response.message() + "\n");
                    } else {
                        showAlert("Lỗi", response.message(), Alert.AlertType.ERROR);
                    }
                }
                case ROOM_LEFT -> {
                    // Xử lý khi người chơi khác rời đi
                    Models.RoomLeft roomLeft = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomLeft.class);
                    if (!roomLeft.playerLeft().equals(currentUser)) {
                        removePlayer(roomLeft.playerLeft());
                    }
                }
                default -> {}
            }
        });
    }

    private void showInviteFriendsDialogWithData(List<Models.FriendInfo> friends) {
        List<Models.FriendInfo> onlineFriends = friends.stream()
            .filter(Models.FriendInfo::isOnline)
            .sorted((a, b) -> Integer.compare(b.totalPoints(), a.totalPoints()))
            .collect(Collectors.toList());

        if (onlineFriends.isEmpty()) {
            showAlert("Thông báo", "Không có bạn bè nào đang online để mời!", Alert.AlertType.INFORMATION);
            return;
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Mời bạn bè chơi");
        dialogStage.initOwner(rootPane.getScene().getWindow());
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        // Main panel với HBox layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #5856D6, #3730A3);");

        Label headerLabel = new Label("Chọn bạn bè để mời vào phòng");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setAlignment(Pos.CENTER);

        // HBox chứa table và buttons ở cạnh
        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER);

        // TableView (bên trái)
        TableView<FriendInfoRow> table = new TableView<>();
        ObservableList<FriendInfoRow> tableData = FXCollections.observableArrayList();
        for (Models.FriendInfo friend : onlineFriends) {
            tableData.add(new FriendInfoRow(friend.username(), friend.totalPoints()));
        }
        table.setItems(tableData);

        TableColumn<FriendInfoRow, String> nameCol = new TableColumn<>("Tên bạn");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        centerAlignColumn(nameCol); // Căn giữa

        TableColumn<FriendInfoRow, Integer> pointsCol = new TableColumn<>("Điểm");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(100);
        centerAlignColumn(pointsCol); // Căn giữa

        table.getColumns().addAll(nameCol, pointsCol);
        styleGlassTable(table);
        table.setPrefHeight(250);
        table.setMinWidth(320);

        // VBox chứa nút mời (bên phải)
        VBox buttonPanel = new VBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10));

        Button inviteBtn = new Button("📧 Gửi lời mời");
        inviteBtn.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15 25;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        inviteBtn.setMinWidth(150);
        
        Button cancelBtn = new Button("Hủy");
        cancelBtn.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15 25;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setMinWidth(150);

        // Hover effects
        inviteBtn.setOnMouseEntered(e -> inviteBtn.setStyle(inviteBtn.getStyle() + "-fx-background-color: #45a049;"));
        inviteBtn.setOnMouseExited(e -> inviteBtn.setStyle(inviteBtn.getStyle().replace("-fx-background-color: #45a049;", "-fx-background-color: #4CAF50;")));
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #c0392b;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(cancelBtn.getStyle().replace("-fx-background-color: #c0392b;", "-fx-background-color: #e74c3c;")));

        // Actions
        inviteBtn.setOnAction(e -> {
            FriendInfoRow selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                sendFriendInvite(selected.getName());
                dialogStage.close();
            } else {
                showError("Vui lòng chọn một bạn bè để mời!");
            }
        });
        
        cancelBtn.setOnAction(e -> dialogStage.close());

        buttonPanel.getChildren().addAll(inviteBtn, cancelBtn);

        contentBox.getChildren().addAll(table, buttonPanel);
        root.getChildren().addAll(headerLabel, contentBox);

        Scene scene = new Scene(root, 550, 350);
        scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void styleGlassTable(TableView<FriendInfoRow> table) {
        table.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2);" +
            "-fx-control-inner-background: transparent;" +
            "-fx-table-cell-border-color: rgba(255, 255, 255, 0.3);" +
            "-fx-selection-bar-non-focused: rgba(255, 255, 255, 0.5);"
        );
        table.setRowFactory(tv -> new TableRow<FriendInfoRow>() {
            @Override
            protected void updateItem(FriendInfoRow item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                }
            }
        });
    }

    private void sendFriendInvite(String friendName) {
        try {
            Models.RoomInviteSend invite = new Models.RoomInviteSend(currentUser, friendName, roomId);
            Message msg = Message.of(MessageType.ROOM_INVITE_SEND, invite);
            netClient.send(msg);
        } catch (Exception e) {
            showError("Lỗi khi gửi lời mời chơi: " + e.getMessage());
        }
    }

    // Lớp nội bộ cho Bảng mời bạn bè
    public static class FriendInfoRow {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty points;

        public FriendInfoRow(String name, int points) {
            this.name = new SimpleStringProperty(name);
            this.points = new SimpleIntegerProperty(points);
        }
        public String getName() { return name.get(); }
        public int getPoints() { return points.get(); }
    }
    
    // Phương thức trợ giúp hiển thị Lỗi
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Phương thức trợ giúp căn giữa cột
    private <T> void centerAlignColumn(TableColumn<FriendInfoRow, T> column) {
        column.setCellFactory(col -> new TableCell<FriendInfoRow, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // Phương thức trợ giúp hiển thị Thông báo
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}