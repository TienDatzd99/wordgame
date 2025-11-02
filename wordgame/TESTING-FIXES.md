# 🔧 Hướng Dẫn Test Các Lỗi Đã Sửa

## ✅ Các lỗi đã được sửa:

### 1. **Lỗi NPE khi winner = null** 
- **Vấn đề**: `winner.equals()` gây lỗi khi winner là null (hết giờ, không ai đoán đúng)
- **Giải pháp**: 
  - Server: Gửi "Hòa" thay vì null khi GAME_END
  - Client: Kiểm tra `winner != null` trước khi so sánh

### 2. **Logic đoán sai - 1 người đoán xong là hết round**
- **Vấn đề**: Chỉ cần 1 người submit là kết thúc round ngay, người còn lại không được đoán
- **Giải pháp**: 
  - Thêm `Set<String> playersSubmitted` để track ai đã submit
  - Chỉ kết thúc round khi:
    - **CẢ 2 người đã submit** HOẶC
    - **Có người đoán đúng** (winner != null)
  - Reset `playersSubmitted` mỗi round mới

### 3. **Mất dữ liệu lobby sau khi game xong**
- **Vấn đề**: `returnToLobby()` tạo LobbyView mới → mất bảng online players và ranking
- **Giải pháp**:
  - GameView lưu reference `parentLobby`
  - LobbyView gọi `gameView.setParentLobby(this)` khi tạo GameView
  - `returnToLobby()` show lại LobbyView cũ thay vì tạo mới
  - Tự động refresh data khi quay về

---

## 🧪 Kịch Bản Test

### **Test Case 1: Cả 2 người submit (không ai đúng)**
1. Mở 2 client: `dat` và `alice`
2. `alice` thách đấu `dat`
3. Trong round 1:
   - `dat` điền từ **sai** (VD: "CAT" khi từ đúng là "BAT")
   - `dat` nhấn "Gửi câu trả lời"
   - `alice` điền từ **sai** (VD: "RAT")
   - `alice` nhấn "Gửi câu trả lời"
4. **Kết quả mong đợi**:
   - ✅ Round **KHÔNG** kết thúc ngay sau khi `dat` submit
   - ✅ Round **KẾT THÚC** sau khi `alice` submit (cả 2 đã submit)
   - ✅ Hiển thị: "⏱️ Hết giờ! Từ đúng: BAT" (vì không ai đúng)
   - ✅ Chuyển sang round 2

### **Test Case 2: 1 người đoán đúng**
1. Trong round 2:
   - `dat` điền từ **đúng** (VD: "BREAD")
   - `dat` nhấn "Gửi câu trả lời"
2. **Kết quả mong đợi**:
   - ✅ Round **KẾT THÚC NGAY** khi `dat` submit đúng
   - ✅ Hiển thị: "🎉 Bạn thắng vòng này! Từ đúng: BREAD (+X điểm)"
   - ✅ `alice` thấy: "😔 Vòng này thắng: dat. Từ đúng: BREAD"
   - ✅ Chuyển sang round 3

### **Test Case 3: 1 người submit, 1 người đợi hết giờ**
1. Trong round 3:
   - `alice` điền từ **sai** và nhấn "Gửi"
   - `dat` **KHÔNG** nhấn gì, đợi hết 25 giây
2. **Kết quả mong đợi**:
   - ✅ Round **KHÔNG** kết thúc khi `alice` submit
   - ✅ Round **KẾT THÚC** khi timer về 0 (hết giờ)
   - ✅ Hiển thị: "⏱️ Hết giờ! Từ đúng: QUESTION"
   - ✅ Chuyển sang round 4

### **Test Case 4: Quay về lobby sau game**
1. Chơi hết 4 rounds
2. Khi popup "🏁 Game kết thúc!" hiện ra:
   - Nhấn OK
3. **Kết quả mong đợi**:
   - ✅ Quay về lobby
   - ✅ Bảng "Người chơi Online" **VẪN CÒN DỮ LIỆU** (dat, alice)
   - ✅ Bảng "Bảng Xếp Hạng" **VẪN CÒN DỮ LIỆU** (top 5)
   - ✅ Điểm của `dat` và `alice` **ĐÃ ĐƯỢC CẬP NHẬT**

### **Test Case 5: Hết giờ, không ai submit**
1. Bắt đầu game mới
2. Trong round 1:
   - **CẢ 2** người **KHÔNG** nhấn gì
   - Đợi hết 15 giây
3. **Kết quả mong đợi**:
   - ✅ Round kết thúc khi timer về 0
   - ✅ Hiển thị: "⏱️ Hết giờ! Từ đúng: XXX"
   - ✅ Không ai được điểm
   - ✅ Chuyển sang round 2

---

## 🎮 Lệnh Chạy Test

### Máy 1 (Server + Client 1):
```bash
# Terminal 1: Start server
java -cp server/target/server-1.0.0-jar-with-dependencies.jar com.dat.wordgame.server.ServerMain

# Terminal 2: Start client
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Login: dat / 123
```

### Máy 2 (Client 2):
```bash
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Login: alice / 123
```

---

## 📝 Log Quan Trọng Cần Xem

Khi test, chú ý các log này trong terminal server:

```
[GameRoom] startRound() called for round X
[GameRoom] Word picked: XXXXX, length: X

# Khi người chơi submit:
GUESS received from XXX: XXXXX

# Khi kết thúc round:
SwingLoginView: Received ROUND_END
LobbyView: Forwarding ROUND_END to GameView

# Khi kết thúc game:
SwingLoginView: Received GAME_END
LobbyView: Forwarding GAME_END to GameView
```

---

## ⚠️ Lỗi Cũ vs Hành Vi Mới

| Tình huống | Hành vi CŨ (LỖI) | Hành vi MỚI (ĐÚNG) |
|------------|-------------------|---------------------|
| Dat submit đúng, Alice chưa submit | Round kết thúc ngay | Round kết thúc ngay (OK) |
| Dat submit sai, Alice chưa submit | Round kết thúc ngay ❌ | Round **KHÔNG** kết thúc, đợi Alice |
| Dat submit sai, Alice submit sai | Round kết thúc | Round kết thúc (OK) |
| Cả 2 không submit, hết giờ | NPE crash ❌ | Hiển thị "Hết giờ!" |
| Game xong, quay về lobby | Bảng trống ❌ | Bảng còn dữ liệu, tự refresh |

---

## ✨ Version Info
- **Build**: 2025-11-01
- **Changes**: 
  - Fixed NPE when winner is null
  - Fixed round ending logic (wait for both players or correct answer)
  - Fixed lobby data loss (reuse existing LobbyView)
