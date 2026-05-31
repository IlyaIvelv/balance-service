package com.balanceservice.service;

import com.balanceservice.dto.TransferDto;
import com.balanceservice.service.impl.TransferServiceImpl;
import com.balanceservice.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {


    @InjectMocks
    private TransferServiceImpl transferService;

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