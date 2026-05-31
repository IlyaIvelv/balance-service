package com.balanceservice.repository;

import com.balanceservice.dto.AccountInfoDto;

import java.math.BigDecimal;

public interface AccountRepository {
    AccountInfoDto lockAndFind(Long userId);

    void updateBalances(Long fromId, Long toId, BigDecimal fromDelta, BigDecimal toDelta);

    void applyInterestToAll(BigDecimal percent, BigDecimal capMultiplier);
}