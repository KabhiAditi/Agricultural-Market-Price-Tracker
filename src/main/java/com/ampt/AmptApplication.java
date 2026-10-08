package com.ampt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point for the Agricultural Market Price Tracker (AMPT).
 *
 * Extends SpringBootServletInitializer so the same codebase can run:
 *  - standalone, with "mvn spring-boot:run" (embedded Tomcat) for local dev, or
 *  - as a WAR deployed into an external Tomcat's webapps/ directory (Task 8).
 */
@SpringBootApplication
public class AmptApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(AmptApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(AmptApplication.class, args);
    }
}
