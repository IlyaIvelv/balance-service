package com.balanceservice.controller;

import com.balanceservice.dto.TransferDto;
import com.balanceservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> transfer(@Valid @RequestBody TransferDto transferDto) {
        // fromUserId сервис возьмёт из SecurityContext
        Long txId = transferService.transfer(transferDto);
        return ResponseEntity.ok(Map.of("transactionId", txId));
    }
}