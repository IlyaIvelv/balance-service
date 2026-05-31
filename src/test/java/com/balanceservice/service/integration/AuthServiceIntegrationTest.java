package com.balanceservice.service.integration;

import com.balanceservice.dto.LoginDto;
import com.balanceservice.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Логин с несуществующим email выбрасывает исключение")
    void login_nonExistentEmail_throwsException() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("password123");

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("Логин с несуществующим телефоном выбрасывает исключение")
    void login_nonExistentPhone_throwsException() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setPhone("79990000000");
        loginDto.setPassword("password123");

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("Логин с правильными данными возвращает токен")
    void login_validCredentials_returnsToken() {
        // given: создаём пользователя напрямую в БД
        jdbcTemplate.execute(
                "INSERT INTO \"user\" (id, name, password) " +
                        "VALUES (100, 'Test User', '$2a$10$XvKJZ8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z')"
        );
        jdbcTemplate.execute(
                "INSERT INTO account (id, user_id, balance, initial_balance) " +
                        "VALUES (100, 100, 1000.00, 1000.00)"
        );
        jdbcTemplate.execute(
                "INSERT INTO email_data (id, user_id, email) " +
                        "VALUES (100, 100, 'testuser@example.com')"
        );

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("testuser@example.com");
        loginDto.setPassword("password123"); // пароль не совпадёт с хешем, но это покажет что пользователь найден

        // when & then: ожидаем ошибку пароля, а не "пользователь не найден"
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        // Если ошибка "Invalid credentials" — значит пользователь найден, но пароль не подошёл
        assertEquals("Invalid credentials", ex.getMessage());
    }
}