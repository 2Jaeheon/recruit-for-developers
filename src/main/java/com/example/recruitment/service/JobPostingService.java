package com.example.recruitment.service;

import com.example.recruitment.model.dto.JobPostingDTO;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * JobPostingService
 * <p>
 * 이 클래스는 채용 공고와 관련된 비즈니스 로직을 담당합니다. 필터링 조건(지역, 경력, 급여, 기술스택)을 기반으로 채용 공고 데이터를 조회하고, 엔티티를 DTO로 변환하여
 * 반환합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JobPostingService {

    /**
     * 채용 공고 데이터베이스 접근을 위한 Repository
     */
    private final JobPostingRepository jobPostingRepository;

    /**
     * 필터링 조건에 맞는 채용 공고를 조회하고 DTO로 변환하여 반환합니다.
     *
     * @param pageable   페이지네이션 및 정렬 정보를 포함하는 객체
     * @param location   지역 필터링 조건 (nullable)
     * @param experience 경력 필터링 조건 (nullable)
     * @param salary     급여 필터링 조건 (nullable)
     * @param techStack  기술스택 필터링 조건 (nullable)
     * @return Page<JobPostingDTO> 필터링된 채용 공고의 DTO 페이지
     * @see JobPostingRepository#findByFilters(String, String, String, String, Pageable)
     */
    public Page<JobPostingDTO> getFilteredJobPostings(
        Pageable pageable,
        String location,
        String experience,
        String salary,
        String techStack) {

        // Repository를 통해 필터링 조건에 맞는 엔티티 목록을 조회합니다.
        Page<JobPosting> jobPostings = jobPostingRepository.findByFilters(location, experience,
            salary, techStack, pageable);

        // 조회된 엔티티 목록을 JobPostingDTO로 변환하여 반환합니다.
        return jobPostings.map(jobPosting ->
            new JobPostingDTO(
                jobPosting.getId(),                 // 채용 공고 ID
                jobPosting.getTitle(),              // 채용 공고 제목
                jobPosting.getLocation(),           // 채용 지역
                jobPosting.getSalary(),             // 급여 조건
                jobPosting.getExperience(),         // 경력 조건
                jobPosting.getCompany().getName(),  // 회사 이름
                jobPosting.getDescription()         // 채용 공고 설명
            )
        );
    }
}