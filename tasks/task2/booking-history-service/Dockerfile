FROM openjdk:21-jdk-slim

WORKDIR /app

COPY ./build/libs/booking-history-service-1.0.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]