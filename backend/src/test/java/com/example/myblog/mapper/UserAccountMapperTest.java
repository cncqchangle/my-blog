package com.example.myblog.mapper;

import com.example.myblog.domain.UserAccount;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserAccountMapperTest {

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Test
    void insertCreatesNewAccount() {
        UserAccount user = new UserAccount();
        user.setAccount("charlie");
        user.setPasswordHash("salt:hash");
        var now = LocalDateTime.of(2026, 4, 25, 10, 30);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        int affectedRows = userAccountMapper.insert(user);

        assertThat(affectedRows).isEqualTo(1);
        assertThat(user.getId()).isNotNull();
        var persisted = userAccountMapper.findByAccount("charlie").orElseThrow();
        assertThat(persisted.getId()).isEqualTo(user.getId());
        assertThat(persisted.getPasswordHash()).isEqualTo("salt:hash");
    }

    @Test
    void findsAccountCaseInsensitivelyForPasswordLookup() {
        UserAccount user = userAccountMapper.findByAccount("ALICE").orElseThrow();
        assertThat(user.getAccount()).isEqualTo("alice");
        assertThat(user.getPasswordHash()).contains(":");
    }

    @Test
    void searchRespectsExclusionAndReturnsStableMatches() {
        var results = userAccountMapper.searchByAccount("al", "alice", 10);
        assertThat(results).extracting("account").containsExactly("ally-reader");
    }
}
