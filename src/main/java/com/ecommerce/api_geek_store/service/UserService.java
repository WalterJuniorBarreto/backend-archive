package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> findAll(String keyword, Pageable pageable, UserDetails authUser);
    UserResponse findById(Long id, UserDetails authUser, String motivo);
    UserResponse create(AdminUserRequest request, UserDetails authUser, String motivo);
    UserResponse update(Long id, AdminUserUpdateRequest request, UserDetails authUser, String motivo);
    void toggleStatus(Long id, UserDetails authUser, String motivo);
    void changePassword(ChangePasswordRequest request, UserDetails userDetails);
    UserResponse getUserProfile(String email);
    UserResponse updateProfile(String email, UserProfileUpdateRequest requet);
}