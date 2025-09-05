# 使用 Maven 3.6.3 和 OpenJDK 8 作为基础镜像
FROM maven:3.6.3-openjdk-8

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml ./
COPY bootstrap/pom.xml ./bootstrap/

# 复制源代码
COPY bootstrap/src ./bootstrap/src

# 构建项目
RUN mvn clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 复制入口脚本
COPY docker/entrypoint.sh /app/docker/entrypoint.sh
RUN chmod +x /app/docker/entrypoint.sh

# 设置入口点
ENTRYPOINT ["sh", "/app/docker/entrypoint.sh"]