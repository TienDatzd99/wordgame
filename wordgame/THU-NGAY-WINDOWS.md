# 🎯 THỬ NGAY - HƯỚNG DẪN CHO MÁY WINDOWS

## ⚡ BƯỚC 1: BỎ QUA PING

**Ping không quan trọng!** Mac thường chặn ping. Thử HTTP trực tiếp:

---

## 🌐 BƯỚC 2: MỞ TRÌNH DUYỆT

### Trên Windows, mở Chrome/Edge/Firefox:

Gõ địa chỉ này vào thanh địa chỉ:

```
http://192.168.0.6:8888/
```

### ✅ Nếu THÀNH CÔNG:
Bạn sẽ thấy danh sách thư mục như:
```
client/
server/
common/
README.md
...
```

→ **HOÀN HẢO!** Tiếp tục BƯỚC 3.

### ❌ Nếu KHÔNG ĐƯỢC:
Bạn thấy lỗi "This site can't be reached"

→ Thử **CÁCH KHÁC** bên dưới.

---

## 📥 BƯỚC 3: DOWNLOAD CLIENT

### Click vào đường dẫn này trong browser:

```
client/target/client-1.0.0-jar-with-dependencies.jar
```

Hoặc gõ trực tiếp:
```
http://192.168.0.6:8888/client/target/client-1.0.0-jar-with-dependencies.jar
```

File JAR (~15MB) sẽ tự động download.

---

## ▶️ BƯỚC 4: CHẠY GAME

### Mở PowerShell/CMD:

```powershell
# Di chuyển đến thư mục Downloads
cd Downloads

# Chạy game
java -jar client-1.0.0-jar-with-dependencies.jar
```

### Nếu báo lỗi "java not found":
Download Java 17 tại: https://www.oracle.com/java/technologies/downloads/

---

## 🔄 CÁCH KHÁC: DÙNG POWERSHELL

Nếu browser không được, dùng PowerShell:

```powershell
# Test kết nối
curl http://192.168.0.6:8888/

# Download client
Invoke-WebRequest -Uri "http://192.168.0.6:8888/client/target/client-1.0.0-jar-with-dependencies.jar" -OutFile "wordgame-client.jar"

# Chạy
java -jar wordgame-client.jar
```

---

## 🔥 CÁCH KHÁC: DÙNG ĐIỆN THOẠI LÀM HOTSPOT

Nếu hoàn toàn không kết nối được qua WiFi router:

### Trên điện thoại (iPhone/Android):
1. Bật **Personal Hotspot** / **Mobile Hotspot**
2. Đặt password (ví dụ: `12345678`)

### Trên Mac:
1. Kết nối đến Hotspot điện thoại
2. Chạy lệnh kiểm tra IP mới:
   ```bash
   ifconfig | grep "inet "
   ```
3. Ghi lại IP mới (ví dụ: `172.20.10.2`)

### Trên Windows:
1. Kết nối đến **CÙNG** Hotspot
2. Mở browser: `http://172.20.10.2:8888/`
3. Download và chạy như BƯỚC 3-4

---

## 🧪 DEBUG: KHI NÀO CẦN HELP

Nếu vẫn không được, chạy các lệnh sau và gửi kết quả:

```powershell
# 1. IP của Windows
ipconfig

# 2. Ping router
ping 192.168.0.1

# 3. Test HTTP
curl http://192.168.0.6:8888/

# 4. Trace route
tracert 192.168.0.6
```

---

## ✅ CHECKLIST

- [ ] Cùng WiFi với Mac (SSID giống nhau)
- [ ] Thử browser: `http://192.168.0.6:8888/`
- [ ] Thử PowerShell: `curl http://192.168.0.6:8888/`
- [ ] Java đã cài: `java -version`
- [ ] Nếu không được → Dùng Hotspot

---

## 🎮 KẾT NỐI ĐẾN SERVER GAME

Sau khi download và chạy client:

1. **Đăng nhập/Đăng ký** tài khoản
2. Client sẽ **TỰ ĐỘNG** kết nối đến server Mac: `192.168.0.6:7777`
3. Nếu báo lỗi "Cannot connect":
   - Kiểm tra server game có chạy trên Mac không
   - Thử Hotspot

---

**XÁC SUẤT THÀNH CÔNG:**
- ✅ Browser HTTP: 70%
- ✅ PowerShell curl: 80%
- ✅ Hotspot: 95%
- ✅ USB transfer: 100%

**KHUYẾN NGHỊ:** Thử Browser trước, nếu không được → Hotspot!
