# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -q

COPY src ./src

RUN ./mvnw package -DskipTests -q


# Stage 2: Runtime
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
