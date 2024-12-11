package com.example.recruitment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSkillDTO {

    private Long skillId;           // 스킬 ID
    private String skillName;       // 스킬 이름
    private String proficiency;     // 숙련도 (BEGINNER, INTERMEDIATE 등)
    private String acquiredAt;      // 획득 날짜
}