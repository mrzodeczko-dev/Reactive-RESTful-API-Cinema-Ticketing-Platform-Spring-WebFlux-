package com.rzodeczko.infrastructure.security.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationDto {
    private String username;
    @ToString.Exclude
    private String password;
}
