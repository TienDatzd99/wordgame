# 🎮 Word Game - Trò chơi đoán từ multiplayer

## 📋 Mô tả

Word Game là một trò chơi đoán từ multiplayer được phát triển bằng Java với kiến trúc client-server. Game có cả giao diện đồ họa hiện đại (Swing) và giao diện console truyền thống.

### ✨ Tính năng chính

- 🔐 **Hệ thống đăng nhập**: Bảo mật với username/password
- 🏆 **Hệ thống điểm số**: Theo dõi điểm và bảng xếp hạng
- 👥 **Multiplayer**: Mời bạn bè chơi cùng
- 💬 **Chat real-time**: Trò chuyện trong game
- ⏰ **Giới hạn thời gian**: Tăng tính thử thách
- 🎯 **Nhiều độ khó**: Easy, Medium, Hard, Insane
- 🎨 **Giao diện hiện đại**: GUI đẹp mắt với Swing

## 🛠️ Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6 hoặc cao hơn
- Windows/Linux/MacOS

## 🚀 Cách chạy

### 1. Khởi động Server

```bash
# Chạy script tự động
run-server.bat

# Hoặc chạy manual
mvn clean package
java -cp server/target/server-1.0.0-jar-with-dependencies.jar com.dat.wordgame.server.ServerMain
```

Server sẽ chạy trên port 7777 và tự động tạo database SQLite với các tài khoản mẫu:
- Username: `alice`, `bob`, `charlie`, `dora`
- Password: `123` (cho tất cả)

### 2. Khởi động Client - Giao diện đồ họa (Khuyến nghị)

```bash
# Chạy script tự động
run-gui.bat

# Hoặc chạy manual
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain
```

### 3. Khởi động Client - Console (Truyền thống)

```bash
# Chạy script tự động
run-console.bat

# Hoặc chạy manual
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --console
```

## 🎮 Hướng dẫn chơi

### Bước 1: Đăng nhập
1. Khởi động server
2. Chạy client GUI hoặc console
3. Nhập tài khoản (sử dụng tài khoản mẫu hoặc tạo mới)

### Bước 2: Mời bạn chơi
1. Trong lobby, xem danh sách người chơi online
2. Nhập tên người chơi vào ô "Mời chơi" và nhấn "Mời"
3. Đợi người được mời chấp nhận

### Bước 3: Chơi game
1. Khi vào phòng, game sẽ bắt đầu với từ bị che
2. Sử dụng các chữ cái gợi ý để đoán từ
3. Nhập từ đoán vào ô input và nhấn "Đoán"
4. Người đoán đúng trước sẽ thắng round
5. Game có nhiều round, người thắng nhiều round nhất sẽ thắng

### Bước 4: Chat
- Trò chuyện với đối thủ trong phòng chơi
- Sử dụng panel chat bên phải (GUI) hoặc lệnh `/chat` (console)

## 🏗️ Kiến trúc dự án

```
wordgame/
├── common/          # Shared models và utilities
│   ├── Models.java  # Data models (LoginReq, RoomState, etc.)
│   ├── Message.java # Message wrapper
│   ├── Json.java    # JSON serialization
│   └── MessageType.java # Message types enum
├── server/          # Game server
│   ├── ServerMain.java    # Entry point
│   ├── ClientSession.java # Handle client connections
│   ├── LobbyManager.java  # Manage lobby và rooms
│   ├── GameRoom.java      # Game logic
│   ├── Persistence.java   # Database operations
│   └── resources/         # Game data (words, schema)
└── client/          # Game client
    ├── ClientMain.java    # Entry point
    ├── ConsoleUI.java     # Console interface
    ├── NetClient.java     # Network client
    └── ui/                # GUI components
        ├── SwingLoginView.java    # Login screen
        └── SwingMainGameView.java # Main game screen
```

## 🎨 Tính năng giao diện GUI

### 🔐 Màn hình đăng nhập
- Giao diện hiện đại với gradient background
- Form đăng nhập với animation
- Kết nối server tự động
- Xử lý lỗi trực quan

### 🎮 Màn hình game chính
- **Panel trái**: Thông tin người chơi, lobby, mời chơi
- **Panel giữa**: Khu vực game với từ đoán, chữ cái gợi ý, timer
- **Panel phải**: Chat real-time
- Responsive design với màu sắc hiện đại

### ✨ Hiệu ứng đặc biệt
- Button hover effects
- Smooth animations
- Progress bar cho timer
- Auto-scroll cho chat
- Color-coded status messages

## 🛠️ Development

### Build project
```bash
mvn clean compile
```

### Package jars
```bash
mvn clean package
```

### Run tests
```bash
mvn test
```

## 📊 Database

Game sử dụng SQLite database với các bảng:
- `users`: Lưu thông tin user (username, password, points)
- `stats`: Lưu thống kê game (wins, losses, bonus)

Database được tạo tự động khi khởi động server lần đầu.

## 🌟 Cải tiến trong tương lai

- [ ] Thêm nhiều game mode
- [ ] Hệ thống achievement
- [ ] Multiplayer rooms lớn hơn
- [ ] Âm thanh và hiệu ứng
- [ ] Mobile app version
- [ ] Spectator mode
- [ ] Tournament system

## 🐛 Troubleshooting

### Lỗi kết nối server
- Kiểm tra server đã chạy chưa
- Kiểm tra port 7777 có bị block không
- Thử restart server và client

### Lỗi build
- Kiểm tra Java 17+ đã cài đặt
- Kiểm tra Maven đã cài đặt
- Chạy `mvn clean` trước khi build

### Lỗi đăng nhập
- Sử dụng tài khoản mẫu: alice/123, bob/123, charlie/123, dora/123
- Kiểm tra kết nối mạng

## 📝 License

This project is open source and available under the MIT License.

## 👥 Contributors

- Phát triển bởi AI Assistant
- Sử dụng Java, Maven, Swing, SQLite
- Thiết kế UI/UX hiện đại

---

🎮 **Chúc bạn chơi game vui vẻ!** 🎮