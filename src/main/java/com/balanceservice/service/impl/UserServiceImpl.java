package com.balanceservice.service.impl;

import com.balanceservice.dto.*;
import com.balanceservice.repository.ContactRepository;
import com.balanceservice.repository.UserRepository;
import com.balanceservice.service.UserService;
import com.balanceservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.balanceservice.dao.generated.Tables.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;
    private final DSLContext dsl;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> search(UserSearchDto dto) {
        return userRepository.search(dto, PageRequest.of(dto.getPage(), dto.getSize()));
    }

    @Override
    @Transactional
    public UserDto updateContacts(ContactUpdateDto dto) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (dto.getEmail() != null && contactRepository.existsEmailForOtherUser(dto.getEmail(), currentUserId)) {
            throw new RuntimeException("Email already in use");
        }
        if (dto.getPhone() != null && contactRepository.existsPhoneForOtherUser(dto.getPhone(), currentUserId)) {
            throw new RuntimeException("Phone already in use");
        }

        return contactRepository.updateContacts(currentUserId, dto);
    }


    @Override
    @Transactional
    public UserDto register(RegisterDto dto) {
        // 1. Проверка уникальности email
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (contactRepository.existsEmailForOtherUser(dto.getEmail(), null)) {
                throw new RuntimeException("Email already in use");
            }
        }

        // 2. Проверка уникальности phone
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            if (contactRepository.existsPhoneForOtherUser(dto.getPhone(), null)) {
                throw new RuntimeException("Phone already in use");
            }
        }

        // 3. Проверка, что хотя бы один контакт указан
        if ((dto.getEmail() == null || dto.getEmail().isBlank()) &&
                (dto.getPhone() == null || dto.getPhone().isBlank())) {
            throw new RuntimeException("At least one contact (email or phone) must be provided");
        }

        // 4. Создаём пользователя
        var userRecord = dsl.newRecord(USER);
        userRecord.setName(dto.getName());
        userRecord.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRecord.store(); // Сохраняем (возвращает void!)

        Long userId = userRecord.getId(); // Получаем ID ОТДЕЛЬНО после store()

        // 5. Добавляем email (если есть)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            var emailRecord = dsl.newRecord(EMAIL_DATA);
            emailRecord.setUserId(userId);
            emailRecord.setEmail(dto.getEmail());
            emailRecord.store();
        }

        // 6. Добавляем phone (если есть)
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            var phoneRecord = dsl.newRecord(PHONE_DATA);
            phoneRecord.setUserId(userId);
            phoneRecord.setPhone(dto.getPhone());
            phoneRecord.store();
        }

        // 7. Создаём аккаунт с нулевым балансом
        var accountRecord = dsl.newRecord(ACCOUNT);
        accountRecord.setUserId(userId);
        accountRecord.setBalance(BigDecimal.valueOf(1000));
        accountRecord.setInitialBalance(BigDecimal.valueOf(1000));
        accountRecord.store();

        // 8. Возвращаем DTO
        return getUserById(userId);
    }

    // Вспомогательный метод для получения пользователя по ID
    private UserDto getUserById(Long userId) {
        var userRecord = dsl.selectFrom(USER)
                .where(USER.ID.eq(userId))
                .fetchOne();

        if (userRecord == null) {
            throw new RuntimeException("User not found");
        }

        var accountRecord = dsl.selectFrom(ACCOUNT)
                .where(ACCOUNT.USER_ID.eq(userId))
                .fetchOne();

        List<String> emails = dsl.select(EMAIL_DATA.EMAIL)
                .from(EMAIL_DATA)
                .where(EMAIL_DATA.USER_ID.eq(userId))
                .fetch(EMAIL_DATA.EMAIL);

        List<String> phones = dsl.select(PHONE_DATA.PHONE)
                .from(PHONE_DATA)
                .where(PHONE_DATA.USER_ID.eq(userId))
                .fetch(PHONE_DATA.PHONE);

        AccountDto accountDto = null;
        if (accountRecord != null) {
            accountDto = new AccountDto(accountRecord.getId(), accountRecord.getBalance());
        }

        return new UserDto(
                userRecord.getId(),
                userRecord.getName(),
                userRecord.getDateOfBirth(),
                accountDto,
                emails,
                phones
        );
    }
}