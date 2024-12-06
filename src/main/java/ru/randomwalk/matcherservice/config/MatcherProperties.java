package ru.randomwalk.matcherservice.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "matcher")
public class MatcherProperties {
    private Integer minWalkTimeInSeconds;
    private Integer offsetBetweenWalksInSeconds;
}
