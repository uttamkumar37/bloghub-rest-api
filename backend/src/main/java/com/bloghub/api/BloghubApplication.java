package com.bloghub.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the BlogHub REST API application.
 * Bootstraps all Spring Boot auto-configuration and component scanning.
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "BlogHub REST API",
        version = "1.0.0",
        description = "Production-ready Blog Application REST API with JWT authentication, " +
                      "role-based access control, posts, comments, and likes management.",
        contact = @Contact(name = "BlogHub Team", email = "support@bloghub.com"),
        license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
    )
)
public class BloghubApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloghubApplication.class, args);
    }
}
