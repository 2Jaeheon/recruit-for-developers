package com.example.recruitment.service;

import com.example.recruitment.model.dto.JobPostingDTO;
import com.example.recruitment.model.entity.JobPosting;
import com.example.recruitment.repository.JobPostingRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
     * 필터링 및 검색 조건에 맞는 채용 공고를 조회합니다.
     *
     * @param pageable    페이지네이션 및 정렬 정보
     * @param location    지역 필터
     * @param experience  경력 필터
     * @param salary      급여 필터
     * @param techStack   기술스택 필터
     * @param keyword     키워드 검색
     * @param companyName 회사명 검색
     * @param position    포지션 검색
     * @return 필터링 및 검색된 채용 공고의 DTO 페이지
     */
    public Page<JobPostingDTO> getJobPostingsWithSearch(
        Pageable pageable,
        String location,
        String experience,
        String salary,
        String techStack,
        String keyword,
        String companyName,
        String position) {

        // Repository를 호출하여 필터링 및 검색 조건에 맞는 채용 공고를 조회
        Page<JobPosting> jobPostings = jobPostingRepository.findByFiltersAndSearch(
            location, experience, salary, techStack, keyword, companyName, position, pageable);

        // 조회된 엔티티를 DTO로 변환
        return jobPostings.map(jobPosting ->
            new JobPostingDTO(
                jobPosting.getId(),
                jobPosting.getTitle(),
                jobPosting.getLocation(),
                jobPosting.getSalary(),
                jobPosting.getExperience(),
                jobPosting.getCompany().getName(),
                jobPosting.getDescription(),
                jobPosting.getViewCount()
            )
        );
    }


    /**
     * 채용 공고 상세 조회 및 조회수 증가
     *
     * @param id 채용 공고 ID
     * @return JobPostingDTO 상세 정보
     */
    public JobPostingDTO getJobDetails(Long jobId) {
        // JobPosting 엔티티를 조회합니다.
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));

        // 조회수 증가
        jobPosting.setViewCount(jobPosting.getViewCount() + 1);
        jobPostingRepository.save(jobPosting);

        // 엔티티를 DTO로 변환하여 반환합니다.
        return new JobPostingDTO(
            jobPosting.getId(),
            jobPosting.getTitle(),
            jobPosting.getLocation(),
            jobPosting.getSalary(),
            jobPosting.getExperience(),
            jobPosting.getCompany().getName(),
            jobPosting.getDescription(),
            jobPosting.getViewCount()
        );
    }

    /**
     * 관련 공고 추천 기능 같은 회사 또는 비슷한 기술스택을 가진 공고를 추천합니다.
     *
     * @param id 채용 공고 ID
     * @return 추천된 공고 목록
     */
    public List<JobPostingDTO> getRelatedJobPostings(Long jobId) {
        // 현재 JobPosting의 정보를 가져옵니다.
        JobPosting currentJob = jobPostingRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));

        String companyName = currentJob.getCompany().getName();
        String description = currentJob.getDescription();

        // description을 ',' 기준으로 나누고 trim
        String keywords = Arrays.stream(description.split(","))
            .map(String::trim)
            .findFirst() // 첫 번째 키워드만 예시로 사용 (여러 키워드 조건 추가 가능)
            .orElse("");

        // 관련 공고를 조회합니다.
        List<JobPosting> relatedJobs = jobPostingRepository.findRelatedJobPostingsByKeywords(
            companyName, keywords, currentJob.getId());

        // JobPosting 엔티티를 DTO로 변환합니다.
        return relatedJobs.stream()
            .map(job -> new JobPostingDTO(
                job.getId(),
                job.getTitle(),
                job.getLocation(),
                job.getSalary(),
                job.getExperience(),
                job.getCompany().getName(),
                job.getDescription(),
                job.getViewCount()
            ))
            .collect(Collectors.toList());
    }
}