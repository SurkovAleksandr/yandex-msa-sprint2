#Пересборка образа

./gradlew bootJar

docker rmi task2-booking-service:latest

docker compose up -d --build