package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.RcmdInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private String curDate;

    private String[] lastNdays;

    @Value("${app.redis.expire-days:15}")
    private int expireDays;

    private final String videoPushedKey = "video-pushed:%s:%s";
    private final String goodsPushedKey = "goods-pushed:%s:%s";

    private synchronized void updateDateInfo() {
        lastNdays = JavaUtils.getLastNdaysArray(expireDays);
        curDate = JavaUtils.getToday();
        logger.debug("expireDays:{}, curDate:{}, lastNdays:{}", expireDays, curDate, Arrays.asList(lastNdays));
    }

    private void checkOrUpdateDateInfo() {
        if (!JavaUtils.getToday().equals(curDate)) {
            updateDateInfo();
        }
    }

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

    public RcmdInfo getRcmdInfo(String userId, String categoryId) {
        String key = String.format("rcmdInfo:%s", userId);
        HashOperations<String, String , String> op = redisDB1.opsForHash();
        String value = op.get(key, categoryId);
        if (value == null) {
            value = "";
        } else {
            op.delete(key, categoryId);
        }
        logger.info("getRcmdInfo key -- {}; rcmdResult -- {}", key, value);
        return parseRcmdInfo(value);
    }

    private String[] getPushedKeys(String userId, String key) {
        checkOrUpdateDateInfo();
        Set<String> result = new HashSet<>();
        for (String day : lastNdays) {
            result.addAll(redisDB0.opsForSet().members(String.format(key, userId, day)));
        }
        return result.toArray(new String[0]);
    }

    /**
     * 根据userId获取最近给用户推送过的视频列表
     *
     * @return
     */
    public String[] getPushedVideos(String userId) {
        return getPushedKeys(userId, videoPushedKey);
    }

    /**
     * 根据userId获取最近给用户推送过的商品列表
     *
     * @return
     */
    public String[] getPushedGoods(String userId) {
        return getPushedKeys(userId, goodsPushedKey);
    }

    private void setPushedKey(String userId, String key, String[] pushedIds) {
        if (pushedIds.length == 0) return;
        String realKey = String.format(key, userId, curDate);
        logger.debug("key:{}", realKey);
        redisDB0.opsForSet().add(realKey, pushedIds);
        redisDB0.expire(realKey, expireDays, TimeUnit.DAYS);
    }

    public void setPushedVideos(String userId, String[] pushedVideoIds) {
        setPushedKey(userId, videoPushedKey, pushedVideoIds);
    }

    public void setPushedGoods(String userId, String[] pushedGoodsIds) {
        setPushedKey(userId, goodsPushedKey, pushedGoodsIds);
    }


    private void deletePushedKey(String userId, String key) {
        List<String> keys = new ArrayList<>();
        for (String day : lastNdays) {
            keys.add(String.format(key, userId, day));
        }
        redisDB0.delete(keys);
    }

    public void deleteVideoPushedKey(String userId) {
        deletePushedKey(userId, videoPushedKey);
    }

    public void deleteGoodsPushedKey(String userId) {
        deletePushedKey(userId, goodsPushedKey);
    }

    /**
     * 解析如下的形式，只需要冒号前面的整数
     * 249163: 0.23237589, 221388: 0.19470099, 249617: 0.18869004, 197323: 0.18331973, 209836: 0.17684466, 205834: 0.16972847
     */
    private List<String> parseSimValue(String value) {
        List<String> result = new ArrayList<String>();
        if ("".equals(value.trim()))
            return result;
        try {
            for (String item : value.toString().split(",")) {
                result.add(item.split(":")[0].trim());
            }
        } catch (Exception e) {
            logger.error("parseSimiValue error:{}", e);
        }
        return result;
    }

    public Map<String, List<String>> getSimByInfoId(String infoId) {
        String hashKey = String.format("simi_%s", infoId);
        Map<String, List<String>> map = new HashMap<>();
        for (String valueKey : new String[]{"a", "v", "l"}) {
            Object valueOrNull = redisDB0.opsForHash().get(hashKey, valueKey);
            List<String> idList = new ArrayList<>();
            if (valueOrNull != null) {
                idList = parseSimValue(valueOrNull.toString());
            }
            map.put(valueKey, idList);
        }
        return map;
    }
}
