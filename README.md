# 사람인 크롤링 기반 API 서버

## 1. 개요

- 사람인 사이트
- 크롤링 기반 API 서버
- API 서버를 통해 사람인 사이트의 데이터를 제공

## 2. 개발 환경

- Java 17
- Spring Boot 3.4.0

## 3. API 목록

자세한 사항은 다음의 [Swagger 문서](https://113.198.66.75:10005/swagger-ui/index.html)를 참고해주세요

## 4. 실행 방법

1. Repository를 Clone한다.
2. `./gradlew bootRun` 명령어를 실행한다.
3. [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) 으로
   접속한다.
4. API 서버를 이용한다.

# 5.실행을 위한 참고사항

크롤링은 Selenium을 이용하였으며, <br>
크롤링은 현재 다른 사람들의 경우에는 Chrome과 ChromeDriver 의 버전이 달라 작동이 불가능합니다.
따라서 우선적으로 동작을 위해 Chrome과 ChormeDriver 파일을 주석처리 해놓았습니다.

따라서 다음의 링크를 통해 Chrome과 ChromeDriver를 다운로드 받아주세요.<br>
두 파일을 다운로드 받은 후, ChromeDriver를 프로젝트의 resources 폴더에 넣어주세요.
또한 두 파일의 버전을 동일해야합니다. <br>

[크롬 다운로드](https://www.google.com/chrome/)<br>
[크롬 드라이버 다운로드](https://chromedriver.chromium.org/downloads)

데이터베이스는 MySQL을 사용하였으며, application.properties 파일을 수정하여 사용하시면 됩니다.

```
spring.datasource.url=jdbc:mysql://localhost:3306/recruitments?serverTimezone=Asia/Seoul
spring.datasource.username=userName
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 6. 기능

### 회원관리

- 회원가입
- 로그인
- 계정 비밀번호 수정
- 계정 삭제
- 회원정보 조회

### 채용공고 상세정보

- 채용공고 검색
- 추천 채용공고 조회
- 채용공고 상세정보 조회
- 채용공고 북마크
- 북마크한 채용공고 조회
- 북마크한 채용공고 삭제

### 채용공고 지원

- 채용공고 지원
- 지원한 채용공고 조회
- 지원한 채용공고 삭제