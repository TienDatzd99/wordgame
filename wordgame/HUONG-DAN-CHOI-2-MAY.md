# 🎮 Hướng Dẫn Chơi Game 2 Máy

## 📋 Yêu Cầu
- 2 máy tính kết nối cùng mạng WiFi/LAN
- Java 17+ đã cài đặt trên cả 2 máy

## 🖥️ MÁY 1 - MÁY CHỦ (Server)

### Bước 1: Tìm địa chỉ IP của máy
**Trên macOS/Linux:**
```bash
ipconfig getifaddr en0
```
hoặc
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Trên Windows:**
```cmd
ipconfig
```
Tìm dòng "IPv4 Address"

**Ví dụ kết quả:** `192.168.0.6`

### Bước 2: Chạy Server
```bash
java -cp server/target/server-1.0.0-jar-with-dependencies.jar com.dat.wordgame.server.ServerMain
```

Thấy dòng: `Server started on port 7777` là thành công! ✅

### Bước 3: Chạy Client (Người chơi 1)
```bash
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
```

**Trong form đăng nhập:**
- Host: `127.0.0.1` (hoặc `localhost`)
- Port: `7777`
- Click **"Kết nối"**
- Đăng nhập hoặc đăng ký tài khoản

---

## 💻 MÁY 2 - MÁY KHÁCH (Client)

### Bước 1: Copy file JAR
Copy file này từ máy chủ sang máy 2:
```
client/target/client-1.0.0-jar-with-dependencies.jar
```

### Bước 2: Chạy Client (Người chơi 2)
```bash
java -cp client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
```

### Bước 3: Kết nối đến Server
**Trong form đăng nhập, thay đổi:**
- **Host:** `192.168.0.6` ⬅️ (IP của máy chủ - Máy 1)
- **Port:** `7777`
- Click **"Kết nối"**

Nếu thấy: **"Kết nối server thành công!"** màu xanh → OK! ✅

### Bước 4: Đăng ký/Đăng nhập
- Click **"ĐĂNG KÝ"** để tạo tài khoản mới
- Hoặc đăng nhập bằng tài khoản có sẵn

---

## 🎯 Cách Chơi

1. **Cả 2 người chơi** đều phải đăng nhập thành công
2. Trong lobby, người chơi 1 **gửi lời mời** cho người chơi 2
3. Người chơi 2 **chấp nhận lời mời**
4. Bắt đầu chơi! 🎉

---

## 🔥 Firewall (Nếu kết nối bị chặn)

### macOS:
```bash
# Cho phép Java nhận kết nối
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /usr/bin/java
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblockapp /usr/bin/java
```

### Windows:
1. Mở **Windows Defender Firewall**
2. Click **"Allow an app through firewall"**
3. Tìm **Java(TM) Platform SE binary**
4. Tick cả **Private** và **Public**
5. Click **OK**

### Linux (Ubuntu/Debian):
```bash
sudo ufw allow 7777/tcp
```

---

## ⚠️ Xử Lý Lỗi

### "Không thể kết nối"
✅ Kiểm tra IP máy chủ có đúng không
✅ Kiểm tra Server có đang chạy không
✅ Kiểm tra Firewall
✅ Kiểm tra 2 máy có cùng mạng không

### "Address already in use"
```bash
# Tắt server cũ
lsof -ti:7777 | xargs kill -9    # macOS/Linux
netstat -ano | findstr :7777     # Windows (xem PID rồi kill)
```

### "Connection refused"
- Server chưa chạy
- Sai IP hoặc Port
- Firewall chặn

---

## 📝 Tài Khoản Mẫu

Nếu muốn test nhanh (không đăng ký):
- **Username:** alice, bob, charlie, dora
- **Password:** 123

---

## 🚀 Quick Start

**Máy 1 (Server):**
```bash
# Terminal 1: Chạy server
java -cp server/target/server-1.0.0-jar-with-dependencies.jar com.dat.wordgame.server.ServerMain

# Terminal 2: Chạy client
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Host: 127.0.0.1, Port: 7777, Kết nối
```

**Máy 2 (Client):**
```bash
java -cp client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Host: <IP_CỦA_MÁY_1>, Port: 7777, Kết nối
```

**Enjoy! 🎮🎉**
