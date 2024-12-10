package com.example.recruitment.model.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}