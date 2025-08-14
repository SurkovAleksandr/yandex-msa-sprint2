#Пересборка образа

docker compose down

docker rmi task3-booking-subgraph:latest

docker compose up -d --build