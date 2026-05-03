package com.ecommerce.api_geek_store.domain.repository;


import com.ecommerce.api_geek_store.domain.model.PasswordResetToken;
import com.ecommerce.api_geek_store.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUser(User user);
    Optional<PasswordResetToken> findByCode(String code);
}


