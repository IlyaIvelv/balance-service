package com.balanceservice.service;

import com.balanceservice.dto.AccountInfoDto;
import com.balanceservice.dto.TransferDto;
import com.balanceservice.repository.AccountRepository;
import com.balanceservice.service.impl.TransferServiceImpl;
import com.balanceservice.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {


    @InjectMocks
    private TransferServiceImpl transferService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Перевод самому себе выбрасывает исключение")
    void transfer_toSelf_throwsException() {
        // given
        TransferDto request = new TransferDto();
        request.setToUserId(1L);
        request.setAmount(BigDecimal.valueOf(100));

        try (var mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            // when & then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> transferService.transfer(request));
            assertEquals("Self-transfer denied", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Перевод превышающий лимит 207% отклоняется")
    void transfer_exceeds207Limit_rejected() {
        // given
        TransferDto request = new TransferDto();
        request.setToUserId(2L);
        request.setAmount(BigDecimal.valueOf(10));

        try (var mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            // Мокаем accountRepository.lockAndFind для sender и recipient
            AccountInfoDto senderAccount = new AccountInfoDto(1L, 1L, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000));
            AccountInfoDto recipientAccount = new AccountInfoDto(2L, 2L, BigDecimal.valueOf(206), BigDecimal.valueOf(100));

            when(accountRepository.lockAndFind(1L)).thenReturn(senderAccount);
            when(accountRepository.lockAndFind(2L)).thenReturn(recipientAccount);

            // when & then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> transferService.transfer(request));
            assertEquals("Transfer exceeds max balance cap (207%)", ex.getMessage());
        }
    }

    @Test
    @DisplayName("TransferDto создаётся корректно")
    void transferDto_creation() {
        // given
        TransferDto dto = new TransferDto();
        dto.setToUserId(2L);
        dto.setAmount(BigDecimal.valueOf(500));

        // then
        assertEquals(2L, dto.getToUserId());
        assertEquals(BigDecimal.valueOf(500), dto.getAmount());
    }
}