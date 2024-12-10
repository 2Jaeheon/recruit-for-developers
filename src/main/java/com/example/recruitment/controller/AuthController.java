package com.example.recruitment.controller;

import com.example.recruitment.model.dto.LoginRequestDTO;
import com.example.recruitment.model.dto.RefreshRequestDTO;
import com.example.recruitment.model.dto.UserProfileDTO;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST 컨트롤러임을 명시하고, "/auth" 경로로 API 요청을 매핑
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // Lombok을 사용해 생성자 주입을 자동으로 생성
public class AuthController {

    private final UserService userService; // UserService를 의존성 주입받음

    /**
     * 회원 가입 API
     *
     * @param user - 요청 본문(RequestBody)에서 전달된 사용자 정보
     * @return 성공 메시지 반환
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // 서비스 계층을 통해 회원 가입 처리
        userService.registerUser(user);

        // 성공 메시지를 반환
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * 로그인 API
     *
     * @param loginRequest - 로그인 요청 정보 (이메일, 비밀번호)
     * @param request      - HttpServletRequest (IP 주소 추출용)
     * @return Access Token 및 Refresh Token 반환
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest,
        HttpServletRequest request) {
        Map<String, String> tokens = userService.login(loginRequest.getEmail(),
            loginRequest.getPassword(), request);
        return ResponseEntity.ok(tokens);
    }

    /**
     * 회원 정보 조회 API
     *
     * @param authentication - 인증된 사용자 정보(SecurityContextHolder에서 가져옴)
     * @return 사용자 프로필 정보 (DTO 형태로 반환)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        // 인증된 사용자의 이메일을 SecurityContext에서 가져옴
        String email = authentication.getName();

        // 서비스 계층을 통해 사용자 정보 조회
        User user = userService.getUserProfile(email);

        // 조회된 User 엔티티를 UserProfileDTO로 변환
        UserProfileDTO userProfileDTO = new UserProfileDTO(
            user.getId(),            // 사용자 ID
            user.getEmail(),         // 사용자 이메일
            user.getName(),          // 사용자 이름
            user.getRole().name()    // 사용자 역할 (ADMIN, USER 등)
        );

        // 프로필 정보를 ResponseEntity로 반환
        return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * 회원 정보 수정 API
     *
     * @param authentication - 인증된 사용자 정보
     * @param updatedUser    - 요청 본문(RequestBody)에서 전달된 수정된 사용자 정보
     * @return 성공 메시지 반환
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(Authentication authentication,
        @RequestBody User updatedUser) {
        // 인증된 사용자의 이메일을 가져옴
        String email = authentication.getName();

        // 서비스 계층을 통해 사용자 프로필 정보 업데이트
        userService.updateUserProfile(email, updatedUser);

        // 성공 메시지 반환
        return ResponseEntity.ok("Profile updated successfully");
    }

    /**
     * 회원 탈퇴 API
     *
     * @param authentication - 인증된 사용자 정보
     * @return 성공 메시지 반환
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        // 인증된 사용자의 이메일을 가져옴
        String email = authentication.getName();

        // 서비스 계층을 통해 사용자 정보 삭제 처리
        userService.deleteUser(email);

        // 성공 메시지 반환
        return ResponseEntity.ok("User deleted successfully");
    }


    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(
        @RequestBody RefreshRequestDTO refreshRequest) {
        System.out.println("토큰 갱신 요청");
        // Refresh Token 검증 및 새로운 Access Token 발급
        String newAccessToken = userService.refreshAccessToken(refreshRequest.getRefreshToken());
        return ResponseEntity.ok("Bearer " + newAccessToken);
    }
}