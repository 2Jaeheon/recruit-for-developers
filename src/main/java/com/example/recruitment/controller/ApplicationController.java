package com.example.recruitment.controller;

import com.example.recruitment.model.dto.ApplicationDTO;
import com.example.recruitment.model.entity.UserSkill.ProficiencyLevel;
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

/**
 * ApplicationController
 *
 * <p>
 * 이 클래스는 채용 공고 지원과 관련된 REST API를 제공합니다. 주요 기능은 다음과 같습니다:
 * <ul>
 *     <li>지원하기</li>
 *     <li>지원 내역 조회</li>
 *     <li>지원 취소</li>
 * </ul>
 * </p>
 *
 * <p>모든 API는 인증된 사용자만 사용할 수 있으며 JWT 토큰이 필요합니다.</p>
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 지원하기 API
     *
     * <p>
     * 사용자가 특정 채용 공고에 지원합니다. 이력서 경로와 숙련도를 함께 저장합니다. 중복 지원 시 에러가 발생합니다.
     * </p>
     *
     * @param request          사용자 인증 정보를 포함하는 HTTP 요청 객체
     * @param jobId            지원할 채용 공고 ID (필수)
     * @param resumePath       사용자의 이력서 경로 (선택)
     * @param proficiencyLevel 사용자의 숙련도 (필수, BEGINNER/INTERMEDIATE/ADVANCED/EXPERT)
     * @return ResponseEntity<String> 지원 완료 메시지 반환
     */
    @PostMapping
    public ResponseEntity<String> applyToJob(HttpServletRequest request,
        @RequestParam Long jobId,
        @RequestParam(required = false) String resumePath,
        @RequestParam ProficiencyLevel proficiencyLevel) {

        // 서비스 호출: 지원 정보 저장
        applicationService.applyToJob(request, jobId, resumePath, proficiencyLevel);

        // 결과 반환
        return ResponseEntity.ok("지원이 완료되었습니다.");
    }

    /**
     * 지원 내역 조회 API
     *
     * <p>
     * 사용자의 지원 내역을 조회합니다. 상태별 필터링과 날짜 정렬이 가능합니다.
     * </p>
     *
     * @param authorizationHeader 사용자 인증 토큰 (Bearer 포함)
     * @param status              지원 상태 필터 (옵션: PENDING, ACCEPTED, REJECTED)
     * @param sortBy              정렬 기준 필드 (기본값: createdAt)
     * @param direction           정렬 방향 (asc 또는 desc, 기본값: desc)
     * @return ResponseEntity<List < ApplicationDTO>> 사용자별 지원 내역 목록 반환
     */
    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getApplications(
        @RequestHeader("Authorization") String authorizationHeader,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

        // 서비스 호출: 지원 내역 조회
        List<ApplicationDTO> applications = applicationService.getApplications(
            authorizationHeader, status, sortBy, direction);

        // 결과 반환
        return ResponseEntity.ok(applications);
    }

    /**
     * 지원 취소 API
     *
     * <p>
     * 사용자가 자신의 지원 내역을 취소합니다. 취소 가능 여부를 확인 후 상태를 업데이트합니다.
     * </p>
     *
     * @param request       사용자 인증 정보를 포함하는 HTTP 요청 객체
     * @param applicationId 취소할 지원 ID (필수)
     * @return ResponseEntity<String> 지원 취소 완료 메시지 반환
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelApplication(HttpServletRequest request,
        @PathVariable("id") Long applicationId) {

        // 서비스 호출: 지원 취소
        applicationService.cancelApplication(request, applicationId);

        // 결과 반환
        return ResponseEntity.ok("지원이 취소되었습니다.");
    }
}