package com.example.recruitment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookmarkDTO {

    private Long bookmarkId;
    private Long jobPostingId;
    private String jobTitle;
    private String createdAt;
}