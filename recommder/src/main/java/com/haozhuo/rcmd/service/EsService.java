package com.haozhuo.rcmd.service;

import com.haozhuo.rcmd.Utils;
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

import java.util.Arrays;
import java.util.stream.Stream;


@Component
public class EsService {
    private static final Logger logger = LoggerFactory.getLogger(EsService.class);
    @Value("${app.rcmd.video.es.video-index}")
    private String videosInfoIndex;

    @Value("${app.rcmd.goods.es.goods-index}")
    private String goodsIndex;

    @Autowired
    private TransportClient client;

    private String[] getVideoIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        return getIndexByCondition(videosInfoIndex, condition, excludeIds, size) ;
    }

    private String[] getGoodsIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        return getIndexByCondition(goodsIndex, condition, excludeIds, size) ;
    }

    private String [] getIndexByCondition(String index, QueryBuilder condition, String[] excludeIds, int size) {
        logger.debug("es index:{}",index);
        SearchRequestBuilder srb = client.prepareSearch(index)
                .setSize(size)
                .setQuery(QueryBuilders.boolQuery()
                        .must(condition)
                        .mustNot(QueryBuilders.idsQuery().addIds(excludeIds)));
        logger.debug("SearchRequestBuilder: {}", srb);
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        String[] result = new String[hits.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = hits[i].getId();
        }
        return result;
    }

    private String[] getVideoIdsByCondition(QueryBuilder condition, int size) {
        return getVideoIdsByCondition(condition, new String[]{}, size);
    }

    /**
     * @param title
     * @param size
     * @return
     */
    public String[] getVideoIdsByTitle(String title, int size) {
        return getVideoIdsByCondition(QueryBuilders.matchQuery("title", title), size);
    }


    /**
     * curl -XGET 'datanode153:9200/videos_info/_search?pretty' -d ' {"size":10,"query":{"bool":{"must":{"match_all":{}},
     * "must_not":[{"ids":{"values":["300","304","305","291","287","302","296"]}}]}}}'
     *
     * @param excludeIds
     * @param size
     * @return
     */
    private String[] getVideoIdsRandomly(String[] excludeIds, int size) {
        return getVideoIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, size);
    }

    private String[] getGoodsIdsRandomly(String[] excludeIds, int size) {
        return getGoodsIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, size);
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
        String[] ids = getVideoIdsByCondition(QueryBuilders.multiMatchQuery(labels, "keywords", "labels", "title"), excludeIds, size);
        logger.debug("labels:{}, video ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getGoodsIdsByLabels(String labels, String[] excludeIds, int size) {
        String[] ids = getGoodsIdsByCondition(QueryBuilders.matchQuery("label", labels), excludeIds, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }

    public String[] getGoodsIdsByLabels(String labels, int size) {
        String[] ids = getGoodsIdsByCondition(QueryBuilders.matchQuery("label", labels), new String[]{}, size);
        logger.debug("labels:{}, goods ids:{}", labels, StringUtils.arrayToCommaDelimitedString(ids));
        return ids;
    }
    /**
     * 旧接口从video这个索引中匹配数据，但是video这个索引已经不维护了。改写后的从video_info匹配数据，不考虑年龄和性别
     * 先从 getVideoIdsByLabel()方法获取数据，如果查到的结果少于特定条数，再使用getVideoIdsRandomly()补充：
     *
     * @param labels
     * @param excludeIds
     * @param size
     * @return
     */
    public String[] getFixedSizeVideoIdsByLabel(String labels, String[] excludeIds, int size) {
        String[] resultByLabels = getVideoIdsByLabel(labels, excludeIds, size);
        if (resultByLabels.length < size) {
            String[] newExcludeIds = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(excludeIds))
                    .toArray(String[]::new);
            String[] randomVideoIds = getVideoIdsRandomly(newExcludeIds, size - resultByLabels.length);
            resultByLabels = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(randomVideoIds))
                    .toArray(String[]::new);
        }
        return resultByLabels;
    }

    public String[] getFixedSizeGoodsIdsByLabel(String labels, String[] excludeIds, int size) {
        String[] resultByLabels = getGoodsIdsByLabels(labels, excludeIds, size);
        if (resultByLabels.length < size) {
            String[] newExcludeIds = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(excludeIds))
                    .toArray(String[]::new);
            String[] randomVideoIds = getGoodsIdsRandomly(newExcludeIds, size - resultByLabels.length);
            resultByLabels = Stream.concat(Arrays.stream(resultByLabels), Arrays.stream(randomVideoIds))
                    .toArray(String[]::new);
        }
        return resultByLabels;
    }

    public String[] getSimilarVideoIds(String videoId, String label, int size) {
        //去掉这些对匹配结果有负面影响的词
        String replacedLabel = Utils.removeStopWords(label);
        logger.debug("replacedLabel:{}", replacedLabel);
        return getVideoIdsByCondition(QueryBuilders.matchQuery("labels", replacedLabel), new String[]{videoId}, size);
    }

    /**
     *
     * curl -XGET 'es1:9200/reportlabel/_search?pretty' -d '{"size":1,"query": {"match": {"healthReportId": "4049171"}}}'
     * @return
     */


    public String getLabelsByReportId() {
        return "";
    }

//    /**
//     * 根据infoId从Earticle中得到labels，根据这个labels从good索引的label，content_name，display_label 中匹配出相应的商品
//     */
//    public String getGoodsIdByLabels(String labels) {
//        return "";
//        getGoodsIdsByLabels(String labels, int size)
//    }
}
