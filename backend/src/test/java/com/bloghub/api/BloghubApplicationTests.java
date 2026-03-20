package com.bloghub.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads without errors.
 */
@SpringBootTest
@ActiveProfiles("test")
class BloghubApplicationTests {

    @Test
    void contextLoads() {
        // If the application context loads successfully, this test passes
    }
}
