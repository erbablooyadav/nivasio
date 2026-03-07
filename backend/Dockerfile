FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew 2>/dev/null || true
RUN ./gradlew bootJar --no-daemon || gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/pg-manager-pro.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
