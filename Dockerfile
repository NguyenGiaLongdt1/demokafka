# Stage 1: Build source code thành file JAR
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml và source code để build
COPY pom.xml .
COPY src ./src

# Build file JAR (bỏ qua unit test để build nhanh trong lúc đóng gói)
RUN mvn clean package -DskipTests

# Stage 2: Runtime image siêu nhẹ (chỉ chứa JRE, không chứa Maven hay mã nguồn thừa)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar đã build xong từ Stage 1 sang Stage 2
COPY --from=builder /app/target/*.jar app.jar

# Khai báo port ứng dụng Spring Boot
EXPOSE 8080

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
