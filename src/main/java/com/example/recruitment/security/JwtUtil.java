package com.example.recruitment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT (JSON Web Token) 유틸리티 클래스
 * - Access Token 및 Refresh Token 생성
 * - 토큰 검증 및 만료 여부 확인
 * - 토큰에서 사용자 정보 추출
 */
@Component
public class JwtUtil {

    // application.properties에서 설정된 JWT 비밀 키 값
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Access Token 유효 시간 (밀리초 단위)
    @Value("${jwt.access-expiration}")
    private long accessTokenExpiration;

    // Refresh Token 유효 시간 (밀리초 단위)
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * Access Token 생성
     *
     * @param email - 토큰에 포함될 사용자 이메일
     * @return 생성된 JWT Access Token 문자열
     */
    public String generateAccessToken(String email) {
        return Jwts.builder()
            .setSubject(email) // 사용자 이메일을 토큰의 Subject에 설정
            .setIssuedAt(new Date()) // 토큰 발급 시간 설정
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration)) // 만료 시간 설정
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()),
                SignatureAlgorithm.HS256) // 비밀 키와 서명 알고리즘 설정
            .compact(); // 최종적으로 토큰 문자열 생성
    }

    /**
     * Refresh Token 생성
     *
     * @param email - 토큰에 포함될 사용자 이메일
     * @return 생성된 JWT Refresh Token 문자열
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .setSubject(email) // 사용자 이메일을 토큰의 Subject에 설정
            .setIssuedAt(new Date()) // 토큰 발급 시간 설정
            .setExpiration(
                new Date(System.currentTimeMillis() + refreshTokenExpiration)) // 만료 시간 설정
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()),
                SignatureAlgorithm.HS256) // 비밀 키와 서명 알고리즘 설정
            .compact(); // 최종적으로 토큰 문자열 생성
    }

    /**
     * 토큰에서 사용자 이메일 추출
     *
     * @param token - 검증할 JWT 토큰
     * @return 토큰의 Subject (사용자 이메일)
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject); // Claims의 Subject (이메일) 추출
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token - 검증할 JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // 현재 시간과 만료 시간 비교
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token     - 검증할 JWT 토큰
     * @param userEmail - 토큰의 Subject(이메일)와 비교할 사용자 이메일
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token, String userEmail) {
        final String email = extractEmail(token); // 토큰에서 사용자 이메일 추출
        return (email.equals(userEmail) && !isTokenExpired(token)); // 이메일 일치 및 만료 여부 확인
    }

    /**
     * 토큰 만료 시간 추출
     *
     * @param token - 검증할 JWT 토큰
     * @return 토큰의 만료 시간 (Date 객체)
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration); // Claims의 Expiration (만료 시간) 추출
    }

    /**
     * Claims에서 특정 정보를 추출하는 메서드
     *
     * @param token          - 검증할 JWT 토큰
     * @param claimsResolver - Claims에서 추출할 정보를 정의하는 함수형 인터페이스
     * @return 추출된 정보 (Claims의 특정 값)
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // JWT 서명 검증 및 Claims(페이로드) 추출
        final Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())) // 서명 키 설정
            .build() // JWT 파서 빌드
            .parseSignedClaims(token) // 토큰 파싱 및 서명 검증
            .getPayload(); // 페이로드(Claims) 추출

        return claimsResolver.apply(claims); // 추출된 Claims에서 원하는 정보 반환
    }
}