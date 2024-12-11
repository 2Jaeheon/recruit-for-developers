package com.example.recruitment.repository;

import com.example.recruitment.model.entity.Application;
import com.example.recruitment.model.entity.Application.Status;
import com.example.recruitment.model.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {


    List<Application> findByUser_Email(String email);// 사용자 이메일로 지원 내역 조회

    Optional<Application> findByUserAndJobPosting_Id(User user, Long jobId); // 중복 지원 체크

    // 상태 필터링과 정렬 지원
    @Query("SELECT a FROM Application a WHERE a.user = :user AND a.status = :status " +
        "ORDER BY CASE WHEN :direction = 'asc' THEN a.createdAt END ASC, " +
        "CASE WHEN :direction = 'desc' THEN a.createdAt END DESC")
    List<Application> findByUserAndStatusOrderBy(User user, Status status, String sortBy,
        String direction);

    // 전체 지원 내역 정렬
    @Query("SELECT a FROM Application a WHERE a.user = :user " +
        "ORDER BY CASE WHEN :direction = 'asc' THEN a.createdAt END ASC, " +
        "CASE WHEN :direction = 'desc' THEN a.createdAt END DESC")
    List<Application> findByUserOrderBy(User user, String sortBy, String direction);
}