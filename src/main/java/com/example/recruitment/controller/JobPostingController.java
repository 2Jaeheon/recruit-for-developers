package com.example.recruitment.controller;

import com.example.recruitment.model.dto.JobPostingDTO;
import com.example.recruitment.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 필터링 및 페이지네이션을 적용하여 채용 공고 데이터를 조회합니다.
     *
     * @param page       조회할 페이지 번호 (기본값: 0)
     * @param size       페이지당 항목 수 (기본값: 20)
     * @param sortBy     정렬 기준 필드 (기본값: "createdAt")
     * @param direction  정렬 방향: asc(오름차순), desc(내림차순) (기본값: "desc")
     * @param location   지역 필터링 조건 (nullable)
     * @param experience 경력 필터링 조건 (nullable)
     * @param salary     급여 필터링 조건 (nullable)
     * @param techStack  기술스택 필터링 조건 (nullable)
     * @return ResponseEntity<Page < JobPostingDTO>> 필터링된 채용 공고 목록의 DTO 페이지
     */
    @GetMapping
    public ResponseEntity<Page<JobPostingDTO>> getJobPostings(
        @RequestParam(defaultValue = "0") int page,              // 페이지 번호 (0부터 시작)
        @RequestParam(defaultValue = "20") int size,             // 한 페이지당 표시할 데이터 수
        @RequestParam(defaultValue = "createdAt") String sortBy, // 정렬 기준 필드
        @RequestParam(defaultValue = "desc") String direction,   // 정렬 방향 (desc 또는 asc)
        @RequestParam(required = false) String location,         // 지역 필터
        @RequestParam(required = false) String experience,       // 경력 필터
        @RequestParam(required = false) String salary,           // 급여 필터
        @RequestParam(required = false) String techStack) {      // 기술스택 필터

        // 정렬 정보 생성 (direction과 sortBy에 따라 정렬 기준 설정)
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);

        // 페이지네이션 정보 생성
        Pageable pageable = PageRequest.of(page, size, sort);

        // 서비스 계층을 호출하여 필터링된 채용 공고 데이터를 가져옵니다.
        Page<JobPostingDTO> jobPostings = jobPostingService.getFilteredJobPostings(
            pageable, location, experience, salary, techStack);

        // 조회 결과를 ResponseEntity에 담아 반환합니다.
        return ResponseEntity.ok(jobPostings);
    }
}