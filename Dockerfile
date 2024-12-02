#Build stage
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

#Run stage
FROM openjdk:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]