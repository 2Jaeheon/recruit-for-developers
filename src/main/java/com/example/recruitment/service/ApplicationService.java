package com.example.recruitment.service;

import com.example.recruitment.model.dto.ApplicationDTO;
import com.example.recruitment.model.entity.Application;
import com.example.recruitment.model.entity.Application.Status;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.model.entity.User;
import com.example.recruitment.repository.ApplicationRepository;
import com.example.recruitment.repository.JobPostingRepository;
import com.example.recruitment.repository.UserRepository;
import com.example.recruitment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserActivityLogService activityLogService; // 활동 로그 서비스 추가
    private final FileAttachmentService fileAttachmentService; // 파일 저장 서비스 추가


    // 지원하기
    public void applyToJob(HttpServletRequest request, Long jobId, String resumePath) {
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

        // 파일 첨부 정보 저장
        if (resumePath != null && !resumePath.isEmpty()) {
            fileAttachmentService.saveFileAttachment(user, application, resumePath);
        }

        // 활동 로그 저장
        activityLogService.logActivity(request, "POST /applications",
            "채용 공고 '" + jobPosting.getTitle() + "'에 지원했습니다.");
    }

    public List<ApplicationDTO> getApplications(String header, String status, String sortBy,
        String direction) {
        String token = header.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        // 사용자 확인
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

        // 활동 로그 저장
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

        // 상태 업데이트 (PENDING 상태만 취소 가능)
        application.setStatus(Status.REJECTED);
        applicationRepository.save(application);

        // 활동 로그 저장
        activityLogService.logActivity(request, "DELETE /applications/" + applicationId,
            "지원 ID '" + applicationId + "'를 취소했습니다.");
    }

    private User extractUserFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다."));
    }
}