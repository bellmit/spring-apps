package com.haozhuo.datag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.datag.model.NewsRcmdMsg;
import com.haozhuo.datag.model.PrefUpdateMsg;
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

    @Value("${app.kafka.topic.news-rcmd-request: news-rcmd-request}")
    private String newsRcmdRequestTopic;


    @Value("${app.kafka.topic.prefs-update: prefs-update}")
    private String prefUpdateTopic;

    public void sendRcmdRequestMsg(NewsRcmdMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(newsRcmdRequestTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}",msg,e);
        }
    }

    public void sendPrefUpdateMsg(PrefUpdateMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(prefUpdateTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}",msg,e);
        }
    }


}
