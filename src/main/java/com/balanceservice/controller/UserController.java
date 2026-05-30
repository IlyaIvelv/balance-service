package com.balanceservice.controller;

import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.RegisterDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import com.balanceservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Управление пользователями и контактами")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Поиск пользователей",
            description = "Поиск с фильтрами: dateOfBirth (больше чем), phone (точное совпадение), " +
                    "name (начинается с), email (точное совпадение). Поддерживается пагинация."
    )
    public ResponseEntity<Page<UserDto>> search(
            @Parameter(description = "Параметры поиска и пагинации")
            @Valid @ModelAttribute UserSearchDto searchDto) {
        return ResponseEntity.ok(userService.search(searchDto));
    }

    @PutMapping("/me/contacts")
    @Operation(
            summary = "Обновить контакты текущего пользователя",
            description = "Добавляет email и/или phone текущему пользователю. " +
                    "Минимум один контакт должен быть указан. " +
                    "Email и phone должны быть уникальны."
    )
    public ResponseEntity<UserDto> updateContacts(
            @Parameter(description = "Данные для обновления контактов")
            @Valid @RequestBody ContactUpdateDto contactDto) {
        return ResponseEntity.ok(userService.updateContacts(contactDto));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя с email и/или phone. " +
                    "Минимум один контакт должен быть указан. " +
                    "Пароль будет захэширован."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "409", description = "Email или phone уже заняты")
    })
    public ResponseEntity<UserDto> register(
            @Parameter(description = "Данные для регистрации", required = true)
            @Valid @RequestBody RegisterDto dto) {
        return ResponseEntity.ok(userService.register(dto));
    }
}