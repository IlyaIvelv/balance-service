package com.balanceservice.service;

import com.balanceservice.dto.LoginDto;
import com.balanceservice.repository.UserRepository;
import com.balanceservice.security.JwtTokenProvider;
import com.balanceservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Логин по email успешен")
    void login_byEmail_success() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        Long userId = 1L;
        String hashedPassword = "$2a$10$hashed";
        String token = "eyJhbGciOiJIUzI1NiJ9...";

        when(userRepository.findIdByCredentials("test@example.com", null))
                .thenReturn(userId);
        when(userRepository.getPasswordById(userId))
                .thenReturn(hashedPassword);
        when(passwordEncoder.matches("password123", hashedPassword))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(userId))
                .thenReturn(token);

        // when
        String result = authService.login(loginDto);

        // then
        assertEquals(token, result);
    }

    @Test
    @DisplayName("Логин по телефону успешен")
    void login_byPhone_success() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setPhone("79991234567");
        loginDto.setPassword("password123");

        Long userId = 2L;
        String hashedPassword = "$2a$10$hashed";
        String token = "eyJhbGciOiJIUzI1NiJ9...";

        when(userRepository.findIdByCredentials(null, "79991234567"))
                .thenReturn(userId);
        when(userRepository.getPasswordById(userId))
                .thenReturn(hashedPassword);
        when(passwordEncoder.matches("password123", hashedPassword))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(userId))
                .thenReturn(token);

        // when
        String result = authService.login(loginDto);

        // then
        assertEquals(token, result);
    }

    @Test
    @DisplayName("Логин с неверным паролем выбрасывает исключение")
    void login_wrongPassword_throwsException() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongPassword");

        Long userId = 1L;
        String hashedPassword = "$2a$10$hashed";

        when(userRepository.findIdByCredentials("test@example.com", null))
                .thenReturn(userId);
        when(userRepository.getPasswordById(userId))
                .thenReturn(hashedPassword);
        when(passwordEncoder.matches("wrongPassword", hashedPassword))
                .thenReturn(false);

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("Логин с несуществующим пользователем выбрасывает исключение")
    void login_nonExistentUser_throwsException() {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("password123");

        when(userRepository.findIdByCredentials("nonexistent@example.com", null))
                .thenReturn(null);

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginDto));
        assertEquals("Invalid credentials", ex.getMessage());
    }
}