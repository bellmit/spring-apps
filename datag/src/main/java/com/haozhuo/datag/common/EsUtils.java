package com.haozhuo.datag.common;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.*;

/**
 * Created by Lucius on 8/21/18.
 */
public class EsUtils {
    public static List<String> getDocIdsAsList(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(x -> x.getId()).collect(Collectors.toList());
    }

    public static String[] getDocIdsAsArray(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(x -> x.getId()).toArray(String[]::new);
    }

}
