package com.docplatform.upload.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic documentUploadedTopic() {
        return TopicBuilder.name("document.uploaded")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic documentFailedTopic() {
        return TopicBuilder.name("document.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
