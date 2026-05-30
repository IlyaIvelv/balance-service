package com.balanceservice.service.impl;

import com.balanceservice.dto.AccountInfoDto;
import com.balanceservice.dto.TransferDto;
import com.balanceservice.repository.AccountRepository;
import com.balanceservice.service.TransferService;
import com.balanceservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Long transfer(TransferDto dto) {
        Long fromId = SecurityUtil.getCurrentUserId();
        Long toId = dto.getToUserId();
        BigDecimal amount = dto.getAmount();



        if (fromId.equals(toId)) {
            throw new RuntimeException("Self-transfer denied");
        }

        AccountInfoDto fromAcc = accountRepository.lockAndFind(fromId);
        AccountInfoDto toAcc = accountRepository.lockAndFind(toId);

        if (fromAcc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }


        BigDecimal maxAllowed = toAcc.getInitialBalance().multiply(BigDecimal.valueOf(2.07));
        if (toAcc.getBalance().add(amount).compareTo(maxAllowed) > 0) {
            throw new RuntimeException("Transfer exceeds max balance cap (207%)");
        }

        accountRepository.updateBalances(fromId, toId, amount.negate(), amount);
        return System.nanoTime(); // Заглушка ID транзакции
    }
}