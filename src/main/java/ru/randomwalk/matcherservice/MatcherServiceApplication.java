package ru.randomwalk.matcherservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
@OpenAPIDefinition(
        servers = {
                @Server(url = "/matcher", description = "Matcher service url")
        }
)
public class MatcherServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatcherServiceApplication.class, args);
    }

}
