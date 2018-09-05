package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.PushedInfoKeys;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @Value("${app.redis.pushed-key-max-size:200}")
    private int pushedKeyMaxSize;
    @Value("${app.redis.expire-days:7}")
    private int expireDays;

    private final String videoPushedKey = "video-pushed:%s:%s";
    private final String goodsPushedKey = "goods-pushed:%s:%s";

    private final String hateTagsKey = "HateTags:%s";
    private final String loveTagsKey = "LoveTags:%s";

    private void deleteHashKey(String key, String hashKey) {
        redisDB0.opsForHash().delete(key, hashKey);
    }

    private InfoALV getInfoALVFromValues(List<Object> values, String key, List<String> hashKeys) {
        InfoALV pushedIds = new InfoALV();
        String[] ids;
        for (int i = 0; i < hashKeys.size(); i++) {
            if (!JavaUtils.isEmpty(values.get(i))) {
                String strValue = values.get(i).toString();
                if (JavaUtils.isNotEmpty(strValue)) {
                    ids = strValue.split(",");
                    pushedIds.setByIndex(ids, i);
                    if (ids.length > pushedKeyMaxSize) deleteHashKey(key, hashKeys.get(i));
                }
            }
        }
        return pushedIds;
    }

    public void initHashIfNotExist(PushedInfoKeys pushedInfoKeys) {
        if (redisDB0.hasKey(pushedInfoKeys.getKey()))
            return;
        logger.debug("redis create hash key:{}", pushedInfoKeys.getKey());
        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), pushedInfoKeys.getChannelRcmdHashKey(), "");
        redisDB0.expire(pushedInfoKeys.getKey(), expireDays, TimeUnit.DAYS);
    }

    public InfoALV getPushedInfoALV(PushedInfoKeys pushedInfoKeys) {
        initHashIfNotExist(pushedInfoKeys);
        List avlHashKeys = pushedInfoKeys.getALVHashKeys();
        List<Object> values = redisDB0.opsForHash().multiGet(pushedInfoKeys.getKey(), avlHashKeys);
        return getInfoALVFromValues(values, pushedInfoKeys.getKey(), (List<String>) avlHashKeys);
    }

    public void setPushedInfoALV(PushedInfoKeys pushedInfoKeys, InfoALV oldInfoALV, InfoALV newInfoALV) {
        if (newInfoALV.size() > 0) {
            Map map = new HashMap<String, String>();
            for (int i = 0; i < 3; i++) {
                if (JavaUtils.isNotEmpty(newInfoALV.getByIndex(i))) {
                    map.put(pushedInfoKeys.getHashKeyByALVIndex(i),
                            StringUtils.arrayToCommaDelimitedString(ArrayUtils.addAll(oldInfoALV.getByIndex(i), newInfoALV.getByIndex(i))));
                }
            }
            redisDB0.opsForHash().putAll(pushedInfoKeys.getKey(), map);
        }
    }

    public String getHateTags(String userId) {
        return getTags(hateTagsKey, userId);
    }

    public String getLoveTags(String userId) {
        return getTags(loveTagsKey, userId);
    }

    private String getTags(String keyFormat, String userId) {
        String key = String.format(keyFormat, userId);
        redisDB0.expire(key, expireDays * 3, TimeUnit.DAYS);
        List<String> tagsList = redisDB0.opsForList().range(key, 0, 10);
        StringBuffer result = new StringBuffer();
        for (String hateTags : tagsList) {
            result.append(hateTags).append(",");
        }
        return result.toString();
    }

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

    private String[] getPushedKeys(String userId, String key) {
        checkOrUpdateDateInfo();
        Set<String> result = new HashSet<>();
        for (String day : lastNdays) {
            result.addAll(redisDB0.opsForSet().members(String.format(key, userId, day)));
        }
        return result.toArray(new String[0]);
    }


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

    private void setSets(String key, String[] value) {
        if (value.length == 0) return;
        redisDB0.opsForSet().add(key, value);
        redisDB0.expire(key, expireDays, TimeUnit.DAYS);
    }

    public void setPushedVideos(String userId, String[] pushedVideoIds) {
        String realKey = String.format(videoPushedKey, userId, curDate);
        setSets(realKey, pushedVideoIds);
    }

    public void setPushedGoods(String userId, String[] pushedGoodsIds) {
        String realKey = String.format(goodsPushedKey, userId, curDate);
        setSets(realKey, pushedGoodsIds);
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
                idList = parseSimValue(valueOrNull.toString().replaceAll("'", "")); //Redis中有数据有问题：'289': 0.43851748, '190': 0.33565181，带了单引号
            }
            map.put(valueKey, idList);
        }
        return map;
    }
}
