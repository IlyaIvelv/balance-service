package com.balanceservice.repository.impl;


import com.balanceservice.dto.ContactUpdateDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.jooq.impl.DSL.*;
import static com.balanceservice.dao.generated.Tables.*;

@Repository
@RequiredArgsConstructor
public class ContactRepositoryImpl implements ContactRepository {
    private final DSLContext dsl;

    @Override
    public boolean existsEmailForOtherUser(String email, Long currentUserId) {
        return dsl.fetchExists(selectOne().from(EMAIL_DATA).where(EMAIL_DATA.EMAIL.eq(email).and(EMAIL_DATA.USER_ID.ne(currentUserId))));
    }

    @Override
    public boolean existsPhoneForOtherUser(String phone, Long currentUserId) {
        return dsl.fetchExists(selectOne().from(PHONE_DATA).where(PHONE_DATA.PHONE.eq(phone).and(PHONE_DATA.USER_ID.ne(currentUserId))));
    }

    @Override
    public boolean hasEmail(Long userId, String email) {
        return dsl.fetchExists(EMAIL_DATA, EMAIL_DATA.USER_ID.eq(userId).and(EMAIL_DATA.EMAIL.eq(email)));
    }

    @Override
    @Transactional
    public UserDto updateContacts(Long userId, ContactUpdateDto dto) {
        if (dto.getEmail() != null) {
            if (!hasEmail(userId, dto.getEmail())) {
                dsl.insertInto(EMAIL_DATA).columns(EMAIL_DATA.USER_ID, EMAIL_DATA.EMAIL).values(userId, dto.getEmail()).execute();
            }
        }
        if (dto.getPhone() != null) {
            if (!dsl.fetchExists(PHONE_DATA, PHONE_DATA.USER_ID.eq(userId).and(PHONE_DATA.PHONE.eq(dto.getPhone())))) {
                dsl.insertInto(PHONE_DATA).columns(PHONE_DATA.USER_ID, PHONE_DATA.PHONE).values(userId, dto.getPhone()).execute();
            }
        }
        // Упрощённая логика: добавление. Удаление/замена реализуется аналогично через DELETE/UPDATE.
        // Возвращаем обновлённого юзера (заглушка маппинга)
        return new UserDto(userId, "Updated User", null, null, List.of(), List.of());
    }
}