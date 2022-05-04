package com.kds.ourmemory.v1.config;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class WebSecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("단방향 암호화 테스트")
    void passwordEncoder() {
        // given
        var rawPassword = "12345678";

        // when
        var encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertAll(
                () -> assertNotEquals(encodedPassword, rawPassword),
                () -> assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
        );
    }

}
