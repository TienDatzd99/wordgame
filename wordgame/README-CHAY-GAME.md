# Word Game - Chạy Game

Để chạy game, bạn có 2 cách:

## Cách 1: Sử dụng script tự động
```bash
start-game.bat
```

## Cách 2: Chạy thủ công

### 1. Chạy Server
Mở Command Prompt và chạy:
```bash
java -jar "D:\wordgame\wordgame\server\target\server-1.0.0-jar-with-dependencies.jar"
```

### 2. Chạy Client (Giao diện GUI)  
Mở Command Prompt khác và chạy:
```bash
java -jar "D:\wordgame\wordgame\client\target\client-1.0.0-jar-with-dependencies.jar"
```

## Cách sử dụng:

1. **Đăng nhập**: Nhập username và password (hoặc để trống để tạo tài khoản mới)
2. **Chọn phòng**: Tham gia phòng có sẵn hoặc tạo phòng mới
3. **Chơi game**: Khi có đủ người chơi, game sẽ bắt đầu
4. **Tìm từ**: Tạo từ từ các chữ cái được cho, từ dài hơn sẽ có điểm cao hơn

## Build lại nếu có thay đổi code:
```bash
D:\maven\bin\mvn.cmd clean package
```

## Ghi chú:
- Server chạy trên cổng 7777
- Cần Java 17 trở lên để chạy
- Dữ liệu game được lưu trong SQLite database