package com.balanceservice.service;

import com.balanceservice.dto.AccountDto;
import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import com.balanceservice.repository.ContactRepository;
import com.balanceservice.repository.UserRepository;
import com.balanceservice.service.impl.UserServiceImpl;
import com.balanceservice.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Поиск без фильтров возвращает пустую страницу")
    void search_noFilters_returnsEmptyPage() {
        // given
        UserSearchDto filters = new UserSearchDto();
        filters.setPage(0);
        filters.setSize(10);

        when(userRepository.search(any(UserSearchDto.class), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // when
        Page<UserDto> result = userService.search(filters);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск по имени возвращает пользователей")
    void search_byName_returnsUsers() {
        // given
        UserSearchDto filters = new UserSearchDto();
        filters.setName("Test");
        filters.setPage(0);
        filters.setSize(10);

        UserDto user = new UserDto(
                1L, "Test User", LocalDate.of(1990, 1, 1),
                new AccountDto(1L, BigDecimal.valueOf(1000.0)),
                Collections.singletonList("test@example.com"),
                Collections.singletonList("79991234567")
        );

        when(userRepository.search(any(UserSearchDto.class), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(user)));

        // when
        Page<UserDto> result = userService.search(filters);

        // then
        assertFalse(result.isEmpty());
        assertEquals("Test User", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("updateContacts с занятым email выбрасывает исключение")
    void updateContacts_emailTaken_throwsException() {
        // given
        ContactUpdateDto dto = new ContactUpdateDto();
        dto.setEmail("taken@example.com");

        try (var mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
            when(contactRepository.existsEmailForOtherUser("taken@example.com", 1L))
                    .thenReturn(true);

            // when & then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.updateContacts(dto));
            assertEquals("Email already in use", ex.getMessage());
        }
    }
}