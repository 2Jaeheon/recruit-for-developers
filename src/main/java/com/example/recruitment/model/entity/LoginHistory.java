package com.example.recruitment.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 로그인 이력 ID (Primary Key)

    @ManyToOne(fetch = FetchType.LAZY) // User 테이블과 다대일 관계
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 로그인한 사용자 정보

    @Column(nullable = false)
    private LocalDateTime loginTime; // 로그인한 시간

    @Column(nullable = false)
    private String ipAddress; // 사용자의 IP 주소
}