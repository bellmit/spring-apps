package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.model.RcmdNewsInfo;
import com.haozhuo.datag.model.textspilt.MyKeyword;
import com.haozhuo.datag.model.textspilt.SimpleArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

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

    @Deprecated
    private final String hateTagsKey = "HateTags:%s";

    @Deprecated
    private final String loveTagsKey = "LoveTags:%s";


    @Value("${app.biz.queueRcmdNum:5,4,3,2}")
    private String queueRcmdNumStr;
    private Integer[] queueRcmdNumArray;

    private final String newsKeywordsKey = "news_keywords";
    private final String newsIndexKeyFormat = "news_ind:%s:%s";
    private final String userPrefsKeyFormat = "user_prefs:%s";
    private final String newsRcmdKeyFormat = "news_rcmd:%s:%s";
    private final String newsPushedKeyFormat = "news_pushed:%s:%s";
    private final String newsRcmdChannelsKey = "news_rcmd_channels";
    private final String newsLockFormat = "news_lock:%s";

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

    public void deleteHashKeyByPushedInfoKeys(PushedInfoKeys pushedInfoKeys, String hashKey) {
        deleteHashKey(pushedInfoKeys.getKey(), hashKey);
    }

    @Deprecated
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

    @Deprecated
    public void addHateTags(String userId, String hateTags) {
        if (JavaUtils.isNotEmpty(hateTags)) {
            String key = String.format(hateTagsKey, userId);
            ListOperations<String, String> oper = redisDB0.opsForList();
            oper.leftPush(key, hateTags);
            oper.trim(key, 0, 30); //只保存最近30条记录
            redisDB0.expire(key, 1, TimeUnit.DAYS); //过期时间1天
        }
    }

    @Deprecated
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
        checkOrUpdateDateInfo();
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
        checkOrUpdateDateInfo();
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

    public void setKeywordsOfArticleInRedis(SimpleArticle simpleArticle) {
        String strInfoId = String.valueOf(simpleArticle.getInformationId());
        HashOperations ho = redisDB0.opsForHash();
        ho.put(newsKeywordsKey, strInfoId, simpleArticle.getChannelIdWithKeywords());

        simpleArticle.getKeywords().stream()
                .forEach(kw -> ho.put(
                        String.format(newsIndexKeyFormat, String.valueOf(simpleArticle.getChannelId()), kw.getName()),
                        strInfoId,
                        String.valueOf(kw.getScore())
                ));
    }

    public List<MyKeyword> getUserPref(String userId, String channelId) {
        Object obj = redisDB0.opsForHash().get(String.format(userPrefsKeyFormat, userId), channelId);
        return MyKeyword.parseKeywordsFromString((String) obj);
    }

    private void setUserPref(HashOperations ho, String userId, String channelId, List<MyKeyword> prefList) {
        String strPref = prefList.stream()
                .map(kw -> kw.getName() + ":" + kw.getScore()).collect(joining(","));
        ho.put(String.format(userPrefsKeyFormat, userId), channelId, strPref);
    }

    private Tuple<List<String>, Boolean> getNewsByUserAndChannel(String userId, String channelId, int count) {
        String rcmdKey = String.format(newsRcmdKeyFormat, userId, channelId);
        SetOperations so = redisDB0.opsForSet();
        List<String> news = so.pop(rcmdKey, count);
        boolean requestRcmd = false;
        if (so.size(rcmdKey) < 10) {
            requestRcmd = true;
        }
        addPushedNewsSet(userId, channelId, news);
        return new Tuple<>(news, requestRcmd);
    }


    public RcmdNewsInfo getRcmdNewsByChannel(String userId, String channelId, int count) {
        Tuple<List<String>, Boolean> tup = getNewsByUserAndChannel(userId, channelId, count);
        RcmdNewsInfo rcmdNewsInfo = new RcmdNewsInfo();
        rcmdNewsInfo.addNews(tup.getT1());
        if (tup.getT2()) {
            rcmdNewsInfo.addRcmdChannelId(channelId);
        }
        return rcmdNewsInfo;
    }

    public boolean unLocked(String userId) {
        return redisDB0.getExpire(String.format(newsLockFormat, userId)) <= 0;
    }

    public void setLocked(String userId) {
        redisDB0.opsForValue().set(String.format(newsLockFormat, userId), "1", 15, TimeUnit.SECONDS);
    }

    private Integer[] getRcmdNumArray() {
        if (queueRcmdNumArray == null) {
            queueRcmdNumArray = stream(queueRcmdNumStr.split(",")).map(x -> Integer.parseInt(x)).toArray(Integer[]::new);
        }
        return queueRcmdNumArray;
    }

    public RcmdNewsInfo getRcmdNews(String userId, int count) {
        Object queue = redisDB0.opsForHash().get(newsRcmdChannelsKey, userId);
        Integer[] queueRcmdNumArray = getRcmdNumArray();
        RcmdNewsInfo info = new RcmdNewsInfo();
        if (queue == null) {
            queue = "1";
        }
        String[] queueChannelIds = queue.toString().split(",");
        if (queueChannelIds.length < queueRcmdNumArray.length) {
            if (unLocked(userId)) {
                info.setInitAllChannels(true);
                setLocked(userId);
            }
        } else {
            for (int i = 0; i < queueRcmdNumArray.length; i++) {
                try {
                    String channelId = queueChannelIds[i];
                    int subCount = queueRcmdNumArray[i];
                    Tuple<List<String>, Boolean> tmp = getNewsByUserAndChannel(userId, channelId, subCount);
                    if (tmp.getT2()) {
                        info.addRcmdChannelId(channelId);
                    }
                    info.addNews(tmp.getT1());
                } catch (Exception ex) {

                }
            }
            if (count < info.getNews().size()) {
                Collections.shuffle(info.getNews());
                info.setNews(info.getNews().subList(0, count));
            }
        }
        return info;
    }

    public void addPushedNewsSet(String userId, String channelId, List<String> values) {
        if (values == null || values.size() == 0) {
            return;
        }
        String pushedNewsKey = String.format(newsPushedKeyFormat, userId, channelId);
        redisDB0.opsForSet().add(pushedNewsKey, values.toArray(new String[]{}));
        redisDB0.expire(pushedNewsKey, expireDays, TimeUnit.DAYS);
    }


    private SimpleArticle getNewsKeywords(HashOperations ho, String informationId) {
        SimpleArticle simpleArticle = new SimpleArticle();
        Object newsInfoObj = ho.get(newsKeywordsKey, informationId);
        if (newsInfoObj == null)
            return simpleArticle;
        String newsInfo = newsInfoObj.toString();
        if ("".equals(newsInfo))
            return simpleArticle;

        String[] infoArray = newsInfo.split(";");

        String channelId = infoArray[0];
        List<MyKeyword> keywords = MyKeyword.parseKeywordsFromString(infoArray[1]);
        simpleArticle.setInformationId(informationId);
        simpleArticle.setChannelId(channelId);
        simpleArticle.setKeywords(keywords);
        return simpleArticle;
    }

    public void deleteKeywordsOfArticleInRedis(String informationId) {
        //get channelId and keywords from Redis.news_keywords where hashKey = infomationId
        HashOperations ho = redisDB0.opsForHash();
        SimpleArticle simpleArticle = getNewsKeywords(ho, informationId);
        if (simpleArticle.isEmpty())
            return;

        //foreach delete news_ind:{channelId}:{keyword}
        simpleArticle.getKeywords().stream()
                .map(kw -> String.format(newsIndexKeyFormat, simpleArticle.getChannelId(), kw.getName()))
                .forEach(key -> ho.delete(key, informationId));

        //delete hashValue from Redis.news_keywords where hashKey = infomationId
        ho.delete(newsKeywordsKey, informationId);
    }

/*    public Set<String> getHomePushedGoodsSkuIds(String userId) {
        return redisDB1.opsForSet().members(String.format(homePushedGoodsSkuId, userId));
    }

    public String[] getHomePushedGoodsSkuIdsArray(String userId) {
        return getHomePushedGoodsSkuIds(userId).toArray(new String[]{});
    }

    public void addHomePushedGoodsByUserId(String userId, String... skuIds) {
        if (skuIds.length > 0) {
            String key = String.format(homePushedGoodsSkuId, userId);
            logger.debug(key);
            redisDB1.opsForSet().add(key, skuIds);
            redisDB1.expire(key, 1, TimeUnit.DAYS);
        }
    }

    public void deleteHomePushedGoodsByUserId(String userId) {
        redisDB1.delete(String.format(homePushedGoodsSkuId, userId));
    }*/


}