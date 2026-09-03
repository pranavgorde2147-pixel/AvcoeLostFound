FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY backend ./backend
COPY frontend ./frontend

RUN rm -rf /workspace/backend/src/main/resources/static && \
    mkdir -p /workspace/backend/src/main/resources/static && \
    cp -r /workspace/frontend/. /workspace/backend/src/main/resources/static/

WORKDIR /workspace/backend
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /workspace/backend/target/lostfound-1.0.0.jar /app/app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-8080} --server.address=0.0.0.0"]
