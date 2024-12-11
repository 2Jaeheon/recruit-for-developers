package com.example.recruitment.service;

import com.example.recruitment.model.entity.User;
import com.example.recruitment.model.entity.UserActivityLog;
import com.example.recruitment.repository.UserActivityLogRepository;
import com.example.recruitment.repository.UserRepository;
import com.example.recruitment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UserActivityLogService
 * <p>
 * 사용자 활동 로그를 기록하는 서비스 클래스입니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 사용자 활동 로그를 저장합니다.
     *
     * @param request     HTTP 요청 객체
     * @param action      수행된 액션 (API 엔드포인트)
     * @param description 상세 설명
     */
    public void logActivity(HttpServletRequest request, String action, String description) {
        // 헤더에서 JWT 토큰 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7); // "Bearer " 제거
        String userEmail = jwtUtil.extractEmail(token);

        // 사용자 찾기
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 로그 저장
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setActionType(action);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        activityLogRepository.save(log);
    }
}