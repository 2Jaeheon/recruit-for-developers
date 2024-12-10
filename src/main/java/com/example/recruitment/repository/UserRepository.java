package com.example.recruitment.repository;

import com.example.recruitment.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);  // 이메일로 사용자 조회

    boolean existsByEmail(String email);       // 이메일 중복 여부 확인
}