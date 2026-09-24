package com.royalpearl.hotel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Emails allowed to self-promote to admin via POST /api/v1/me/claim-admin.
 * Loaded from environment: APP_ADMIN_ALLOWLIST=email1@x.com,email2@x.com
 */
@Component
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminAllowlistProperties {

    /**
     * Comma-separated list of allowed admin emails.
     * Bound from app.admin.allowlist in application.yml or
     * APP_ADMIN_ALLOWLIST environment variable.
     */
    private List<String> allowlist = List.of();
}
