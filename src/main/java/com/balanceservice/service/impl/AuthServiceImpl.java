package com.balanceservice.service.impl;

import com.balanceservice.dto.LoginDto;
import com.balanceservice.repository.UserRepository;
import com.balanceservice.service.AuthService;
import com.balanceservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String login(LoginDto dto) {
        Long userId = userRepository.findIdByCredentials(dto.getEmail(), dto.getPhone());
        if (userId == null || !passwordEncoder.matches(dto.getPassword(), userRepository.getPasswordById(userId))) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtTokenProvider.generateToken(userId);
    }
}