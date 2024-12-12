package ru.randomwalk.matcherservice.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "matcher")
public class MatcherProperties {
    private Integer minWalkTimeInSeconds;
    private Integer offsetBetweenWalksInSeconds;
}
