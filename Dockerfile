# Build
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /app

# Кэшируем зависимости отдельным слоем —
# пересобирается только если поменялись build.gradle/settings.gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Копируем исходники и собираем jar
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon -x test

# Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Непривилегированный пользователь — не запускаем процесс от root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]