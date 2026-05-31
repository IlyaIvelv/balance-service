package com.balanceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal initialBalance;
}