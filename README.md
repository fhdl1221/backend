# 🍦 SoftDay - Backend

**소프트데이** 서비스의 데이터 처리, 사용자 인증, AI 분석 연동을 담당하는 **REST API 서버**입니다.
클라이언트(Frontend)로부터 받은 데이터를 안전하게 저장하고, Google Gemini AI를 활용하여 사용자의 감정과 스트레스를 분석합니다.

---

## 📦 배포 (Deployment) 
* **API Base URL:** `https://softday-backend.onrender.com/api`
* **Platform:** Render (Docker)
* **Status:** Live 🟢
*(현재 Render를 통해 배포되어 실행 중입니다.)*

---

## ✨ 주요 기능 (Key Features)

* **🔐 보안 인증 (Authentication)**
    - JWT(JSON Web Token) 기반의 Stateless 인증 시스템을 구축하여 안전한 로그인/회원가입을 지원합니다.
    - Spring Security를 통해 API 엔드포인트별 접근 권한을 관리합니다.
* **😊 마음체크 (일일 체크인)**
    - 사용자의 하루 감정 상태와 스트레스 원인을 DB에 저장하고 관리합니다.
* **🤖 위톡 (AI 챗봇 서비스)**
    - Google Gemini API와 연동하여 사용자의 대화 내용을 분석하고, 위로와 조언을 생성합니다.
    - 대화 맥락(Context)을 유지하며 JSON 형식으로 감정 데이터를 추출합니다.
* **📊 데이터 분석**
    - 축적된 체크인 데이터를 바탕으로 주간/월간 스트레스 추이와 주요 원인 통계를 집계합니다.
* **🔔 스마트 알림**
    - 스케줄러가 매일 오전 9시에 실행되어, 스트레스 지수가 높은 사용자에게 맞춤형 웹 푸시(Web Push) 알림을 전송합니다.

---

## 🛠 기술 스택 (Tech Stack)

* **Framework:** Spring Boot 3.3
* **Language:** Java 17
* **Database:** PostgreSQL (Supabase)
* **ORM:** Spring Data JPA (Hibernate)
* **Security:** Spring Security, JWT
* **AI Model:** Google Gemini Pro
* **Deployment:** Render (Docker)

---

## 📂 Folder Structure

```bash
src/main/java/com/example/backend/
├── config/         # Security, CORS, Gemini API 설정 클래스
├── controller/     # 클라이언트 요청을 처리하는 API 엔드포인트 (Auth, Chat, User 등)
├── dto/            # 계층 간 데이터 전송을 위한 객체 (Request/Response DTO)
├── model/          # DB 테이블과 매핑되는 JPA 엔티티 (User, DailyCheckIn 등)
├── repository/     # DB 데이터 접근을 위한 JPA Repository 인터페이스
├── scheduler/      # 정기적인 작업을 처리하는 스케줄러 (스트레스 체크 알림)
├── service/        # 핵심 비즈니스 로직 구현 (로그인, 분석, 알림 발송 등)
└── BackendApplication.java # 애플리케이션 진입점
```

---

## 💻 로컬에서 실행하기 (For Developers)
이 프로젝트를 로컬 환경에서 실행해보고 싶다면 아래 절차를 따르세요.

1. 환경 변수 설정 (application.properties)
src/main/resources/application.properties 파일에 다음 설정이 필요합니다. (실제 값은 보안상 비워져 있습니다.)

```properties
# Database (Supabase)
spring.datasource.url=jdbc:postgresql://[YOUR_DB_HOST]:5432/postgres
spring.datasource.username=[YOUR_DB_USER]
spring.datasource.password=[YOUR_DB_PASSWORD]

# JWT
app.jwtSecret=[YOUR_JWT_SECRET_KEY]
app.jwtExpirationInMs=604800000

# Gemini AI
gemini.api.key=[YOUR_GEMINI_API_KEY]

# Web Push (VAPID)
vapid.public.key=[YOUR_VAPID_PUBLIC_KEY]
vapid.private.key=[YOUR_VAPID_PRIVATE_KEY]
vapid.subject=mailto:your-email@example.com
```

2. 빌드 (Build)
Maven을 사용하여 프로젝트를 빌드합니다.
```bash
mvn clean install
```

3. 실행 (Run)
Spring Boot 애플리케이션을 실행합니다.
```bash
mvn spring-boot:run
```

서버가 실행되면 http://localhost:8080에서 API 요청을 받을 수 있습니다.
