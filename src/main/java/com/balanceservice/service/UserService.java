package com.balanceservice.service;

import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.RegisterDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserDto> search(UserSearchDto dto);

    UserDto updateContacts(ContactUpdateDto dto);

    UserDto register(RegisterDto dto);

}
