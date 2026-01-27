# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN sh mvnw clean install -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]