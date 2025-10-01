# ==========================================
# 첫 번째 스테이지: 빌드 스테이지
# ==========================================
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Gradle 설정 파일 먼저 복사 (캐시 최적화)
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Gradle wrapper 복사
COPY gradle gradle
COPY gradlew .

# 의존성 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 및 .env 복사
COPY .env .
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon -x test

# ==========================================
# 두 번째 스테이지: 실행 스테이지
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 첫 번째 스테이지에서 .env 파일 복사
COPY --from=builder /app/.env .env

# 환경변수 기본값
ENV SPRING_PROFILES_ACTIVE=prod

# 헬스체크용 curl 설치
RUN apk add --no-cache curl

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]