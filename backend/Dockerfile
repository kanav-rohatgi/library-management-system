FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn --batch-mode --no-transfer-progress package

FROM eclipse-temurin:17-jre
COPY --from=build --chmod=644 /build/target/library-management-system-1.0.0-SNAPSHOT.jar /app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
