package com.ecommerce.api_geek_store.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "password_reset_token",
        indexes = {
                @Index(name = "idx_pwd_token_code", columnList = "code"),
                @Index(name = "idx_pwd_token_user", columnList = "user_id")
        }
)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public PasswordResetToken(String code, User user, int expirationMinutes) {
        this.code = code;
        this.user = user;
        this.expirationTime = LocalDateTime.now().plusMinutes(expirationMinutes);
    }
}
