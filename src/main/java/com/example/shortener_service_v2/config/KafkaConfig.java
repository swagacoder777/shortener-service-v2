package com.example.shortener_service_v2.config;

import com.example.shortener_service_v2.event.LinkClickedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, LinkClickedEvent> producerFactory(
            org.springframework.core.env.Environment environment
    ) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty(
                        "spring.kafka.bootstrap-servers",
                        "localhost:9092"
                )
        );
        properties.put(
                ProducerConfig.ACKS_CONFIG,
                environment.getProperty("spring.kafka.producer.acks", "all")
        );
        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, LinkClickedEvent> kafkaTemplate(
            ProducerFactory<String, LinkClickedEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}