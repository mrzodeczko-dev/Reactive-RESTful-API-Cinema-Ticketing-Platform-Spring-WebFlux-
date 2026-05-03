package com.app.domain.security;

import com.app.domain.generic.GenericEntity;
import com.app.domain.security.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public abstract sealed class BaseUser implements GenericEntity permits Admin, User {

    @Id
    private String id;

    private String username;
    private String password;
    private Role role;

    public BaseUser(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}

