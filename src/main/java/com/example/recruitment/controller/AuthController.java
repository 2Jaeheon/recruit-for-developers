package com.example.recruitment.controller;

import com.example.recruitment.model.dto.LoginRequestDTO;
import com.example.recruitment.model.dto.RefreshRequestDTO;
import com.example.recruitment.model.dto.UserProfileDTO;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 *
 * <p>인증 및 사용자 관리 관련 API를 제공하는 컨트롤러입니다.</p>
 * <p>주요 기능:</p>
 * <ul>
 *     <li>회원 가입</li>
 *     <li>로그인 및 토큰 발급</li>
 *     <li>회원 정보 조회 및 수정</li>
 *     <li>회원 탈퇴</li>
 *     <li>Access Token 갱신</li>
 * </ul>
 */
@Tag(name = "Auth", description = "인증 및 사용자 관리 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 회원 가입 API
     *
     * @param user 회원가입에 필요한 사용자 정보 (이메일, 이름, 비밀번호)
     * @return 회원가입 성공 메시지
     */
    @Operation(summary = "회원 가입", description = "이메일, 이름, 패스워드를 사용해 사용자를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입이 완료되었습니다.")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입에 필요한 사용자 정보 (이메일, 이름, 비밀번호)",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @Schema(example = "{ \"email\": \"hong@gmail.com\", \"name\": \"홍길동\", \"password\": \"password123\" }")
            )
        )
        @RequestBody User user) {

        userService.registerUser(user); // 사용자 등록 서비스 호출
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 API
     *
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @param request      HTTP 요청 객체 (IP 추적 등 활용 가능)
     * @return Access Token 및 Refresh Token 반환
     */
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access 및 Refresh Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 반환")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
        @Parameter(description = "로그인 요청 정보 (이메일, 비밀번호)", required = true)
        @RequestBody LoginRequestDTO loginRequest,
        HttpServletRequest request) {

        Map<String, String> tokens = userService.login(loginRequest.getEmail(),
            loginRequest.getPassword(), request); // 로그인 처리 및 토큰 발급
        return ResponseEntity.ok(tokens);
    }

    /**
     * 회원 정보 조회 API
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자 프로필 정보 (스킬 포함)
     */
    @Operation(summary = "회원 정보 조회", description = "인증된 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(
        @Parameter(hidden = true) Authentication authentication) {

        String email = authentication.getName(); // 인증된 사용자의 이메일 추출
        UserProfileDTO userProfileDTO = userService.getUserProfileWithSkills(
            email); // 프로필 조회 서비스 호출
        return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * 회원 정보 수정 API
     *
     * @param authentication 인증된 사용자 정보
     * @param updatedUser    수정할 사용자 정보 (이메일, 비밀번호)
     * @return 성공 메시지 반환
     */
    @Operation(summary = "회원 정보 수정", description = "비밀번호를 수정합니다. 바꾸고 싶은 계정의 이메일과 새로운 비밀번호를 입력하세요.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
    })
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
        @Parameter(hidden = true) Authentication authentication,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 사용자 정보 (이메일, 비밀번호)",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                schema = @Schema(example = "{ \"email\": \"hong@gmail.com\", \"password\": \"newPassword123\" }")
            )
        )
        @RequestBody User updatedUser) {

        String email = authentication.getName(); // 인증된 사용자의 이메일 추출
        userService.updateUserProfile(email, updatedUser); // 회원 정보 수정 서비스 호출
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * 회원 탈퇴 API
     *
     * @param authentication 인증된 사용자 정보
     * @return 성공 메시지 반환
     */
    @Operation(summary = "회원 탈퇴", description = "인증된 사용자의 계정을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴가 완료되었습니다.")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(
        @Parameter(hidden = true) Authentication authentication) {

        String email = authentication.getName(); // 인증된 사용자의 이메일 추출
        userService.deleteUser(email); // 회원 탈퇴 서비스 호출
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    /**
     * 토큰 갱신 API
     *
     * @param refreshRequest Refresh Token 요청 객체
     * @return 새로운 Access Token 반환
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Access Token 갱신 성공")
    })
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(
        @Parameter(description = "Refresh Token", required = true)
        @RequestBody RefreshRequestDTO refreshRequest) {

        String newAccessToken = userService.refreshAccessToken(
            refreshRequest.getRefreshToken()); // 토큰 갱신 처리
        return ResponseEntity.ok("Bearer " + newAccessToken);
    }
}