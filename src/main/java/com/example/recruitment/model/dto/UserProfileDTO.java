package com.example.recruitment.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {

    private Long id;
    private String email;
    private String name;
    private String role;
    private List<UserSkillDTO> skills; // 사용자 스킬 리스트

    public UserProfileDTO(Long id, String email, String name, String role,
        List<UserSkillDTO> skills) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.skills = skills;
    }
}