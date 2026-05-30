package com.balanceservice.repository.impl;

import com.balanceservice.dto.AccountDto;
import com.balanceservice.dto.UserDto;
import com.balanceservice.dto.UserSearchDto;
import com.balanceservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.balanceservice.dao.generated.Tables.*;
import static org.jooq.impl.DSL.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final DSLContext dsl;

    @Override
    public Long findIdByCredentials(String email, String phone) {
        // Ищем userId по email или phone в соответствующих таблицах
        if (email != null) {
            Long id = dsl.select(EMAIL_DATA.USER_ID)
                    .from(EMAIL_DATA)
                    .where(EMAIL_DATA.EMAIL.eq(email))
                    .fetchOne(EMAIL_DATA.USER_ID);
            if (id != null) return id;
        }
        if (phone != null) {
            return dsl.select(PHONE_DATA.USER_ID)
                    .from(PHONE_DATA)
                    .where(PHONE_DATA.PHONE.eq(phone))
                    .fetchOne(PHONE_DATA.USER_ID);
        }
        return null;
    }

    @Override
    public String getPasswordById(Long userId) {
        return dsl.select(USER.PASSWORD)
                .from(USER)
                .where(USER.ID.eq(userId))
                .fetchOne(USER.PASSWORD);
    }

    @Override
    public Page<UserDto> search(UserSearchDto filters, Pageable pageable) {
        var q = dsl.select(USER.ID, USER.NAME, USER.DATE_OF_BIRTH, ACCOUNT.BALANCE, ACCOUNT.ID)
                .from(USER)
                .leftJoin(ACCOUNT).on(USER.ID.eq(ACCOUNT.USER_ID))
                .where(trueCondition());

        if (filters.getDateOfBirth() != null)
            q = q.and(USER.DATE_OF_BIRTH.gt(filters.getDateOfBirth()));
        if (filters.getName() != null)
            q = q.and(USER.NAME.likeIgnoreCase(filters.getName() + "%"));

        // Фильтры по email/phone через EXISTS в отдельных таблицах
        if (filters.getEmail() != null)
            q = q.andExists(selectOne()
                    .from(EMAIL_DATA)
                    .where(EMAIL_DATA.USER_ID.eq(USER.ID)
                            .and(EMAIL_DATA.EMAIL.eq(filters.getEmail()))));
        if (filters.getPhone() != null)
            q = q.andExists(selectOne()
                    .from(PHONE_DATA)
                    .where(PHONE_DATA.USER_ID.eq(USER.ID)
                            .and(PHONE_DATA.PHONE.eq(filters.getPhone()))));

        long total = dsl.fetchCount(q);

        List<UserDto> users = q.orderBy(USER.ID)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch()
                .map(r -> new UserDto(
                        r.get(USER.ID),
                        r.get(USER.NAME),
                        r.get(USER.DATE_OF_BIRTH),
                        new AccountDto(r.get(ACCOUNT.ID), r.get(ACCOUNT.BALANCE)),
                        getEmails(r.get(USER.ID)),
                        getPhones(r.get(USER.ID))
                ));

        return new PageImpl<>(users, pageable, total);
    }

    private List<String> getEmails(Long userId) {
        return dsl.select(EMAIL_DATA.EMAIL)
                .from(EMAIL_DATA)
                .where(EMAIL_DATA.USER_ID.eq(userId))
                .fetch(EMAIL_DATA.EMAIL);
    }

    private List<String> getPhones(Long userId) {
        return dsl.select(PHONE_DATA.PHONE)
                .from(PHONE_DATA)
                .where(PHONE_DATA.USER_ID.eq(userId))
                .fetch(PHONE_DATA.PHONE);
    }
}