package com.haozhuo.rcmd.service;

import com.haozhuo.rcmd.common.EsUtils;
import com.haozhuo.rcmd.common.Utils;
import com.haozhuo.rcmd.model.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

@Component
public class EsService {
    private static final Logger logger = LoggerFactory.getLogger(EsService.class);
    @Value("${app.es.video-index}")
    private String videosInfoIndex;

    @Value("${app.es.live-index}")
    private String livesInfoIndex;

    @Value("${app.es.goods-index}")
    private String goodsIndex;

    @Value("${app.es.article-index}")
    private String articleIndex;

    @Value("${app.es.reportlabel-index}")
    private String reportlabelIndex;

    @Autowired
    private TransportClient client;

    private String[] getVideoIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(videosInfoIndex, condition, excludeIds, from, size);
    }

    private String[] getGoodsIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(goodsIndex, condition, excludeIds, from, size);
    }

    private String[] getLiveIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(livesInfoIndex, condition, excludeIds, from, size);
    }

    private String[] getArticleIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(articleIndex, condition, excludeIds, from, size);
    }


    private String[] getIndexByCondition(String index, QueryBuilder condition, String[] excludeIds, int from, int size) {
        logger.debug("es index:{}", index);
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setFrom(from)
                .setSize(size)
                .setQuery(QueryBuilders.boolQuery()
                        .must(condition)
                        .mustNot(QueryBuilders.idsQuery().addIds(excludeIds)));
        return EsUtils.getDocIdsAsArray(srb);
    }

    /**
     * @param title
     * @param size
     * @return
     */
    public String[] getVideoIdsByTitle(String title, int size) {
        return getVideoIdsByCondition(QueryBuilders.matchQuery("title", title), new String[]{}, 0, size);
    }


    /**
     * curl -XGET 'datanode153:9200/videos_info/_search?pretty' -d '{"size":10,"query":{"bool":{"must":{"multi_match":
     * {"query":"大叔","fields":["keywords","labels","title"]}},"must_not":[{"ids":{"values":["295"]}}]}}}'
     *
     * @param labels
     * @param excludeIds
     * @param size
     * @return
     */
    public String[] getVideoIdsByLabel(String labels, String[] excludeIds, int size) {
        String[] ids = getVideoIdsByCondition(QueryBuilders.multiMatchQuery(labels, "keywords", "labels", "title"), excludeIds, 0, size);
        logger.debug("labels:{}, video ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getVideoIdsByLabel(String labels, int size) {
        return getVideoIdsByLabel(labels, new String[]{}, size);
    }

    public String[] getLivesIdsByLabel(String labels, String[] excludeIds, int size) {
        String[] ids = getLiveIdsByCondition(QueryBuilders.multiMatchQuery(labels, "keywords", "labels", "title"), excludeIds, 0, size);
        logger.debug("labels:{}, live ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getLivesIdsByLabel(String labels, int size) {
        return getLivesIdsByLabel(labels, new String[]{}, size);
    }

    public String[] getArticleIdsByLabel(String labels, String[] excludeIds, int size) {
        String[] ids = getArticleIdsByCondition(QueryBuilders.multiMatchQuery(labels, "keywords", "labels", "title"), excludeIds, 0, size);
        logger.debug("labels:{}, live ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getArticleIdsByLabel(String labels, int size) {
        return getArticleIdsByLabel(labels, new String[]{}, size);
    }

    public List<ArticleContent> getArticleContentByLabels(String labels, int size, Map<Integer, String> categoryIdNameMap) {
        QueryBuilder qb = QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("title", labels).slop(0).analyzer("ik_smart").boost(0.80f))
                .should(QueryBuilders.matchPhraseQuery("abstracts", labels).slop(0).analyzer("ik_smart").boost(0.15f))
                .should(QueryBuilders.matchPhraseQuery("content", labels).slop(0).analyzer("ik_smart").boost(0.05f));
        SearchHit[] hits = client.prepareSearch(articleIndex).setQuery(qb).setSize(size).execute().actionGet().getHits().getHits();
        List<ArticleContent> result = new ArrayList<>(hits.length);
        for (SearchHit hit : hits) {
            result.add(new ArticleContent(
                    hit.getId(),
                    hit.getSource().get("title").toString(),
                    hit.getSource().get("abstracts").toString(),
                    hit.getSource().get("content").toString(),
                    hit.getSource().get("crawler_time").toString(),
                    new Double(hit.getScore()),
                    categoryIdNameMap.get(Integer.parseInt(hit.getType()))));
        }
        return result;
    }

    public String[] getGoodsIdsByLabels(String labels, String[] excludeIds, int size) {
        String[] ids = getGoodsIdsByCondition(QueryBuilders.matchQuery("label", labels), excludeIds, 0, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getGoodsIdsByLabels(String labels, int from, int size) {
        String[] ids = getGoodsIdsByCondition(QueryBuilders.matchQuery("label", labels), new String[]{}, from, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getSimilarVideoIds(String videoId, String label, int size) {
        //去掉这些对匹配结果有负面影响的词
        String replacedLabel = Utils.removeStopWords(label);
        logger.debug("replacedLabel:{}", replacedLabel);
        return getVideoIdsByCondition(QueryBuilders.matchQuery("labels", replacedLabel), new String[]{videoId}, 0, size);
    }

    /**
     * curl -XGET 'es1:9200/reportlabel/_search?pretty' -d '{"size":1,"query": {"match": {"healthReportId": "4049171"}}}'
     *
     * @return
     */
    public String getLabelsByReportId(String reportId) {
        SearchRequestBuilder srb = client.prepareSearch(reportlabelIndex).setSize(1)
                .setQuery(QueryBuilders.matchQuery("healthReportId", reportId.trim()));
        logger.debug(srb.toString());
        SearchHit[] searchHits = srb.execute().actionGet().getHits().getHits();
        String result = "";
        if (searchHits.length == 1) {
            Object obj = searchHits[0].getSource().get("label");
            if (obj != null) {
                result = obj.toString();
            }
        }
        return result;
    }

    private QueryBuilder getLiveOrVideoBasicBuilder(AbnormalParam param) {
        return QueryBuilders.boolQuery().should(QueryBuilders.multiMatchQuery(param.getExceptionItemName(), "title", "labels", "keywords").boost(80))
                .should(QueryBuilders.multiMatchQuery(param.getExceptionItemAlias(), "title", "labels", "keywords").boost(20))
                .should(QueryBuilders.multiMatchQuery(param.getPossibleDiseases(), "title", "labels", "keywords").boost(40))
                .should(QueryBuilders.multiMatchQuery(param.getPossibleDiseaseAlias(), "title", "labels", "keywords").boost(10))
                .should(QueryBuilders.multiMatchQuery(param.getPossibleSymptoms(), "title", "labels", "keywords").boost(5))
                .should(QueryBuilders.multiMatchQuery(param.getPossibleSymptomAlias(), "title", "labels", "keywords"));
    }

    public List<String> getLiveIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(livesInfoIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param)).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }

    public List<String> getVideoIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(videosInfoIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param)).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }


    public String[] getArticleIdsByAbnormStr(String abnormialStr, String abnormialAliasStr, int size, boolean isComplement) {
        SearchRequestBuilder srb = client.prepareSearch(articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.multiMatchQuery(abnormialStr, "title", "keywords", "labels").boost(100)) // abnormialStr的优先级高于abnormialAliasStr
                        .should(QueryBuilders.multiMatchQuery(abnormialAliasStr, "title", "keywords", "labels").boost(20))
        ).setSize(size);
        String[] result = EsUtils.getDocIdsAsArray(srb);
        logger.debug("getArticleIdsByAbnormStr compSize:{}", size - result.length);
        //如果不够数 就随机地补充几个
        if (size > result.length && isComplement) {
            String[] randomArticleIds = getArticleIdsRandomly(result, size - result.length);
            result = Stream.concat(Arrays.stream(result), Arrays.stream(randomArticleIds))
                    .toArray(String[]::new);
        }
        return result;
    }

    private String[] getArticleIdsRandomly(String[] excludeIds, int size) {
        return getArticleIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, 0, size);
    }

    public boolean isExistKeyword(String keyword) {
        SearchRequestBuilder srb = client.prepareSearch(videosInfoIndex, livesInfoIndex, articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("title", keyword))
                        .should(QueryBuilders.matchPhraseQuery("labels", keyword))
                        .should(QueryBuilders.matchPhraseQuery("keywords", keyword))).setSize(1);
        boolean isExist = true;
        if(EsUtils.getDocIdsAsList(srb).size() == 0) {
            SearchRequestBuilder srb2 = client.prepareSearch(articleIndex).setQuery(
                    QueryBuilders.matchPhraseQuery("title", keyword)).setSize(1);
            isExist = EsUtils.getDocIdsAsList(srb2).size() == 1;
        }
        return isExist;
    }

    public void updateVideo(VideoInfo videoInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("keywords", videoInfo.getKeywords());
        map.put("two_level_tag", videoInfo.getTwoLevelTag());
        map.put("one_level_tag", videoInfo.getOneLevelTag());
        map.put("basic_tags", videoInfo.getBasicTags());
        map.put("title", videoInfo.getTitle());
        map.put("category", videoInfo.getCategory());
        map.put("labels", videoInfo.getLabels());
        map.put("label_ids", videoInfo.getLabelIds());
        client.prepareIndex(videosInfoIndex, "docs", videoInfo.getId().toString()).setSource(map).get();
    }

    public void deleteVideo(String id) {
        client.prepareDelete(videosInfoIndex, "docs", id).get();
    }

    public void updateLive(LiveInfo liveInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("keywords", liveInfo.getKeywords());
        map.put("two_level_tag", liveInfo.getTwoLevelTag());
        map.put("one_level_tag", liveInfo.getOneLevelTag());
        map.put("basic_tags", liveInfo.getBasicTags());
        map.put("title", liveInfo.getTitle());
        map.put("category", liveInfo.getCategory());
        map.put("labels", liveInfo.getLabels());
        map.put("label_ids", liveInfo.getLabelIds());
        map.put("is_pay", liveInfo.getIsPay());
        map.put("play_time", liveInfo.getPlayTime());
        client.prepareIndex(livesInfoIndex, "docs", liveInfo.getId().toString()).setSource(map).get();
    }

    public void deleteLive(String id) {
        client.prepareDelete(livesInfoIndex, "docs", id).get();
    }

    public void updateGoods(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("content_name", goods.getContentName());
        map.put("second_class", goods.getSecondClass());
        map.put("display_label", goods.getDisplayLabel());
        map.put("first_label", goods.getFirstLabel());
        map.put("label", goods.getLabel());
        map.put("first_class", goods.getFirstClass());
        map.put("basic_label", goods.getBasicLabel());
        map.put("second_label", goods.getSecondLabel());
        client.prepareIndex(goodsIndex, "docs", goods.getContentId().toString()).setSource(map).get();
    }

    public void deleteGoods(String id) {
        client.prepareDelete(goodsIndex, "docs", id).get();
    }


}


//-------------------------------------------------- 以下接口暂时无用 -----------------------------------------------
//
//    /**
//     * 旧接口从video这个索引中匹配数据，但是video这个索引已经不维护了。改写后的从video_info匹配数据，不考虑年龄和性别
//     * 先从 getVideoIdsByLabel()方法获取数据，如果查到的结果少于特定条数，再使用getVideoIdsRandomly()补充：
//     *
//     * @param labels
//     * @param excludeIds
//     * @param size
//     * @return
//     */
//    public String[] getFixedSizeVideoIdsByLabel(String labels, String[] excludeIds, int size) {
//        String[] resultByLabels = getVideoIdsByLabel(labels, excludeIds, size);
//        if (resultByLabels.length < size) {
//            String[] newExcludeIds = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(excludeIds))
//                    .toArray(String[]::new);
//            String[] randomVideoIds = getVideoIdsRandomly(newExcludeIds, size - resultByLabels.length);
//            resultByLabels = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(randomVideoIds))
//                    .toArray(String[]::new);
//        }
//        return resultByLabels;
//    }
//
//    public String[] getFixedSizeGoodsIdsByLabel(String labels, String[] excludeIds, int size) {
//        String[] resultByLabels = getGoodsIdsByLabels(labels, excludeIds, size);
//        if (resultByLabels.length < size) {
//            String[] newExcludeIds = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(excludeIds))
//                    .toArray(String[]::new);
//            String[] randomVideoIds = getGoodsIdsRandomly(newExcludeIds, size - resultByLabels.length);
//            resultByLabels = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(randomVideoIds))
//                    .toArray(String[]::new);
//        }
//        return resultByLabels;
//    }
//
//
//    /**
//     * curl -XGET 'datanode153:9200/videos_info/_search?pretty' -d ' {"size":10,"query":{"bool":{"must":{"match_all":{}},
//     * "must_not":[{"ids":{"values":["300","304","305","291","287","302","296"]}}]}}}'
//     *
//     * @param excludeIds
//     * @param size
//     * @return
//     */
//    private String[] getVideoIdsRandomly(String[] excludeIds, int size) {
//        return getVideoIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, 0, size);
//    }
//
//    private String[] getGoodsIdsRandomly(String[] excludeIds, int size) {
//        return getGoodsIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, 0, size);
//    }



