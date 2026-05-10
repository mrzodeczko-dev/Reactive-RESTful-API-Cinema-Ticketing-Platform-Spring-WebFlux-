package com.rzodeczko.infrastructure.persistence.initscripts;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Yaml mapping:
 * <pre>
 * app:
 *   admin:
 *     username: ${ADMIN_USERNAME:admin}
 *     password: ${ADMIN_PASSWORD:admin1234}
 * </pre>
 */
@ConfigurationProperties(prefix = "app.admin")
public record AppAdminCredentials(String username, String password) {
}