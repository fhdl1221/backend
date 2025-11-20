# 1. 빌드 단계 (Maven과 Java 17을 이용해 프로젝트 빌드)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. 실행 단계 (빌드된 결과물만 가져와서 가볍게 실행)
# [수정] openjdk:17-jdk-slim 대신 eclipse-temurin:17-jdk 사용
FROM eclipse-temurin:17-jdk
WORKDIR /app
# 빌드 단계에서 생성된 jar 파일을 app.jar라는 이름으로 복사
COPY --from=build /app/target/*.jar app.jar

# 8080 포트 개방
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]