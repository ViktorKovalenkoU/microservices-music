package org.example.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.kafka.ResourceUploadedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResourceUploadedEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, ResourceUploadedEvent> consumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, ResourceUploadedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setCommonErrorHandler(
                new DefaultErrorHandler(
                        new FixedBackOff(2000L, 3)
                )
        );

        return factory;
    }
}
