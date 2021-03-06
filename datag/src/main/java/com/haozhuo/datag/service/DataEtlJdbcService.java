package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.model.crm.HealthyClub;
import com.haozhuo.datag.model.textspilt.SimpleArticle;
import com.haozhuo.datag.util.SqlSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.haozhuo.datag.model.Goods.listToStr;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

/**
 * Created by Lucius on 8/16/18.
 */
@SuppressWarnings({"WeakerAccess", "JavaDoc"})
@Component
public class DataEtlJdbcService {
    private static final Logger logger = LoggerFactory.getLogger(DataEtlJdbcService.class);

    public Map<String, String[]> channelEsTypeMap;
    private final Map<String, String> labelNameIdMap = new HashMap<>();
    private List<DiseaseNorm> someDiseaseNormList;
    private List<DiseaseNorm> allDiseaseNormList;
    private List<DiseaseNormCheckItem> diseaseNormCheckItemList;
    private final String regEx = "[\\s+`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    //private final String regEx = "[\\s+`~!@#$%^&*()=\|{}':;\\[\\].。<>/?！￥…（）—\\-【】　‘\\;；:：”“’，、？\]";
    private final String goodsTable;
    private final String liveTable;
    private final String videoTable;
    private final String articleTable;
    private final String articleTags;
    public final static List<String> stopWords = new ArrayList<>();

    @Qualifier("dataetlJdbc") //选择jdbc连接池
    private final JdbcTemplate dataetlDB;

    @Autowired
    public DataEtlJdbcService(JdbcTemplate jdbcTemplate, Environment env) {
        this.dataetlDB = jdbcTemplate;
        liveTable = env.getProperty("app.mysql.live");
        videoTable = env.getProperty("app.mysql.video");
        articleTable = env.getProperty("app.mysql.article");
        goodsTable = env.getProperty("app.mysql.goods");
        articleTags = env.getProperty("app.biz.article-tags", "title,tags,keywords");

        updateChannelEsTypeMap();
        initLabelMap();
        initStopWords();
        someDiseaseNormList = getSomeDiseaseNormList();
        allDiseaseNormList = getAllDiseaseNormList();
        diseaseNormCheckItemList = getDiseaseNormCheckItemList();
    }

    private void initLabelMap() {
        List<Tuple<String, String>> list = dataetlDB.query("select id, label from disease_label",
                (resultSet, i) -> new Tuple<>(resultSet.getString("id"), resultSet.getString("label")));
        for (Tuple<String, String> tuple : list) {
            //labelIdNameMap.put(tuple.getT1(), tuple.getT2());
            labelNameIdMap.put(tuple.getT2(), tuple.getT1());
        }
    }

    private List<DiseaseNorm> getSomeDiseaseNormList() {
        return dataetlDB.query("select unnorm, norm from disease_norm x where x.norm in" +
                        " ('肝功能异常','幽门螺旋杆菌感染','血清谷丙转氨酶增高','尿隐血阳性','谷氨酰转肽酶增高'," +
                        " '前列腺增生','肝囊肿','血清总胆红素增高','直接胆红素增高','间接胆红素增高','前列腺囊肿'," +
                        " '乙肝小三阳','血清乳酸脱氢酶增高','血清碱性磷酸酶增高','肾脏损害','前列腺回声异常'," +
                        " '前列腺特异性抗原增高','甲胎蛋白增高','血清结合胆红素偏高','尿胆红素阳性','铁蛋白增高'," +
                        " '前列腺炎','肝脏增大','肝硬化','CA50增高','前列腺占位性病变')",
                (resultSet, i) -> new DiseaseNorm(resultSet.getString("unnorm"), resultSet.getString("norm")));

    }

    private List<DiseaseNormCheckItem> getDiseaseNormCheckItemList() {
        return dataetlDB.query("select y.unnorm,y.norm,x.check_item from disease_check x left join disease_norm y on x.disease_norm = y.norm",
                (resultSet, i) -> new DiseaseNormCheckItem(resultSet.getString("unnorm"), resultSet.getString("norm"), resultSet.getString("check_item")));
    }

    private List<DiseaseNorm> getAllDiseaseNormList() {
        return dataetlDB.query("select unnorm, norm from disease_norm x",
                (resultSet, i) -> new DiseaseNorm(resultSet.getString("unnorm"), resultSet.getString("norm")));

    }

    private void initStopWords() {
        List<String> list = dataetlDB.query("select name from stop_words;", (resultSet, i) -> resultSet.getString("name"));
        stopWords.addAll(list);
    }

    private void updateChannelEsTypeMap() {
        List<Tuple<String, String>> list = dataetlDB.query("select parent_id as channelId, channel_id as categoryId  from channel where parent_id >0 union \n" +
                "select  channel_id as channelId,parent_id as categoryId from channel where parent_id = 0", (resultSet, i) -> new Tuple<>(resultSet.getString("channelId"), resultSet.getString("categoryId")));
        channelEsTypeMap = list.stream().collect(groupingBy(Tuple::getT1, mapping(x -> x.getT1() + "_" + x.getT2(), toList())))
                .entrySet().stream().collect(toMap(Map.Entry::getKey, x -> x.getValue().toArray(new String[0])));
        logger.info("updateChannelEsTypeMap:{}", channelEsTypeMap.entrySet().stream().map(x -> x.getKey() + "->" + String.join(",", x.getValue())).collect(joining(" | ")));

    }


    public Map<String, String> getPortraitMap(int type) {
        String sql;
        if (type == 1) {
            sql = "select id, name from portrait_tag union select id,label as name from disease_label";
        } else {
            sql = "select id,label as name from disease_label";
        }
        List<Tuple<String, String>> list = dataetlDB.query(sql, (resultSet, i) -> new Tuple<>(resultSet.getString("name"), resultSet.getString("id")));
        Map<String, String> result = new HashMap<>();
        for (Tuple<String, String> tuple : list) {
            result.put(tuple.getT1(), tuple.getT2());

        }
        return result;
    }


    public List<Goods> getGoodsList(int from, int size) {
        return dataetlDB.query(
                String.format("select sku_id,goods_ids,name,description,category,sub_category,goods_tags,search_keywords,city_ids, rcmd_score,goods_type,sales_num,create_time from %s x limit ?, ?", goodsTable),
                new Object[]{from, size},
                (resultSet, i) -> {
                    // Optional.ofNullable() 表示传入的参数可能为null
                    // orElse() 表示如果传入的是null就赋予另一个值
                    Goods goods = new Goods();
                    goods.setSkuId(resultSet.getString("sku_id"));
                    goods.setGoodsIds(Arrays.asList(resultSet.getString("goods_ids").split(",")));
                    goods.setGoodsName(resultSet.getString("name"));
                    goods.setGoodsDescription(resultSet.getString("description"));
                    goods.setCategory(resultSet.getString("category"));
                    goods.setSubCategory(resultSet.getString("sub_category"));
                    goods.setGoodsTags(Arrays.asList(resultSet.getString("goods_tags").split(",")));
                    goods.setSearchKeywords(resultSet.getString("search_keywords"));
                    goods.setRcmdScore(resultSet.getInt("rcmd_score"));
                    goods.setGoodsType(resultSet.getInt("goods_type"));
                    goods.setSalesNum(resultSet.getInt("sales_num"));
                    goods.setCityIds(Arrays.asList(resultSet.getString("city_ids").split(",")));
                    goods.setCreateTime(resultSet.getString("create_time"));
                    return goods;
                });
    }

    public List<String> getGoodsSkuIdsByLikeStr(String field, String str) {
        List<String> goodsIdList = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            goodsIdList = dataetlDB.query(
                    String.format("select sku_id from %s x where x.%s like ?", goodsTable, field),
                    new Object[]{str},
                    (resultSet, i) -> {
                        // Optional.ofNullable() 表示传入的参数可能为null
                        // orElse() 表示如果传入的是null就赋予另一个值
                        return resultSet.getString("sku_id");
                    });
        } catch (Exception ex) {
            logger.debug("getGoodsSkuIdsByLikeStr error", ex);
        }
        return goodsIdList;
    }

    /**
     * 根据videoId获取videoLabels
     *
     * @param videoId
     * @return
     */
    public String getVideoTagsById(String videoId) {
        String labels = "";
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            labels = dataetlDB.queryForObject(
                    String.format("select tags from %s where id = ?", videoTable),
                    new Object[]{videoId},
                    (resultSet, i) -> resultSet.getString("tags"));
        } catch (Exception ex) {
            logger.debug("getVideoTagsById error", ex);
            logger.info("video4中没有这个id:{}", videoId);
        }
        return labels;
    }

    private String getUnPushedVideoOrLiveIdSQL(String[] pushedIds, boolean isVideo, String channelId) {
        StringBuffer sql = new StringBuffer(String.format("select id from %s where 1=1 ", isVideo ? videoTable : liveTable));

        if (pushedIds != null && pushedIds.length >= 1) {
            sql.append(String.format(" and id not in(%s) ", String.join(",", pushedIds)));
        }
        if (JavaUtils.isNotEmpty(channelId)) {
            sql.append(String.format(" and channel_id = %s ", channelId));
        }
        sql.append(" limit 1");
        return sql.toString();
    }

    public String getUnPushedVideoOrLiveId(String[] pushedIds, boolean isVideo, String channelId) {
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            String sql = getUnPushedVideoOrLiveIdSQL(pushedIds, isVideo, channelId);
            logger.debug(sql);
            return dataetlDB.queryForObject(sql,
                    (resultSet, i) -> resultSet.getString("id"));
        } catch (Exception ex) {
            return null;
        }
    }

    public String getSingleNormTag(String abnormal) {
        String regexNormTag = abnormal.replaceAll(regEx, "")
                .replaceAll("Ⅰ", "1")
                .replaceAll("Ⅱ", "2")
                .replaceAll("Ⅲ", "3")
                .toLowerCase();
        String result = getNormTag(regexNormTag, allDiseaseNormList);
        if (result == null) {
            result = regexNormTag;
        }
        return result;
    }

    private String[] parseAbnormal(String abnormals) {
        return abnormals.replaceAll(regEx, "")
                .replaceAll("Ⅰ", "1")
                .replaceAll("Ⅱ", "2")
                .toLowerCase()
                .split(",");
    }

    public Set<String> getNormTags(String abnormals) {
        return stream(parseAbnormal(abnormals))
                .map(abnormal -> getNormTag(abnormal, someDiseaseNormList)).filter(x -> x != null)
                .collect(toSet());
    }

    public Tuple<Set<String>, Set<String>> getNormTagCheckItems(String abnormals) {
        String[] unnormArray = parseAbnormal(abnormals);
        Set<String> normList = new HashSet<>();
        Set<String> checkItemList = new HashSet<>();
        for (String unnorm : unnormArray) {
            for (DiseaseNormCheckItem nc : diseaseNormCheckItemList) {
                if (unnorm.contains(nc.getUnNorm())) {
                    normList.add(nc.getNorm());
                    checkItemList.add(nc.getCheckItem());
                }
            }
        }
        return new Tuple<>(normList, checkItemList);
    }

    private String getNormTag(String abnormal, List<DiseaseNorm> normList) {
        for (DiseaseNorm diseaseNorm : normList) {
            if (abnormal.contains(diseaseNorm.getUnNorm())) {
                return diseaseNorm.getNorm();
            }
        }
        return null;
    }

    /**
     * 根据infoId获取Tags
     */
    public String getTagsKeywordsByInfoId(String infoId) {
        String tags = "";
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            String sql = String.format("select concat(%s) as tags from %s  where information_id = ?", articleTags, articleTable);
            logger.debug("sql:{}", sql);
            tags = dataetlDB.queryForObject(sql, new Object[]{infoId}, (resultSet, i) -> resultSet.getString("tags"));
        } catch (Exception ex) {
            logger.debug("getTagsByInfoId error", ex);
        }
        return tags;
    }


    public void updateVideo(Video video) {
        String query = String.format("INSERT INTO `%s` (`id`, `title`, `status`, `url`, `channel_id`, `category_id`, " +
                " `tags`, `time_len`, `description`, `create_time`, `update_time`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE `title` = ?, `status` = ?, `url` = ?, `channel_id` = ?, `category_id` = ?, `tags` = ?," +
                " `time_len` = ?, `description` = ?, `create_time` = ?, `update_time` = ?", videoTable);
        dataetlDB.update(query, video.getId(), video.getTitle(), video.getStatus(), video.getUrl(), video.getChannelId(),
                video.getCategoryId(), video.getTags(), video.getTimeLen(), video.getDescription(), video.getCreateTime(),
                video.getUpdateTime(), video.getTitle(), video.getStatus(), video.getUrl(), video.getChannelId(), video.getCategoryId(),
                video.getTags(), video.getTimeLen(), video.getDescription(), video.getCreateTime(), video.getUpdateTime());
    }

    public void updateArticle(Article article) {
        String query = String.format("INSERT INTO `%s` (`information_id`, `status`, `title`, `image`, `images`, `content`, `channel_id`," +
                " `category_id`, `tags`, `create_time`, `update_time`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY " +
                " UPDATE `status` = ?, `title` = ?, `image` = ?, `images` = ?, `content` = ?, `channel_id` = ?, `category_id`   = ?," +
                " `tags` = ?, `create_time` = ?, `update_time` = ?", articleTable);
        dataetlDB.update(query, article.getInformationId(), article.getStatus(), article.getTitle(), article.getImage(), article.getImages(),
                article.getContent(), article.getChannelId(), article.getCategoryId(), article.getTags(), article.getCreateTime(), article.getUpdateTime(),
                article.getStatus(), article.getTitle(), article.getImage(), article.getImages(), article.getContent(), article.getChannelId(), article.getCategoryId(),
                article.getTags(), article.getCreateTime(), article.getUpdateTime());
    }

    public void updateKeywordsOfArticle(SimpleArticle article) {
        String query = String.format("UPDATE `%s` SET keywords = ? WHERE information_id = ?", articleTable);
        dataetlDB.update(query, article.getStrKeywords(), article.getInformationId());
    }


    public void updateChannel(Channel channel) {
        String query = "INSERT INTO `channel` (`channel_id`, `name`, `sort_num`, `parent_id`, `create_time`, `create_operator`," +
                " `update_operator`, `update_time`) VALUES(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `sort_num` = ?," +
                " `parent_id` = ?, `create_time` = ?, `create_operator` = ?, `update_operator` = ?, `update_time` = ?";
        dataetlDB.update(query, channel.getChannelId(), channel.getName(), channel.getSortNum(), channel.getParentId(), channel.getCreateTime(),
                channel.getCreateOperator(), channel.getUpdateOperator(), channel.getUpdateTime(), channel.getName(), channel.getSortNum(),
                channel.getParentId(), channel.getCreateTime(), channel.getCreateOperator(), channel.getUpdateOperator(), channel.getUpdateTime());
        updateChannelEsTypeMap();
    }

    public void updateLive(Live liveInfo) {
        String query = String.format("INSERT INTO `%s` (`id`, `title`, `status`, `description`, `channel_id`,`category_id`, " +
                " `tags`, `is_pay`, `play_time`, `update_time`)  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  " +
                " ON DUPLICATE KEY UPDATE  `title`=?, `status`=?, `description`=?, `channel_id`=?,`category_id`=?," +
                "`tags`=?,`is_pay`=?, `play_time`=?, `update_time`=?", liveTable);
        dataetlDB.update(query, liveInfo.getId(), liveInfo.getTitle(), liveInfo.getStatus(), liveInfo.getDescription(),
                liveInfo.getChannelId(), liveInfo.getCategoryId(), liveInfo.getTags(), liveInfo.getIsPay(),
                liveInfo.getPlayTime(), liveInfo.getUpdateTime(), liveInfo.getTitle(), liveInfo.getStatus(),
                liveInfo.getDescription(), liveInfo.getChannelId(), liveInfo.getCategoryId(), liveInfo.getTags(),
                liveInfo.getIsPay(), liveInfo.getPlayTime(), liveInfo.getUpdateTime());

    }

    public void updateGoods(Goods goods) {
        String goodsIds = listToStr(goods.getGoodsIds());
        String goodsTags = listToStr(goods.getGoodsTags());
        String cityIds = listToStr(goods.getCityIds());
        String query = String.format("INSERT INTO `%s` (`sku_id`, `name`, `description`, `category`, `sub_category`, " +
                " `goods_tags`, `search_keywords`, `city_ids`, `rcmd_score`, `create_time`, `goods_ids`, `goods_type`, `sales_num`) " +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) ON DUPLICATE KEY UPDATE `name` = ?, `description` = ?, " +
                "`category` = ?, `sub_category` = ?, `goods_tags` = ?, `search_keywords` = ?, `city_ids` = ?, `rcmd_score` = ?," +
                " `create_time` = ?, `goods_ids` = ?, `goods_type` = ?, `sales_num` =? ", goodsTable);
        dataetlDB.update(query, goods.getSkuId(), goods.getGoodsName(), goods.getGoodsDescription(), goods.getCategory(),
                goods.getSubCategory(), goodsTags, goods.getSearchKeywords(), cityIds, goods.getRcmdScore(), goods.getCreateTime(),
                goodsIds, goods.getGoodsType(), goods.getSalesNum(), goods.getGoodsName(), goods.getGoodsDescription(),
                goods.getCategory(), goods.getSubCategory(), goodsTags, goods.getSearchKeywords(), cityIds,
                goods.getRcmdScore(), goods.getCreateTime(), goodsIds, goods.getGoodsType(), goods.getSalesNum());
    }

    public void deleteVideo(long id) {
        dataetlDB.update(String.format("update %s set status = 0 where id = ?", videoTable), id);
    }

    public void deleteArticle(long id) {
        dataetlDB.update(String.format("update %s set status = 0 where information_id = ?", articleTable), id);
    }

    public void deleteLive(long id) {
        dataetlDB.update(String.format("delete from %s where id = ?", liveTable), id);
    }

    public void deleteChannel(long id) {
        dataetlDB.update("delete from channel where channel_id = ?", id);
        updateChannelEsTypeMap();
    }

    public void deleteGoods(String skuId) {
        dataetlDB.update(String.format("delete from %s where sku_id = ?", goodsTable), skuId);
    }

    public void updatePermitUsers(long healthReportId, int flag) {
        String date = JavaUtils.getCurrent();
        dataetlDB.update("INSERT INTO `permit_users` (`date`,`health_report_id`,`flag`) VALUES (?, ?, ?) on duplicate key update date=?", date, healthReportId, flag, date);
    }

    public List getBylabel(String label) {
        //System.out.println(label);
        final List forumList = new ArrayList();
        if (label.trim().length() < 1) {
           // System.out.println(label+"无报告时");
            return forumList;
        } else {
            String la = SqlSplit.getSql(label);
            String sql = "select DISTINCT b.body_checkname from label_body a,body b where a.body_id = b.body_id and a.name in " + "(" + la + ")";
            dataetlDB.query(sql, new RowCallbackHandler() {
                //将结果集中的数据映射到List中
                public void processRow(ResultSet rs) throws SQLException {
                    forumList.add(rs.getString(1));
                }
            });
            return forumList;
        }
    }
    /**
     * 根据infoId获取Title、image
     */
    public List<HealthyClub> getTitleAndImageByInfoId(String infoId) {
        // String str2 = StringUtils.join(infoId, ",");
        // Map<String, Map<String, String>> map = null;
        List<HealthyClub> list = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            String sql = "select information_id, title,image from article4 where information_id in ("+infoId+")";
            System.out.println("1："+sql);

            list =dataetlDB.query(sql,
                    (resultSet, i) -> {
                        HealthyClub healthyClub = new HealthyClub(

                                resultSet.getInt("information_id"),
                                resultSet.getString("title"),
                                resultSet.getString("image")
                        );
                        System.out.println("2："+sql);
                        return healthyClub;
                    }
            );
        } catch (Exception ex) {
            System.out.println(ex);
            logger.error("getOpsMallOrder error", ex);
        }
        return list;
    }

}
