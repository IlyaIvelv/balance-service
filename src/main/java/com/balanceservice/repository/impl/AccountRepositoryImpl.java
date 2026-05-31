package com.balanceservice.repository.impl;

import com.balanceservice.dto.AccountInfoDto;
import com.balanceservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.balanceservice.dao.generated.Tables.ACCOUNT;
import static org.jooq.impl.DSL.least;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("resource")
public class AccountRepositoryImpl implements AccountRepository {
    private final DSLContext dsl;

    @Override
    @Transactional
    public AccountInfoDto lockAndFind(Long userId) {
        // Используем selectFrom вместо select(...) — это вернёт AccountRecord с геттерами
        var record = dsl.selectFrom(ACCOUNT)
                .where(ACCOUNT.USER_ID.eq(userId))
                .forUpdate()
                .fetchOne();

        return record != null
                ? new AccountInfoDto(
                record.getId(),
                record.getUserId(),
                record.getBalance(),
                record.getInitialBalance()
        )
                : null;
    }

    @Override
    public void updateBalances(Long fromId, Long toId, BigDecimal fromDelta, BigDecimal toDelta) {
        dsl.update(ACCOUNT).set(ACCOUNT.BALANCE, ACCOUNT.BALANCE.add(fromDelta)).where(ACCOUNT.USER_ID.eq(fromId)).execute();
        dsl.update(ACCOUNT).set(ACCOUNT.BALANCE, ACCOUNT.BALANCE.add(toDelta)).where(ACCOUNT.USER_ID.eq(toId)).execute();
    }

    @Override
    public void applyInterestToAll(BigDecimal percent, BigDecimal capMultiplier) {
        dsl.update(ACCOUNT)
                .set(ACCOUNT.BALANCE,
                        least(
                                ACCOUNT.BALANCE.mul(BigDecimal.ONE.add(percent)),
                                ACCOUNT.INITIAL_BALANCE.mul(capMultiplier)
                        )
                )
                .where(ACCOUNT.BALANCE.gt(BigDecimal.ZERO))
                .execute();
    }
}