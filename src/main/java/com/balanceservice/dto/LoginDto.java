package com.balanceservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LoginDto {
    private String email;

    @Pattern(regexp = "^7\\d{10}$", message = "Phone must match format: 79207865432")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
}