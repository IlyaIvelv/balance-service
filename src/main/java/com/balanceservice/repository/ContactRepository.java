package com.balanceservice.repository;

import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.UserDto;

public interface ContactRepository {
    boolean existsEmailForOtherUser(String email, Long currentUserId);
    boolean existsPhoneForOtherUser(String phone, Long currentUserId);
    boolean hasEmail(Long userId, String email);
    UserDto updateContacts(Long userId, ContactUpdateDto dto);
}