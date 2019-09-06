package com.haozhuo.datag.service;

import com.haozhuo.datag.model.bisys.UserBehavior;
import com.haozhuo.datag.model.bisys.UserBehaviorDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
public class UserBehaviorService {
    @Autowired
    private KafkaService kafkaMessageService;
   // @ Autowired
   // private RedisSpringHelper redisSpringHelper;
    public void save(UserBehaviorDTO dto) {

        if (dto.getTest() == null || dto.getTest() == false) {
            UserBehavior userBehavior = new UserBehavior();
            BeanUtils.copyProperties(dto, userBehavior);
            userBehavior.setBhvDatetime(new Date());
            // 取消mongodb存储
//        userBehaviorRepository.save(userBehavior);
            // send to kafka
            kafkaMessageService.sendUserBehaviorMessage(userBehavior);
        }
    }
}

