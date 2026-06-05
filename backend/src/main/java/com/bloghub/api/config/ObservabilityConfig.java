package com.bloghub.api.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> bloghubCommonTags() {
        return registry -> registry.config().commonTags(
                "application", "bloghub-api",
                "service", "backend"
        );
    }
}
