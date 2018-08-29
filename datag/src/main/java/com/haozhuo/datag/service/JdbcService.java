package com.haozhuo.datag.service;

import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.model.Article;
import com.haozhuo.datag.model.Channel;
import com.haozhuo.datag.model.LiveInfo;
import com.haozhuo.datag.model.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Lucius on 8/16/18.
 */
@Component
public class JdbcService {
    private static final Logger logger = LoggerFactory.getLogger(JdbcService.class);

    public final Map<Integer, Integer> categoryIdCountMap;
    public final Map<String, String> labelIdNameMap = new HashMap<>();
    public final Map<String, String> labelNameIdMap = new HashMap<>();
    private final String liveTable;
    private final String videoTable;
    private final String articleTable;
    Random rand = new Random();

    @Qualifier("dataetlJdbc") //选择jdbc连接池
    private final JdbcTemplate dataetlDB;

    @Autowired
    public JdbcService(JdbcTemplate jdbcTemplate, Environment env) {
        logger.info("init JdbcService .................");
        this.dataetlDB = jdbcTemplate;
        liveTable = env.getProperty("app.mysql.live");
        videoTable = env.getProperty("app.mysql.video");
        articleTable = env.getProperty("app.mysql.article");
        this.categoryIdCountMap = getCategoryIdCountMap();
        initLabelMap();
        logger.debug("labelIdNameMap:{}", labelIdNameMap);
        logger.debug("labelNameIdMap:{}", labelNameIdMap);
    }

    public List<String> getRandomInfos(int categoryId, int pageSize, int getNumber) {

        int count = categoryIdCountMap.get(categoryId);
        int limitBegin = 0;
        if (count > pageSize) {
            limitBegin = rand.nextInt(-pageSize);
        }
        return dataetlDB.query(String.format("select x.information_id from %s x where x.status = 1 and x.category_id = ? limit ?, ?", articleTable), new Object[]{categoryId, limitBegin, getNumber}, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("information_id");
            }
        });
    }

    /**
     * 获取频道以及对应的文章数的映射关系
     *
     * @return
     */

    public Map<Integer, Integer> getCategoryIdCountMap() {
        //查询每个频道有多少文章
        Map<Integer, Integer> resultMap = new HashMap<>();

        String sql = String.format("select x.category_id, count(1) as num from %s x group by x.category_id ", articleTable);

        List<Tuple<Integer, Integer>> list = dataetlDB.query(sql, new RowMapper<Tuple<Integer, Integer>>() {
            @Override
            public Tuple<Integer, Integer> mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Tuple<Integer, Integer>(resultSet.getInt("category_id"), resultSet.getInt("num"));
            }
        });

        int totalSize = 0;
        for (Tuple<Integer, Integer> tuple : list) {
            resultMap.put(tuple.getT1(), tuple.getT2());
            totalSize += tuple.getT2();
        }
        resultMap.put(1, totalSize); //推荐的id为1
        logger.info("getCategoryIdCountMap:{}", resultMap.toString());
        return resultMap;
    }


    private void initLabelMap() {
        //查询每个频道有多少文章
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

    /**
     * 根据userId，获取动态的labelIds
     *
     * @param userId
     * @return
     */
    private List<String> getDynamicLabelIdList(String userId) {
        String dynamicLabelSql = String.format("select distinct label_id from dynamic_userid_label x where x.user_id = ? and x.update_time > ?");
        return dataetlDB.query(dynamicLabelSql, new Object[]{userId, JavaUtils.getNdaysAgo(30)}, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("label_id");
            }
        });
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
        return StringUtils.collectionToDelimitedString(getLabelSetByUserId(userId), ",");
    }

    /**
     * 根据userId获取用户的labelNames
     *
     * @param userId
     * @return
     */
    public Set<String> getLabelSetByUserId(String userId) {
        Set<String> labelIdSet = new HashSet<>();
        labelIdSet.addAll(getDynamicLabelIdList(userId));
        labelIdSet.addAll(getReportLabelIdList(userId));
        Set<String> labelNameSet = new HashSet<>();
        for (String labelId : labelIdSet) {
            if (labelIdNameMap.containsKey(labelId)) {
                labelNameSet.add(labelIdNameMap.get(labelId));
            }
        }
        return labelNameSet;
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

    @Deprecated
    public String getLabelsByInfoId(String infoId) {
        StringBuffer result = new StringBuffer();
        String labelsIds = "";
        try {
            //当数据库中返回的数据为0条时，即查找不到这个用户时，这里会报错
            labelsIds = dataetlDB.queryForObject("select x.disease_label_ids from  article x where x.information_id =?", new Object[]{infoId}, new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet resultSet, int i) throws SQLException {
                    return resultSet.getString("disease_label_ids");
                }
            });
        } catch (Exception ex) {
            logger.debug("getTagsByInfoId error", ex);
        }
        for (String labelId : labelsIds.split(",")) {
            if (labelIdNameMap.containsKey(labelId)) {
                result.append(labelIdNameMap.get(labelId));
            }
        }
        return result.toString();
    }

    public String getLabelIdsByNames(String labelNames) {
        List<String> list = new ArrayList<>();
        for (String labelName : labelNames.split(",")) {
            if (this.labelNameIdMap.containsKey(labelName)) {
                list.add(this.labelNameIdMap.get(labelName));
            }
        }
        return StringUtils.collectionToCommaDelimitedString(list);
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


    public void updateChannel(Channel channel) {
        String query = "INSERT INTO `channel` (`channel_id`, `name`, `sort_num`, `parent_id`, `create_time`, `create_operator`," +
                " `update_operator`, `update_time`) VALUES(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `sort_num` = ?," +
                " `parent_id` = ?, `create_time` = ?, `create_operator` = ?, `update_operator` = ?, `update_time` = ?";
        dataetlDB.update(query, channel.getChannelId(), channel.getName(), channel.getSortNum(), channel.getParentId(), channel.getCreateTime(),
                channel.getCreateOperator(), channel.getUpdateOperator(), channel.getUpdateTime(), channel.getName(), channel.getSortNum(),
                channel.getParentId(), channel.getCreateTime(), channel.getCreateOperator(), channel.getUpdateOperator(), channel.getUpdateTime());
    }

    public void updateLive(LiveInfo liveInfo) {
        String query = String.format("INSERT INTO `%s` (`id`, `title`, `one_level_tag`, `two_level_tag`, `labels`, " +
                " `label_ids`, `keywords`, `basic_tags`, `category`,`is_pay`,`play_time`) " +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE `title` = ?, `one_level_tag` = ?, " +
                " `two_level_tag` = ?, `labels` = ?, `label_ids` = ?, `keywords` = ?, `basic_tags` = ?, `category` = ?,`is_pay` = ?, `play_time` = ?", liveTable);
        dataetlDB.update(query, liveInfo.getId(), liveInfo.getTitle(), liveInfo.getOneLevelTag(), liveInfo.getTwoLevelTag(),
                liveInfo.getLabels(), liveInfo.getLabelIds(), liveInfo.getKeywords(), liveInfo.getBasicTags(), liveInfo.getCategory(),
                liveInfo.getIsPay(), liveInfo.getPlayTime(), liveInfo.getTitle(), liveInfo.getOneLevelTag(), liveInfo.getTwoLevelTag(), liveInfo.getLabels(),
                liveInfo.getLabelIds(), liveInfo.getKeywords(), liveInfo.getBasicTags(), liveInfo.getCategory(), liveInfo.getIsPay(), liveInfo.getPlayTime());
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
    }

    public void updatePermitUsers(long healthReportId, int flag) {
        String date = JavaUtils.getCurrent();
        dataetlDB.update("INSERT INTO `permit_users` (`date`,`health_report_id`,`flag`) VALUES (?, ?, ?) on duplicate key update date=?", date, healthReportId, flag, date);

    }
}
