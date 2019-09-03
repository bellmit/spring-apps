package com.haozhuo.datag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.datag.model.RcmdMsg;
import com.haozhuo.datag.model.PrefUpdateMsg;
import com.haozhuo.datag.model.bisys.UserBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 8/14/18.
 */
@SuppressWarnings("unchecked")
@Component
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaTemplate kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("WeakerAccess")
    @Autowired
    public KafkaService(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${app.kafka.topic.news-rcmd-request: news-rcmd-request}")
    private String newsRcmdRequestTopic;

    @Value("${app.kafka.topic.prefs-update: prefs-update}")
    private String prefUpdateTopic;

    @Value("${app.kafka.topic.userBehaviorTopic: dev-syn-table-userBehavior}")
    private String devSynRableUserBehavior;

    public void sendRcmdRequestMsg(RcmdMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(newsRcmdRequestTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}", msg, e);
        }
    }

    public void sendPrefUpdateMsg(PrefUpdateMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(prefUpdateTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}", msg, e);
        }
    }
    public void sendUserBehaviorMessage(UserBehavior msg) {
        try {
            //System.out.println("topic："+devSynRableUserBehavior);
            kafkaTemplate.send(devSynRableUserBehavior, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}", msg, e);
        }
    }


}
