package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 회사 이름을 기준으로 회사를 조회
     *
     * @param name 회사 이름
     * @return 중복된 회사가 있으면 반환
     */
    Optional<Company> findByName(String name);
}