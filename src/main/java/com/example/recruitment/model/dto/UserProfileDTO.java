package com.example.recruitment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDTO {

    private Long id;
    private String email;
    private String name;
    private String role;

    public UserProfileDTO(Long id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}