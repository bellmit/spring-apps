package com.haozhuo.datag.common;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucius on 8/21/18.
 */
public class EsUtils {
    private static final Logger logger = LoggerFactory.getLogger(EsUtils.class);

    public static List<String> getDocIdsAsList(SearchRequestBuilder srb) {
        //logger.debug("getDocIdsAsList:{}", srb);
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        List<String> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            list.add(hit.getId());
        }
        return list;
    }

    public static String[] getDocIdsAsArray(SearchRequestBuilder srb) {
       // logger.debug("getDocIdsAsList:{}", srb);
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        String[] array = new String[hits.length];

        for (int i = 0; i < hits.length; i++) {
            array[i] = hits[i].getId();
        }
        return array;
    }
}
