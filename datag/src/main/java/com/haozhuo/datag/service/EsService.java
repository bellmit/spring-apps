package com.haozhuo.datag.service;

import com.haozhuo.datag.common.EsUtils;
import com.haozhuo.datag.common.Utils;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.service.biz.InfoRcmdService;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.GaussDecayFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@Component
public class EsService {
    private static final Logger logger = LoggerFactory.getLogger(EsService.class);
    @Value("${app.es.video-index}")
    private String videoIndex;

    @Value("${app.es.live-index}")
    private String liveIndex;

    @Value("${app.es.goods-index}")
    private String goodsIndex;

    @Value("${app.es.article-index}")
    private String articleIndex;

    @Value("${app.es.heat-article-index:heat_article}")
    private String heatArticleIndex;

    @Value("${app.es.heat-article-index:heat_video}")
    private String heatVideoIndex;

    private final int channelVideoId = Integer.parseInt(InfoRcmdService.channelVideoId);

    public String getVideoIndex() {
        return videoIndex;
    }

    public String getLiveIndex() {
        return liveIndex;
    }

    public String getGoodsIndex() {
        return goodsIndex;
    }

    public String getArticleIndex() {
        return articleIndex;
    }

    public String getHeatArticleIndex() {
        return heatArticleIndex;
    }

    public String getHeatVideoIndex() {
        return heatVideoIndex;
    }

    @Value("${app.es.reportlabel-index}")
    private String reportlabelIndex;

    @Autowired
    private TransportClient client;
    private GaussDecayFunctionBuilder createTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("create_time", "now", "30d", "0d", 0.8D);
    private GaussDecayFunctionBuilder playTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("play_time", "now", "30d", "0d", 0.8D);
    private GaussDecayFunctionBuilder heatGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("heat", "3000", "1000", "0", 0.8D);
    private GaussDecayFunctionBuilder isPayGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("is_pay", "1", "1", "0", 0.8D);

    private String[] recommend(String index, QueryBuilder query, int size, String... types) {
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setSize(size).setQuery(
                        new FunctionScoreQueryBuilder(query, getTimeGaussFunction(index))
                );
        if (types != null && types.length > 0) {
            srb.setTypes(types);
        }
        // logger.debug(srb.toString());
        return EsUtils.getDocIdsAsArray(srb);
    }

    /**
     * curl -XGET "192.168.1.152:9200/article4/_search?pretty" -d '{"size":10,"query":{"function_score":{"query":{"bool":{"should":[{"multi_match":{"query":"风湿关节炎食疗方剂","fields":["title","tags"],"boost":3}},{"multi_match":{"query":"肺炎近视","fields":["title","tags"],"boost":1}}],"must_not":[{"match":{"tags":"近视"}},{"ids":{"values":["131025","131574","131808"]}}]}},"functions":[{"gauss":{"create_time":{"origin":"now","scale":"30d","offset":"15d","decay":"0.8"}}}]}}}'
     */
    public String[] personalizedRecommend(String index, String tags, String hateTags, String[] pushedIds, int size, String... types) {
        String tagField = getTagField(index);
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(hateTags)))
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds))
                .should(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(tags)).boost(3));

        return recommend(index, query, size, types);
    }


    private String getTagField(String index) {
        if (videoIndex.equals(index) || articleIndex.equals(index)) {
            return "tags";
        } else {
            return "labels";
        }
    }

    private GaussDecayFunctionBuilder getTimeGaussFunction(String index) {
        if (liveIndex.equals(index)) {
            return playTimeGaussDecayFunction;
        } else {
            return createTimeGaussDecayFunction;
        }
    }


    private GaussDecayFunctionBuilder getHeatGaussFunction(String index) {
        if (liveIndex.equals(index)) {
            return isPayGaussDecayFunction;
        } else {
            return heatGaussDecayFunction;
        }
    }

    public String[] commonRecommend(String index, String hateTags, String[] pushedIds, int size, String... types) {
        String tagField = getTagField(index);
        //logger.debug(StringUtils.arrayToCommaDelimitedString(types));
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(hateTags)))
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds));
        return recommend(index, query, size, types);
    }


    public String[] heatRecommend(String index, int pageNo, int size, String... types) {
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setSize(size)
                .setFrom((pageNo - 1) * size);
        if (types != null && types.length > 0) {
            srb.setTypes(types);
        }

        srb.setQuery(
                new FunctionScoreQueryBuilder(
                        new FunctionScoreQueryBuilder(
                                QueryBuilders.matchAllQuery(),
                                getTimeGaussFunction(index)
                        ),
                        getHeatGaussFunction(index)
                )
        );
        logger.debug(srb.toString());
        return EsUtils.getDocIdsAsArray(srb);
    }

    private String[] getVideoIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(videoIndex, condition, excludeIds, from, size);
    }

    private String[] getGoodsIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(goodsIndex, condition, excludeIds, from, size);
    }

    private String[] getLiveIdsByCondition(QueryBuilder condition, String[] excludeIds, int from, int size) {
        return getIndexByCondition(liveIndex, condition, excludeIds, from, size);
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
     * 如果fieldNames不输入，默认设为 "tags"
     */
    public String[] getVideoIds(String text, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getVideoIdsByCondition(QueryBuilders.multiMatchQuery(text, fieldNames), excludeIds, 0, size);
        logger.debug("labels:{}, video ids:{}", text, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    /**
     * 如果fieldNames不输入，默认设为"tags"
     *
     * @param labels
     * @param size
     * @param fieldNames
     * @return
     */
    public String[] getVideoIds(String labels, int size, String... fieldNames) {
        return getVideoIds(labels, new String[]{}, size, fieldNames);
    }

    public String[] getLivesIds(String labels, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"keywords", "labels"};
        }
        String[] ids = getLiveIdsByCondition(QueryBuilders.multiMatchQuery(labels, fieldNames), excludeIds, 0, size);
        logger.debug("labels:{}, live ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getLivesIds(String labels, int size, String... fieldNames) {
        return getLivesIds(labels, new String[]{}, size, fieldNames);
    }


    public String[] getArticleIds(String text, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getArticleIdsByCondition(QueryBuilders.multiMatchQuery(text, fieldNames), excludeIds, 0, size);
        logger.debug("text:{}, article ids:{}", text, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getArticleIds(String labels, int size, String... fieldNames) {
        return getArticleIds(labels, new String[]{}, size, fieldNames);
    }

    public String[] getGoodsIdsByLabels(String labels, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"label"};
        }
        String[] ids = getGoodsIdsByCondition(QueryBuilders.multiMatchQuery(labels, fieldNames), excludeIds, 0, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getGoodsIdsByLabels(String labels, int from, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"label"};
        }
        String[] ids = getGoodsIdsByCondition(QueryBuilders.multiMatchQuery(labels, fieldNames), new String[]{}, from, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getSimilarVideoIdsByTags(String videoId, String tags, int size) {
        //去掉这些对匹配结果有负面影响的词
        String replacedTags = Utils.removeStopWords(tags);
        logger.debug("replacedLabel:{}", replacedTags);
        return getVideoIdsByCondition(QueryBuilders.matchQuery("tags", replacedTags), new String[]{videoId}, 0, size);
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
        return stream(searchHits).map(x -> x.getSource().get("label")).findFirst().orElse("").toString();

    }

    private QueryBuilder getLiveOrVideoBasicBuilder(AbnormalParam param, String... fieldNames) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (param.getExceptionItemName() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getExceptionItemName(), fieldNames).boost(80));
        if (param.getExceptionItemAlias() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getExceptionItemAlias(), fieldNames).boost(60));
        if (param.getPossibleDiseases() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getPossibleDiseases(), fieldNames).boost(40));
        if (param.getPossibleDiseaseAlias() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getPossibleDiseaseAlias(), fieldNames).boost(10));
        if (param.getPossibleSymptoms() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getPossibleSymptoms(), fieldNames).boost(5));
        if (param.getPossibleSymptomAlias() != null)
            builder.should(QueryBuilders.multiMatchQuery(param.getPossibleSymptomAlias(), fieldNames));
        return builder;
    }

    public List<String> getLiveIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(liveIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param, "labels", "keywords")).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }

    public List<String> getVideoIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(videoIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param, "tags")).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }

    public List<String> getArticleIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(articleIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param, "tags")).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }

    public String[] getArticleIdsByAbnormStr(String abnormialStr, String abnormialAliasStr, int size, boolean isComplement) {
        SearchRequestBuilder srb = client.prepareSearch(articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.multiMatchQuery(abnormialStr, "tags").boost(100)) // abnormialStr的优先级高于abnormialAliasStr
                        .should(QueryBuilders.multiMatchQuery(abnormialAliasStr, "tags").boost(20))
        ).setSize(size);
        String[] result = EsUtils.getDocIdsAsArray(srb);
        logger.debug("getArticleIdsByAbnormStr compSize:{}", size - result.length);
        //如果不够数 就随机地补充几个
        if (size > result.length && isComplement) {
            String[] randomArticleIds = getArticleIdsRandomly(result, size - result.length);
            result = Stream.concat(stream(result), stream(randomArticleIds))
                    .toArray(String[]::new);
        }
        return result;
    }

    private String[] getArticleIdsRandomly(String[] excludeIds, int size) {
        return getArticleIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, 0, size);
    }

    public boolean isExistKeyword(String keyword) {
        SearchRequestBuilder srb = client.prepareSearch(videoIndex, articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("title", keyword))
                        .should(QueryBuilders.matchPhraseQuery("tags", keyword))).setSize(1);
        boolean isExist = true;
        if (EsUtils.getDocIdsAsList(srb).size() == 0) {
            SearchRequestBuilder srb2 = client.prepareSearch(liveIndex).setQuery(
                    QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchPhraseQuery("title", keyword))
                            .should(QueryBuilders.matchPhraseQuery("labels", keyword))
                            .should(QueryBuilders.matchPhraseQuery("keywords", keyword))).setSize(1);
            isExist = EsUtils.getDocIdsAsList(srb2).size() == 1;
        }
        return isExist;
    }

    public void updateArticle(Article article) {
        deleteIdByQuery(articleIndex, String.valueOf(String.valueOf(article.getInformationId())));
        Map<String, Object> map = new HashMap<>();
        map.put("tags", article.getTags());
        map.put("title", article.getTitle());
        map.put("content", article.getContent());
        map.put("create_time", article.getCreateTime());
        map.put("update_time", article.getUpdateTime());
        client.prepareIndex(articleIndex, article.getChannelId() + "_" + article.getCategoryId(), String.valueOf(article.getInformationId())).setSource(map).get();
    }

    public void updateVideo(Video video) {
        deleteIdByQuery(videoIndex, String.valueOf(String.valueOf(video.getId())));
        Map<String, Object> map = new HashMap<>();
        map.put("tags", video.getTags());
        map.put("title", video.getTitle());
        map.put("description", video.getDescription());
        map.put("create_time", video.getCreateTime());
        map.put("update_time", video.getUpdateTime());

        client.prepareIndex(videoIndex, video.getChannelId() + "_" + video.getCategoryId(), String.valueOf(video.getId())).setSource(map).get();
    }

    /**
     * 由于document的type未知。不能使用类似
     * curl -X DELETE "datanode153:9200/article4/_doc/1"的删除方式，
     * 所以采用以下删除方式：
     * curl -X POST "datanode153:9200/article4/_delete_by_query?" -H 'Content-Type: application/json' -d'{"query": {"ids" : {"values" : ["1"]}}}'
     *
     * @param id
     */
    public void deleteVideo(long id) {
        deleteIdByQuery(videoIndex, String.valueOf(id));
        deleteIdByQuery(heatVideoIndex, String.valueOf(id));
    }

    public void deleteArticle(long id) {
        deleteIdByQuery(articleIndex, String.valueOf(id));
        deleteIdByQuery(heatArticleIndex, String.valueOf(id));
    }

    private void deleteIdByQuery(String index, String id) {
        BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.idsQuery().addIds(String.valueOf(id)))
                .source(index).get();
        response.getDeleted();
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
        client.prepareIndex(liveIndex, "docs", liveInfo.getId().toString()).setSource(map).get();
    }

    public void deleteLive(long id) {
        client.prepareDelete(liveIndex, "docs", String.valueOf(id)).get();
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


    public void updateInfoHeat(InfoHeat infoHeat) {

        Map<String, Object> map = new HashMap<>();
        map.put("heat", infoHeat.getHeat());
        map.put("create_time", infoHeat.getCreateTime());
        String index = heatArticleIndex;
        if (infoHeat.getChannelId() == channelVideoId) {
            index = heatVideoIndex;
        }
        deleteIdByQuery(index, String.valueOf(infoHeat.getInfoId()));
        client.prepareIndex(index, infoHeat.getChannelId() + "_" + infoHeat.getCategoryId(), String.valueOf(infoHeat.getInfoId())).setSource(map).get();
    }

    public void deleteGoods(long id) {
        client.prepareDelete(goodsIndex, "docs", String.valueOf(id)).get();
    }

}






