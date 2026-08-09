#!/bin/bash
echo "========================================"
echo "  Starting Full Application"
echo "========================================"

SCRIPT_DIR="$(dirname "$0")"

echo ""
echo "[1/3] Starting Backend in background..."
cd "$SCRIPT_DIR/backend"
chmod +x mvnw
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "[2/3] Waiting 20 seconds for backend to start..."
sleep 20

echo "[3/3] Starting Frontend..."
cd "$SCRIPT_DIR/frontend"
npm install
npm start

# Cleanup on exit
kill $BACKEND_PID 2>/dev/null
