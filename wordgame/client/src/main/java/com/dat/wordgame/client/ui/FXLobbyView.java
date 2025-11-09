package com.dat.wordgame.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu; // Dành cho việc đăng xuất
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FXLobbyView {

    private Stage stage;
    private NetClient netClient;
    private String currentUser;
    private StackPane rootPane; // Đặt làm biến thành viên để quay lại

    // UI Components
    private TableView<PlayerRow> playersTable;
    private TableView<RankingRow> rankingTable;
    private ObservableList<PlayerRow> playersData;
    private ObservableList<RankingRow> rankingData;
    private Label welcomeLabel;

    // Child views
    private FXGameView currentGameView; // JavaFX GameView
    private FXRoomView currentRoomView; // JavaFX RoomView
    
    private boolean waitingForFriendList = false;
    
    // Match history dialog references
    private ObservableList<MatchHistoryRow> currentHistoryData;
    private Stage currentHistoryDialog;
    
    // Track online players for leaderboard highlighting
    private Set<String> onlinePlayers = new HashSet<>();

    public FXLobbyView(Stage stage, NetClient netClient, String username) {
        this.stage = stage;
        this.netClient = netClient;
        this.currentUser = username;

        // Note: netClient.listen() đã được gọi trong FXLoginView
        // FXLoginView sẽ forward messages đến FXLobbyView

        initializeUI();
        requestPlayersList();
        requestRankingData();
    }

    private void initializeUI() {
        // Root with purple gradient
        rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #8559d7);");

        // Main content
        VBox mainContent = new VBox(25);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(30));
        mainContent.setMaxWidth(1400);

        // Header
        VBox header = createHeader();

        // Tables container
        HBox tablesContainer = createTablesContainer();
        VBox.setVgrow(tablesContainer, Priority.ALWAYS);

        // Bottom buttons
        HBox buttonBar = createButtonBar();

        mainContent.getChildren().addAll(header, tablesContainer, buttonBar);
        rootPane.getChildren().add(mainContent);

        Scene scene = new Scene(rootPane, 1200, 800);
        stage.setTitle("WordGame - Lobby");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            try {
                netClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Platform.exit();
        });
    }

    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        welcomeLabel = new Label("Chào mừng, " + currentUser + "!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        welcomeLabel.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Chọn chế độ chơi hoặc xem bảng xếp hạng");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.8);");

        header.getChildren().addAll(welcomeLabel, subtitle);
        return header;
    }

    private HBox createTablesContainer() {
        HBox container = new HBox(25);
        container.setAlignment(Pos.CENTER);

        // Players panel
        VBox playersPanel = createPlayersPanel();
        HBox.setHgrow(playersPanel, Priority.ALWAYS);

        // Ranking panel
        VBox rankingPanel = createRankingPanel();
        HBox.setHgrow(rankingPanel, Priority.ALWAYS);

        container.getChildren().addAll(playersPanel, rankingPanel);
        return container;
    }

    private VBox createPlayersPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 25;"
        );

        Label title = new Label("🎮 Người chơi đang online");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Create players table
        playersTable = new TableView<>();
        playersData = FXCollections.observableArrayList();
        playersTable.setItems(playersData);

        TableColumn<PlayerRow, String> nameCol = new TableColumn<>("Tên người chơi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<PlayerRow, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);

        TableColumn<PlayerRow, Integer> pointsCol = new TableColumn<>("Điểm số");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(100);

        playersTable.getColumns().addAll(nameCol, statusCol, pointsCol);
        styleTable(playersTable);

        // Context menu for challenging
        playersTable.setRowFactory(tv -> {
            TableRow<PlayerRow> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                PlayerRow player = row.getItem();
                if (player != null && !player.getName().equals(currentUser)) {
                    ContextMenu contextMenu = createChallengeMenu(player.getName());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        VBox.setVgrow(playersTable, Priority.ALWAYS);
        panel.getChildren().addAll(title, playersTable);
        return panel;
    }

    private VBox createRankingPanel() {
        VBox panel = new VBox(15);
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 25;"
        );

        Label title = new Label("🏆 Bảng xếp hạng");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Create ranking table
        rankingTable = new TableView<>();
        rankingData = FXCollections.observableArrayList();
        rankingTable.setItems(rankingData);

        TableColumn<RankingRow, Integer> rankCol = new TableColumn<>("Hạng");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(70);

        TableColumn<RankingRow, String> nameCol = new TableColumn<>("Tên người chơi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<RankingRow, Integer> totalPointsCol = new TableColumn<>("Tổng điểm");
        totalPointsCol.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
        totalPointsCol.setPrefWidth(120);

        TableColumn<RankingRow, String> winsCol = new TableColumn<>("Số trận thắng");
        winsCol.setCellValueFactory(new PropertyValueFactory<>("wins"));
        winsCol.setPrefWidth(130);

        rankingTable.getColumns().addAll(rankCol, nameCol, totalPointsCol, winsCol);
        styleTable(rankingTable);
        
        // Thêm styling cho hàng: nền sáng nếu online, chuột phải để thách đấu
        rankingTable.setRowFactory(tv -> {
            TableRow<RankingRow> row = new TableRow<>();
            
            // Tạo context menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem challengeItem = new MenuItem("⚔️ Gửi thách đấu");
            challengeItem.setOnAction(e -> {
                if (!row.isEmpty()) {
                    String playerName = row.getItem().getName();
                    sendChallenge(playerName);
                }
            });
            contextMenu.getItems().add(challengeItem);
            
            // Chỉ hiện context menu khi chuột phải vào người chơi online (không phải chính mình)
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    String playerName = row.getItem().getName();
                    
                    // Không cho thách đấu chính mình
                    if (playerName.equals(currentUser)) {
                        return;
                    }
                    
                    // Chỉ hiện menu nếu người chơi đang online
                    if (onlinePlayers.contains(playerName)) {
                        challengeItem.setText("⚔️ Gửi thách đấu đến " + playerName);
                        contextMenu.show(row, event.getScreenX(), event.getScreenY());
                    } else {
                        showInfo(playerName + " hiện đang offline, không thể gửi thách đấu!");
                    }
                }
            });
            
            // Style: Nền sáng nếu online
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String playerName = newItem.getName();
                    boolean isOnline = onlinePlayers.contains(playerName);
                    boolean isCurrentUser = playerName.equals(currentUser);
                    
                    if (isCurrentUser) {
                        // Highlight người chơi hiện tại màu khác
                        row.setStyle("-fx-background-color: rgba(52, 152, 219, 0.3);");
                    } else if (isOnline) {
                        // Online: nền sáng
                        row.setStyle("-fx-background-color: rgba(46, 204, 113, 0.2);");
                    } else {
                        // Offline: nền tối
                        row.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05);");
                    }
                }
            });
            
            return row;
        });

        VBox.setVgrow(rankingTable, Priority.ALWAYS);
        panel.getChildren().addAll(title, rankingTable);
        return panel;
    }

    private void styleTable(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-control-inner-background: rgba(255,255,255,0.1);" +
                "-fx-table-cell-border-color: rgba(255,255,255,0.2);"
        );
        
        table.setPlaceholder(new Label("Không có dữ liệu"));
        
        // Cần thêm CSS để style header và text fill, nhưng
        // các đoạn mã gốc không có, nên giữ đơn giản.
    }

    private ContextMenu createChallengeMenu(String playerName) {
        ContextMenu menu = new ContextMenu();
        MenuItem challengeItem = new MenuItem("⚔️ Gửi thách đấu đến " + playerName);
        challengeItem.setStyle("-fx-background-color: #5856d6; -fx-text-fill: white;");
        challengeItem.setOnAction(e -> sendChallenge(playerName));
        menu.getItems().add(challengeItem);
        return menu;
    }

    private void sendChallenge(String playerName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thách đấu");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có muốn gửi lời thách đấu đến " + playerName + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Models.InviteSend inviteMsg = new Models.InviteSend(currentUser, playerName);
                Message msg = Message.of(MessageType.INVITE_SEND, inviteMsg);
                netClient.send(msg);
                showInfo("Đã gửi lời thách đấu đến " + playerName + "!\nChờ đối thủ chấp nhận...");
            } catch (Exception ex) {
                showError("Lỗi khi gửi thách đấu: " + ex.getMessage());
            }
        }
    }

    private HBox createButtonBar() {
        HBox buttonBar = new HBox(20);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        Button startGameBtn = createStyledButton("🏠 Tạo phòng", "#2ecc71");
        Button friendsBtn = createStyledButton("👥 Bạn bè", "#9b59b6");
        Button historyBtn = createStyledButton("📜 Lịch sử đấu", "#f39c12");
        Button refreshBtn = createStyledButton("🔄 Làm mới", "#3498db");
        Button logoutBtn = createStyledButton("🚪 Đăng xuất", "#e74c3c");

        startGameBtn.setOnAction(e -> createRoom());
        friendsBtn.setOnAction(e -> showFriendsDialog());
        historyBtn.setOnAction(e -> showMatchHistory());
        refreshBtn.setOnAction(e -> refreshData());
        logoutBtn.setOnAction(e -> logout());

        buttonBar.getChildren().addAll(startGameBtn, friendsBtn, historyBtn, refreshBtn, logoutBtn);
        return buttonBar;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        String baseStyle = "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 25;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";
        
        String hoverStyle = "-fx-background-color: derive(" + color + ", -20%);" +
                "-fx-text-fill: white;" +
                "-fx-padding: 12 25;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        return button;
    }

    private void createRoom() {
        String roomId = "room_" + System.currentTimeMillis();
        List<String> players = new ArrayList<>();
        players.add(currentUser);

        // Khởi chạy FXRoomView
        currentRoomView = new FXRoomView(netClient, currentUser, roomId, players, this);
        stage.getScene().setRoot(currentRoomView.getView());
    }

    private void showFriendsDialog() {
        waitingForFriendList = true;
        Message request = Message.of(MessageType.FRIEND_LIST_REQ, new Models.FriendListReq(currentUser));
        try {
            netClient.send(request);
        } catch (Exception e) {
            waitingForFriendList = false;
            showError("Lỗi khi lấy danh sách bạn bè: " + e.getMessage());
        }
    }

    private void refreshData() {
        requestPlayersList();
        requestRankingData();
    }

    private void showMatchHistory() {
        // Tạo dialog hiển thị lịch sử đấu
        Stage historyDialog = new Stage();
        historyDialog.initOwner(stage);
        historyDialog.setTitle("📜 Lịch sử đấu");
        
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background: linear-gradient(to bottom, #5856d6, #8559d7);");
        
        Label titleLabel = new Label("📜 Lịch sử đấu của " + currentUser);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        // Tạo bảng hiển thị lịch sử
        TableView<MatchHistoryRow> historyTable = new TableView<>();
        ObservableList<MatchHistoryRow> historyData = FXCollections.observableArrayList();
        
        TableColumn<MatchHistoryRow, String> dateCol = new TableColumn<>("Ngày");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(150);
        
        TableColumn<MatchHistoryRow, String> opponentCol = new TableColumn<>("Đối thủ");
        opponentCol.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        opponentCol.setPrefWidth(150);
        
        TableColumn<MatchHistoryRow, String> resultCol = new TableColumn<>("Kết quả");
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultCol.setPrefWidth(120);
        
        TableColumn<MatchHistoryRow, Integer> scoreCol = new TableColumn<>("Điểm");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setPrefWidth(80);
        
        historyTable.getColumns().addAll(dateCol, opponentCol, resultCol, scoreCol);
        historyTable.setItems(historyData);
        styleTable(historyTable);
        
        // Gửi request lên server để lấy lịch sử đấu
        Models.MatchHistoryReq request = new Models.MatchHistoryReq(currentUser, 20);
        Message requestMsg = Message.of(MessageType.MATCH_HISTORY_REQ, request);
        System.out.println("[FXLobbyView] Creating match history request for: " + currentUser);
        System.out.println("[FXLobbyView] Request message: " + requestMsg);
        netClient.send(requestMsg);
        System.out.println("[FXLobbyView] Match history request sent!");
        
        // Store reference để update khi nhận response
        currentHistoryData = historyData;
        currentHistoryDialog = historyDialog;
        
        Label noteLabel = new Label("⏳ Đang tải lịch sử đấu...");
        noteLabel.setWrapText(true);
        noteLabel.setStyle("-fx-text-fill: #ffeb3b; -fx-font-size: 12px;");
        
        Button closeBtn = createStyledButton("Đóng", "#e74c3c");
        closeBtn.setOnAction(e -> {
            currentHistoryData = null;
            currentHistoryDialog = null;
            historyDialog.close();
        });
        
        mainLayout.getChildren().addAll(titleLabel, historyTable, noteLabel, closeBtn);
        
        Scene scene = new Scene(mainLayout, 600, 450);
        historyDialog.setScene(scene);
        historyDialog.show();
    }

    private void logout() {
        try {
            netClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return to login
        Stage loginStage = new Stage();
        FXLoginView loginView = new FXLoginView();
        try {
            loginView.start(loginStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stage.close();
    }

    public void show() {
        stage.show();
        refreshData(); // Làm mới dữ liệu khi quay lại
    }

    public void requestPlayersList() {
        Message request = Message.of(MessageType.PLAYER_LIST, currentUser);
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy danh sách người chơi: " + e.getMessage());
        }
    }

    public void requestRankingData() {
        Message request = Message.of(MessageType.LEADERBOARD, currentUser);
        try {
            netClient.send(request);
        } catch (Exception e) {
            showError("Lỗi khi lấy bảng xếp hạng: " + e.getMessage());
        }
    }

    public void handleMessage(Message message) {
        System.out.println("FXLobbyView: handleMessage called with type: " + message.type);

        // Chuyển tiếp đến các view con JavaFX nếu chúng đang hoạt động
        if (currentGameView != null) {
            switch (message.type) {
                case ROUND_START, ROUND_TICK, ROUND_END, GAME_END, GUESS_UPDATE, CHAT:
                    currentGameView.onMessage(message);
                    return;
            }
        } else if (currentRoomView != null) {
            switch (message.type) {
                case CHAT, ROOM_LEFT, FRIEND_LIST_RESP, ROOM_INVITE_RESP:
                    currentRoomView.handleMessage(message);
                    return;
            }
        }
        
        // Nếu không, xử lý tại Lobby
        Platform.runLater(() -> {
            switch (message.type) {
                case LOBBY_SNAPSHOT, PLAYER_LIST, LEADERBOARD -> {
                    // Server có thể trả về LobbySnapshot cho cả 3 message types
                    Models.LobbySnapshot snapshot = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.LobbySnapshot.class);
                    
                    // Cập nhật danh sách người chơi online
                    if (snapshot.online() != null && !snapshot.online().isEmpty()) {
                        playersData.clear();
                        onlinePlayers.clear(); // Clear và rebuild danh sách online
                        for (Models.PlayerBrief player : snapshot.online()) {
                            String status = player.status() != null ? player.status() : "🟢 Online";
                            playersData.add(new PlayerRow(player.name(), status, player.points()));
                            onlinePlayers.add(player.name()); // Lưu lại người chơi online
                        }
                        System.out.println("[FXLobbyView] Updated players list: " + playersData.size() + " players");
                    }

                    // Cập nhật bảng xếp hạng
                    if (snapshot.leaderboard() != null && !snapshot.leaderboard().isEmpty()) {
                        rankingData.clear();
                        int rank = 1;
                        for (Models.PlayerBrief player : snapshot.leaderboard()) {
                            rankingData.add(new RankingRow(rank++, player.name(), player.points(), String.valueOf(player.wins())));
                        }
                        System.out.println("[FXLobbyView] Updated leaderboard: " + rankingData.size() + " players");
                        
                        // Refresh table để cập nhật styling (online/offline)
                        rankingTable.refresh();
                    }
                }
                case INVITE_RECEIVE -> {
                    Models.InviteReceive invite = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.InviteReceive.class);
                    handleInviteReceived(invite.from());
                }
                case ROOM_CREATED -> {
                    Models.RoomState roomState = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomState.class);
                    handleRoomCreated(roomState);
                }
                case ROOM_JOINED -> {
                    Models.RoomState roomState = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomState.class);
                    openGameView(roomState);
                }
                case ROOM_INVITE_RECEIVE -> {
                    Models.RoomInviteReceive invite = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomInviteReceive.class);
                    handleRoomInviteReceived(invite.from(), invite.roomId());
                }
                case FRIEND_REQUEST_RECEIVE -> {
                    Models.FriendRequestReceive request = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendRequestReceive.class);
                    handleFriendRequestReceived(request.from());
                }
                case FRIEND_LIST_RESP -> {
                    if (waitingForFriendList) {
                        waitingForFriendList = false;
                        Models.FriendListResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendListResp.class);
                        showFriendsDialogWithData(response.friends());
                    }
                }
                case FRIEND_INVITE_RESP -> {
                    Models.FriendInviteResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.FriendInviteResp.class);
                    if (response.success()) {
                        showInfo(response.message());
                    } else {
                        showError(response.message());
                    }
                }
                case ROOM_INVITE_RESP -> {
                     Models.RoomInviteResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.RoomInviteResp.class);
                     if (response.success()) {
                         showInfo(response.message());
                     } else {
                         showError(response.message());
                     }
                }
                case USER_SEARCH_RESP -> {
                    Models.UserSearchResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.UserSearchResp.class);
                    handleUserSearchResponse(response.results());
                }
                case MATCH_HISTORY_RESP -> {
                    Models.MatchHistoryResp response = Json.GSON.fromJson(Json.GSON.toJson(message.payload), Models.MatchHistoryResp.class);
                    handleMatchHistoryResponse(response.matches());
                }
                case ROOM_LEFT -> {
                    // Đã được xử lý bằng cách chuyển tiếp ở trên
                }
            }
        });
    }

    private void handleInviteReceived(String fromPlayer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lời thách đấu");
        alert.setHeaderText(null);
        alert.setContentText(fromPlayer + " đã gửi lời thách đấu đến bạn!\n\nBạn có chấp nhận không?");

        Optional<ButtonType> result = alert.showAndWait();
        try {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Models.InviteAccept acceptMsg = new Models.InviteAccept(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.INVITE_ACCEPT, acceptMsg);
                netClient.send(msg);
            } else {
                Models.InviteReject rejectMsg = new Models.InviteReject(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.INVITE_REJECT, rejectMsg);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi thách đấu: " + ex.getMessage());
        }
    }

    private void handleRoomCreated(Models.RoomState roomState) {
        System.out.println("FXLobbyView: handleRoomCreated for room " + roomState.roomId());
        
        List<String> players = new ArrayList<>();
        players.add(roomState.host());
        if (roomState.opponent() != null && !roomState.opponent().isEmpty()) {
            players.add(roomState.opponent());
        }

        // Khởi chạy FXRoomView
        currentRoomView = new FXRoomView(netClient, currentUser, roomState.roomId(), players, this);
        stage.getScene().setRoot(currentRoomView.getView());
    }

    private void handleRoomInviteReceived(String fromPlayer, String roomId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lời mời vào phòng");
        alert.setHeaderText(null);
        alert.setContentText(fromPlayer + " đã mời bạn vào phòng chơi!\n\nBạn có chấp nhận không?");

        Optional<ButtonType> result = alert.showAndWait();
        try {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Models.RoomInviteAccept acceptMsg = new Models.RoomInviteAccept(fromPlayer, currentUser, roomId);
                Message msg = Message.of(MessageType.ROOM_INVITE_ACCEPT, acceptMsg);
                netClient.send(msg);
            } else {
                Models.RoomInviteReject rejectMsg = new Models.RoomInviteReject(fromPlayer, currentUser, roomId);
                Message msg = Message.of(MessageType.ROOM_INVITE_REJECT, rejectMsg);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi lời mời vào phòng: " + ex.getMessage());
        }
    }

    private void handleFriendRequestReceived(String fromPlayer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lời mời kết bạn");
        alert.setHeaderText(null);
        alert.setContentText(fromPlayer + " muốn kết bạn với bạn!\n\nBạn có chấp nhận không?");

        Optional<ButtonType> result = alert.showAndWait();
        try {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Models.FriendRequestAccept accept = new Models.FriendRequestAccept(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.FRIEND_REQUEST_ACCEPT, accept);
                netClient.send(msg);
                showInfo("Đã chấp nhận lời mời kết bạn từ " + fromPlayer + "!");
            } else {
                Models.FriendRequestReject reject = new Models.FriendRequestReject(fromPlayer, currentUser);
                Message msg = Message.of(MessageType.FRIEND_REQUEST_REJECT, reject);
                netClient.send(msg);
            }
        } catch (Exception ex) {
            showError("Lỗi khi phản hồi lời mời kết bạn: " + ex.getMessage());
        }
    }

    private void showFriendsDialogWithData(List<Models.FriendInfo> friends) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Danh sách bạn bè");
        dialogStage.initOwner(stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #8559d7);");

        Label header = new Label("Danh sách bạn bè (" + friends.size() + ")");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setStyle("-fx-text-fill: white;");

        // Friends table
        TableView<FriendRow> friendsTable = new TableView<>();
        ObservableList<FriendRow> friendsData = FXCollections.observableArrayList();

        friends.sort((a, b) -> {
            if (a.isOnline() && !b.isOnline()) return -1;
            if (!a.isOnline() && b.isOnline()) return 1;
            return Integer.compare(b.totalPoints(), a.totalPoints());
        });

        for (Models.FriendInfo friend : friends) {
            String status = friend.isOnline() ? "🟢 Online" : "🔴 Offline";
            friendsData.add(new FriendRow(friend.username(), status, friend.totalPoints()));
        }

        TableColumn<FriendRow, String> nameCol = new TableColumn<>("Tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<FriendRow, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<FriendRow, Integer> pointsCol = new TableColumn<>("Điểm");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(100);

        friendsTable.getColumns().addAll(nameCol, statusCol, pointsCol);
        friendsTable.setItems(friendsData);
        styleTable(friendsTable);
        
        // Search panel
        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("🔍 Tìm kiếm:");
        searchLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Nhập tên người chơi...");
        searchField.setPrefWidth(200);
        
        Button searchBtn = createStyledButton("Tìm", "#3498db");
        searchBtn.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                showUserSearchDialog(searchText);
            }
        });
        
        searchField.setOnAction(e -> searchBtn.fire());
        searchPanel.getChildren().addAll(searchLabel, searchField, searchBtn);

        // Buttons
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);

        Button addFriendBtn = createStyledButton("➕ Thêm bạn", "#2ecc71");
        Button closeBtn = createStyledButton("Đóng", "#e74c3c");

        addFriendBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Thêm bạn");
            dialog.setHeaderText(null);
            dialog.setContentText("Nhập tên người chơi để kết bạn:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(friendName -> {
                if (!friendName.trim().isEmpty()) {
                    sendFriendRequest(friendName.trim());
                }
            });
        });

        closeBtn.setOnAction(e -> dialogStage.close());
        buttonBar.getChildren().addAll(addFriendBtn, closeBtn);

        VBox.setVgrow(friendsTable, Priority.ALWAYS);
        root.getChildren().addAll(header, searchPanel, friendsTable, buttonBar);

        Scene scene = new Scene(root, 550, 500);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
    
    private void showUserSearchDialog(String searchText) {
        try {
            Models.UserSearchReq request = new Models.UserSearchReq(searchText, currentUser);
            Message msg = Message.of(MessageType.USER_SEARCH_REQ, request);
            netClient.send(msg);
        } catch (Exception e) {
            showError("Lỗi khi tìm kiếm người chơi: " + e.getMessage());
        }
    }
    
    private void handleMatchHistoryResponse(List<Models.MatchHistoryEntry> matches) {
        System.out.println("[FXLobbyView] Received match history response with " + matches.size() + " entries");
        
        Platform.runLater(() -> {
            if (currentHistoryData != null) {
                currentHistoryData.clear();
                
                if (matches.isEmpty()) {
                    System.out.println("[FXLobbyView] No match history data available");
                } else {
                    for (Models.MatchHistoryEntry entry : matches) {
                        System.out.println("[FXLobbyView] Adding match: " + entry.date() + " vs " + entry.opponent() + " - " + entry.result());
                        currentHistoryData.add(new MatchHistoryRow(
                            entry.date(), 
                            entry.opponent(), 
                            entry.result(), 
                            entry.score(),
                            entry.opponentScore()
                        ));
                    }
                    System.out.println("[FXLobbyView] Successfully loaded " + matches.size() + " match history entries");
                }
            } else {
                System.out.println("[FXLobbyView] WARNING: currentHistoryData is null!");
            }
        });
    }

    private void handleUserSearchResponse(List<Models.UserSearchResult> results) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Kết quả tìm kiếm");
        dialogStage.initOwner(stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #5856d6, #8559d7);");

        Label header = new Label("🔍 Tìm thấy " + results.size() + " kết quả");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.setStyle("-fx-text-fill: white;");

        // Results table
        TableView<SearchResultRow> resultsTable = new TableView<>();
        ObservableList<SearchResultRow> resultsData = FXCollections.observableArrayList();

        for (Models.UserSearchResult result : results) {
            String status = result.isFriend() ? "🟢 Đã kết bạn" : "⚪ Chưa kết bạn";
            String action = result.isFriend() ? "Mời chơi" : "Kết bạn";
            resultsData.add(new SearchResultRow(result.username(), result.points(), status, action, result.isFriend()));
        }

        TableColumn<SearchResultRow, String> nameCol = new TableColumn<>("Tên người chơi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<SearchResultRow, Integer> pointsCol = new TableColumn<>("Điểm số");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsCol.setPrefWidth(100);

        TableColumn<SearchResultRow, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);

        TableColumn<SearchResultRow, Void> actionCol = new TableColumn<>("Thao tác");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> {
            TableCell<SearchResultRow, Void> cell = new TableCell<>() {
                private final Button btn = new Button();
                {
                    btn.setOnAction(e -> {
                        SearchResultRow row = getTableView().getItems().get(getIndex());
                        if (row.isFriend()) {
                            sendGameInviteToFriend(row.getName());
                        } else {
                            sendFriendRequest(row.getName());
                        }
                        dialogStage.close();
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        SearchResultRow row = getTableView().getItems().get(getIndex());
                        btn.setText(row.getAction());
                        btn.setStyle(row.isFriend() ? 
                            "-fx-background-color: #9b59b6; -fx-text-fill: white;" :
                            "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        setGraphic(btn);
                    }
                }
            };
            return cell;
        });

        resultsTable.getColumns().addAll(nameCol, pointsCol, statusCol, actionCol);
        resultsTable.setItems(resultsData);
        styleTable(resultsTable);

        Button closeBtn = createStyledButton("Đóng", "#e74c3c");
        closeBtn.setOnAction(e -> dialogStage.close());
        HBox buttonBar = new HBox(closeBtn);
        buttonBar.setAlignment(Pos.CENTER);

        VBox.setVgrow(resultsTable, Priority.ALWAYS);
        root.getChildren().addAll(header, resultsTable, buttonBar);

        Scene scene = new Scene(root, 600, 400);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
    
    private void sendGameInviteToFriend(String friendName) {
        try {
            String roomId = "room_" + System.currentTimeMillis();
            Models.RoomInviteSend invite = new Models.RoomInviteSend(currentUser, friendName, roomId);
            Message msg = Message.of(MessageType.ROOM_INVITE_SEND, invite);
            netClient.send(msg);
            showInfo("Đã gửi lời mời chơi đến " + friendName + "!");
        } catch (Exception e) {
            showError("Lỗi khi gửi lời mời chơi: " + e.getMessage());
        }
    }

    private void sendFriendRequest(String friendName) {
        try {
            Models.FriendRequest request = new Models.FriendRequest(currentUser, friendName);
            Message msg = Message.of(MessageType.FRIEND_REQUEST_SEND, request);
            netClient.send(msg);
            showInfo("Đã gửi lời mời kết bạn đến " + friendName + "!");
        } catch (Exception e) {
            showError("Lỗi khi gửi lời mời kết bạn: " + e.getMessage());
        }
    }

    private void openGameView(Models.RoomState roomState) {
        System.out.println("FXLobbyView: Opening GameView for room " + roomState.roomId());

        List<String> players = new ArrayList<>();
        players.add(roomState.host());
        if (roomState.opponent() != null && !roomState.opponent().isEmpty()) {
            players.add(roomState.opponent());
        }

        // Khởi chạy JavaFX GameView
        currentGameView = new FXGameView(netClient, currentUser, players, roomState.roomId(), this);
        showGameView(currentGameView.getView());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Lấy danh sách người chơi online (dùng cho invite dialog trong GameView)
     */
    public List<String> getOnlinePlayers() {
        List<String> onlinePlayers = new ArrayList<>();
        for (PlayerRow row : playersData) {
            if ("🟢 Online".equals(row.getStatus())) {
                onlinePlayers.add(row.getName());
            }
        }
        return onlinePlayers;
    }
    
    /**
     * Quay về lobby view từ game view
     */
    public void returnFromGame() {
        Platform.runLater(() -> {
            // Xóa currentGameView nếu có
            currentGameView = null;
            currentRoomView = null;
            
            // Hiển thị lại lobby bằng cách set root về rootPane
            stage.getScene().setRoot(rootPane);
            
            // Yêu cầu cập nhật DỮ LIỆU MỚI - bao gồm cả PLAYER_LIST và LEADERBOARD
            try {
                Message playerListMsg = Message.of(MessageType.PLAYER_LIST, currentUser);
                netClient.send(playerListMsg);
                
                Message leaderboardMsg = Message.of(MessageType.LEADERBOARD, currentUser);
                netClient.send(leaderboardMsg);
                
                System.out.println("[FXLobbyView] Refreshed lobby data after game ended");
            } catch (Exception e) {
                showError("Lỗi làm mới lobby: " + e.getMessage());
            }
        });
    }
    
    /**
     * Quay về lobby view từ room view
     */
    public void returnFromRoom() {
        Platform.runLater(() -> {
            // Xóa currentRoomView nếu có
            currentRoomView = null;
            currentGameView = null;
            
            // Hiển thị lại lobby bằng cách set root về rootPane
            stage.getScene().setRoot(rootPane);
            
            // Yêu cầu cập nhật dữ liệu mới
            try {
                Message msg = Message.of(MessageType.PLAYER_LIST, null);
                netClient.send(msg);
            } catch (Exception e) {
                showError("Lỗi làm mới lobby: " + e.getMessage());
            }
        });
    }
    
    /**
     * Hiển thị game view (được gọi từ RoomView khi bắt đầu game)
     */
    public void showGameView(Parent gameViewRoot) {
        // Thay đổi scene sang game view
        stage.getScene().setRoot(gameViewRoot);
    }

    // Data classes for TableView
    public static class PlayerRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty status;
        private final SimpleIntegerProperty points;

        public PlayerRow(String name, String status, int points) {
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(status);
            this.points = new SimpleIntegerProperty(points);
        }
        public String getName() { return name.get(); }
        public String getStatus() { return status.get(); }
        public int getPoints() { return points.get(); }
    }

    public static class RankingRow {
        private final SimpleIntegerProperty rank;
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty totalPoints;
        private final SimpleStringProperty wins;

        public RankingRow(int rank, String name, int totalPoints, String wins) {
            this.rank = new SimpleIntegerProperty(rank);
            this.name = new SimpleStringProperty(name);
            this.totalPoints = new SimpleIntegerProperty(totalPoints);
            this.wins = new SimpleStringProperty(wins);
        }
        public int getRank() { return rank.get(); }
        public String getName() { return name.get(); }
        public int getTotalPoints() { return totalPoints.get(); }
        public String getWins() { return wins.get(); }
    }

    public static class FriendRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty status;
        private final SimpleIntegerProperty points;

        public FriendRow(String name, String status, int points) {
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(status);
            this.points = new SimpleIntegerProperty(points);
        }
        public String getName() { return name.get(); }
        public String getStatus() { return status.get(); }
        public int getPoints() { return points.get(); }
    }
    
    public static class SearchResultRow {
        private final SimpleStringProperty name;
        private final SimpleIntegerProperty points;
        private final SimpleStringProperty status;
        private final SimpleStringProperty action;
        private final boolean isFriend;
        
        public SearchResultRow(String name, int points, String status, String action, boolean isFriend) {
            this.name = new SimpleStringProperty(name);
            this.points = new SimpleIntegerProperty(points);
            this.status = new SimpleStringProperty(status);
            this.action = new SimpleStringProperty(action);
            this.isFriend = isFriend;
        }
        
        public String getName() { return name.get(); }
        public int getPoints() { return points.get(); }
        public String getStatus() { return status.get(); }
        public String getAction() { return action.get(); }
        public boolean isFriend() { return isFriend; }
    }

    public static class MatchHistoryRow {
        private final SimpleStringProperty date;
        private final SimpleStringProperty opponent;
        private final SimpleStringProperty result;
        private final SimpleIntegerProperty score;
        private final SimpleIntegerProperty opponentScore;

        public MatchHistoryRow(String date, String opponent, String result, int score, int opponentScore) {
            this.date = new SimpleStringProperty(date);
            this.opponent = new SimpleStringProperty(opponent);
            this.result = new SimpleStringProperty(result);
            this.score = new SimpleIntegerProperty(score);
            this.opponentScore = new SimpleIntegerProperty(opponentScore);
        }

        public String getDate() { return date.get(); }
        public String getOpponent() { return opponent.get(); }
        public String getResult() { return result.get(); }
        public int getScore() { return score.get(); }
        public int getOpponentScore() { return opponentScore.get(); }
    }
}