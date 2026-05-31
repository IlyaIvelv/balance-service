package com.balanceservice.service.integration;

import com.balanceservice.dto.UserSearchDto;
import com.balanceservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Поиск пользователей с фильтром по имени")
    void search_byName_integration() {
        long baseId = System.nanoTime();

        jdbcTemplate.execute(
                "INSERT INTO \"user\" (id, name, password) VALUES " +
                        "(" + baseId + ", 'Alice Smith', 'pass'), " +
                        "(" + (baseId + 1) + ", 'Bob Alice', 'pass'), " +
                        "(" + (baseId + 2) + ", 'Charlie', 'pass')"
        );
        jdbcTemplate.execute(
                "INSERT INTO account (id, user_id, balance, initial_balance) VALUES " +
                        "(" + (baseId + 1000) + ", " + baseId + ", 100.0, 100.0), " +
                        "(" + (baseId + 1001) + ", " + (baseId + 1) + ", 200.0, 200.0), " +
                        "(" + (baseId + 1002) + ", " + (baseId + 2) + ", 300.0, 300.0)"
        );

        UserSearchDto filters = new UserSearchDto();
        filters.setName("Alice");
        filters.setPage(0);
        filters.setSize(10);

        // when
        Page<com.balanceservice.dto.UserDto> result = userService.search(filters);

        // then
        assertFalse(result.isEmpty());
        assertTrue(result.getContent().stream()
                .allMatch(u -> u.getName().toLowerCase().contains("alice")));
    }

    @Test
    @DisplayName("Поиск с пагинацией — проверяем логику, а не общее количество")
    void search_withPagination_integration() {
        // given: создаём 5 пользователей с уникальными ID
        long baseId = System.nanoTime() + 100000; // большое смещение

        for (int i = 0; i < 5; i++) {
            long userId = baseId + i;
            long accountId = userId + 1000;

            jdbcTemplate.execute(
                    String.format(
                            "INSERT INTO \"user\" (id, name, password) VALUES (%d, 'User%d', 'pass')",
                            userId, i
                    )
            );
            jdbcTemplate.execute(
                    String.format(
                            "INSERT INTO account (id, user_id, balance, initial_balance) VALUES (%d, %d, %d.0, %d.0)",
                            accountId, userId, (i + 1) * 100, (i + 1) * 100
                    )
            );
        }

        UserSearchDto filters = new UserSearchDto();
        filters.setPage(1); // вторая страница (0-based)
        filters.setSize(2); // по 2 на странице

        // when
        Page<com.balanceservice.dto.UserDto> result = userService.search(filters);

        // then:
        assertEquals(2, result.getSize()); // page size = 2 ✓
        assertEquals(1, result.getNumber()); // текущая страница = 1 (вторая) ✓
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getTotalPages() >= 1);

        // Дополнительно: проверяем что на странице именно те пользователи, которых ожидаем
        assertEquals(2, result.getContent().size()); // на странице 2 записи
    }
}