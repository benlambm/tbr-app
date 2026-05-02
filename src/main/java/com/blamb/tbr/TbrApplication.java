package com.blamb.tbr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * @SpringBootApplication is a convenience annotation that bundles three things:
 *   - @Configuration       — this class can define Spring beans
 *   - @EnableAutoConfiguration — Spring scans the classpath and auto-wires
 *                            beans for everything it finds (Tomcat, JPA,
 *                            Thymeleaf, etc.)
 *   - @ComponentScan       — find @Controller / @Service / @Repository
 *                            classes in this package and subpackages
 *
 * SpringApplication.run() boots the embedded Tomcat server and starts the app.
 */
@SpringBootApplication
public class TbrApplication {

    public static void main(String[] args) {
        SpringApplication.run(TbrApplication.class, args);
    }
}
