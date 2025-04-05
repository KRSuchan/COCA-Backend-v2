
# COCA Backend

이 프로젝트는 **COCA** 서비스의 백엔드입니다.  
  
COCA 시스템은 사용자 중심의 일정 관리 및 그룹 협업 플랫폼을 제공함으로써 일상생활, 학업, 업무 등 다양한 분야에서의 효율적인 일정 관리와 소통을 목표로 합니다.  
본 시스템은 사용자가 직면할 수 있는 일정 관리의 불편함을 해소하고, 개인 및 그룹 단위의 일정 공유와 관리를 원활하게 하여 개인의 생산성 향상과 그룹 간 협업을 촉진합니다.  
소프트웨어의 주요 기능을 통해 COCA 시스템은 사용자가 개인 및 그룹 일정을 효율적으로 관리하고, 다른 사용자들과의 협업과 소통을 원활하게 하는 플랫폼을 제공함으로써, 사용자의 생산성과 협업 효율을 극대화하는 것을 목적으로 합니다.  
  
**COCA** 서비스의 프론트엔드는 [해당 링크](https://github.com/kit-COCA/cocaFront2)를 참고해주세요.
![](https://github.com/user-attachments/assets/b564ebf3-897d-4df5-977e-cfb3139599e1) | ![](https://github.com/user-attachments/assets/34cdc9da-fb51-4963-b57d-00a8c95764d6)
---|---|

## 👨‍💻👩‍💻 역할 분담
- **이수찬**: 프로젝트 총괄, DB 설계, 시스템 구조 설계, Spring 백엔드 구현  
- **이상헌**: 프론트 서버 통신 구현, 핵심 알고리즘(빈일정) 구현, CORS 관리, Spring 백엔드 보조, 프론트 UI 보조  
- **임희열**: React UI/UX 설계 및 구현, 프론트 서버 통신 보조  
- **이채연**: Spring 백엔드 구현, 프론트 서버 통신 보조, 핵심 알고리즘(MD5) 구현  

## 🚀 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5, Spring Security 6.2.4
- **File Storage**: AWS S3 (첨부파일 관리)
- **Database**: MySQL
- **Token Management**: Redis (JWT 관리)
- **API Documentation**: Notion

## 📌 주요 기능(42) 
- **회원 관리**(6): 회원 가입, 로그인, 로그아웃, 개인 정보 조회 및 수정, 회원 탈퇴
- **일정 관리**(14): 빈 일정 찾기, 개인 일정 관리(CRUD), 그룹 일정 관리(CRUD) 등 14개 기능
- **그룹 관리**(10): 그룹 관리(CRUD), 그룹 공지 관리(CRUD) 등 10개 기능
- **친구 관리**(5): 친구 관리(CRUD), 친구 일정 조회
- **요청 관리**(4): 요청 관리(CRUD)
- **회원 인증**(3): 회원 토큰 생성, 인증, 삭제

## 📖 API 문서  
COCA 백엔드에서 제공하는 API 명세서는 아래 Notion 페이지에서 확인할 수 있습니다.  
[📄 COCA JSON API 문서](https://bitter-nut-ad9.notion.site/COCA-Json-API-Doc-1b7328d2c5d94c058f0bacc363d484e8)

## 🖥️ 시스템 구조도
![시스템구조도](https://github.com/user-attachments/assets/24f5f984-5380-4cfb-9127-7f36710438d3)

## 📂 프로젝트 구조
```
cocaBack/
    ├── src/main/java/project/coca
        ├── controller  # API 컨트롤러
        ├── service     # 비즈니스 로직
        ├── repository  # 데이터베이스 처리
        ├── domain      # 도메인 클래스
        ├── dto         # DTO 클래스
        ├── jwt         # JWT 클래스
        ├── config      # 설정 파일
        ├── CocaApplication.java      # SpringBoot main
        └── InitData.java             # 시작 데이터 구성 클래스
    ├── src/main/resources/
        ├── application.yml  # 환경 설정
        └── ...
    ├── README.md
    ├── .gitignore
    ├── build.gradle
    └── ...
```
## 📊 ERD  
![COCA_DB 설계V3](https://github.com/user-attachments/assets/5ee2763c-56c9-4e09-9320-d15bb307c0bc)

## 🧱 클래스 다이어그램
### 빈일정 찾기 시스템 클래스
![image](https://github.com/user-attachments/assets/6872a2a2-3b63-4f8c-9ce1-b0907c6cf2b5)
자세한 클래스 다이어그램은 프로젝트 문서 혹은 리뷰 블로그 참고 바랍니다.  
[3. 설계명세서(이수찬, 이상헌, 임희열, 이채연)v1.pdf](https://github.com/kit-COCA/cocaBack/blob/main/documents/3.%20%E1%84%89%E1%85%A5%E1%86%AF%E1%84%80%E1%85%A8%E1%84%86%E1%85%A7%E1%86%BC%E1%84%89%E1%85%A6%E1%84%89%E1%85%A5(%E1%84%8B%E1%85%B5%E1%84%89%E1%85%AE%E1%84%8E%E1%85%A1%E1%86%AB%2C%20%E1%84%8B%E1%85%B5%E1%84%89%E1%85%A1%E1%86%BC%E1%84%92%E1%85%A5%E1%86%AB%2C%20%E1%84%8B%E1%85%B5%E1%86%B7%E1%84%92%E1%85%B4%E1%84%8B%E1%85%A7%E1%86%AF%2C%20%E1%84%8B%E1%85%B5%E1%84%8E%E1%85%A2%E1%84%8B%E1%85%A7%E1%86%AB)v1.pdf)    
[5. 최종보고서(이수찬, 이상헌, 임희열, 이채연)v1.pdf](https://github.com/kit-COCA/cocaBack/blob/main/documents/5.%20%E1%84%8E%E1%85%AC%E1%84%8C%E1%85%A9%E1%86%BC%E1%84%87%E1%85%A9%E1%84%80%E1%85%A9%E1%84%89%E1%85%A5(%E1%84%8B%E1%85%B5%E1%84%89%E1%85%AE%E1%84%8E%E1%85%A1%E1%86%AB%2C%20%E1%84%8B%E1%85%B5%E1%84%89%E1%85%A1%E1%86%BC%E1%84%92%E1%85%A5%E1%86%AB%2C%20%E1%84%8B%E1%85%B5%E1%86%B7%E1%84%92%E1%85%B4%E1%84%8B%E1%85%A7%E1%86%AF%2C%20%E1%84%8B%E1%85%B5%E1%84%8E%E1%85%A2%E1%84%8B%E1%85%A7%E1%86%AB)v1.pdf)  
혹은 [COCA 시스템 개발 리뷰](https://velog.io/@lsc4814/COCA-v1-%EA%B5%AC%ED%98%84-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EB%A6%AC%EB%B7%B0)를 참고해주세요

## ⚙️ 실행 방법
### 1. 환경 변수 설정
`cocaBack/src/main/resources/application.yml`을 아래 내용을 참고하여 수정합니다:
```yaml
jwt:
  secret: "your-jwt-secret-key"
spring:
  datasource:
    url: jdbc:mysql://your-mysql-endpoint:3306/dbname
    username: your-mysql-username
    password: your-mysql-password
  redis:
    host: your-redis-host
    port: your-redis-port
  cloud:
    aws:
      credentials:
        access-key: your-access-key
        secret-key: your-secret-key
      s3:
        bucket: your-bucket-name
      region:
        static: your-region
```
### 2. `.jar` 파일 생성 및 실행
터미널에서 아래 내용 입력 후 Enter:
``` 
./gradlew build
java -jar build/libs/cocaBack-0.0.1-SNAPSHOT.jar
```
---
#### 더 자세한 내용은 [프로젝트 문서](https://github.com/kit-COCA/cocaBack/tree/main/documents)를 참고해주세요.
