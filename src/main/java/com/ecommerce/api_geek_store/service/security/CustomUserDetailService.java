package com.ecommerce.api_geek_store.service.security;

import com.ecommerce.api_geek_store.domain.model.Role;
import com.ecommerce.api_geek_store.domain.model.User;
import com.ecommerce.api_geek_store.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
public class CustomUserDetailService implements UserDetailsService {

   private static final Logger log = LoggerFactory.getLogger(CustomUserDetailService.class);

    private final UserRepository userRepository;

   public CustomUserDetailService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.debug("Intentando autenticar usuario: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Intento de login fallido: Usuario no encontrado -> {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado con email: " + email);
                });


        String roleName = user.getRol() != null ? user.getRol().name() : Role.ROLE_USER.name();

        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));


        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}