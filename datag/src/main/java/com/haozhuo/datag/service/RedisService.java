package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.PushedInfoKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    @Value("${app.redis.pushed-key-max-size:400}")
    private int pushedKeyMaxSize;
    @Value("${app.redis.expire-days:7}")
    private int expireDays;

    private final String videoPushedKey = "video-pushed:%s:%s";
    private final String goodsPushedKey = "goods-pushed:%s:%s";

    private final String hateTagsKey = "HateTags:%s";
    private final String loveTagsKey = "LoveTags:%s";

    private final List<Object> avlHashKeys = Arrays.asList("a", "v", "l");

    private void deleteHashKey(String key, Object... hashKeys) {
        redisDB0.opsForHash().delete(key, hashKeys);
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

    public void deleteHashKeyByPushedInfoKeys(PushedInfoKeys pushedInfoKeys) {
        deleteHashKey(pushedInfoKeys.getKey(), pushedInfoKeys.getALVHashKeys().toArray());
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
            Map map = IntStream.range(0, 3)
                    .filter(i -> JavaUtils.isNotEmpty(newInfoALV.getByIndex(i)))
                    .boxed().collect(
                            toMap(
                                    i -> pushedInfoKeys.getHashKeyByALVIndex(i),
                                    i -> Stream.concat(stream(oldInfoALV.getByIndex(i)), stream(newInfoALV.getByIndex(i))).collect(joining(","))
                            )
                    );

            redisDB0.opsForHash().putAll(pushedInfoKeys.getKey(), map);
        }
    }


    public void addHateTags(String userId, String hateTags) {
        if (JavaUtils.isNotEmpty(hateTags)) {
            String key = String.format(hateTagsKey, userId);
            ListOperations<String, String> oper = redisDB0.opsForList();
            oper.leftPush(key, hateTags);
            oper.trim(key, 0, 30); //只保存最近30条记录
            redisDB0.expire(key, 90, TimeUnit.DAYS); //过期时间90天
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
        //redisDB0.expire(key, expireDays * 3, TimeUnit.DAYS);
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
        return stream(lastNdays)
                .flatMap(day -> redisDB0.opsForSet().members(String.format(key, userId, day)).stream())
                .toArray(String[]::new);
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
        setSets(String.format(videoPushedKey, userId, curDate), pushedVideoIds);
    }

    public void setPushedGoods(String userId, String[] pushedGoodsIds) {
        setSets(String.format(goodsPushedKey, userId, curDate), pushedGoodsIds);
    }

    private void deletePushedKey(String userId, String key) {
        redisDB0.delete(
                stream(lastNdays).map(day -> String.format(key, userId, day)).collect(toList())
        );
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
        return stream(value.split(",")).filter(x -> !"".equals(x.trim())).map(item -> item.split(":")[0].trim())
                .collect(toList());
    }

    public Map<String, List<String>> getSimByInfoId(String infoId) {
        List<Object> hashValues = redisDB0.opsForHash().multiGet(String.format("simi_%s", infoId), avlHashKeys);
        return IntStream.range(0, avlHashKeys.size()).boxed().collect(toMap(
                i -> avlHashKeys.get(i).toString(),
                i -> parseSimValue(Optional.ofNullable(hashValues.get(i)).orElse("").toString().replaceAll("'", ""))
        ));
    }
}
