package com.haozhuo.rcmd.service;

import com.haozhuo.rcmd.model.RcmdInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by Lucius on 8/17/18.
 */
@Component
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    @Autowired
    @Qualifier("redisTemplate0")
    private StringRedisTemplate redisDB0;

    @Autowired
    @Qualifier("redisTemplate1")
    private StringRedisTemplate redisDB1;

    /**
     * 将如
     * a:0:119962,a:0:127555,a:0:117354,a:0:118816,a:0:118330,v:0:59,a:0:119292,a:0:127676,a:0:124924,l:2:61
     * 的形式转换成RcmdResult
     *
     * @param rcmdInfo
     * @return
     */
    private RcmdInfo parseRcmdInfo(String rcmdInfo) {
        RcmdInfo rcmdResult = new RcmdInfo();
        for (String item : rcmdInfo.split(",")) {
            String[] array = item.split(":");
            if (array.length == 3) {
                if ("a".equals(array[0])) {
                    rcmdResult.getArticle().add(array[2]);
                } else if ("v".equals(array[0])) {
                    rcmdResult.getVideo().add(array[2]);
                } else if ("l".equals(array[0])) {
                    rcmdResult.getLive().add(array[2]);
                }
            }
        }
        return rcmdResult;
    }

    public RcmdInfo getRcmdInfo(String userId, int rcmdType) {
        String key = String.format("rcmd-infos:%d:%s", rcmdType, userId);
        String value = redisDB1.opsForList().leftPop(key);
        if (value == null) value = "";
        logger.info("getRcmdInfo key -- {}; rcmdResult -- {}", key, value);
        return parseRcmdInfo(value);
    }
}
