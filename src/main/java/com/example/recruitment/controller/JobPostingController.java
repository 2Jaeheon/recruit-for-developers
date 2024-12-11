package com.example.recruitment.controller;

import com.example.recruitment.model.dto.JobPostingDTO;
import com.example.recruitment.service.JobPostingService;
import com.example.recruitment.service.UserActivityLogService;
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
 * <p>
 * 이 클래스는 채용 공고 데이터를 조회하기 위한 REST API 엔드포인트를 제공합니다. 필터링, 페이지네이션 및 정렬 기능을 지원합니다.
 * </p>
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    /**
     * 채용 공고 비즈니스 로직을 담당하는 서비스
     */
    private final JobPostingService jobPostingService;
    private final UserActivityLogService activityLogService;

    /**
     * 필터링 및 검색 조건을 적용하여 채용 공고 데이터를 조회합니다.
     *
     * @param page        페이지 번호
     * @param size        페이지당 항목 수
     * @param sortBy      정렬 기준 필드
     * @param direction   정렬 방향
     * @param location    지역 필터
     * @param experience  경력 필터
     * @param salary      급여 필터
     * @param techStack   기술스택 필터
     * @param keyword     키워드 검색
     * @param companyName 회사명 검색
     * @param position    포지션 검색
     * @return 필터링 및 검색된 채용 공고 목록
     */
    @GetMapping
    public ResponseEntity<Page<JobPostingDTO>> getJobPostings(
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String experience,
        @RequestParam(required = false) String salary,
        @RequestParam(required = false) String techStack,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String companyName,
        @RequestParam(required = false) String position) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 서비스 호출
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
     * @return ResponseEntity<JobPostingDTO> 상세 정보 및 관련 공고
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingDTO> getJobDetails(HttpServletRequest request,
        @PathVariable Long id) {
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
     * @return ResponseEntity<List < JobPostingDTO>> 추천된 공고 목록
     */
    @GetMapping("/{id}/related")
    public ResponseEntity<List<JobPostingDTO>> getRelatedJobs(HttpServletRequest request,
        @PathVariable Long id) {
        List<JobPostingDTO> relatedJobs = jobPostingService.getRelatedJobPostings(id);

        // 활동 로그 기록
        activityLogService.logActivity(
            request, "GET /jobs/" + id + "/related", "관련 공고 조회");

        return ResponseEntity.ok(relatedJobs);
    }
}