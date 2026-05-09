package com.rzodeczko.infrastructure.security.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class SecretKeyConfig {

    @Bean
    public SecretKey secretKey(@Value("${jwt.secret-key}") String base64) {
        byte[] decoded = Decoders.BASE64.decode(base64);
        return Keys.hmacShaKeyFor(decoded);
    }
}
