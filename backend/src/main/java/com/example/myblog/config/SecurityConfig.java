package com.example.myblog.config;

import com.example.myblog.domain.ApiError;
import com.example.myblog.security.Pbkdf2SaltedPasswordEncoder;
import com.example.myblog.security.SessionCookieRefreshFilter;
import com.example.myblog.security.SessionUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.session.autoconfigure.DefaultCookieSerializerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            SessionCookieRefreshFilter sessionCookieRefreshFilter) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html", "/pages/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(sessionCookieRefreshFilter, AuthorizationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((_, response, _) ->
                                writeJsonError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required"))
                        .accessDeniedHandler((_, response, _) ->
                                writeJsonError(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to perform this action")));
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(SessionUserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new Pbkdf2SaltedPasswordEncoder();
    }

    @Bean
    DefaultCookieSerializerCustomizer defaultCookieSerializerCustomizer(
            @Value("${APP_SESSION_TIMEOUT:7d}") Duration sessionTimeout) {
        int maxAgeSeconds = Math.toIntExact(sessionTimeout.getSeconds());
        return serializer -> {
            serializer.setCookieName("JSESSIONID");
            serializer.setCookieMaxAge(maxAgeSeconds);
        };
    }

    private static void writeJsonError(HttpServletResponse response,
                                       HttpStatus status,
                                       String code,
                                       String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = new ApiError(code, message);
        String json = "{\"code\":\"" + escape(error.code()) + "\",\"message\":\"" + escape(error.message()) + "\"}";
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
