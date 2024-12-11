package com.example.recruitment.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JwtAuthenticationFilter
 * <p>
 * JWT (JSON Web Token) 기반 인증을 처리하는 필터입니다. HTTP 요청마다 실행되며, 다음과 같은 역할을 수행합니다:
 * <ul>
 *     <li>HTTP 요청 헤더에서 JWT 토큰을 추출</li>
 *     <li>JWT 토큰의 유효성을 검증</li>
 *     <li>유효한 토큰인 경우 Spring Security의 인증 객체를 설정</li>
 *     <li>만료된 토큰이 발견되면 JSON 형태의 에러 메시지를 반환</li>
 * </ul>
 * 이 필터는 Spring Security 필터 체인에서 **`OncePerRequestFilter`**를 확장하여
 * 각 요청당 한 번만 실행됩니다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // JWT 관련 유틸리티 클래스

    /**
     * HTTP 요청을 가로채 JWT 토큰을 검증하고, 유효한 경우 인증 정보를 설정합니다.
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인 (다음 필터로 전달)
     * @throws ServletException 예외 발생 시
     * @throws IOException      IO 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // Swagger 요청은 필터에서 제외
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return; // 필터링 중단
        }
        final String authHeader = request.getHeader("Authorization"); // Authorization 헤더 추출
        final String jwt;        // JWT 토큰을 저장할 변수
        final String userEmail;  // JWT에서 추출한 사용자 이메일

        try {
            // 1. Authorization 헤더 확인
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // 유효하지 않은 Authorization 헤더인 경우 다음 필터로 넘어감
                filterChain.doFilter(request, response);
                return;
            }

            // 2. "Bearer " 접두사 제거 후 JWT 추출
            jwt = authHeader.substring(7); // 인덱스 7부터 시작해 토큰만 추출

            // 3. JWT 토큰에서 사용자 이메일 (Subject) 추출
            userEmail = jwtUtil.extractEmail(jwt);

            // 4. SecurityContext에 인증 정보가 없는 경우만 처리
            if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. 토큰 유효성 검사 (서명 및 만료 시간 확인)
                if (jwtUtil.validateToken(jwt, userEmail)) {

                    // 6. 유효한 경우 Spring Security 인증 객체 생성
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userEmail, null, null);

                    // 7. 요청의 세부 정보를 인증 객체에 설정
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                    // 8. SecurityContext에 인증 객체 설정
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 9. 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            // 10. JWT 만료 예외 처리
            handleExpiredJwtException(response, ex);
        }
    }

    /**
     * ExpiredJwtException을 처리하고 JSON 형태의 에러 메시지를 반환합니다.
     * <p>
     * 만료된 토큰이 발견되면 HTTP 상태 코드를 401 (Unauthorized)로 설정하고, JSON 형태의 응답을 클라이언트에 반환합니다.
     *
     * @param response HTTP 응답 객체
     * @param ex       ExpiredJwtException 예외 객체
     * @throws IOException IO 예외 발생 시
     */
    private void handleExpiredJwtException(HttpServletResponse response, ExpiredJwtException ex)
        throws IOException {
        // 1. HTTP 상태 코드 설정 (401 Unauthorized)
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // 2. 응답 타입 설정 (application/json)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 3. JSON 형태의 에러 메시지 생성
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "JWT Token is expired");

        // 4. ObjectMapper를 사용해 JSON 형태로 응답 작성
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper()
            .writeValueAsString(errorResponse));
    }
}