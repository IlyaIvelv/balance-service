package com.balanceservice.service.integration;

import com.balanceservice.dto.TransferDto;
import com.balanceservice.service.TransferService;
import com.balanceservice.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Успешный перевод между пользователями")
    void transfer_success() {
        // given: уникальные ID для каждого запуска
        long senderId = System.nanoTime();
        long recipientId = senderId + 1;

        jdbcTemplate.execute(
                "INSERT INTO \"user\" (id, name, password) VALUES " +
                        "(" + senderId + ", 'Sender', '$2a$10$hashed'), " +
                        "(" + recipientId + ", 'Recipient', '$2a$10$hashed')"
        );
        jdbcTemplate.execute(
                "INSERT INTO account (id, user_id, balance, initial_balance) VALUES " +
                        "(" + (senderId + 1000) + ", " + senderId + ", 1000.0, 1000.0), " +
                        "(" + (recipientId + 1000) + ", " + recipientId + ", 100.0, 100.0)"
        );

        TransferDto request = new TransferDto();
        request.setToUserId(recipientId);
        request.setAmount(BigDecimal.valueOf(50));

        try (var mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(senderId);
            assertDoesNotThrow(() -> transferService.transfer(request));
        }

        // then
        BigDecimal senderBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE user_id = ?", BigDecimal.class, senderId);
        BigDecimal recipientBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM account WHERE user_id = ?", BigDecimal.class, recipientId);

        assertEquals(0, senderBalance.compareTo(BigDecimal.valueOf(950.0)));
        assertEquals(0, recipientBalance.compareTo(BigDecimal.valueOf(150.0)));
    }

    @Test
    @DisplayName("Перевод превышающий лимит 207% отклоняется")
    void transfer_exceeds207Limit_rejected() {
        // given
        long senderId = System.nanoTime() + 10000;
        long recipientId = senderId + 1;

        jdbcTemplate.execute(
                "INSERT INTO \"user\" (id, name, password) VALUES " +
                        "(" + senderId + ", 'Sender', '$2a$10$hashed'), " +
                        "(" + recipientId + ", 'Recipient', '$2a$10$hashed')"
        );
        jdbcTemplate.execute(
                "INSERT INTO account (id, user_id, balance, initial_balance) VALUES " +
                        "(" + (senderId + 1000) + ", " + senderId + ", 1000.0, 1000.0), " +
                        "(" + (recipientId + 1000) + ", " + recipientId + ", 206.0, 100.0)"
        );

        TransferDto request = new TransferDto();
        request.setToUserId(recipientId);
        request.setAmount(BigDecimal.valueOf(10));

        try (var mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(senderId);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> transferService.transfer(request));
            assertEquals("Transfer exceeds max balance cap (207%)", ex.getMessage());
        }
    }
}