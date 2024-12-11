package com.example.recruitment.controller;

import com.example.recruitment.model.dto.ApplicationDTO;
import com.example.recruitment.model.entity.UserSkill.ProficiencyLevel;
import com.example.recruitment.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApplicationController
 *
 * <p>이 클래스는 채용 공고 지원과 관련된 REST API를 제공합니다.</p>
 */
@Tag(name = "Application", description = "채용 공고 지원 API")
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 지원하기 API
     *
     * @param request          사용자 인증 정보를 포함하는 HTTP 요청 객체
     * @param jobId            지원할 채용 공고 ID (필수)
     * @param resumePath       사용자의 이력서 경로 (선택)
     * @param proficiencyLevel 사용자의 숙련도 (필수)
     * @return 성공 메시지 반환
     */
    @Operation(summary = "채용 공고 지원", description = "사용자가 특정 채용 공고에 지원합니다. 중복 지원은 불가능합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "지원이 완료되었습니다."),})
    @PostMapping
    public ResponseEntity<String> applyToJob(HttpServletRequest request,
        @Parameter(description = "지원할 채용 공고 ID", required = true) @RequestParam Long jobId,
        @Parameter(description = "이력서 파일 경로") @RequestParam(required = false) String resumePath,
        @Parameter(description = "숙련도", required = true, example = "BEGINNER") @RequestParam ProficiencyLevel proficiencyLevel) {
        applicationService.applyToJob(request, jobId, resumePath, proficiencyLevel);
        return ResponseEntity.ok("지원이 완료되었습니다.");
    }

    /**
     * 지원 내역 조회 API
     *
     * @param status    지원 상태 필터
     * @param sortBy    정렬 기준 필드
     * @param direction 정렬 방향
     * @return 지원 내역 목록
     */
    @Operation(summary = "지원 내역 조회", description = "사용자의 지원 내역을 상태별로 필터링하고 날짜 순으로 정렬합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "지원 내역 조회 성공"),})
    @GetMapping
    public ResponseEntity<List<ApplicationDTO>> getApplications(HttpServletRequest request,
        @Parameter(description = "지원 상태 필터 (PENDING, ACCEPTED, REJECTED)") @RequestParam(required = false) String status,
        @Parameter(description = "정렬 기준 필드 (기본값: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: desc)") @RequestParam(defaultValue = "desc") String direction) {
        List<ApplicationDTO> applications = applicationService.getApplications(request, status,
            sortBy, direction);
        return ResponseEntity.ok(applications);
    }

    /**
     * 지원 취소 API
     *
     * @param request       사용자 인증 정보를 포함하는 HTTP 요청 객체
     * @param applicationId 취소할 지원 ID
     * @return 성공 메시지 반환
     */
    @Operation(summary = "지원 취소", description = "사용자가 자신의 특정 지원 내역을 취소합니다."
        + "여기서 입력하는 id는 jobId가 아닌 Application DB에 저장된 Application id입니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "지원이 취소되었습니다."),})
    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelApplication(HttpServletRequest request,
        @Parameter(description = "취소할 지원 ID", required = true) @PathVariable("id") Long applicationId) {
        applicationService.cancelApplication(request, applicationId);
        return ResponseEntity.ok("지원이 취소되었습니다.");
    }
}