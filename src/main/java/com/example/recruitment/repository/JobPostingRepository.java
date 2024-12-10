package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Company;
import com.example.recruitment.model.entity.JobPosting;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JobPostingRepository
 * <p>
 * 이 인터페이스는 채용 공고 데이터베이스 연산을 위한 JPA Repository입니다. JpaRepository를 상속받아 기본적인 CRUD 기능과 추가 쿼리 메서드를
 * 제공합니다.
 * </p>
 */
@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    /**
     * 회사와 제목을 기준으로 중복된 채용 공고를 찾는 메서드입니다.
     * <p>
     * 특정 회사와 제목이 동일한 채용 공고가 이미 존재하는지 확인할 때 사용됩니다.
     * </p>
     *
     * @param title   채용 공고 제목
     * @param company 회사 엔티티 객체
     * @return Optional<JobPosting> 중복된 JobPosting이 있을 경우 반환
     */
    Optional<JobPosting> findByTitleAndCompany(String title, Company company);

    /**
     * 필터링 조건에 따라 채용 공고를 조회하는 메서드입니다.
     * <p>
     * 사용자가 입력한 필터 값 (지역, 경력, 급여, 기술스택)에 따라 동적으로 쿼리를 생성합니다. 파라미터 값이 null일 경우 해당 조건은 무시됩니다.
     * </p>
     *
     * @param location   지역 필터 (nullable)
     * @param experience 경력 필터 (nullable)
     * @param salary     급여 필터 (nullable)
     * @param techStack  기술 스택 필터 (nullable)
     * @param pageable   페이지네이션 및 정렬 정보
     * @return Page<JobPosting> 필터링된 채용 공고 목록을 페이지 형식으로 반환
     */
    @Query("SELECT jp FROM JobPosting jp " +
        "WHERE (:location IS NULL OR jp.location LIKE %:location%) " + // 지역 필터: null이면 무시
        "AND (:experience IS NULL OR jp.experience LIKE %:experience%) " + // 경력 필터: null이면 무시
        "AND (:salary IS NULL OR jp.salary LIKE %:salary%) " + // 급여 필터: null이면 무시
        "AND (:techStack IS NULL OR jp.description LIKE %:techStack%)")
    // 기술스택 필터: null이면 무시
    Page<JobPosting> findByFilters(
        @Param("location") String location,       // 지역 필터 파라미터
        @Param("experience") String experience,   // 경력 필터 파라미터
        @Param("salary") String salary,           // 급여 필터 파라미터
        @Param("techStack") String techStack,     // 기술스택 필터 파라미터
        Pageable pageable);                       // 페이지네이션 정보
}