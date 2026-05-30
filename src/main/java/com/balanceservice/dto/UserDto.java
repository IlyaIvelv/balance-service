package com.balanceservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private AccountDto account;
    private List<String> emails;
    private List<String> phones;
}