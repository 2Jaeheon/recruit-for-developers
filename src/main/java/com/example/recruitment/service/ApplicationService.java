package com.example.recruitment.service;

import com.example.recruitment.model.dto.ApplicationDTO;
import com.example.recruitment.model.entity.Application;
import com.example.recruitment.model.entity.Application.Status;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.model.entity.Skill;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.model.entity.UserSkill;
import com.example.recruitment.model.entity.UserSkill.ProficiencyLevel;
import com.example.recruitment.model.entity.UserSkillId;
import com.example.recruitment.repository.ApplicationRepository;
import com.example.recruitment.repository.JobPostingRepository;
import com.example.recruitment.repository.SkillRepository;
import com.example.recruitment.repository.UserRepository;
import com.example.recruitment.repository.UserSkillRepository;
import com.example.recruitment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ApplicationService
 *
 * <p>
 * 이 클래스는 지원 신청, 조회, 취소와 관련된 비즈니스 로직을 처리합니다. 추가적으로, 지원 시 공고의 description에서 스킬을 추출하고 사용자 숙련도와 함께
 * 저장합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserActivityLogService activityLogService; // 활동 로그 서비스
    private final FileAttachmentService fileAttachmentService; // 파일 첨부 서비스
    private final SkillRepository skillRepository; // 스킬 저장소
    private final UserSkillRepository userSkillRepository; // 사용자-스킬 저장소

    /**
     * 지원하기
     *
     * <p>
     * 사용자가 특정 채용 공고에 지원합니다. 중복 지원 여부를 확인하고, 이력서를 첨부할 수 있으며, 지원 시 공고의 description에 있는 스킬 정보를 저장합니다.
     * </p>
     *
     * @param request          HTTP 요청 객체 (인증 정보 포함)
     * @param jobId            지원할 공고의 ID
     * @param resumePath       이력서 파일 경로 (선택 사항)
     * @param proficiencyLevel 사용자의 숙련도 (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
     */
    public void applyToJob(HttpServletRequest request, Long jobId, String resumePath,
        ProficiencyLevel proficiencyLevel) {

        // JWT 토큰에서 사용자 정보 추출
        User user = extractUserFromRequest(request);

        // 중복 지원 체크
        Optional<Application> existingApplication =
            applicationRepository.findByUserAndJobPosting_Id(user, jobId);

        if (existingApplication.isPresent()) {
            throw new IllegalArgumentException("이미 지원한 공고입니다.");
        }

        // 채용 공고 조회
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("해당 채용 공고가 존재하지 않습니다."));

        // 지원 정보 저장
        Application application = new Application();
        application.setUser(user);
        application.setJobPosting(jobPosting);
        application.setResumePath(resumePath);
        applicationRepository.save(application);

        // 파일 첨부 저장 (이력서가 존재하는 경우)
        if (resumePath != null && !resumePath.isEmpty()) {
            fileAttachmentService.saveFileAttachment(user, application, resumePath);
        }

        // 활동 로그 기록
        activityLogService.logActivity(request, "POST /applications",
            "채용 공고 '" + jobPosting.getTitle() + "'에 지원했습니다.");

        // 스킬 저장 및 UserSkill 연결
        saveSkillsAndUserSkills(jobPosting.getDescription(), user, application, proficiencyLevel);
    }

    /**
     * 지원 내역 조회
     *
     * @param header    인증 헤더 (JWT 토큰)
     * @param status    지원 상태 필터 (PENDING, ACCEPTED, REJECTED)
     * @param sortBy    정렬 기준 필드
     * @param direction 정렬 방향 (asc, desc)
     * @return 지원 내역을 DTO 형태로 반환
     */
    public List<ApplicationDTO> getApplications(String header, String status, String sortBy,
        String direction) {
        // JWT 토큰에서 사용자 이메일 추출
        String token = header.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));

        // 상태 필터링 및 정렬 기준 설정
        List<Application> applications;
        if (status != null && !status.isEmpty()) {
            applications = applicationRepository.findByUserAndStatusOrderBy(user,
                Status.valueOf(status.toUpperCase()), sortBy, direction);
        } else {
            applications = applicationRepository.findByUserOrderBy(user, sortBy, direction);
        }

        // 활동 로그 기록
        activityLogService.logActivity(email, "GET /applications",
            "지원 내역을 조회했습니다. 필터: " + (status != null ? status : "전체"));

        // 결과를 DTO로 변환
        return applications.stream()
            .map(app -> new ApplicationDTO(
                app.getId(),
                app.getUser().getId(),
                app.getUser().getEmail(),
                app.getJobPosting().getId(),
                app.getJobPosting().getTitle(),
                app.getStatus().name(),
                app.getResumePath(),
                app.getCreatedAt().toString()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 지원 취소
     *
     * @param request       HTTP 요청 객체 (인증 정보 포함)
     * @param applicationId 취소할 지원의 ID
     */
    public void cancelApplication(HttpServletRequest request, Long applicationId) {
        User user = extractUserFromRequest(request);

        // 지원 내역 조회
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("해당 지원 내역이 존재하지 않습니다."));

        // 본인 확인
        if (!application.getUser().equals(user)) {
            throw new IllegalArgumentException("본인의 지원만 취소할 수 있습니다.");
        }

        // 취소 가능 여부 확인
        if (application.getStatus() == Status.REJECTED) {
            throw new IllegalStateException("이미 취소된 지원입니다.");
        }

        if (application.getStatus() == Status.ACCEPTED) {
            throw new IllegalStateException("승인된 지원은 취소할 수 없습니다.");
        }

        // 상태 업데이트
        application.setStatus(Status.REJECTED);
        applicationRepository.save(application);

        // 활동 로그 기록
        activityLogService.logActivity(request, "DELETE /applications/" + applicationId,
            "지원 ID '" + applicationId + "'를 취소했습니다.");
    }

    /**
     * JWT 토큰에서 사용자 정보 추출
     *
     * @param request HTTP 요청 객체
     * @return 사용자 정보
     */
    private User extractUserFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }

    /**
     * 스킬 저장 및 UserSkill 연결
     *
     * @param description      공고 설명 (콤마로 나눈 스킬들)
     * @param user             사용자 정보
     * @param application      지원 정보
     * @param proficiencyLevel 사용자의 숙련도
     */
    private void saveSkillsAndUserSkills(String description, User user, Application application,
        ProficiencyLevel proficiencyLevel) {
        // description에서 스킬 추출 및 저장
        Arrays.stream(description.split(","))
            .map(String::trim)
            .forEach(skillName -> {
                // 스킬 저장 (중복 체크)
                Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(skillName);
                        newSkill.setDescription(skillName);
                        return skillRepository.save(newSkill);
                    });

                // UserSkill 저장
                UserSkillId userSkillId = new UserSkillId(user.getId(), skill.getId());
                if (!userSkillRepository.existsById(userSkillId)) {
                    UserSkill userSkill = new UserSkill();
                    userSkill.setId(userSkillId);
                    userSkill.setUser(user);
                    userSkill.setSkill(skill);
                    userSkill.setProficiencyLevel(proficiencyLevel);
                    userSkill.setAcquiredAt(LocalDateTime.now());
                    userSkillRepository.save(userSkill);
                }
            });
    }
}