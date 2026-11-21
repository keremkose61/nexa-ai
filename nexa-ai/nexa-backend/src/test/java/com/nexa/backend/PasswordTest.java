package com.nexa.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    @Test
    void testBcrypt() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Admin123!");
        System.out.println("BCRYPT_HASH_ADMIN123=" + hash);
        boolean matches = encoder.matches("Admin123!", hash);
        System.out.println("MATCHES=" + matches);
    }
}
