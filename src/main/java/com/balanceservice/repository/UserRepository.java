package com.balanceservice.repository;

import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    Long findIdByCredentials(String email, String phone);
    String getPasswordById(Long userId);
    Page<UserDto> search(UserSearchDto filters, Pageable pageable);
}