package com.example.recruitment.controller;

import com.example.recruitment.model.dto.JobPostingDTO;
import com.example.recruitment.service.JobPostingService;
import com.example.recruitment.service.UserActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JobPostingController
 *
 * <p>채용 공고 데이터를 조회하기 위한 REST API 엔드포인트를 제공합니다.</p>
 * <p>이 API는 필터링, 페이지네이션 및 정렬 기능을 지원합니다.</p>
 */
@Tag(name = "JobPosting", description = "채용 공고 관리 API") // Swagger 문서용 태그
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final UserActivityLogService activityLogService;

    /**
     * 채용 공고 목록 조회 API
     *
     * <p>필터링 및 검색 조건을 적용하여 채용 공고 데이터를 조회합니다.</p>
     *
     * @param page        페이지 번호 (기본값: 0)
     * @param size        페이지당 항목 수 (기본값: 20)
     * @param sortBy      정렬 기준 필드 (기본값: createdAt)
     * @param direction   정렬 방향 (asc 또는 desc, 기본값: desc)
     * @param location    지역 필터
     * @param experience  경력 필터
     * @param salary      급여 필터
     * @param techStack   기술 스택 필터
     * @param keyword     키워드 검색
     * @param companyName 회사명 검색
     * @param position    포지션 검색
     * @return 필터링 및 검색된 채용 공고 목록 (페이징 적용)
     */
    @Operation(summary = "채용 공고 목록 조회", description = "필터링, 페이지네이션 및 정렬 기능을 통해 채용 공고 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채용 공고 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<JobPostingDTO>> getJobPostings(
        HttpServletRequest request,
        @Parameter(description = "페이지 번호 (기본값: 0)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지당 항목 수 (기본값: 20)") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "정렬 기준 필드 (기본값: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: desc)") @RequestParam(defaultValue = "desc") String direction,
        @Parameter(description = "지역 필터") @RequestParam(required = false) String location,
        @Parameter(description = "경력 필터") @RequestParam(required = false) String experience,
        @Parameter(description = "급여 필터") @RequestParam(required = false) String salary,
        @Parameter(description = "기술 스택 필터") @RequestParam(required = false) String techStack,
        @Parameter(description = "키워드 검색") @RequestParam(required = false) String keyword,
        @Parameter(description = "회사명 검색") @RequestParam(required = false) String companyName,
        @Parameter(description = "포지션 검색") @RequestParam(required = false) String position) {

        // 정렬 및 페이지네이션 설정
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 서비스 호출 및 채용 공고 조회
        Page<JobPostingDTO> jobPostings = jobPostingService.getJobPostingsWithSearch(
            pageable, location, experience, salary, techStack, keyword, companyName, position);

        // 활동 로그 기록
        activityLogService.logActivity(
            request, "GET /jobs", "채용 공고 목록 조회");

        return ResponseEntity.ok(jobPostings);
    }

    /**
     * 채용 공고 상세 조회 API
     *
     * @param id 채용 공고 ID
     * @return 채용 공고 상세 정보
     */
    @Operation(summary = "채용 공고 상세 조회", description = "특정 채용 공고의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채용 공고 상세 조회 성공")
    })
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingDTO> getJobDetails(
        HttpServletRequest request,
        @Parameter(description = "조회할 채용 공고 ID", required = true) @PathVariable Long id) {

        // 채용 공고 상세 정보 조회
        JobPostingDTO jobDetails = jobPostingService.getJobDetails(id);

        // 활동 로그 기록
        activityLogService.logActivity(
            request, "GET /jobs/" + id, "채용 공고 상세 조회");

        return ResponseEntity.ok(jobDetails);
    }

    /**
     * 관련 공고 추천 API
     *
     * @param id 채용 공고 ID
     * @return 추천된 공고 목록
     */
    @Operation(summary = "관련 공고 추천", description = "특정 채용 공고와 관련된 추천 공고 목록을 조회합니다. 추천 공고가 없으면 빈 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "관련 공고 추천 조회 성공")
    })
    @GetMapping("/{id}/related")
    public ResponseEntity<List<JobPostingDTO>> getRelatedJobs(
        HttpServletRequest request,
        @Parameter(description = "조회할 채용 공고 ID", required = true) @PathVariable Long id) {

        // 관련 공고 추천 조회
        List<JobPostingDTO> relatedJobs = jobPostingService.getRelatedJobPostings(id);

        // 활동 로그 기록
        activityLogService.logActivity(
            request, "GET /jobs/" + id + "/related", "관련 공고 조회");

        return ResponseEntity.ok(relatedJobs);
    }
}