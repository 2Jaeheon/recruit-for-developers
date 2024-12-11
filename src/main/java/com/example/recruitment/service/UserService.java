package com.example.recruitment.service;

import com.example.recruitment.model.dto.UserProfileDTO;
import com.example.recruitment.model.dto.UserSkillDTO;
import com.example.recruitment.model.entity.LoginHistory;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.repository.LoginHistoryRepository;
import com.example.recruitment.repository.UserRepository;
import com.example.recruitment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // Lombok이 생성자를 자동 생성하여 필요한 의존성을 주입
public class UserService {

    // 의존성 주입: Repository, JwtUtil, PasswordEncoder
    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository; // 로그인 이력 저장소
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    /**
     * 이메일 형식 검증을 위한 정규식 패턴 RFC 5322 기준으로 작성된 이메일 검증용 정규식
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * 사용자 회원가입 처리
     *
     * @param user - 회원가입 요청에서 전달된 사용자 정보
     * @return 저장된 사용자 정보 반환
     */
    public User registerUser(User user) {
        // 이메일 형식 검증
        if (!isValidEmail(user.getEmail())) {
            throw new ValidationException("Invalid email format. Please provide a valid email.");
        }

        // 중복 회원 검사
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already exists. Please use a different email.");
        }

        // 비밀번호 암호화: 평문 비밀번호를 BCrypt 해싱 알고리즘으로 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 사용자 정보 저장
        return userRepository.save(user);
    }

    /**
     * 이메일 형식 검증 메서드
     *
     * @param email - 검증할 이메일
     * @return 이메일이 유효하면 true, 아니면 false
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 사용자 로그인 처리 및 로그인 이력 저장
     *
     * @param email    - 사용자 이메일
     * @param password - 사용자 비밀번호
     * @param request  - HttpServletRequest (IP 주소 추출)
     * @return Access Token 및 Refresh Token 반환
     */
    public Map<String, String> login(String email, String password, HttpServletRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 로그인 이력 저장
        saveLoginHistory(user, request);

        // Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return tokens;
    }

    /**
     * 사용자 프로필 조회
     *
     * @param email - 인증된 사용자의 이메일
     * @return 조회된 사용자 정보 반환
     */
    public User getUserProfile(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 사용자 프로필 조회 (UserSkill 포함)
    public UserProfileDTO getUserProfileWithSkills(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // UserSkill 리스트를 UserSkillDTO로 변환
        List<UserSkillDTO> skills = user.getUserSkills().stream()
            .map(skill -> new UserSkillDTO(
                skill.getSkill().getId(),
                skill.getSkill().getName(),
                skill.getProficiencyLevel().name(),
                skill.getAcquiredAt().toString()
            ))
            .collect(Collectors.toList());

        // UserProfileDTO 반환
        return new UserProfileDTO(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().name(),
            skills
        );
    }

    /**
     * 사용자 프로필 정보 수정
     *
     * @param email       - 인증된 사용자의 이메일
     * @param updatedUser - 수정할 사용자 정보
     * @return 수정된 사용자 정보 반환
     */
    public User updateUserProfile(String email, User updatedUser) {
        User user = getUserProfile(email);

        // 이름 업데이트
        user.setName(updatedUser.getName());

        // 비밀번호가 변경되었을 경우 암호화 후 저장
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * 사용자 계정 삭제
     *
     * @param email - 인증된 사용자의 이메일
     */
    public void deleteUser(String email) {
        User user = getUserProfile(email);
        userRepository.delete(user);
    }

    /**
     * Refresh Token 검증 및 새로운 Access Token 발급
     *
     * @param refreshToken - 클라이언트가 전달한 Refresh Token
     * @return 새로운 Access Token
     */
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token의 유효성 검증
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Refresh Token에서 사용자 이메일 추출
        String email = jwtUtil.extractEmail(refreshToken);

        // 사용자 존재 여부 확인
        userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // 새로운 Access Token 발급
        return jwtUtil.generateAccessToken(email);
    }

    /**
     * 로그인 이력을 저장하는 메서드
     *
     * @param user    - 로그인한 사용자
     * @param request - HttpServletRequest (IP 주소 추출용)
     */
    private void saveLoginHistory(User user, HttpServletRequest request) {
        System.out.println("로그인 이력 저장");
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setUser(user);
        loginHistory.setIpAddress(request.getRemoteAddr()); // 요청의 IP 주소 설정
        loginHistory.setLoginTime(LocalDateTime.now());     // 현재 시간 기록

        System.out.println("로그인 이력: " + loginHistory.getUser().getEmail());
        System.out.println("로그인 시간: " + loginHistory.getLoginTime());
        System.out.println("IP 주소: " + loginHistory.getIpAddress());

        loginHistoryRepository.save(loginHistory); // 이력 저장
    }
}