package com.royalpearl.hotel.config;

import com.royalpearl.hotel.security.JwtService;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.entity.UserRole;
import com.royalpearl.hotel.user.repository.UserRepository;
import com.royalpearl.hotel.user.repository.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * Extension point for Google OAuth2 login.
 *
 * When a user signs in with Google:
 *   1. We upsert the user record (provider = 'google').
 *   2. We assign the default 'user' role.
 *   3. We issue a JWT and redirect to the frontend with the token as a query param.
 *
 * To enable, add the spring.security.oauth2.client.registration.google block
 * to application.yml and set GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET env vars.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2Config {

    private final UserRepository     userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService         jwtService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return new OAuth2SuccessHandler();
    }

    private class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

        @Override
        @Transactional
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication)
                throws IOException {

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email    = oauth2User.getAttribute("email");
            String name     = oauth2User.getAttribute("name");
            String picture  = oauth2User.getAttribute("picture");

            if (email == null) {
                response.sendRedirect(frontendUrl + "/login?error=no_email");
                return;
            }

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .fullName(name)
                        .avatarUrl(picture)
                        .provider("google")
                        .emailVerified(true)
                        .build();
                return userRepository.save(newUser);
            });

            // Ensure default role
            if (!userRoleRepository.existsByUserIdAndRole(user.getId(), AppRole.user)) {
                userRoleRepository.save(UserRole.builder()
                        .user(user)
                        .role(AppRole.user)
                        .build());
            }

            // Re-load roles
            user = userRepository.findById(user.getId()).orElseThrow();
            List<String> roles = user.getRoles().stream()
                    .map(r -> r.getRole().name())
                    .toList();

            String accessToken = jwtService.generateAccessToken(user.getId(), roles);

            // Redirect to frontend with token in query param
            // Frontend should extract and store in memory / sessionStorage
            response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + accessToken);
            log.info("OAuth2 login success for {}", email);
        }
    }
}
