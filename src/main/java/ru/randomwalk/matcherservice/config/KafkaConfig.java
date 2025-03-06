package ru.randomwalk.matcherservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> configProperties = new HashMap<>();
        JsonSerializer jsonSerializer = new JsonSerializer().noTypeInfo();
        configProperties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()
        );
        configProperties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        configProperties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                jsonSerializer
        );
        return new DefaultKafkaProducerFactory<>(configProperties, new StringSerializer(), jsonSerializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(producerFactory(kafkaProperties));
    }

    @Bean
    public KafkaListenerContainerFactory<?> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerConfig(kafkaProperties));
        return factory;
    }

    public ConsumerFactory<String, String> consumerConfig(KafkaProperties kafkaConfig) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getConsumer().getGroupId());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        return new DefaultKafkaConsumerFactory<>(properties);
    }
}
