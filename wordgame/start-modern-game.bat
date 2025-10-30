@echo off
echo ========================================
echo    🎮 WORD GAME - GIAO DIEN HIEN DAI 🎮
echo ========================================
echo.
echo Đang khởi động server...
start "Word Game Server" /min java -jar "D:\wordgame\wordgame\server\target\server-1.0.0-jar-with-dependencies.jar"

echo Chờ server khởi động...
timeout /t 3 /nobreak >nul

echo.
echo 🚀 Đang mở giao diện game đẹp mắt...
echo.
echo Tính năng mới:
echo   ✨ Giao diện gradient đẹp mắt
echo   🎨 Thiết kế hiện đại như Tailwind CSS  
echo   💬 Chat trực tiếp với emoji
echo   🎯 Nút bấm có hiệu ứng hover
echo   📱 Responsive design
echo.

java -jar "D:\wordgame\wordgame\client\target\client-1.0.0-jar-with-dependencies.jar"

echo.
echo Game đã đóng. Nhấn Enter để thoát...
pause >nul