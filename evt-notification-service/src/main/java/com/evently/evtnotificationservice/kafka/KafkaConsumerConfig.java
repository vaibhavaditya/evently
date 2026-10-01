package com.evently.evtnotificationservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    // =========================
    // KAFKA CONSUMER
    // =========================

    @Bean
    public ConsumerFactory<String, EventMessage> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "notification-service-group"
        );

        config.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        config.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        config.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JacksonJsonDeserializer.class
        );

        config.put(
                JacksonJsonDeserializer.TRUSTED_PACKAGES,
                "com.evently.evtnotificationservice.kafka"
        );

        config.put(
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE,
                EventMessage.class
        );

        config.put(
                JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS,
                false
        );

        return new DefaultKafkaConsumerFactory<>(config);
    }


    // =========================
    // KAFKA PRODUCER
    // Used by DLT
    // =========================

    @Bean
    public ProducerFactory<Object, Object> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(config);
    }


    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(
            ProducerFactory<Object, Object> producerFactory
    ) {

        return new KafkaTemplate<>(producerFactory);
    }


    // =========================
    // DEAD LETTER TOPIC
    // =========================

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {

        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new org.apache.kafka.common.TopicPartition(
                                record.topic() + ".dlt",
                                record.partition()
                        )
        );
    }


    // =========================
    // ERROR HANDLER
    // =========================

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer
    ) {

        /*
         * Original attempt + 2 retries = 3 attempts total.
         */
        FixedBackOff backOff =
                new FixedBackOff(1000L, 2L);

        return new DefaultErrorHandler(
                recoverer,
                backOff
        );
    }


    // =========================
    // LISTENER CONTAINER
    // =========================

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventMessage>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, EventMessage> consumerFactory,
            CommonErrorHandler kafkaErrorHandler
    ) {

        ConcurrentKafkaListenerContainerFactory<String, EventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL
        );

        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}