package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Company;
import com.example.recruitment.model.entity.JobPosting;
import java.util.List;
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
     * 필터링 및 검색 조건에 맞는 채용 공고를 조회합니다.
     *
     * @param location    지역 필터
     * @param experience  경력 필터
     * @param salary      급여 필터
     * @param techStack   기술 스택 필터
     * @param keyword     키워드 검색
     * @param companyName 회사명 검색
     * @param position    포지션 검색
     * @param pageable    페이지네이션 및 정렬 정보
     * @return 필터링 및 검색된 채용 공고 목록
     */
    @Query("SELECT jp FROM JobPosting jp " +
        "WHERE (:location IS NULL OR jp.location LIKE %:location%) " +
        "AND (:experience IS NULL OR jp.experience LIKE %:experience%) " +
        "AND (:salary IS NULL OR jp.salary LIKE %:salary%) " +
        "AND (:techStack IS NULL OR jp.description LIKE %:techStack%) " +
        "AND (:keyword IS NULL OR jp.title LIKE %:keyword% OR jp.description LIKE %:keyword%) " +
        "AND (:companyName IS NULL OR jp.company.name LIKE %:companyName%) " +
        "AND (:position IS NULL OR jp.title LIKE %:position%)")
    Page<JobPosting> findByFiltersAndSearch(
        @Param("location") String location,
        @Param("experience") String experience,
        @Param("salary") String salary,
        @Param("techStack") String techStack,
        @Param("keyword") String keyword,
        @Param("companyName") String companyName,
        @Param("position") String position,
        Pageable pageable);

    /**
     * 같은 회사 또는 비슷한 기술스택을 가진 공고를 찾는 쿼리
     *
     * @param companyName 회사명
     * @param description 기술스택(설명)
     * @param excludeId   현재 공고 ID (자기 자신 제외)
     * @return 관련 공고 목록
     */
    @Query("SELECT jp FROM JobPosting jp " +
        "WHERE jp.id != :excludeId AND (jp.company.name = :companyName " +
        "OR (:keywords IS NOT NULL AND :keywords != '' AND jp.description LIKE %:keywords%))")
    List<JobPosting> findRelatedJobPostingsByKeywords(
        @Param("companyName") String companyName,
        @Param("keywords") String keywords,
        @Param("excludeId") Long excludeId);
}