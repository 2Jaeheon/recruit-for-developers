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
     * 사용자 활동 로그를 저장합니다. (HttpServletRequest 사용)
     *
     * @param request     HTTP 요청 객체
     * @param action      수행된 액션 (API 엔드포인트)
     * @param description 상세 설명
     */
    public void logActivity(HttpServletRequest request, String action, String description) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        String userEmail = jwtUtil.extractEmail(token);
        saveLog(userEmail, action, description);
    }

    /**
     * 사용자 활동 로그를 저장합니다. (HttpServletRequest 없이 직접 이메일 사용)
     *
     * @param userEmail   사용자 이메일
     * @param action      수행된 액션 (API 엔드포인트)
     * @param description 상세 설명
     */
    public void logActivity(String userEmail, String action, String description) {
        saveLog(userEmail, action, description);
    }

    /**
     * 활동 로그 저장 로직
     */
    private void saveLog(String userEmail, String action, String description) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setActionType(action);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());

        activityLogRepository.save(log);
    }
}