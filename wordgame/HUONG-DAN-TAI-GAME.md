# 📥 HƯỚNG DẪN TẢI VÀ CHƠI GAME

## 🌐 Thông tin Server

**IP:** `192.168.0.6`  
**Port HTTP:** `8080`  
**Port Game Server:** `7777`

---

## 📱 CÁCH 1: TẢI QUA TRÌNH DUYỆT (ĐƠN GIẢN NHẤT)

### Bước 1: Mở trình duyệt
Truy cập địa chỉ sau trong Chrome/Safari/Firefox:

```
http://192.168.0.6:8080/
```

### Bước 2: Download files
Bạn sẽ thấy danh sách thư mục. Click vào:

1. **Server:** `server/target/server-1.0.0-jar-with-dependencies.jar`
2. **Client:** `client/target/client-1.0.0-jar-with-dependencies.jar`

### Bước 3: Chạy game
```bash
# Chạy client để chơi
java -jar client-1.0.0-jar-with-dependencies.jar
```

**Lưu ý:** Không cần chạy server trên máy của bạn nếu server đã chạy trên máy host (192.168.0.6)

---

## 💻 CÁCH 2: TẢI BẰNG TERMINAL (MAC/LINUX)

### Download files:
```bash
# Tạo thư mục
mkdir wordgame
cd wordgame

# Download client
curl -O http://192.168.0.6:8080/client/target/client-1.0.0-jar-with-dependencies.jar

# (Tùy chọn) Download server nếu muốn host
curl -O http://192.168.0.6:8080/server/target/server-1.0.0-jar-with-dependencies.jar
```

### Chạy game:
```bash
# Chơi game (kết nối đến server 192.168.0.6:7777)
java -jar client-1.0.0-jar-with-dependencies.jar
```

---

## 🪟 CÁCH 3: TẢI BẰNG POWERSHELL (WINDOWS)

### Download files:
```powershell
# Tạo thư mục
mkdir wordgame
cd wordgame

# Download client
Invoke-WebRequest -Uri "http://192.168.0.6:8080/client/target/client-1.0.0-jar-with-dependencies.jar" -OutFile "client.jar"

# (Tùy chọn) Download server
Invoke-WebRequest -Uri "http://192.168.0.6:8080/server/target/server-1.0.0-jar-with-dependencies.jar" -OutFile "server.jar"
```

### Chạy game:
```powershell
# Chơi game
java -jar client.jar
```

---

## 🎮 HƯỚNG DẪN CHƠI

### 1. Đăng nhập/Đăng ký
- Mở client → Nhập username và password
- Click "Đăng nhập" hoặc "Đăng ký"

### 2. Tạo/Vào phòng
- Click "🏠 Tạo phòng mới" để tạo phòng
- Hoặc chờ người khác mời

### 3. Bắt đầu chơi
- Cần 2 người trong phòng
- Host click "🚀 Bắt đầu game"

### 4. Chơi game
- 4 rounds, độ khó tăng dần
- Đoán từ bằng cách click chữ cái
- Submit để kiểm tra
- Đúng = viền xanh ✅
- Sai = viền đỏ ❌
- Chơi đến khi đúng hoặc hết giờ

### 5. Điểm số
- Đoán đúng trước: +3 điểm
- Đoán đúng sau: +1 điểm
- Người có điểm cao nhất sau 4 rounds thắng

---

## ⚠️ YÊU CẦU HỆ THỐNG

### Phần mềm cần thiết:
- ✅ **Java 17 trở lên** (bắt buộc)
  - Kiểm tra: `java -version`
  - Download: https://www.oracle.com/java/technologies/downloads/

### Kết nối mạng:
- ✅ Cùng mạng WiFi với server (192.168.0.6)
- ✅ Firewall cho phép kết nối port 7777

---

## 🔧 XỬ LÝ SỰ CỐ

### Lỗi: "Không thể kết nối server"
**Nguyên nhân:**
- Không cùng mạng WiFi
- Server chưa chạy
- Firewall chặn

**Giải pháp:**
1. Kiểm tra cùng WiFi
2. Ping thử: `ping 192.168.0.6`
3. Hỏi host xem server có đang chạy không

### Lỗi: "java: command not found"
**Nguyên nhân:** Chưa cài Java

**Giải pháp:**
```bash
# Mac (Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# Windows
# Download từ: https://www.oracle.com/java/technologies/downloads/
```

### Lỗi: "Cannot download file"
**Nguyên nhân:** HTTP server chưa chạy hoặc sai IP

**Giải pháp:**
1. Kiểm tra server HTTP: `http://192.168.0.6:8080/`
2. Hỏi host IP address mới
3. Thử download bằng browser

---

## 📞 LIÊN HỆ HỖ TRỢ

Nếu gặp vấn đề, liên hệ:
- **Host:** Tiến Đạt
- **IP Server:** 192.168.0.6
- **Port Game:** 7777
- **Port Download:** 8080

---

## 🎯 TIPS CHƠI HAY

1. **Bắt đầu với nguyên âm:** A, E, I, O, U
2. **Chữ phổ biến:** T, N, S, R, H
3. **Xem độ dài từ:** Round 1-2 ngắn, 3-4 dài
4. **Đoán nhanh:** Người đầu tiên +3 điểm
5. **Đừng spam:** Suy nghĩ trước khi submit

Chúc bạn chơi vui! 🎮✨
