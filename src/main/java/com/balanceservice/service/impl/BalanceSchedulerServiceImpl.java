package com.balanceservice.service.impl;

import com.balanceservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceSchedulerServiceImpl {
    private final AccountRepository accountRepository;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void applyInterest() {
        accountRepository.applyInterestToAll(BigDecimal.valueOf(0.10), BigDecimal.valueOf(2.07));
    }
}