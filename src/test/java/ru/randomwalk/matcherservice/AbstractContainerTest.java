package ru.randomwalk.matcherservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractContainerTest {

    private static final PostgreSQLContainer<?> DATABASE_CONTAINER;
    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        DATABASE_CONTAINER = new PostgreSQLContainer<>(
                DockerImageName.parse("postgis/postgis:16-3.5")
                        .asCompatibleSubstituteFor("postgres")
        )
                .withDatabaseName("random_walk_postgres")
                .withUsername("postgres")
                .withPassword("postgres");
        DATABASE_CONTAINER.start();

        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
        KAFKA_CONTAINER.start();
    }

    @BeforeAll
    public static void setUp() {
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE_CONTAINER::getUsername);
        registry.add("spring.datasource.password", DATABASE_CONTAINER::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }
}