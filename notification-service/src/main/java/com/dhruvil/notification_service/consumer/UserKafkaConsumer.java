package com.dhruvil.notification_service.consumer;

import com.dhruvil.event.ApprovalGroupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserKafkaConsumer {
    @KafkaListener(topics = "user-random-topic")
    public void handleUserRandomTopic1 (String msg ) {
        log.info("message received : {}",msg) ;
    }

    @KafkaListener(topics = "user-random-topic")
    public void handleUserRandomTopic2 (String msg ) {
        log.info("message received : {}",msg) ;
    }
    @KafkaListener(topics = "user-random-topic")
    public void handleUserRandomTopic3  (String msg ) {
        log.info("message received : {}",msg) ;
    }

    @KafkaListener(topics = "approval-group-topic")
    public void handleApprovalGrouptTopic (ApprovalGroupEvent event){
        log.info("handleApprovalGrouptTopic : {}" , event);
    }

}