package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.*;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    void sendRecoveryCode(String email);
    void resetPassword(PasswordResetRequest request);
    AuthResponse loginWithGoogle(String googleToken);
    void validateRecoveryCode(String email, String code);
    void confirmToken(String token);
}
