package com.haozhuo.datag.service;

import com.haozhuo.datag.common.EsUtils;
import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Tuple;
import com.haozhuo.datag.common.Utils;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.model.crm.UserIdTagsId;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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

    @Value("${app.es.portrait-index:portrait}")
    private String portraitIndex;

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

    @Value("${app.es.reportlabel-index}")
    private String reportlabelIndex;

    private String countryId = "000000";

    private double goodsRcmdScoreDecay;

    @Autowired
    private TransportClient client;
    private GaussDecayFunctionBuilder createTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("create_time", "now", "30d", "0d", 0.8D);
    private GaussDecayFunctionBuilder playTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("play_time", "now", "30d", "0d", 0.8D);
    private GaussDecayFunctionBuilder goodsRcmdScoreGaussDecayFunction;

    public EsService(Environment env) {
        this.goodsRcmdScoreDecay = Double.parseDouble(env.getProperty("app.biz.goodsRcmdScoreDecay", "0.9"));
        this.goodsRcmdScoreGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("rcmdScore", Goods.SCORE_MAX, 10, 0, goodsRcmdScoreDecay);
    }

    private String[] recommend(String index, QueryBuilder query, int size, String... types) {
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setSize(size).setQuery(
                        new FunctionScoreQueryBuilder(query, getTimeGaussFunction(index))
                );
        if (types != null && types.length > 0) {
            srb.setTypes(types);
        }
        return EsUtils.getDocIdsAsArray(srb);
    }

    /**
     * curl -XGET "192.168.1.152:9200/article4/_search?pretty" -d '{"size":10,"query":{"function_score":{"query":{"bool":{"should":[{"multi_match":{"query":"风湿关节炎食疗方剂","fields":["title","tags"],"boost":3}},{"multi_match":{"query":"肺炎近视","fields":["title","tags"],"boost":1}}],"must_not":[{"match":{"tags":"近视"}},{"ids":{"values":["131025","131574","131808"]}}]}},"functions":[{"gauss":{"create_time":{"origin":"now","scale":"30d","offset":"15d","decay":"0.8"}}}]}}}'
     */
    public String[] personalizedRecommend(String index, String tags, String hateTags, String[] pushedIds, int size, String... types) {
        String tagField = getTagField();
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(hateTags)))
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds))
                .should(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(tags)).boost(3));

        return recommend(index, query, size, types);
    }


    private String getTagField() {
        return "tags";
    }

    private GaussDecayFunctionBuilder getTimeGaussFunction(String index) {
        if (liveIndex.equals(index)) {
            return playTimeGaussDecayFunction;
        } else {
            return createTimeGaussDecayFunction;
        }
    }

    public String[] commonRecommend(String index, String hateTags, String[] pushedIds, int size, String... types) {
        String tagField = getTagField();
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.matchQuery(tagField, Utils.removeStopWords(hateTags)))
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds));
        return recommend(index, query, size, types);
    }

    public String[] commonRecommend(String index, String[] pushedIds, int size, String... types) {
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds));
        return recommend(index, query, size, types);
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
        logger.debug("tags:{}, video ids:{}", text, StringUtils.arrayToCommaDelimitedString(ids));
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

    public String[] getLivesIds(String tags, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getLiveIdsByCondition(QueryBuilders.multiMatchQuery(tags, fieldNames), excludeIds, 0, size);
        logger.debug("tags:{}, live ids:{}", tags, StringUtils.arrayToCommaDelimitedString(ids));
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

    @Deprecated
    public String[] getGoodsIdsByLabels(String labels, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"label"};
        }
        String[] ids = getGoodsIdsByCondition(QueryBuilders.multiMatchQuery(labels, fieldNames), excludeIds, 0, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    @Deprecated
    public String[] getGoodsIdsByLabels(String labels, int from, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"label"};
        }
        String[] ids = getGoodsIdsByCondition(QueryBuilders.multiMatchQuery(labels, fieldNames), new String[]{}, from, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    private QueryBuilder goodsSearchCityIdsQueryBuilder(String keywords,String cityId) {
        return new FunctionScoreQueryBuilder(QueryBuilders.boolQuery()
                .must(QueryBuilders.multiMatchQuery(keywords, defaultGoodsSearchFields))
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchQuery("cityIds", cityId))
                        .should(QueryBuilders.matchQuery("cityIds", countryId))), goodsRcmdScoreGaussDecayFunction);
    }

    private static final String [] defaultGoodsSearchFields = new String[]{"name", "category", "subCategory", "goodsTags", "thirdTags"};

    private SearchHit[] getGoodsIdsByKeywordsAndCityIdsHits(String keywords, String cityId, int from, int size) {
        SearchRequestBuilder srb = client.prepareSearch(goodsIndex)
                .setFrom(from)
                .setSize(size)
                .setQuery(goodsSearchCityIdsQueryBuilder(keywords, cityId)).setFetchSource(new String[]{"goodsIds", "rcmdScore"}, null);
        return srb.execute().actionGet().getHits().getHits();
    }

    public List<String> getBestMatchSkuIdsByKeywordsAndCityIds(String keywords, String cityId, int from, int size) {
        SearchRequestBuilder srb = client.prepareSearch(goodsIndex)
                .setSize(size)
                .setFrom(from)
                .setQuery(goodsSearchCityIdsQueryBuilder(keywords, cityId));
        System.out.println( srb);
        return EsUtils.getDocIdsAsList(srb);
    }

    public String getBestMatchGoodsIdByKeywordsAndCityIds(String keywords, String cityId, int from, int size) {
        List<Tuple> result = stream(getGoodsIdsByKeywordsAndCityIdsHits(keywords, cityId, from, size))
                .map(hit -> new Tuple(hit.getSource().get("goodsIds"), hit.getSource().get("rcmdScore"))).collect(toList());
        List<List<String>> goodsIdsOfHighestScore =
                result.stream().filter(x -> ((Integer) x.getT2()) == Goods.SCORE_MAX)
                        .map(x -> (List<String>) x.getT1()).collect(toList());
        if (goodsIdsOfHighestScore.size() == 0) {
            goodsIdsOfHighestScore = result.stream().map(x -> (List<String>) x.getT1()).collect(toList());
        }
        String goodsId = "";
        List<String> goodsIds = null;
        if (goodsIdsOfHighestScore.size() > 0) {
            Collections.shuffle(goodsIdsOfHighestScore);
            goodsIds = goodsIdsOfHighestScore.get(0);
            Collections.shuffle(goodsIds);
            //主键是skuID即文档ID，但是根据业务需求，这里不需要skuID，而是返回该skuID下众多goodsIds下的一个即可。所以取第一个。
            goodsId = goodsIds.get(0);
        }
        return goodsId;
    }

    public Goods getGoodsBySkuId(String skuId) {
        GetResponse response = client.prepareGet(goodsIndex, "_all", skuId).get();
        Goods goods = null;
        Map source = response.getSource();
        if (source != null) {
            goods = new Goods();
            goods.setSkuId(response.getId());
            goods.setGoodsType(Integer.parseInt(response.getType()));
            goods.setCategory((String) source.get("category"));
            goods.setCityIds((List<String>) source.get("cityIds"));
            goods.setGoodsIds((List<String>) source.get("goodsIds"));
            goods.setCreateTime((String) source.get("createTime"));
            goods.setGoodsDescription((String) source.get("description"));
            goods.setGoodsName((String) source.get("name"));
            goods.setGoodsTags((List<String>) source.get("goodsTags"));
            goods.setSubCategory((String) source.get("subCategory"));
            goods.setThirdTags((List<String>) source.get("thirdTags"));
            goods.setRcmdScore((Integer) source.get("rcmdScore"));
            goods.setSalesNum((Integer) source.get("salesNum"));
        }
        return goods;
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

    /**
     * lv03,jbg02|159|zc03,jbg11,61
     *
     * @param strTagIds
     * @return
     */
    private BoolQueryBuilder getQueryBuilderForPortrait(String strTagIds) {

        String fieldName = "tagIds";
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        for (String oneLevelTagIds : strTagIds.split("\\|")) {
            BoolQueryBuilder builder2 = QueryBuilders.boolQuery();
            for (String tagId : oneLevelTagIds.split(",")) {
                builder2.must(QueryBuilders.termQuery(fieldName, tagId));
            }
            builder.should(builder2);
        }
        return builder;
    }

    public List<String> getUserIdsByPortraitTagIds(String strTagIds, String searchAfterUid, int size) {
        SearchRequestBuilder srb = client.prepareSearch(portraitIndex)
                .setSize(size)
                .setQuery(getQueryBuilderForPortrait(strTagIds))
                .addSort("_uid", SortOrder.DESC);
        if (JavaUtils.isNotEmpty(searchAfterUid) && !"null".equals(searchAfterUid)) {
            srb.searchAfter(new String[]{"docs#" + searchAfterUid});
        }
        return EsUtils.getDocIdsAsList(srb);

    }

    public long getCountByPortraitTagIds(String strTagIds) {
        SearchRequestBuilder srb = client.prepareSearch(portraitIndex)
                .setSize(1)
                .setQuery(getQueryBuilderForPortrait(strTagIds));
        return srb.execute().actionGet().getHits().getTotalHits();
    }

    public List<UserIdTagsId> getPortraitByUserIdArray(String[] userIds) {
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add(portraitIndex, "docs", userIds)
                .get();

        List<UserIdTagsId> list = new ArrayList<>(userIds.length);
        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                UserIdTagsId userIdTagsId = new UserIdTagsId();
                userIdTagsId.setUserId(response.getId());
                userIdTagsId.setTagIds(response.getSource().get("tagIds"));
                list.add(userIdTagsId);
            }
        }
        return list;
    }

    public List<String> getDiseaseLabelListByUserId(String userIds) {
        GetResponse response = client.prepareGet(portraitIndex, "docs", userIds).get();
        Map<String, Object> source = response.getSource();
        List<String> diseaseLabelList;
        if (source == null) {
            diseaseLabelList = new ArrayList<>();
        } else {
            diseaseLabelList = (List<String>) response.getSource().getOrDefault("diseaseLabels", new ArrayList<String>());
        }
        return diseaseLabelList;
    }

    public String getDiseaseLabelsByUserId(String userIds) {
       return Utils.removeStopWords(getDiseaseLabelListByUserId(userIds).stream().collect(joining(",")));
    }

    public List<String> getLiveIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(liveIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param, "tags")).setSize(size);
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
        SearchRequestBuilder srb = client.prepareSearch(videoIndex, liveIndex, articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("title", keyword))
                        .should(QueryBuilders.matchPhraseQuery("tags", keyword))).setSize(1);
        return EsUtils.getDocIdsAsList(srb).size() > 0;
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
    }

    public void deleteArticle(long id) {
        deleteIdByQuery(articleIndex, String.valueOf(id));
    }

    private void deleteIdByQuery(String index, String id) {
        BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.idsQuery().addIds(String.valueOf(id)))
                .source(index).get();
        response.getDeleted();
    }

    public void updateLive(Live liveInfo) {
        deleteIdByQuery(liveIndex, String.valueOf(String.valueOf(liveInfo.getId())));
        Map<String, Object> map = new HashMap<>();
        map.put("tags", liveInfo.getTags());
        map.put("description", liveInfo.getDescription());
        map.put("title", liveInfo.getTitle());
        map.put("is_pay", liveInfo.getIsPay());
        map.put("play_time", liveInfo.getPlayTime());
        client.prepareIndex(liveIndex, liveInfo.getChannelId() + "_" + liveInfo.getCategoryId(), liveInfo.getId().toString()).setSource(map).get();
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

    public void deleteLive(long id) {
        deleteIdByQuery(liveIndex, String.valueOf(id));
    }

    public void updateGoods(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsIds", goods.getGoodsIds());
        map.put("name", goods.getGoodsName());
        map.put("category", goods.getCategory());
        map.put("subCategory", goods.getSubCategory());
        map.put("goodsTags", goods.getGoodsTags());
        map.put("thirdTags", goods.getThirdTags());
        map.put("description", goods.getGoodsDescription());
        map.put("cityIds", goods.getCityIds());
        map.put("rcmdScore", goods.getRcmdScore());
        map.put("createTime", goods.getCreateTime());
        map.put("salesNum", goods.getSalesNum());
        client.prepareIndex(goodsIndex, String.valueOf(goods.getGoodsType()), goods.getSkuId()).setSource(map).get();
    }

    public void deleteGoods(String skuId) {
        deleteIdByQuery(goodsIndex, String.valueOf(skuId));
    }

}






