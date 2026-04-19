# Stage 1: build the jar using Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: run the jar using Amazon Corretto
FROM amazoncorretto:21
WORKDIR /app
LABEL authors="BEN & CO"
COPY --from=build /app/target/*.jar Stripe-Processing-Service.jar
EXPOSE 2222
ENTRYPOINT ["java", "-jar", "Stripe-Processing-Service.jar"]