package com.example.recruitment.repository;

import com.example.recruitment.model.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    // 별도의 메서드가 필요하면 여기에 추가
}