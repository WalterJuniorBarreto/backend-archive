package com.ecommerce.api_geek_store.util;


import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${app.security.jwt.expiration-seconds:86400}")
    private long jwtExpirationSeconds;

    @Value("${app.security.cookie.secure:false}")
    private boolean secureCookie;

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(jwtExpirationSeconds)
                .sameSite("Lax")
                .build();
    }



    public ResponseCookie deleteJwtCookie() {
        return ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

}