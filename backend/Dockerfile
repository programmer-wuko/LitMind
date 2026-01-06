# 使用Maven官方镜像作为构建环境
FROM maven:3.8.5-openjdk-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并构建项目
COPY src ./src
RUN mvn clean package -DskipTests

# 使用轻量级的Java运行时镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建好的jar文件
COPY --from=build /app/target/litmind-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8081

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]