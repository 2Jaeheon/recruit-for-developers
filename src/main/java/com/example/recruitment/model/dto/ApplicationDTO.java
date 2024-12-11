package com.example.recruitment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {

    private Long id;
    private Long userId;         // 사용자 ID만 포함
    private String userEmail;    // 사용자 이메일만 포함
    private Long jobId;          // 채용 공고 ID
    private String jobTitle;     // 채용 공고 제목
    private String status;       // 지원 상태
    private String resumePath;   // 이력서 경로
    private String appliedAt;    // 지원 날짜
}