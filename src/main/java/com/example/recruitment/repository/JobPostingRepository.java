package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Company;
import com.example.recruitment.model.entity.JobPosting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    /**
     * 회사와 제목을 기준으로 중복된 채용 공고를 찾는 메서드
     *
     * @param title   채용 공고 제목
     * @param company 회사 엔티티
     * @return 중복된 JobPosting이 있으면 반환
     */
    Optional<JobPosting> findByTitleAndCompany(String title, Company company);
}