FROM openjdk:8-jre-slim

WORKDIR /app

# 复制 Maven 构建的 jar 包
COPY fish-chat-bootstrap/target/fish-chat-bootstrap-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
