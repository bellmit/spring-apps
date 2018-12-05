package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.model.textspilt.SimpleArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.haozhuo.datag.model.Goods.listToStr;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

/**
 * Created by Lucius on 8/16/18.
 */
@Component
public class DataetlJdbcService {
    private static final Logger logger = LoggerFactory.getLogger(DataetlJdbcService.class);

    public Map<String, String[]> channelEsTypeMap;
    public final Map<String, String> labelIdNameMap = new HashMap<>();
    public final Map<String, String> labelNameIdMap = new HashMap<>();
    private final String goodsTable;
    private final String liveTable;
    private final String videoTable;
    private final String articleTable;
    private final String articleTags;
    public final static List<String> stopwords = new ArrayList<>();

    @Qualifier("dataetlJdbc") //选择jdbc连接池
    private final JdbcTemplate dataetlDB;

    @Autowired
    public DataetlJdbcService(JdbcTemplate jdbcTemplate, Environment env) {
        this.dataetlDB = jdbcTemplate;
        liveTable = env.getProperty("app.mysql.live");
        videoTable = env.getProperty("app.mysql.video");
        articleTable = env.getProperty("app.mysql.article");
        goodsTable = env.getProperty("app.mysql.goods");
        articleTags = env.getProperty("app.biz.articleTags", "title,tags,keywords");

        updateChannelEsTypeMap();
        initLabelMap();
        initStopwords();
        logger.debug("labelIdNameMap:{}", labelIdNameMap);
        logger.debug("labelNameIdMap:{}", labelNameIdMap);
    }

    private void initLabelMap() {
        List<Tuple<String, String>> list = dataetlDB.query("select id, label from disease_label", new RowMapper<Tuple<String, String>>() {
            @Override
            public Tuple<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<String, String>(resultSet.getString("id"), resultSet.getString("label"));
            }
        });
        for (Tuple<String, String> tuple : list) {
            labelIdNameMap.put(tuple.getT1(), tuple.getT2());
            labelNameIdMap.put(tuple.getT2(), tuple.getT1());
        }
    }

    private void initStopwords() {
        List<String> list = dataetlDB.query("select name from stop_words;", new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("name");
            }
        });
        stopwords.addAll(list);
    }

    private void updateChannelEsTypeMap() {
        List<Tuple<String, String>> list = dataetlDB.query("select parent_id as channelId, channel_id as categoryId  from channel where parent_id >0 union \n" +
                "select  channel_id as channelId,parent_id as categoryId from channel where parent_id = 0", new RowMapper<Tuple<String, String>>() {
            @Override
            public Tuple<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<>(resultSet.getString("channelId"), resultSet.getString("categoryId"));
            }
        });
        channelEsTypeMap = list.stream().collect(groupingBy(Tuple::getT1, mapping(x -> x.getT1() + "_" + x.getT2(), toList())))
                .entrySet().stream().collect(toMap(x -> x.getKey(), x -> x.getValue().stream().toArray(String[]::new)));
        logger.info("updateChannelEsTypeMap:{}", channelEsTypeMap.entrySet().stream().map(x -> x.getKey() + "->" + stream(x.getValue()).collect(joining(","))).collect(joining(" | ")));

    }

    /**
     * 根据userId，获取最近一份报告的疾病labelIds
     *
     * @param userId
     * @return
     */
    private List<String> getReportLabelIdList(String userId) {
        String labelIds = "";
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            labelIds = dataetlDB.queryForObject(
                    "select label_ids from report_userid_label where user_id = ?",
                    new Object[]{userId},
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet resultSet, int i) throws SQLException {
                            return resultSet.getString("label_ids");
                        }
                    });
        } catch (Exception ex) {
            logger.info("report_userid_label中没有这个userId:{}", userId);
        }
        return Arrays.asList(labelIds.split(","));
    }


    /**
     * 根据userId获取用户的labelNames
     *
     * @param userId
     * @return
     */
    public String getLabelStrByUserId(String userId) {
        return getLabelSetByUserId(userId).stream().collect(joining(","));
    }

    /**
     * 根据userId获取用户的labelNames
     *
     * @param userId
     * @return
     */
    public Set<String> getLabelSetByUserId(String userId) {
        return getReportLabelIdList(userId).stream()
                .map(labelId -> this.labelIdNameMap.getOrDefault(labelId, null))
                .filter(x -> x != null)
                .collect(Collectors.toSet());
    }

    public Map<String, String> getPortraitMap(int type) {
        String sql;
        if (type == 1) {
            sql = "select id, name from portrait_tag union select id,label as name from disease_label";
        } else {
            sql = "select id,label as name from disease_label";
        }
        List<Tuple<String, String>> list = dataetlDB.query(sql, new RowMapper<Tuple<String, String>>() {
            @Override
            public Tuple<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<String, String>(resultSet.getString("name"), resultSet.getString("id"));
            }
        });
        Map<String, String> result = new HashMap<>();
        for (Tuple<String, String> tuple : list) {
            result.put(tuple.getT1(), tuple.getT2());

        }
        return result;
    }

    public String getInfoTagsById(Long infoId) {
        String tags = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            tags = dataetlDB.queryForObject(
                    String.format("select title, tags from %s x where x.information_id = ?", articleTable),
                    new Object[]{infoId},
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet resultSet, int i) throws SQLException {
                            // Optional.ofNullable() 表示传入的参数可能为null
                            // orElse() 表示如果传入的是null就赋予另一个值
                            return Optional.ofNullable(resultSet.getString("tags"))
                                    .orElse(resultSet.getString("title"));
                        }
                    });
        } catch (Exception ex) {
            logger.debug("getInfoTagsById error", ex);
        }
        return tags;
    }

    public List<Goods> getGoodsList(int from, int size) {
        return dataetlDB.query(
                String.format("select sku_id,goods_ids,name,description,category,sub_category,goods_tags,search_keywords,city_ids, rcmd_score,goods_type,sales_num,create_time from %s x limit ?, ?", goodsTable),
                new Object[]{from,size},
                new RowMapper<Goods>() {
                    @Override
                    public Goods mapRow(ResultSet resultSet, int i) throws SQLException {
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
                    }
                });
    }
    public List<String> getGoodsSkuIdsByLikeStr(String field, String str) {
        List<String> goodsIdList = null;
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            goodsIdList = dataetlDB.query(
                    String.format("select sku_id from %s x where x.%s like ?", goodsTable, field),
                    new Object[]{str},
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet resultSet, int i) throws SQLException {
                            // Optional.ofNullable() 表示传入的参数可能为null
                            // orElse() 表示如果传入的是null就赋予另一个值
                            return resultSet.getString("sku_id");
                        }
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
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet resultSet, int i) throws SQLException {
                            return resultSet.getString("tags");
                        }
                    });
        } catch (Exception ex) {
            logger.debug("getVideoTagsById error", ex);
            logger.info("video4中没有这个id:{}", videoId);
        }
        return labels;
    }

    /**
     * 根据infoId获取Tags
     */
    public String getTagsByInfoId(String infoId) {
        String tags = "";
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            String sql = String.format("select x.tags from  %s x where x.information_id =?", articleTable);
            logger.debug("sql:{}", sql);
            tags = dataetlDB.queryForObject(sql, new Object[]{infoId}, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getString("tags");
                }
            });
        } catch (Exception ex) {
            logger.debug("getTagsByInfoId error", ex);
        }
        return tags;
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
            tags = dataetlDB.queryForObject(sql, new Object[]{infoId}, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getString("tags");
                }
            });
        } catch (Exception ex) {
            logger.debug("getTagsByInfoId error", ex);
        }
        return tags;
    }

//    @Deprecated
//    public String getLabelsByInfoId(String infoId) {
//        String labelsIds = "";
//        try {
//            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
//            labelsIds = dataetlDB.queryForObject("select x.disease_label_ids from  article x where x.information_id =?", new Object[]{infoId}, new RowMapper<String>() {
//                @Override
//                public String mapRow(ResultSet resultSet, int i) throws SQLException {
//                    return resultSet.getString("disease_label_ids");
//                }
//            });
//        } catch (Exception ex) {
//            logger.debug("getTagsByInfoId error", ex);
//        }
//
//        return stream(labelsIds.split(","))
//                .map(labelName -> labelIdNameMap.getOrDefault(labelName, null))
//                .filter(x -> x != null)
//                .collect(Collectors.joining(","));
//
//    }

    public String getLabelIdsByNames(String labelNames) {
        return stream(labelNames.split(","))
                .map(labelName -> this.labelNameIdMap.getOrDefault(labelName, null))
                .filter(x -> x != null)
                .collect(Collectors.joining(","));
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
                " UPDATE `status` = ?, `title` = ?, `image` = ?, `images` = ?, `content` = ?, `channel_id` = ?, `category_id` = ?," +
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
        dataetlDB.update(query, goods.getSkuId(), goods.getGoodsName(), goods.getGoodsDescription(), goods.getGoodsDescription(),
                goods.getSubCategory(), goodsTags, goods.getSearchKeywords(), cityIds, goods.getRcmdScore(), goods.getCreateTime(),
                goodsIds, goods.getGoodsType(), goods.getSalesNum(),goods.getGoodsName(), goods.getGoodsDescription(),
                goods.getGoodsDescription(), goods.getSubCategory(), goodsTags, goods.getSearchKeywords(), cityIds,
                goods.getRcmdScore(), goods.getCreateTime(), goodsIds, goods.getGoodsType(),goods.getSalesNum());
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
}
