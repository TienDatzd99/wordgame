# 📚 TỔNG HỢP KIẾN THỨC - WORD GAME PROJECT

## 🎯 TỔNG QUAN DỰ ÁN

### Thông tin cơ bản
- **Tên dự án:** WordGame (WordleCup)
- **Phiên bản:** v2.5.1
- **Ngôn ngữ chính:** Java 17
- **Build tool:** Maven 3.x
- **Kiến trúc:** Client-Server Model
- **Giao thức:** TCP Socket + JSON Protocol

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### 1. **Mô hình Client-Server**

#### Server Side (Port 7777)
- **Vai trò:** Trung tâm xử lý logic game, quản lý phòng, xác thực người chơi
- **IP:** 0.0.0.0 (lắng nghe trên tất cả network interfaces)
- **Threading:** Multi-threaded (mỗi client = 1 thread riêng)

#### Client Side
- **Vai trò:** Giao diện người dùng, gửi/nhận dữ liệu từ server
- **UI Framework:** Java Swing
- **Pattern:** MVC-like structure

### 2. **Cấu trúc Multi-Module Maven**

```
wordgame/
├── common/          # Shared models, messages, utilities
├── server/          # Server logic, game management
└── client/          # UI, network client
```

**Lợi ích:**
- Code reusability (common module dùng chung)
- Separation of concerns
- Dễ maintain và scale
- Build độc lập từng module

---

## 💻 CÔNG NGHỆ VÀ KIẾN THỨC SỬ DỤNG

### 1. **LẬP TRÌNH MẠNG (Network Programming)**

#### A. TCP Socket Programming
```java
// Server: ServerSocket
ServerSocket serverSocket = new ServerSocket(7777);
Socket clientSocket = serverSocket.accept();

// Client: Socket
Socket socket = new Socket("localhost", 7777);
```

**Kiến thức:**
- TCP vs UDP (chọn TCP vì cần reliable connection)
- Client-Server communication
- BufferedReader/PrintWriter cho I/O streams
- Multithreading với Socket (mỗi client 1 thread)

#### B. JSON Protocol
```java
// Message format
{
    "type": "LOGIN_REQ",
    "payload": {
        "username": "dat",
        "password": "123"
    }
}
```

**Kiến thức:**
- JSON serialization/deserialization
- Protocol design (Message types, Payloads)
- Error handling

#### C. Message Types (15+ loại)
```java
public enum MessageType {
    LOGIN_REQ, LOGIN_OK, LOGIN_ERR,
    REGISTER_REQ, REGISTER_OK, REGISTER_ERR,
    LOBBY_SNAPSHOT,
    ROOM_CREATE, ROOM_JOIN, ROOM_LEAVE,
    INVITE_SEND, INVITE_RECEIVE, INVITE_ACCEPT, INVITE_REJECT,
    ROUND_START, ROUND_END, ROUND_TICK,
    GUESS_SUBMIT, GUESS_UPDATE,
    GAME_END,
    CHAT,
    SURRENDER
}
```

---

### 2. **CƠ SỞ DỮ LIỆU (Database)**

#### SQLite Database
```sql
CREATE TABLE players (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    total_points INTEGER DEFAULT 0,
    games_played INTEGER DEFAULT 0,
    games_won INTEGER DEFAULT 0,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

**Kiến thức:**
- SQL queries (SELECT, INSERT, UPDATE)
- JDBC (Java Database Connectivity)
- PreparedStatement (prevent SQL injection)
- Transaction management
- Connection pooling concepts

**File:** `wordgame.db`

---

### 3. **JAVA SWING - GIAO DIỆN NGƯỜI DÙNG**

#### A. UI Components
- **JFrame** - Cửa sổ chính
- **JPanel** - Container cho components
- **JTable** - Bảng dữ liệu (players, leaderboard)
- **JButton** - Nút bấm
- **JTextField** - Input text
- **JTextArea** - Chat, hiển thị văn bản
- **JList** - Danh sách người chơi
- **JLabel** - Nhãn, tiêu đề
- **JOptionPane** - Dialog thông báo

#### B. Layout Managers
```java
BorderLayout    // North, South, East, West, Center
FlowLayout      // Xếp ngang từ trái sang phải
GridLayout      // Lưới m x n
BoxLayout       // Vertical/Horizontal stacking
```

#### C. Event Handling
```java
// ActionListener
button.addActionListener(e -> handleClick());

// MouseListener
component.addMouseListener(new MouseAdapter() {
    public void mouseEntered(MouseEvent e) { ... }
    public void mouseExited(MouseEvent e) { ... }
});

// WindowListener
addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) { ... }
});
```

#### D. Custom Painting
```java
@Override
protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    
    // Gradient background
    GradientPaint gradient = new GradientPaint(...);
    g2d.setPaint(gradient);
    g2d.fillRect(0, 0, width, height);
    
    // Glassmorphism effect
    g2d.setColor(new Color(255, 255, 255, 40)); // RGBA
    g2d.fillRoundRect(x, y, w, h, 20, 20);
}
```

**Kiến thức:**
- Graphics2D API
- Anti-aliasing
- Color với alpha channel (transparency)
- Custom UI rendering

---

### 4. **MULTITHREADING & CONCURRENCY**

#### A. Thread Management
```java
// Server - Thread per client
new Thread(() -> handleClient(socket)).start();

// Client - Incoming message loop
Thread incomingLoop = new Thread(() -> {
    while (running) {
        String line = reader.readLine();
        processMessage(line);
    }
});
incomingLoop.start();
```

#### B. Synchronization
```java
// Thread-safe collections
private final Map<String, ClientSession> sessions = 
    new ConcurrentHashMap<>();

// Synchronized methods
public synchronized void broadcast(Message msg) { ... }
```

#### C. SwingUtilities
```java
// Update UI from background thread
SwingUtilities.invokeLater(() -> {
    label.setText("Updated!");
});
```

**Kiến thức:**
- Thread lifecycle
- Race conditions
- Deadlock prevention
- Thread-safe programming
- EDT (Event Dispatch Thread) trong Swing

---

### 5. **DESIGN PATTERNS**

#### A. Singleton Pattern
```java
public class LobbyManager {
    private static LobbyManager instance;
    
    public static LobbyManager getInstance() {
        if (instance == null) {
            instance = new LobbyManager();
        }
        return instance;
    }
}
```

#### B. Observer Pattern
```java
// Message listener pattern
netClient.setOnMessage(msg -> {
    handleMessage(msg);
});
```

#### C. MVC-like Architecture
- **Model:** `Models.java`, `GameRoom.java`
- **View:** `LobbyView.java`, `GameView.java`, `RoomView.java`
- **Controller:** `NetClient.java`, `ClientSession.java`

#### D. Factory Pattern
```java
// Message creation
Message msg = new Message(MessageType.LOGIN_REQ, payload);
```

---

### 6. **GAME LOGIC**

#### A. Wordle Game Mechanics
```java
public class GameRoom {
    private String targetWord;
    private int currentRound = 1;
    private Map<String, Integer> playerScores;
    private Set<String> playersCorrect;
    private long roundStartTime;
}
```

**Tính năng:**
- 4 rounds, độ khó tăng dần
- Từ 3-4 chữ cái (Easy) → 8+ chữ cái (Insane)
- Continuous play mode: chơi đến khi đúng hoặc hết giờ
- First correct bonus: người đoán đúng trước +3 điểm

#### B. Scoring System
```java
// First correct: +3 điểm
// Other correct: +1 điểm
// Round winner: người đoán đúng trước
// Game winner: tổng điểm cao nhất sau 4 rounds
```

#### C. Word Database
```
words_easy.txt      (3-4 chữ cái)
words_medium.txt    (5-6 chữ cái)
words_hard.txt      (7 chữ cái)
words_insane.txt    (8+ chữ cái)
```

**Tổng:** 89 từ unique

---

### 7. **MAVEN BUILD SYSTEM**

#### A. Project Structure
```xml
<project>
    <groupId>com.dat.wordgame</groupId>
    <artifactId>wordgame</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>common</module>
        <module>server</module>
        <module>client</module>
    </modules>
</project>
```

#### B. Dependencies
```xml
<!-- JSON Processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>

<!-- SQLite JDBC -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
</dependency>
```

#### C. Maven Plugins
```xml
<!-- Compiler Plugin -->
<maven-compiler-plugin>
    <source>17</source>
    <target>17</target>
</maven-compiler-plugin>

<!-- Assembly Plugin (JAR with dependencies) -->
<maven-assembly-plugin>
    <descriptorRefs>
        <descriptorRef>jar-with-dependencies</descriptorRef>
    </descriptorRefs>
</maven-assembly-plugin>
```

**Commands:**
```bash
mvn clean package -DskipTests
mvn compile
mvn test
```

---

### 8. **JAVA CORE CONCEPTS**

#### A. Collections Framework
```java
// List
List<String> players = new ArrayList<>();

// Map
Map<String, GameRoom> rooms = new HashMap<>();

// Set
Set<String> playersCorrect = new HashSet<>();

// Thread-safe
ConcurrentHashMap<String, ClientSession> sessions;
```

#### B. Lambda Expressions
```java
button.addActionListener(e -> handleClick());
playersList.forEach(player -> System.out.println(player));
```

#### C. Streams API
```java
List<String> online = players.stream()
    .filter(p -> p.getStatus().equals("online"))
    .map(Player::getName)
    .collect(Collectors.toList());
```

#### D. Exception Handling
```java
try {
    socket.connect(address);
} catch (IOException e) {
    showError("Không thể kết nối server!");
} finally {
    cleanup();
}
```

---

### 9. **UI/UX DESIGN CONCEPTS**

#### A. Glassmorphism Effect
```java
// Semi-transparent background
g2d.setColor(new Color(255, 255, 255, 40));
g2d.fillRoundRect(0, 0, w, h, 20, 20);

// Blur effect simulation với borders
g2d.setColor(new Color(255, 255, 255, 80));
g2d.drawRoundRect(0, 0, w-1, h-1, 20, 20);
```

#### B. Color Scheme
- **Primary:** RGB(88, 86, 214) - Purple
- **Secondary:** RGB(133, 89, 215) - Lighter Purple
- **Success:** RGB(34, 197, 94) - Green
- **Danger:** RGB(239, 68, 68) - Red
- **Gold:** RGB(255, 215, 0) - Host indicator

#### C. Visual Feedback
- **Correct guess:** Green border (3px)
- **Incorrect guess:** Red border (3px)
- **Hover effects:** Brighter colors
- **Animations:** Timer with auto-removal

---

### 10. **ADVANCED FEATURES**

#### A. Surrender System
```java
// Player A surrenders
MessageType.SURRENDER

// Notification to Player B
Chat message: "🏆 System: {player} đã đầu hàng! Bạn thắng!"

// Auto return to lobby after 1 second
Timer timer = new Timer(1000, e -> returnToLobby());
```

#### B. Invite Friends System
```java
// Real-time search
DocumentListener -> filter online players

// Invite flow
INVITE_SEND → INVITE_RECEIVE → INVITE_ACCEPT/REJECT
```

#### C. Continuous Play Mode
```java
// Keep playing until correct or timeout
Set<String> playersCorrect;

// Only end when both correct OR timeout
if (playersCorrect.size() == 2 || timeout) {
    endRound();
}
```

---

## 🔐 BẢO MẬT (Security)

### 1. SQL Injection Prevention
```java
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM players WHERE username = ?"
);
stmt.setString(1, username);
```

### 2. Input Validation
```java
// Username validation
if (username.length() < 3) {
    return "Username phải >= 3 ký tự";
}

// SQL injection prevention
if (username.contains("'") || username.contains(";")) {
    return "Ký tự không hợp lệ";
}
```

### 3. Password Handling
```java
// Note: Hiện tại dùng plain text (KHÔNG an toàn)
// Nên upgrade lên BCrypt hash trong tương lai
```

---

## 📊 QUẢN LÝ STATE

### 1. Server State
```java
// Active sessions
Map<String, ClientSession> sessions;

// Active game rooms
Map<String, GameRoom> activeRooms;

// Player database
SQLite wordgame.db
```

### 2. Client State
```java
public enum ClientState {
    DISCONNECTED,
    LOBBY,
    IN_ROOM,
    IN_GAME
}
```

### 3. Data Persistence
- **LobbyView reference:** Giữ dữ liệu khi vào/rời phòng
- **Database:** Lưu thông tin player, điểm số
- **Real-time sync:** LOBBY_SNAPSHOT updates

---

## 🧪 TESTING & DEBUGGING

### 1. Logging
```java
System.out.println("[Server] Client connected: " + username);
System.out.println("[WordService] Picked word: " + word);
```

### 2. Error Handling
```java
try {
    // Network operations
} catch (IOException e) {
    e.printStackTrace();
    showErrorDialog("Lỗi kết nối!");
}
```

### 3. Build Commands
```bash
# Clean build
mvn clean package -DskipTests

# Run server
java -jar server/target/server-1.0.0-jar-with-dependencies.jar

# Run client
java -jar client/target/client-1.0.0-jar-with-dependencies.jar
```

---

## 🌐 NETWORKING DETAILS

### 1. Protocol Design
```
Client → Server: LOGIN_REQ
Server → Client: LOGIN_OK + LOBBY_SNAPSHOT

Client → Server: GUESS_SUBMIT
Server → All: GUESS_UPDATE (real-time broadcast)

Server → All: ROUND_TICK (countdown every second)
```

### 2. Message Flow Example
```
1. Login: LOGIN_REQ → LOGIN_OK → LOBBY_SNAPSHOT
2. Create Room: User clicks → RoomView opens
3. Start Game: START_GAME → ROUND_START → GameView
4. Guess: GUESS_SUBMIT → GUESS_UPDATE (broadcast)
5. Round End: ROUND_END → Next round or GAME_END
6. Surrender: SURRENDER → Chat notification → Auto return
```

### 3. Broadcasting
```java
// Broadcast to all players in room
room.getPlayers().forEach(player -> {
    sessions.get(player).send(message);
});

// Broadcast to all online users
sessions.values().forEach(session -> {
    session.send(lobbySnapshot);
});
```

---

## 📁 FILE STRUCTURE

```
wordgame/
├── common/
│   └── src/main/java/com/dat/wordgame/common/
│       ├── Json.java           # JSON utilities
│       ├── Message.java        # Message wrapper
│       ├── MessageType.java    # Enum 15+ types
│       ├── Models.java         # Data models
│       └── Payloads.java       # Request/Response DTOs
│
├── server/
│   └── src/main/java/com/dat/wordgame/server/
│       ├── ServerMain.java     # Entry point
│       ├── ClientSession.java  # Per-client handler
│       ├── LobbyManager.java   # Singleton lobby
│       ├── GameRoom.java       # Game logic
│       ├── Persistence.java    # Database
│       ├── WordService.java    # Word management
│       └── resources/
│           ├── schema.sql
│           └── words_*.txt
│
└── client/
    └── src/main/java/com/dat/wordgame/client/
        ├── ClientMain.java     # Entry point
        ├── NetClient.java      # Network client
        ├── ClientState.java    # State enum
        ├── IncomingLoop.java   # Message receiver
        └── ui/
            ├── SwingLoginView.java
            ├── LobbyView.java
            ├── RoomView.java
            └── GameView.java
```

---

## 🎓 KIẾN THỨC NỀN TẢNG CẦN NẮM

### 1. Lập trình hướng đối tượng (OOP)
- ✅ Encapsulation (private fields, public methods)
- ✅ Inheritance (extends JFrame, JPanel)
- ✅ Polymorphism (interface implementations)
- ✅ Abstraction (abstract methods)

### 2. Java Collections
- ✅ ArrayList, HashMap, HashSet
- ✅ Iterator pattern
- ✅ Comparator/Comparable

### 3. Exception Handling
- ✅ try-catch-finally
- ✅ Custom exceptions
- ✅ Error propagation

### 4. I/O Streams
- ✅ BufferedReader/Writer
- ✅ File I/O
- ✅ Network streams

### 5. Multithreading
- ✅ Thread creation
- ✅ Runnable interface
- ✅ Synchronization
- ✅ Thread-safe code

---

## 🚀 CÁC TÍNH NĂNG ĐỘC ĐÁO

### 1. **Continuous Play Mode**
- Không giới hạn số lần đoán trong 1 round
- Chơi đến khi đúng hoặc hết thời gian
- Visual feedback (red/green borders)

### 2. **Glassmorphism UI**
- Semi-transparent panels
- Blur effects
- Modern gradient backgrounds
- Rounded corners

### 3. **Real-time Features**
- Live leaderboard updates
- Instant chat messages
- Player online/offline status
- Game state synchronization

### 4. **Smart Word Selection**
- Difficulty scaling by round
- No duplicate words in same game
- Length-based filtering

### 5. **User Experience**
- Auto-refresh leaderboard
- Keyboard input support
- Hover effects
- Confirmation dialogs
- Status notifications

---

## 📈 METRICS & STATISTICS

### Dòng code (Lines of Code)
- **Total:** ~3,500+ lines
- **Server:** ~1,200 lines
- **Client:** ~2,000 lines
- **Common:** ~300 lines

### Files
- **Java files:** 20+ files
- **Resource files:** 5+ files
- **Configuration:** pom.xml (3 files)

### Features
- **Message types:** 15+ types
- **UI screens:** 4 main views
- **Word database:** 89 words
- **Max players per room:** 2
- **Game rounds:** 4

---

## 🎯 CÂU HỎI GIẢNG VIÊN CÓ THỂ HỎI

### 1. **Về Kiến trúc**
**Q:** Tại sao chọn mô hình Client-Server thay vì Peer-to-Peer?
**A:** 
- Centralized game logic → Chống gian lận
- Easier synchronization
- Single source of truth
- Scalable (nhiều clients)

### 2. **Về Threading**
**Q:** Xử lý race condition như thế nào?
**A:**
- ConcurrentHashMap cho sessions
- synchronized methods cho broadcast
- SwingUtilities.invokeLater cho UI updates

### 3. **Về Protocol**
**Q:** Tại sao dùng JSON thay vì Binary?
**A:**
- Human-readable → dễ debug
- Platform-independent
- Easy to extend
- Library support (Gson)

### 4. **Về UI**
**Q:** Tại sao chọn Swing thay vì JavaFX?
**A:**
- Simpler, more mature
- Better documentation
- Native look and feel
- Lighter weight

### 5. **Về Game Logic**
**Q:** Làm thế nào đảm bảo fair play?
**A:**
- Server-side validation
- No client-side game state
- Timer đồng bộ
- First correct bonus prevents tie

### 6. **Về Security**
**Q:** Có implement security mechanisms nào?
**A:**
- PreparedStatement (SQL injection prevention)
- Input validation
- Session management
- (Future: Password hashing với BCrypt)

### 7. **Về Scalability**
**Q:** Hệ thống scale như thế nào khi có nhiều user?
**A:**
- Thread pool executor (future improvement)
- Database connection pooling
- Efficient data structures
- Async message processing

### 8. **Về Testing**
**Q:** Đã test như thế nào?
**A:**
- Manual testing 2+ clients
- Edge cases (timeout, disconnect)
- Cross-platform testing (Mac)
- Stress testing với multiple rooms

---

## 🔧 FUTURE IMPROVEMENTS

### 1. Technical
- [ ] Password hashing (BCrypt)
- [ ] Thread pool executor
- [ ] Connection pooling
- [ ] Logging framework (Log4j)
- [ ] Unit tests (JUnit)
- [ ] Config file (properties)

### 2. Features
- [ ] Ranking system với ELO
- [ ] Friend system
- [ ] Private rooms với password
- [ ] Spectator mode
- [ ] Replay system
- [ ] Achievement system

### 3. UI/UX
- [ ] Sound effects
- [ ] Animations
- [ ] Dark/Light theme toggle
- [ ] Custom avatar
- [ ] Emoji support
- [ ] Better error messages

---

## 📚 TÀI LIỆU THAM KHẢO

### Official Docs
- Java SE 17 Documentation
- Java Swing Tutorial
- SQLite Documentation
- Maven Guide
- Socket Programming Guide

### Design Patterns
- Gang of Four Design Patterns
- MVC Architecture
- Client-Server Pattern

### UI Design
- Material Design Guidelines
- Glassmorphism CSS
- Color Theory

---

## ✅ CHECKLIST TRƯỚC KHI BÁO CÁO

### Kiến thức cần nắm vững:
- [x] TCP Socket Programming
- [x] Multithreading & Synchronization
- [x] Java Swing components & layouts
- [x] Maven multi-module structure
- [x] JSON protocol design
- [x] SQLite database & JDBC
- [x] Design patterns (Singleton, Observer, MVC)
- [x] Game logic & scoring system
- [x] Event handling trong Swing
- [x] Custom painting & Graphics2D

### Demo cần chuẩn bị:
- [x] Login/Register
- [x] Create room & invite
- [x] Play full game (4 rounds)
- [x] Surrender feature
- [x] Leaderboard updates
- [x] Chat system
- [x] Visual feedback (red/green borders)
- [x] Return to lobby (preserve data)

### Code cần giải thích được:
- [x] Message handling flow
- [x] Thread-safe broadcasting
- [x] UI update từ background thread
- [x] Game room lifecycle
- [x] Word selection algorithm
- [x] Scoring calculation
- [x] Database queries
- [x] Network error handling

---

## 🎤 KẾT LUẬN

Project **WordGame** này đã áp dụng nhiều kiến thức quan trọng:

1. **Lập trình mạng:** TCP Socket, JSON Protocol, Client-Server
2. **Lập trình đa luồng:** Threading, Synchronization, Concurrency
3. **Cơ sở dữ liệu:** SQLite, JDBC, SQL queries
4. **Giao diện người dùng:** Java Swing, Event handling, Custom painting
5. **Kiến trúc phần mềm:** Multi-module Maven, Design patterns, MVC
6. **Game development:** Game logic, Scoring, State management

**Điểm mạnh:**
- Kiến trúc rõ ràng, dễ maintain
- Real-time communication ổn định
- UI/UX hiện đại, smooth
- Code organized, readable
- Features phong phú

**Học được gì:**
- Network programming skills
- Multithreading best practices
- UI design principles
- Project management với Maven
- Problem-solving trong real-world app

---

**Người thực hiện:** Tiến Đạt  
**Thời gian:** November 2025  
**Version:** 2.5.1  
**GitHub:** https://github.com/TienDatzd99/wordgame
