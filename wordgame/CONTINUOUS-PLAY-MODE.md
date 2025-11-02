# 🎮 Cơ Chế Chơi Game Mới - Continuous Play

## ✨ Thay Đổi Logic Game

### **Cơ chế CŨ** (đã bỏ):
- ❌ Submit 1 lần duy nhất
- ❌ Submit xong đợi → không làm gì được
- ❌ Round kết thúc ngay khi có người submit

### **Cơ chế MỚI** (hiện tại):
- ✅ **Submit nhiều lần** - liên tục thử cho đến khi đúng hoặc hết giờ
- ✅ **Submit sai** → Viền đỏ hiện ra 1 giây, xóa đáp án, tiếp tục chơi
- ✅ **Submit đúng** → Viền xanh, đợi đối thủ hoặc hết giờ
- ✅ **Điểm cho người nhanh hơn** - ai đúng TRƯỚC được điểm cao hơn
- ✅ **Round chỉ kết thúc khi**:
  - CẢ 2 người đều trả lời đúng, HOẶC
  - Hết thời gian (timer = 0)

---

## 🎯 Kịch Bản Chơi

### **Tình huống 1: Cả 2 đều đúng**
```
Timeline:
00:15 - Round bắt đầu (từ: "APPLE")
00:12 - dat điền APPLE → Submit → ✅ Đúng! (viền xanh, đợi alice)
00:09 - alice điền APPLE → Submit → ✅ Đúng!
00:09 - Round kết thúc ngay lập tức
Kết quả: dat thắng (+điểm cao vì nhanh hơn 3 giây)
```

### **Tình huống 2: 1 đúng, 1 hết giờ không đúng**
```
Timeline:
00:15 - Round bắt đầu (từ: "BREAD")
00:10 - dat điền BREAD → Submit → ✅ Đúng! (viền xanh)
00:08 - alice điền BREAK → Submit → ❌ Sai! (viền đỏ, 4/5 đúng vị trí)
00:06 - alice điền BRENT → Submit → ❌ Sai! (viền đỏ, 3/5 đúng vị trí)
00:02 - alice điền BRASS → Submit → ❌ Sai! (viền đỏ, 2/5 đúng vị trí)
00:00 - Hết giờ!
Kết quả: dat thắng (+điểm, vì đúng duy nhất)
```

### **Tình huống 3: Cả 2 đều sai, hết giờ**
```
Timeline:
00:15 - Round bắt đầu (từ: "TIGER")
00:10 - dat điền LIGHT → Submit → ❌ Sai! (viền đỏ, 2/5 đúng)
00:08 - dat điền RIVER → Submit → ❌ Sai! (viền đỏ, 3/5 đúng)
00:05 - alice điền LIGER → Submit → ❌ Sai! (viền đỏ, 4/5 đúng)
00:00 - Hết giờ!
Kết quả: Hòa (không ai đúng)
```

### **Tình huống 4: Submit nhiều lần, cuối cùng đúng**
```
Timeline:
00:15 - Round bắt đầu (từ: "HOUSE")
00:12 - alice điền MOUSE → Submit → ❌ Sai! (viền đỏ, 4/5 đúng)
00:10 - alice điền HORSE → Submit → ❌ Sai! (viền đỏ, 3/5 đúng)
00:08 - alice điền HOUSE → Submit → ✅ Đúng! (viền xanh)
00:05 - dat điền HOUSE → Submit → ✅ Đúng!
00:05 - Round kết thúc
Kết quả: alice thắng (đúng trước dat 3 giây)
```

---

## 🎨 Hiệu Ứng Giao Diện

### **Khi submit SAI**:
- 🔴 **Viền đỏ** (3px) hiện xung quanh answer slots trong 1 giây
- ❌ Statusbar: "❌ Sai rồi! Thử lại! (X/Y đúng vị trí)"
- 🗑️ Đáp án bị **xóa tự động**, quay lại trạng thái trống
- 🔄 Có thể điền lại ngay lập tức

### **Khi submit ĐÚNG**:
- 🟢 **Viền xanh** (3px) hiện xung quanh answer slots
- ✅ Statusbar: "✅ Chính xác! Đợi đối thủ..."
- 📌 Đáp án **GIỮ NGUYÊN** trên màn hình
- ⏳ Vẫn có thể tiếp tục chơi (nhưng không cần vì đã đúng)

### **Progress indicators**:
- **Tôi**: `X/Y đúng vị trí` (màu cam nếu sai, xanh nếu đúng hết)
- **Đối thủ**: `X/Y đúng vị trí` (cập nhật real-time)

---

## 🏆 Tính Điểm

### **Công thức**:
```java
if (đúng) {
    baseScore = 10;
    timeBonus = (thời_gian_còn_lại / tổng_thời_gian) * 3.0;
    totalScore = baseScore + timeBonus;
}

Người đúng TRƯỚC = điểm cao hơn (vì còn nhiều thời gian hơn)
```

### **Ví dụ**:
- Round 15 giây, từ 5 chữ
- dat đúng lúc còn 12s: `10 + (12/15)*3 = 10 + 2.4 = 12.4 điểm`
- alice đúng lúc còn 9s: `10 + (9/15)*3 = 10 + 1.8 = 11.8 điểm`
- → **dat thắng round** (nhanh hơn)

---

## 🧪 Test Cases

### **Test 1: Spam submit sai**
1. dat điền từ sai → Submit
2. Viền đỏ hiện ra
3. Sau 1 giây viền biến mất
4. Đáp án tự xóa
5. Điền lại → Submit
6. Lặp lại nhiều lần
**Expected**: Không crash, viền đỏ mỗi lần sai

### **Test 2: Cả 2 đúng cùng lúc**
1. dat và alice cùng điền đúng trong vòng 1 giây
2. **Expected**: Người submit TRƯỚC được tính là winner

### **Test 3: Đúng rồi vẫn submit**
1. dat submit đúng → viền xanh
2. dat xóa và submit lại
3. **Expected**: Vẫn được gửi request, server vẫn nhận (nhưng không thay đổi winner)

### **Test 4: Hết giờ khi đang điền**
1. dat đang điền chữ (chưa submit)
2. Timer về 0
3. **Expected**: 
   - Round kết thúc
   - Nếu alice đã đúng → alice thắng
   - Nếu không ai đúng → Hòa

---

## 📋 Server Logic Mới

```java
// GameRoom.java
playersCorrect = new HashSet<>();  // Track who got correct
firstCorrect = null;                // Who answered first
firstCorrectTime = 0;               // Timestamp

onGuess(player, answer):
    if (answer == correctWord && !playersCorrect.contains(player)) {
        playersCorrect.add(player);
        if (firstCorrect == null) {
            firstCorrect = player;  // Mark as winner
        }
        
        if (playersCorrect.size() >= 2) {
            endRound(firstCorrect);  // Both correct
        }
    }
    // If wrong, just send GUESS_UPDATE, don't end round

tick():
    if (timer == 0) {
        endRound(firstCorrect);  // Time's up, firstCorrect is winner
    }
```

---

## 🚀 Chạy Test

```bash
# Terminal 1: Server
java -cp server/target/server-1.0.0-jar-with-dependencies.jar com.dat.wordgame.server.ServerMain

# Terminal 2: Client 1
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Login: dat / 123

# Terminal 3: Client 2
java -cp client/target/client-1.0.0-jar-with-dependencies.jar com.dat.wordgame.client.ClientMain --swing
# Login: alice / 123
```

### **Bước test**:
1. alice challenge dat
2. Vào round 1 (từ 3-4 chữ, 15 giây)
3. **dat**: Thử submit sai 2-3 lần → thấy viền đỏ mỗi lần
4. **alice**: Submit sai 1 lần, sau đó submit đúng → thấy viền xanh
5. **dat**: Submit đúng
6. **Kết quả**: Round kết thúc, alice thắng (đúng trước)

---

## ⚠️ So Sánh Logic

| Hành động | Logic CŨ | Logic MỚI |
|-----------|----------|-----------|
| Submit lần 1 (sai) | Round kết thúc ❌ | Viền đỏ, tiếp tục chơi ✅ |
| Submit lần 2 (sai) | Không được submit ❌ | Viền đỏ, tiếp tục chơi ✅ |
| Submit đúng | Round kết thúc ngay ❌ | Đợi đối thủ hoặc hết giờ ✅ |
| Cả 2 đúng | Người submit trước thắng | Người submit trước thắng ✅ |
| Hết giờ, 1 người đúng | NPE crash ❌ | Người đúng thắng ✅ |
| Hết giờ, không ai đúng | NPE crash ❌ | Hòa ✅ |

---

## 🎯 Version
- **Build**: 2025-11-01 19:43
- **Feature**: Continuous Play Mode
- **Changes**: 
  - Allow multiple guess submissions
  - Red border for incorrect answers
  - Green border for correct answers
  - Round only ends when both correct OR time's up
  - Winner is first person to answer correctly
