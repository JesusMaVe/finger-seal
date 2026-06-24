package com.dataforge.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        // ponytail: SimpleMeterRegistry works in all environments.
        // In production, Spring Boot's auto-configured CompositeMeterRegistry
        // takes over via @Primary. This bean ensures tests have a MeterRegistry
        // even when the test framework disables observability auto-configuration.
        return new SimpleMeterRegistry();
    }
}
