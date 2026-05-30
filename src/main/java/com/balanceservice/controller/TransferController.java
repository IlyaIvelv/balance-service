package com.balanceservice.controller;

import com.balanceservice.dto.TransferDto;
import com.balanceservice.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Переводы денег между пользователями")
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    @Operation(
            summary = "Перевести деньги",
            description = "Перевод средств с аккаунта текущего пользователя на аккаунт другого пользователя. " +
                    "fromUserId берётся из JWT токена. " +
                    "Проверяется: достаточно средств, не перевод самому себе, не превышен лимит 207% от начального депозита."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешен",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "403", description = "Недостаточно средств или превышен лимит"),
            @ApiResponse(responseCode = "409", description = "Конфликт (перевод самому себе)")
    })
    public ResponseEntity<Map<String, Long>> transfer(
            @Parameter(description = "Данные перевода: ID получателя и сумма", required = true)
            @Valid @RequestBody TransferDto transferDto) {
        Long transactionId = transferService.transfer(transferDto);
        return ResponseEntity.ok(Map.of("transactionId", transactionId));
    }
}