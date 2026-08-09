package com.dhruvil.auth_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${kafka.topic.user-random-topic}")
    private String KAFKA_RANDOM_USER_TOPIC ;

    @Value("${kafka.topic.approval-group-topic}")
    private String KAFKA_APPROVAL_GROUP_TOPIC ;

    @Bean
    public NewTopic userRandomTopic () {
        return new NewTopic(KAFKA_RANDOM_USER_TOPIC , 3 , (short) 1) ;
    }

    @Bean
    public NewTopic approvalGroupTopic () {
        return new NewTopic(KAFKA_APPROVAL_GROUP_TOPIC , 3 , (short) 1) ;
    }
}
