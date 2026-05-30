package com.balanceservice.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class UserSearchDto {
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^7\\d{10}$", message = "Phone must match format: 79207865432")
    private String phone;

    private String name;

    private String email;

    @Min(value = 0, message = "Page must be >= 0")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be >= 1")
    private Integer size = 20;
}