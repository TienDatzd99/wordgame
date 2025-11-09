#!/usr/bin/env python3
"""
Simple HTTP File Server for Word Game
Chia sẻ file JAR client và server cho các máy khác trong mạng
"""

import http.server
import socketserver
import os
import socket

# Cấu hình
PORT = 8080
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def end_headers(self):
        # Thêm CORS headers để cho phép download từ mọi nguồn
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        super().end_headers()
    
    def do_GET(self):
        # Custom root path để hiển thị trang download
        if self.path == '/':
            self.send_response(200)
            self.send_header('Content-type', 'text/html; charset=utf-8')
            self.end_headers()
            
            # Lấy IP của server
            hostname = socket.gethostname()
            local_ip = socket.gethostbyname(hostname)
            
            html = f"""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>🎮 Word Game - Download</title>
                <style>
                    * {{
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }}
                    body {{
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 20px;
                    }}
                    .container {{
                        background: white;
                        border-radius: 20px;
                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                        padding: 40px;
                        max-width: 700px;
                        width: 100%;
                    }}
                    h1 {{
                        color: #667eea;
                        text-align: center;
                        margin-bottom: 10px;
                        font-size: 2.5em;
                    }}
                    .subtitle {{
                        text-align: center;
                        color: #666;
                        margin-bottom: 30px;
                        font-size: 1.1em;
                    }}
                    .server-info {{
                        background: #f0f4ff;
                        border-left: 4px solid #667eea;
                        padding: 15px;
                        margin-bottom: 30px;
                        border-radius: 5px;
                    }}
                    .server-info strong {{
                        color: #667eea;
                    }}
                    .download-section {{
                        margin: 30px 0;
                    }}
                    .download-btn {{
                        display: block;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        text-decoration: none;
                        padding: 20px 30px;
                        border-radius: 10px;
                        text-align: center;
                        margin: 15px 0;
                        font-size: 1.2em;
                        font-weight: bold;
                        transition: transform 0.2s, box-shadow 0.2s;
                        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                    }}
                    .download-btn:hover {{
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
                    }}
                    .download-btn.server {{
                        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                        box-shadow: 0 4px 15px rgba(245, 87, 108, 0.4);
                    }}
                    .download-btn.server:hover {{
                        box-shadow: 0 6px 20px rgba(245, 87, 108, 0.6);
                    }}
                    .instructions {{
                        background: #fffbf0;
                        border-left: 4px solid #ffc107;
                        padding: 20px;
                        border-radius: 5px;
                        margin-top: 30px;
                    }}
                    .instructions h3 {{
                        color: #f57c00;
                        margin-bottom: 15px;
                    }}
                    .instructions ol {{
                        margin-left: 20px;
                        line-height: 1.8;
                    }}
                    .instructions code {{
                        background: #fff;
                        padding: 2px 8px;
                        border-radius: 3px;
                        color: #e91e63;
                        font-family: 'Courier New', monospace;
                    }}
                    .icon {{
                        font-size: 1.5em;
                        margin-right: 10px;
                    }}
                    .footer {{
                        text-align: center;
                        margin-top: 30px;
                        color: #999;
                        font-size: 0.9em;
                    }}
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎮 Word Game</h1>
                    <p class="subtitle">Trò chơi đoán từ multiplayer</p>
                    
                    <div class="server-info">
                        <p><strong>📡 Server IP:</strong> {local_ip}</p>
                        <p><strong>🔌 Port:</strong> {PORT}</p>
                        <p><strong>🎯 Game Server:</strong> {local_ip}:8888</p>
                    </div>
                    
                    <div class="download-section">
                        <h2>📥 Tải xuống</h2>
                        <a href="/client/target/client-1.0.0-jar-with-dependencies.jar" 
                           class="download-btn" download>
                            <span class="icon">💻</span>
                            Tải Client (Người chơi)
                        </a>
                        <a href="/server/target/server-1.0.0-jar-with-dependencies.jar" 
                           class="download-btn server" download>
                            <span class="icon">🖥️</span>
                            Tải Server (Máy chủ)
                        </a>
                    </div>
                    
                    <div class="instructions">
                        <h3>📖 Hướng dẫn sử dụng</h3>
                        
                        <h4>🎮 Cho người chơi (Client):</h4>
                        <ol>
                            <li>Tải file <code>client-1.0.0-jar-with-dependencies.jar</code></li>
                            <li>Cài đặt <strong>Java 17</strong> hoặc mới hơn</li>
                            <li>Chạy: <code>java -jar client-1.0.0-jar-with-dependencies.jar</code></li>
                            <li>Kết nối đến server: <code>{local_ip}:8888</code></li>
                            <li>Đăng nhập và bắt đầu chơi!</li>
                        </ol>
                        
                        <h4 style="margin-top: 15px;">🖥️ Cho máy chủ (Server):</h4>
                        <ol>
                            <li>Tải file <code>server-1.0.0-jar-with-dependencies.jar</code></li>
                            <li>Chạy: <code>java -jar server-1.0.0-jar-with-dependencies.jar</code></li>
                            <li>Server sẽ chạy trên cổng <strong>8888</strong></li>
                        </ol>
                        
                        <h4 style="margin-top: 15px;">⚠️ Lưu ý:</h4>
                        <ul style="list-style: none; margin-left: 0;">
                            <li>✅ Yêu cầu Java 17 trở lên</li>
                            <li>✅ Đảm bảo firewall cho phép port 8888</li>
                            <li>✅ Tất cả máy phải cùng mạng LAN</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>🚀 Powered by Python HTTP Server</p>
                        <p>© 2025 Word Game Project</p>
                    </div>
                </div>
            </body>
            </html>
            """
            self.wfile.write(html.encode('utf-8'))
        else:
            # Xử lý các request khác bình thường
            super().do_GET()

def get_local_ip():
    """Lấy địa chỉ IP local của máy"""
    try:
        # Tạo socket để lấy IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except:
        return socket.gethostbyname(socket.gethostname())

if __name__ == '__main__':
    # Lấy IP của máy
    local_ip = get_local_ip()
    hostname = socket.gethostname()
    
    print("=" * 60)
    print("🎮  WORD GAME - FILE SHARING SERVER")
    print("=" * 60)
    print(f"📂  Directory: {DIRECTORY}")
    print(f"🌐  Server running on:")
    print(f"    • Local:   http://localhost:{PORT}")
    print(f"    • Network: http://{local_ip}:{PORT}")
    print(f"    • Hostname: http://{hostname}:{PORT}")
    print(f"\n🎯  Game Server: {local_ip}:8888")
    print("\n📱  Các máy khác có thể truy cập:")
    print(f"    http://{local_ip}:{PORT}")
    print("\n⚠️  Nhấn Ctrl+C để dừng server")
    print("=" * 60)
    
    # Khởi động server
    with socketserver.TCPServer(("", PORT), MyHTTPRequestHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n\n🛑 Server đã dừng!")
