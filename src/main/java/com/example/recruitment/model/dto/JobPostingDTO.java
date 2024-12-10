package com.example.recruitment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobPostingDTO {

    private Long id;
    private String title;
    private String location;
    private String salary;
    private String experience;
    private String companyName;
    private String description;
}