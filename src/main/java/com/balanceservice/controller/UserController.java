package com.balanceservice.controller;

import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import com.balanceservice.service.UserService;
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
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDto>> search(@Valid @ModelAttribute UserSearchDto searchDto) {
        return ResponseEntity.ok(userService.search(searchDto));
    }

    @PutMapping("/me/contacts")
    public ResponseEntity<UserDto> updateContacts(@Valid @RequestBody ContactUpdateDto contactDto) {
        return ResponseEntity.ok(userService.updateContacts(contactDto));
    }
}