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

/**
 * AuthController
 * <p>
 * 이 컨트롤러는 인증 및 사용자 관리와 관련된 API를 제공합니다. 경로: "/auth"
 * <p>
 * 제공하는 기능:
 * - 회원 가입
 * - 로그인 및 토큰 발급
 * - 회원 정보 조회 및 수정
 * - 회원 탈퇴
 * - 토큰 갱신
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor // Lombok을 사용해 생성자 주입을 자동으로 생성
public class AuthController {

    // 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
    private final UserService userService;

    /**
     * 회원 가입 API
     *
     * @param user 요청 본문(RequestBody)에서 전달된 사용자 정보
     * @return ResponseEntity<String> 성공 메시지 반환
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        // 서비스 계층을 통해 회원 가입 처리
        userService.registerUser(user);

        // 성공 메시지를 반환
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     *
     * @param loginRequest 로그인 요청 정보 (이메일과 비밀번호)
     * @param request      HttpServletRequest (IP 주소 추출에 사용)
     * @return ResponseEntity<Map < String, String>> Access Token 및 Refresh Token 반환
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO loginRequest,
        HttpServletRequest request) {
        // 사용자 로그인 처리 및 토큰 생성
        Map<String, String> tokens = userService.login(loginRequest.getEmail(),
            loginRequest.getPassword(), request);

        // Access Token과 Refresh Token 반환
        return ResponseEntity.ok(tokens);
    }

    /**
     * 회원 정보 조회 API
     *
     * @param authentication 인증된 사용자 정보 (Spring SecurityContext에서 제공)
     * @return ResponseEntity<UserProfileDTO> 사용자 프로필 정보 (스킬 포함)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        // 인증된 사용자의 이메일 가져오기
        String email = authentication.getName();

        // 사용자 프로필 및 스킬 정보를 가져와 DTO로 변환
        UserProfileDTO userProfileDTO = userService.getUserProfileWithSkills(email);

        // 사용자 프로필 반환
        return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * 회원 정보 수정 API
     *
     * @param authentication 인증된 사용자 정보
     * @param updatedUser    요청 본문(RequestBody)에서 전달된 수정된 사용자 정보
     * @return ResponseEntity<String> 성공 메시지 반환
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(Authentication authentication,
        @RequestBody User updatedUser) {
        // 인증된 사용자의 이메일 가져오기
        String email = authentication.getName();

        // 사용자 프로필 정보 업데이트
        userService.updateUserProfile(email, updatedUser);

        // 성공 메시지 반환
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * 회원 탈퇴 API
     *
     * @param authentication 인증된 사용자 정보
     * @return ResponseEntity<String> 성공 메시지 반환
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        // 인증된 사용자의 이메일 가져오기
        String email = authentication.getName();

        // 사용자 정보 삭제 처리
        userService.deleteUser(email);

        // 성공 메시지 반환
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    /**
     * 토큰 갱신 API
     *
     * @param refreshRequest 요청 본문에서 전달된 Refresh Token
     * @return ResponseEntity<String> 새로운 Access Token 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(
        @RequestBody RefreshRequestDTO refreshRequest) {
        System.out.println("토큰 갱신 요청");

        // Refresh Token 검증 및 새로운 Access Token 발급
        String newAccessToken = userService.refreshAccessToken(refreshRequest.getRefreshToken());

        // 새 Access Token 반환
        return ResponseEntity.ok("Bearer " + newAccessToken);
    }
}