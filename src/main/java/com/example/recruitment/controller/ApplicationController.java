package com.example.recruitment.controller;

import com.example.recruitment.model.dto.ApplicationDTO;
import com.example.recruitment.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 지원하기 API
     *
     * @param jobId      지원할 공고 ID
     * @param resumePath 이력서 경로 (선택)
     * @return ResponseEntity
     */
    @PostMapping
    public ResponseEntity<String> applyToJob(HttpServletRequest request,
        @RequestParam Long jobId,
        @RequestParam(required = false) String resumePath) {
        applicationService.applyToJob(request, jobId, resumePath);
        return ResponseEntity.ok("지원이 완료되었습니다.");
    }

    /**
     * 지원 내역 조회 API
     *
     * @param authorizationHeader 사용자 인증 토큰
     * @param status              지원 상태 필터 (옵션)
     * @param sortBy              정렬 기준 필드 (기본값: createdAt)
     * @param direction           정렬 방향 (asc 또는 desc, 기본값: desc)
     * @return 사용자별 지원 내역
     */
    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getApplications(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

        // 서비스 호출
        List<ApplicationDTO> applications = applicationService.getApplications(
            authorizationHeader, status, sortBy, direction);

        // 결과 반환
        return ResponseEntity.ok(applications);
    }

    /**
     * 지원 취소 API
     *
     * @param applicationId 지원 ID
     * @return ResponseEntity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelApplication(HttpServletRequest request,
        @PathVariable("id") Long applicationId) {
        applicationService.cancelApplication(request, applicationId);
        return ResponseEntity.ok("지원이 취소되었습니다.");
    }
}