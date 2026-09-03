#!/bin/bash
# AVCOE Lost & Found - Start Script

echo "Starting AVCOE Lost & Found Platform..."
echo "Open http://localhost:8080 in your browser"
echo ""
echo "Demo Accounts:"
echo "  finder@example.com   / password123"
echo "  claimant1@example.com / password123"
echo "  claimant2@example.com / password123"
echo "  admin@example.com    / admin123"
echo ""
echo "Press Ctrl+C to stop the server"
echo "============================================"

java -jar "$(dirname "$0")/backend/target/lostfound-1.0.0.jar"
