package com.haozhuo.datag.service;

import com.haozhuo.datag.common.EsUtils;
import com.haozhuo.datag.common.JavaUtils;
import com.haozhuo.datag.common.Utils;
import com.haozhuo.datag.model.*;
import com.haozhuo.datag.model.crm.UserIdTagsId;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
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

    public String getArticleIndex() {
        return articleIndex;
    }

    private final Random random = new Random();

    @Value("${app.es.reportlabel-index}")
    private String reportLabelIndex;

    private final static String countryId = "000000";

    private final GoodsTypeProportion goodsTypeProportion = new GoodsTypeProportion();

    private final String[] goodsSearchFields;
    @Autowired
    private TransportClient client;
    private final GaussDecayFunctionBuilder createTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("create_time", "now", "30d", "0d", 0.8);
    private final GaussDecayFunctionBuilder playTimeGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("play_time", "now", "30d", "0d", 0.8D);
    private final GaussDecayFunctionBuilder goodsRcmdScoreGaussDecayFunction = ScoreFunctionBuilders.gaussDecayFunction("rcmdScore", Goods.SCORE_MAX, 10, 0, 0.9);
    private final ExponentialDecayFunctionBuilder goodsCreateTimeExpDecayFunction = ScoreFunctionBuilders.exponentialDecayFunction("createTime", "now", "180d", "0d", 0.8);
    private final ExponentialDecayFunctionBuilder goodsSaleNumExpDecayFunction = ScoreFunctionBuilders.exponentialDecayFunction("salesNum", 10000, 500, 9000, 0.5D);

    public EsService(Environment env) {
        goodsSearchFields = env.getProperty("app.es.goods-search-fields","name,goodsTags").split(",");
        logger.info("goodsSearchFields:{}", Arrays.asList(goodsSearchFields));
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
    public String[] personalizedRecommend(String index, String tags, String[] pushedIds, int size, String... types) {
        String tagField = "tags";
        QueryBuilder query = QueryBuilders.boolQuery()
                //.mustNot(matchQuery(tagField, Utils.removeStopWords(hateTags)))
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds))
                .should(matchQuery(tagField, Utils.removeStopWords(tags)).boost(3));

        return recommend(index, query, size, types);
    }

    private String[] getIndexByCondition(String index, QueryBuilder condition, String[] excludeIds, int size) {
        logger.debug("es index:{}", index);
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setFrom(0)
                .setSize(size)
                .setQuery(QueryBuilders.boolQuery()
                        .must(condition)
                        .mustNot(QueryBuilders.idsQuery().addIds(excludeIds)));
        return EsUtils.getDocIdsAsArray(srb);
    }

    private GaussDecayFunctionBuilder getTimeGaussFunction(String index) {
        if (liveIndex.equals(index)) {
            return playTimeGaussDecayFunction;
        } else {
            return createTimeGaussDecayFunction;
        }
    }


//    public String[] commonRecommend(String index, String[] pushedIds, int size, String... types) {
//        QueryBuilder query = QueryBuilders.boolQuery()
//                //.mustNot(matchQuery("tags", Utils.removeStopWords(hateTags)))
//                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds));
//        return recommend(index, query, size, types);
//    }

    public String[] commonRecommend(String index, String[] pushedIds, int size, String... types) {
        QueryBuilder query = QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.idsQuery().addIds(pushedIds));
        return recommend(index, query, size, types);
    }

    /**
     * curl -XGET 'es1:9200/reportlabel/_search?pretty' -d '{"size":1,"query": {"match": {"healthReportId": "4049171"}}}'
     *
     * @return
     */
    public String getLabelsByReportId(String reportId) {
        SearchRequestBuilder srb = client.prepareSearch(reportLabelIndex).setSize(1)
                .setQuery(matchQuery("healthReportId", reportId.trim()));
        logger.debug(srb.toString());
        SearchHit[] searchHits = srb.execute().actionGet().getHits().getHits();
        return stream(searchHits).map(x -> x.getSource().get("label")).findFirst().orElse("").toString();

    }

    private QueryBuilder getLiveOrVideoBasicBuilder(AbnormalParam param) {
        String fieldNames = "tags";
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

    public boolean isExistKeyword(String keyword) {
        SearchRequestBuilder srb = client.prepareSearch(videoIndex, liveIndex, articleIndex).setQuery(
                QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("title", keyword))
                        .should(QueryBuilders.matchPhraseQuery("tags", keyword))).setSize(1);
        return EsUtils.getDocIdsAsList(srb).size() > 0;
    }

    private void deleteIdByQuery(String index, String id) {
        BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.idsQuery().addIds(String.valueOf(id)))
                .source(index).get();
        response.getDeleted();
    }


    //------------------- portrait start -----------------------------

    /**
     * lv03,jbg02|159|zc03,jbg11,61
     */
    private BoolQueryBuilder getQueryBuilderForPortrait(String strTagIds) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        for (String oneLevelTagIds : strTagIds.split("\\|")) {
            BoolQueryBuilder builder2 = QueryBuilders.boolQuery();
            for (String tagId : oneLevelTagIds.split(",")) {
                builder2.must(QueryBuilders.termQuery("tagIds", tagId));
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

    private List<String> getPortraitDiseaseLabelListByUserId(String userIds) {
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

    public String getPortraitDiseaseLabelsByUserId(String userId) {
        return Utils.removeStopWords(String.join(",", getPortraitDiseaseLabelListByUserId(userId)));
    }

    //------------------- portrait end ----------------------------------


    //---------------------- goods start -----------------------------------

    public void updateGoods(Goods goods) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsIds", goods.getGoodsIds());
        map.put("name", goods.getGoodsName());
        map.put("category", goods.getCategory());
        map.put("subCategory", goods.getSubCategory());
        map.put("goodsTags", goods.getGoodsTags());
        map.put("searchKeywords", goods.getSearchKeywords());
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

    public String[] getGoodsIdsByKeywords(GoodsSearchParams params) {
        return getSkuIdGoodsIdsByLabels(params).stream().map(SkuIdGoodsIds::getRandomGoodsId).toArray(String[]::new);
    }

    private BoolQueryBuilder goodsSearchBuilder(GoodsSearchParams params) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (params.getCityId() != null) {
            boolQueryBuilder.must(
                    QueryBuilders.boolQuery()
                            .should(matchQuery("cityIds", params.getCityId()))
                            .should(matchQuery("cityIds", countryId))
            );
        }
        String[] excludeSkuIds = params.getExcludeSkuIds();
        if (excludeSkuIds != null && excludeSkuIds.length > 0) {
            boolQueryBuilder.mustNot(QueryBuilders.idsQuery().addIds(excludeSkuIds));
        }
        if (params.getKeywords() != null) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(params.getKeywords(), goodsSearchFields));
        }
        return boolQueryBuilder;
    }


    private void checkIfUpdateGoodsTypeCount() {
        if (goodsTypeProportion.needUpdate()) {
            goodsTypeProportion.setProportionGoodsType(goodsTypeCount());
            goodsTypeProportion.setUpdateGoodsTypeNum(System.currentTimeMillis());
            logger.info("UpdateGoodsTypeCount:" +
                    stream(goodsTypeProportion.getProportionGoodsType()).mapToObj(String::valueOf).collect(joining(",")));
        }
    }

    @Async("rcmdExecutor")
    private CompletableFuture<Long> getFutureGoodsCountByType(String goodsType) {
        return CompletableFuture.completedFuture(
                client.prepareSearch(goodsIndex).setTypes(goodsType).setSize(0).get().getHits().getTotalHits());
    }

    private double[] goodsTypeCount() {
        CompletableFuture<Long> g1 = getFutureGoodsCountByType("1");
        CompletableFuture<Long> g2 = getFutureGoodsCountByType("2");
        CompletableFuture<Long> g3 = getFutureGoodsCountByType("3");
        CompletableFuture.allOf(g1, g2, g3).join();
        try {
            long g1Count = g1.get();
            long g2Count = g2.get();
            long g3Count = g3.get();
            double total = g1Count + g2Count + g3Count;
            return new double[]{g1Count / total, g2Count / total, g3Count / total};
        } catch (Exception e) {
            logger.error("error", e);
            return goodsTypeProportion.getProportionGoodsType();
        }
    }

    public Set<SkuIdGoodsIds> getSkuIdsBySalesAndNews(String cityId, int pageNo, int totalSize, double salesPercent) throws Exception {
        checkIfUpdateGoodsTypeCount();
        int sizeBySales = (int) (totalSize * salesPercent);
        int sizeByNew = totalSize - sizeBySales;

        int[] sizeArray = goodsTypeProportion.getSizeArray(sizeBySales);

        CompletableFuture<List<SkuIdGoodsIds>> newGoods = getFutureSkuIdsByScoreFunction(
                new GoodsSearchParams().cityId(cityId).pageNo(pageNo).size(sizeByNew), goodsCreateTimeExpDecayFunction);

        CompletableFuture<List<SkuIdGoodsIds>> salesGoodsType1 = getFutureSkuIdsByScoreFunction(
                new GoodsSearchParams().cityId(cityId).pageNo(pageNo).goodsType("1").size(sizeArray[0]), goodsSaleNumExpDecayFunction);

        CompletableFuture<List<SkuIdGoodsIds>> salesGoodsType2 = getFutureSkuIdsByScoreFunction(
                new GoodsSearchParams().cityId(cityId).pageNo(pageNo).goodsType("2").size(sizeArray[1]), goodsSaleNumExpDecayFunction);

        CompletableFuture<List<SkuIdGoodsIds>> salesGoodsType3 = getFutureSkuIdsByScoreFunction(
                new GoodsSearchParams().cityId(cityId).pageNo(pageNo).goodsType("3").size(sizeArray[2]), goodsSaleNumExpDecayFunction);

        CompletableFuture.allOf(newGoods, salesGoodsType1, salesGoodsType2, salesGoodsType3).join();

        Set<SkuIdGoodsIds> set = new HashSet<>();
        set.addAll(salesGoodsType1.get());
        set.addAll(salesGoodsType2.get());
        set.addAll(salesGoodsType3.get());
        set.addAll(newGoods.get());
        logger.debug("salesGoodsType1:" + salesGoodsType1.get().size());
        logger.debug("salesGoodsType2:" + salesGoodsType2.get().size());
        logger.debug("salesGoodsType3:" + salesGoodsType3.get().size());
        logger.debug("newGoods:" + newGoods.get().size());
        return set;
    }

    private List<SkuIdGoodsIds> getSkuIdGoodsIdsFromSRB(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        List<SkuIdGoodsIds> result = new ArrayList<>();
        List<String> goodsIdsList = null;
        int rcmdScore = 0;
        for (SearchHit hit : hits) {
            Object goodsIdsObj = hit.getSource().get("goodsIds");
            if (goodsIdsObj != null) goodsIdsList = (List<String>) goodsIdsObj;
            Object rcmdScoreObj = hit.getSource().get("rcmdScore");
            if (rcmdScoreObj != null) rcmdScore = (Integer) rcmdScoreObj;
            result.add(new SkuIdGoodsIds(hit.getId(), goodsIdsList, rcmdScore, hit.getScore()));
        }
        return result;
    }

    @Async("rcmdExecutor")
    private CompletableFuture<List<SkuIdGoodsIds>> getFutureSkuIdsByScoreFunction(GoodsSearchParams params, ScoreFunctionBuilder<ExponentialDecayFunctionBuilder> scoreFunctionBuilder) {
        List<SkuIdGoodsIds> list = getGoodsIdsTemplate(
                QueryBuilders.functionScoreQuery(goodsSearchBuilder(params), scoreFunctionBuilder),
                params.getFrom(), params.getSize(), params.getGoodsType());
        return CompletableFuture.completedFuture(list);
    }

    public List<SkuIdGoodsIds> getSkuIdGoodsIdsByLabels(GoodsSearchParams params) {
        return getGoodsIdsTemplate(goodsSearchBuilder(params), params.getFrom(), params.getSize());
    }

    private List<SkuIdGoodsIds> getGoodsIdsTemplate(QueryBuilder query, int from, int size, String goodsType) {
        SearchRequestBuilder srb = client.prepareSearch(goodsIndex)
                .setFrom(from)
                .setSize(size)
                .setQuery(query);
        if (goodsType != null) {
            srb.setTypes(goodsType);
        }
        return getSkuIdGoodsIdsFromSRB(srb);
    }

    private List<SkuIdGoodsIds> getGoodsIdsTemplate(QueryBuilder query, int from, int size) {
        return getGoodsIdsTemplate(query, from, size, null);
    }

    public String getOneOfMatchedGoodsId(GoodsSearchParams params) {
        List<SkuIdGoodsIds> list = getGoodsIdsTemplate(
                new FunctionScoreQueryBuilder(goodsSearchBuilder(params), goodsRcmdScoreGaussDecayFunction), params.getFrom(), params.getSize());
        if (list.size() == 0) {
            return null;
        }
        List<SkuIdGoodsIds> filteredList =
                list.stream().filter(x -> x.getRcmdScore() == Goods.SCORE_MAX).collect(toList());
        if (filteredList.size() == 0) {
            filteredList = list;
        }
        return filteredList.get(random.nextInt(filteredList.size())).getRandomGoodsId();
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
            goods.setSearchKeywords((String) source.get("searchKeywords"));
            goods.setRcmdScore((Integer) source.get("rcmdScore"));
            goods.setSalesNum((Integer) source.get("salesNum"));
        }
        return goods;
    }

    //---------------------- goods end -----------------------------------


    //---------------------- article start -----------------------------------

    private String[] getArticleIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        return getIndexByCondition(articleIndex, condition, excludeIds, size);
    }

    public String[] getArticleIds(String text, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getArticleIdsByCondition(QueryBuilders.multiMatchQuery(text, fieldNames), excludeIds, size);
        logger.debug("text:{}, article ids:{}", text, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getArticleIds(String labels, int size, String... fieldNames) {
        return getArticleIds(labels, new String[]{}, size, fieldNames);
    }

    public List<String> getArticleIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(articleIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param)).setSize(size);
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
        return getArticleIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, size);
    }

    public void updateArticle(Article article) {
        deleteIdByQuery(articleIndex, String.valueOf(article.getInformationId()));
        Map<String, Object> map = new HashMap<>();
        map.put("tags", article.getTags());
        map.put("title", article.getTitle());
        map.put("content", article.getContent());
        map.put("create_time", article.getCreateTime());
        map.put("update_time", article.getUpdateTime());
        client.prepareIndex(articleIndex, article.getChannelId() + "_" + article.getCategoryId(), String.valueOf(article.getInformationId())).setSource(map).get();
    }

    public void deleteArticle(long id) {
        deleteIdByQuery(articleIndex, String.valueOf(id));
    }

    //---------------------- article end -----------------------------------


    //---------------------- live start -----------------------------------

    private String[] getLiveIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        return getIndexByCondition(liveIndex, condition, excludeIds, size);
    }

    public String[] getLivesIds(String tags, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getLiveIdsByCondition(QueryBuilders.multiMatchQuery(tags, fieldNames), excludeIds, size);
        logger.debug("tags:{}, live ids:{}", tags, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getLivesIds(String labels, int size, String... fieldNames) {
        return getLivesIds(labels, new String[]{}, size, fieldNames);
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

    public void deleteLive(long id) {
        deleteIdByQuery(liveIndex, String.valueOf(id));
    }

    public List<String> getLiveIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(liveIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param)).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }
    //---------------------- live end -----------------------------------


    //---------------------- video start -----------------------------------

    public void updateVideo(Video video) {
        deleteIdByQuery(videoIndex, String.valueOf(video.getId()));
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
    }

    public List<String> getVideoIdsByAbnorm(AbnormalParam param, int size) {
        SearchRequestBuilder srb = client.prepareSearch(videoIndex)
                .setQuery(getLiveOrVideoBasicBuilder(param)).setSize(size);
        return EsUtils.getDocIdsAsList(srb);
    }

    /**
     * 如果fieldNames不输入，默认设为 "tags"
     */
    public String[] getVideoIds(String text, String[] excludeIds, int size, String... fieldNames) {
        if (fieldNames.length == 0) {
            fieldNames = new String[]{"tags"};
        }
        String[] ids = getVideoIdsByCondition(QueryBuilders.multiMatchQuery(text, fieldNames), excludeIds, size);
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

    public String[] getSimilarVideoIdsByTags(String videoId, String tags, int size) {
        //去掉这些对匹配结果有负面影响的词
        String replacedTags = Utils.removeStopWords(tags);
        logger.debug("replacedLabel:{}", replacedTags);
        return getVideoIdsByCondition(matchQuery("tags", replacedTags), new String[]{videoId}, size);
    }

    @SuppressWarnings("SameParameterValue")
    private String[] getVideoIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        return getIndexByCondition(videoIndex, condition, excludeIds, size);
    }

    //---------------------- video end -----------------------------------
}






