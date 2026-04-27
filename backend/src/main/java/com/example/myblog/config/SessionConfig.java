package com.example.myblog.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    @Bean
    SessionRepositoryCustomizer<JdbcIndexedSessionRepository> sessionRepositoryCustomizer(
            @Value("${APP_SESSION_TIMEOUT:7d}") Duration sessionTimeout) {
        return repository -> repository.setDefaultMaxInactiveInterval(sessionTimeout);
    }
}
