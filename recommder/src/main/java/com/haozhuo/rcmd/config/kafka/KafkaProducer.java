package com.haozhuo.rcmd.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haozhuo.rcmd.model.RcmdInfoMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by Lucius on 8/14/18.
 */
@Service
public class KafkaProducer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    /**
     * 下面这种通过构造器注入的方式是官网的写法。我其他地方都写成了：
     *      @Autowired
     *      private KafkaTemplate kafkaTemplate;
     * 的形式。就可以省略构造器了，但不是final了。
     * 请参考：https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-kafka-sending-a-message
     */
    private final KafkaTemplate kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public KafkaProducer(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${app.rcmd.info.kafka-topic}")
    private String rcmdInfoTopic;

    public void sendRcmdInfoMsg(RcmdInfoMsg msg) {
        try {
            logger.debug("send msg:{}", msg);
            kafkaTemplate.send(rcmdInfoTopic, objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            logger.error("发送Kafka消息{}失败:{}",msg,e);
        }

    }
}
