#!/bin/bash

echo "===== STARTING CLIENT WITH DEBUG LOGGING ====="
echo "Client will log to: /tmp/client-debug.log"
echo "Server should log to: /tmp/server.log"
echo ""
echo "After starting game:"
echo "1. Click 'Thoát game' button (RED button, not X)"
echo "2. Click YES in confirmation dialog"
echo "3. Check logs:"
echo "   tail -f /tmp/client-debug.log"
echo "   tail -f /tmp/server.log"
echo ""

java -jar /Users/tiendat/BTLLTM/wordgame/wordgame/client/target/client-1.0.0-jar-with-dependencies.jar 2>&1 | tee /tmp/client-debug.log
