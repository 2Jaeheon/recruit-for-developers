package com.example.recruitment.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증을 처리하는 필터 클래스. 이 필터는 요청마다 실행되며, JWT 토큰을 검사하여 유효한 사용자인 경우 인증을 설정한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 관련 유틸리티 클래스 의존성 주입
    private final JwtUtil jwtUtil;

    /**
     * 필터 로직: HTTP 요청에서 JWT 토큰을 검사하고 유효한 경우 Spring Security 인증 컨텍스트에 사용자 설정
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
        // 1. HTTP 요청 헤더에서 "Authorization" 값을 가져옴
        final String authHeader = request.getHeader("Authorization");
        final String jwt;        // JWT 토큰을 저장할 변수
        final String userEmail;  // 사용자 이메일 (토큰에서 추출될 정보)

        // 2. Authorization 헤더가 존재하지 않거나 "Bearer "로 시작하지 않는 경우 필터를 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 다음 필터로 요청 전달
            return; // 필터 로직 종료
        }

        // 3. Authorization 헤더에서 "Bearer " 이후의 실제 JWT 토큰 부분만 추출
        jwt = authHeader.substring(7); // "Bearer " 다음 문자열을 가져옴 (인덱스 7부터)

        // 4. JWT 토큰에서 사용자 이메일(주제, Subject) 추출
        userEmail = jwtUtil.extractEmail(jwt);

        // 5. SecurityContext에 이미 인증 정보가 설정되어 있지 않고, 토큰에서 이메일을 성공적으로 추출한 경우
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. JWT 토큰의 유효성 검사 (서명 및 만료 시간 확인)
            if (jwtUtil.validateToken(jwt, userEmail)) {

                // 7. JWT 토큰이 유효한 경우 Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userEmail, null, null);

                // 8. 요청의 세부 정보를 인증 객체에 설정 (예: IP, 세션 ID)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. SecurityContext에 인증 객체 설정
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. 필터 체인의 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}