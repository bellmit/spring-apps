package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.StringUtil;
import com.haozhuo.datag.model.InfoALV;
import com.haozhuo.datag.model.InfoArticle;
import com.haozhuo.datag.model.PushedInfoKeys;
import com.haozhuo.datag.model.RcmdNewsInfo;
import com.haozhuo.datag.model.textspilt.MyKeyword;
import com.haozhuo.datag.model.textspilt.SimpleArticle;
import com.haozhuo.datag.service.biz.InfoRcmdService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
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

@SuppressWarnings({"unchecked", "unused"})
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

    @SuppressWarnings("SpellCheckingInspection")
    private String[] lastNdays;

    @Value("${app.redis.pushed-key-max-size:400}")
    private int pushedKeyMaxSize;

    @Value("${app.redis.expire-days:7}")
    private int expireDays;

    private final static String videoPushedKey = "video-pushed:%s:%s";
    private final static String goodsPushedKey = "goods-pushed:%s:%s";

    @Value("${app.biz.queue-rcmd-num:5,4,3,3}")
    private String queueRcmdNumStr;
    private Integer[] queueRcmdNumArray;

    private final static String newsKeywordsKey = "news_keywords";
    private final static String newsIndexKeyFormat = "news_ind:%s:%s";

    private final static String newsRcmdKeyFormat = "news_rcmd:%s:%s";
    private final static String newsPushedKeyFormat = "news_pushed:%s:%s";
    private final static String newsRcmdChannelsKey = "news_rcmd_channels";
    private final static String newsLockFormat = "news_lock:%s";

    private final static String newsBasePref = "base_pref:%s";

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



    public void deletePushedInfoKeysAll(PushedInfoKeys pushedInfoKeys) {
        deleteHashKey(pushedInfoKeys.getKey(), pushedInfoKeys.getALVHashKeys().toArray());
    }

    public void deletePushedInfoKey(String key, String hashKey) {
        deleteHashKey(key, hashKey);
    }

    /**
     * if exist return else init
     * @param pushedInfoKeys
     */
    private void initHashIfNotExist(PushedInfoKeys pushedInfoKeys) {
        if (redisDB0.hasKey(pushedInfoKeys.getKey()))
            return;
        logger.debug("redis create hash key:{}", pushedInfoKeys.getKey());
        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), PushedInfoKeys.getChannelRcmdHashKeyForArticle(), "");
        redisDB0.expire(pushedInfoKeys.getKey(), expireDays, TimeUnit.DAYS);
    }

    private void initHashIfNotExistForA(PushedInfoKeys pushedInfoKeys) {
        if (redisDB0.opsForHash().hasKey(pushedInfoKeys.getKey(),PushedInfoKeys.getChannelRcmdHashKeyForArticle()))
            return;
        logger.debug("redis create hash key:{}", pushedInfoKeys.getKey());
        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), PushedInfoKeys.getChannelRcmdHashKeyForArticle(),"" );
        redisDB0.expire(pushedInfoKeys.getKey(), expireDays, TimeUnit.DAYS);
    }
    private void initHashLabelIfNotExistForA(PushedInfoKeys pushedInfoKeys) {
        if (redisDB0.opsForHash().hasKey(pushedInfoKeys.getKey(),"label"))
            return;
        logger.debug("redis create hash key:{}", pushedInfoKeys.getKey());
        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), "label", InfoRcmdService.baseLabels );
        redisDB0.expire(pushedInfoKeys.getKey(), expireDays, TimeUnit.DAYS);
    }

    /**
     *
     * @param pushedInfoKeys
     * @return
     */

    public InfoALV getPushedInfoALV(PushedInfoKeys pushedInfoKeys) {
        //10000_0_a
        initHashIfNotExist(pushedInfoKeys);
        // 0 article
        //0 %s_%s_a", channelId, categoryId
        List avlHashKeys = pushedInfoKeys.getALVHashKeys();
        //0 295382,295374,295372,295371,295369,295367,295359,295358,295355
        List<Object> values = redisDB0.opsForHash().multiGet(pushedInfoKeys.getKey(), avlHashKeys);
        return getInfoALVFromValues(values, pushedInfoKeys.getKey(), (List<String>) avlHashKeys);
    }

    public String getPushedInfoArticle(PushedInfoKeys pushedInfoKeys) {
        //10000_0_a
        initHashIfNotExistForA(pushedInfoKeys);
        // 0 article
        //0 %s_%s_a", channelId, categoryId
        //0 295382,295374,295372,295371,295369,295367,295359,295358,295355
        String values = (String) redisDB0.opsForHash().get(pushedInfoKeys.getKey(), pushedInfoKeys.getArticleHashKey());
        return values;
    }
    public String getPushedLablefoArticle(PushedInfoKeys pushedInfoKeys) {
        //10000_0_a
        initHashLabelIfNotExistForA(pushedInfoKeys);
        // 0 article
        //0 %s_%s_a", channelId, categoryId
        //0 295382,295374,295372,295371,295369,295367,295359,295358,295355
        System.out.println(pushedInfoKeys.getKey());
        String values = (String) redisDB0.opsForHash().get(pushedInfoKeys.getKey(),"label");
        return values;
    }
    public void putPushedInfoArticle(PushedInfoKeys pushedInfoKeys,String[] set) {
        //deletePushedInfoKey(pushedInfoKeys.getKey(),pushedInfoKeys.getArticleHashKey());
        String init ="";
        if(set.length>0){
            init = StringUtils.join(set,",");
        }
        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), pushedInfoKeys.getArticleHashKey(),init);
    }

    public void putPushedLabelInfoArticle(PushedInfoKeys pushedInfoKeys,String set) {
        //deletePushedInfoKey(pushedInfoKeys.getKey(),pushedInfoKeys.getArticleHashKey());

        redisDB0.opsForHash().put(pushedInfoKeys.getKey(), "label",set);
    }


    public void setPushedInfoALV(PushedInfoKeys pushedInfoKeys, InfoALV oldInfoALV, InfoALV newInfoALV) {
        if (newInfoALV.size() > 0) {
            Map map = IntStream.range(0, 3)
                    .filter(i -> JavaUtils.isNotEmpty(newInfoALV.getByIndex(i)))
                    .boxed().collect(
                            toMap(
                                    pushedInfoKeys::getHashKeyByALVIndex,
                                    i -> Stream.concat(stream(oldInfoALV.getByIndex(i)), stream(newInfoALV.getByIndex(i))).collect(joining(","))
                            )
                    );

            redisDB0.opsForHash().putAll(pushedInfoKeys.getKey(), map);
        }
    }

    private synchronized void updateDateInfo() {
        lastNdays = JavaUtils.getLastDaysArray(expireDays);
        curDate = JavaUtils.getToday();
        logger.debug("expireDays:{}, curDate:{}, last n days:{}", expireDays, curDate, Arrays.asList(lastNdays));
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
    public String[] getPushedGoodsSkuIds(String userId) {
        return getPushedKeys(userId, goodsPushedKey);
    }

    private void addSets(String key, String[] value) {
        if (value.length == 0) return;
        redisDB0.opsForSet().add(key, value);
        redisDB0.expire(key, expireDays, TimeUnit.DAYS);
    }

    public void setPushedVideos(String userId, String[] pushedVideoIds) {
        addSets(String.format(videoPushedKey, userId, curDate), pushedVideoIds);
    }

    public void addPushedGoodsSkuIds(String userId, String[] pushedGoodsIds) {
        addSets(String.format(goodsPushedKey, userId, curDate), pushedGoodsIds);
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

    public void deletePushedGoodsSkuIds(String userId) {
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

        simpleArticle.getKeywords()
                .forEach(kw -> ho.put(
                        String.format(newsIndexKeyFormat, String.valueOf(simpleArticle.getChannelId()), kw.getName()),
                        strInfoId,
                        String.valueOf(kw.getScore())
                ));
    }

    /**
     *
     * @param userId
     * @param channelId
     * @param count
     * @return
     */

    public RcmdNewsInfo getRcmdNewsByChannel(String userId, String channelId, int count) {
        String rcmdKey = String.format(newsRcmdKeyFormat, userId, channelId);
        SetOperations newsSet = redisDB0.opsForSet();
        List<String> news = newsSet.pop(rcmdKey, count);

        //
        addPushedNewsSet(userId, channelId, news);

        RcmdNewsInfo rcmdNewsInfo = new RcmdNewsInfo();
        rcmdNewsInfo.addNews(news);

        if (newsSet.size(rcmdKey) < count) { //如果推荐池中的数量已经少于count,那么告知推荐
            rcmdNewsInfo.addChannelId(channelId);
        }
        return rcmdNewsInfo;
    }

    private boolean unLocked(String userId) {
        return redisDB0.getExpire(String.format(newsLockFormat, userId)) <= 0;
    }

    private void setLocked(String userId) {
        redisDB0.opsForValue().set(String.format(newsLockFormat, userId), "1", 15, TimeUnit.SECONDS);
    }

    private Integer[] getRcmdNumArray() {
        if (queueRcmdNumArray == null) {
            queueRcmdNumArray = stream(queueRcmdNumStr.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
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

        // 推荐channel
        if (queueChannelIds.length < queueRcmdNumArray.length) {
            //已经过期
            if (unLocked(userId)) {
                info.addDefaultChannelIds();
                setLocked(userId);
            }
        } else {
            for (int i = 0; i < queueRcmdNumArray.length; i++) {
                try {
                    String channelId = queueChannelIds[i];
                    int subCount = queueRcmdNumArray[i];
                    RcmdNewsInfo subNewsInfo = getRcmdNewsByChannel(userId, channelId, subCount);
                    if (subNewsInfo.getChannelIdList().size() > 0) {
                        info.addChannelId(channelId);
                    }
                    info.addNews(subNewsInfo.getNews());
                } catch (Exception ignored) {
                }
            }
            Collections.shuffle(info.getNews());
        }
        return info;
    }

    private void addPushedNewsSet(String userId, String channelId, List<String> values) {
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
        //get channelId and keywords from Redis.news_keywords where hashKey = informationId
        HashOperations ho = redisDB0.opsForHash();
        SimpleArticle simpleArticle = getNewsKeywords(ho, informationId);
        if (simpleArticle.isEmpty())
            return;

        //foreach delete news_ind:{channelId}:{keyword}
        simpleArticle.getKeywords().stream()
                .map(kw -> String.format(newsIndexKeyFormat, simpleArticle.getChannelId(), kw.getName()))
                .forEach(key -> ho.delete(key, informationId));

        //delete hashValue from Redis.news_keywords where hashKey = informationId
        ho.delete(newsKeywordsKey, informationId);
    }

    public Set<String> getPositiveBasePref(String userId, int count) {
        return redisDB0.opsForZSet().reverseRangeByScore(String.format(newsBasePref, userId), 1D, Double.MAX_VALUE, 0, count);
    }




}