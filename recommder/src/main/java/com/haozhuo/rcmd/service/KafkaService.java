package com.haozhuo.rcmd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.model.RcmdRequestMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 8/14/18.
 */
@Component
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaTemplate kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public KafkaService(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${app.kafka.topic.article-rcmd-request}")
    private String articleRcmdRequestTopic;

    public void sendRcmdRequestMsg(RcmdRequestMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(articleRcmdRequestTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}",msg,e);
        }

    }
}
