package com.haozhuo.rcmd.service;

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
public class ElasticsearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    @Value("${app.rcmd.video.es.video-index}")
    private String videosInfoIndex;
    @Autowired
    private TransportClient esClient;

    private String[] getVideoIdsByCondition(QueryBuilder condition, String[] excludeIds, int size) {
        SearchRequestBuilder srb = esClient.prepareSearch(videosInfoIndex)
                .setTypes("docs")
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

    /**
     * curl -XGET 'datanode153:9200/videos_info/_search?pretty' -d '{"size":10,"query":{"bool":{"must":{"multi_match":{"query":"大叔","fields":["keywords","labels","title"]}},"must_not":[{"ids":{"values":["295"]}}]}}}'
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

    /**
     * curl -XGET 'datanode153:9200/videos_info/_search?pretty' -d ' {"size":10,"query":{"bool":{"must":{"match_all":{}},"must_not":[{"ids":{"values":["300","304","305","291","287","302","296"]}}]}}}'
     *
     * @param excludeIds
     * @param size
     * @return
     */
    public String[] getVideoIdsRandomly(String[] excludeIds, int size) {
        return getVideoIdsByCondition(QueryBuilders.matchAllQuery(), excludeIds, size);
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
}
