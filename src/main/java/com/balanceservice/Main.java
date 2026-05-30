package com.balanceservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "Balance Service API",
                version = "1.0.0",
                description = "Сервис управления балансом пользователей. " +
                        "Аутентификация через JWT, поиск пользователей, управление контактами, " +
                        "переводы денег, автоматическое начисление +10% каждые 30 сек."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "Введите JWT токен (без префикса 'Bearer')"
        )
})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}