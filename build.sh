#!/bin/bash
# AVCOE Lost & Found - Build and Run Script

set -e

echo "============================================"
echo "  AVCOE Campus Lost & Found Platform"
echo "  Amrutvahini College of Engineering"
echo "============================================"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed. Please install Java 17+."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "Java: $JAVA_VERSION"

# Navigate to backend directory
cd "$(dirname "$0")/backend"

# Check if Maven wrapper exists, otherwise use system Maven
if [ -f "./mvnw" ]; then
    MAVEN_CMD="./mvnw"
else
    MAVEN_CMD="mvn"
fi

if ! command -v $MAVEN_CMD &> /dev/null && [ "$MAVEN_CMD" = "mvn" ]; then
    echo "ERROR: Maven is not installed. Please install Maven or use the Maven wrapper."
    exit 1
fi

echo ""
echo "Step 1: Compiling backend..."
$MAVEN_CMD compile -q

echo "Step 2: Building JAR..."
$MAVEN_CMD package -DskipTests -q

echo "Step 3: Copying frontend to static resources..."
cp -r ../frontend/* src/main/resources/static/ 2>/dev/null || true

echo "Step 4: Rebuilding with frontend..."
$MAVEN_CMD package -DskipTests -q

echo ""
echo "============================================"
echo "  Build complete!"
echo "============================================"
echo ""
echo "To start the application:"
echo "  java -jar target/lostfound-1.0.0.jar"
echo ""
echo "Then open http://localhost:8080 in your browser"
echo ""
echo "Demo Accounts:"
echo "  finder@example.com   / password123"
echo "  claimant1@example.com / password123"
echo "  claimant2@example.com / password123"
echo "  admin@example.com    / admin123"
echo "============================================"
