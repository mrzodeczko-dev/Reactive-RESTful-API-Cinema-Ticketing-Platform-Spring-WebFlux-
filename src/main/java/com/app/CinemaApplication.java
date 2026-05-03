package com.app;

import io.jsonwebtoken.Jwts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.crypto.SecretKey;

@SpringBootApplication
@EnableAspectJAutoProxy
public class CinemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaApplication.class, args);
    }

    @Bean
    public SecretKey secretKey() {
        return Jwts.SIG.HS512.key().build();
    }

    @Bean
    public JavaMailSender mailSender() {
        return new JavaMailSenderImpl();
    }
}
