package com.royalpearl.hotel.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** Base64-encoded (or plain) secret key – min 256 bits for HS256. */
    private String secret;

    /** Access token TTL in seconds (default 15 min). */
    private long accessTokenTtlSeconds = 900;

    /** Refresh token TTL in seconds (default 7 days). */
    private long refreshTokenTtlSeconds = 604_800;
}
