package com.haozhuo.datag.common;

import com.haozhuo.datag.model.DocIdScore;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.*;

/**
 * Created by Lucius on 8/21/18.
 */
public class EsUtils {
    public static List<String> getDocIdsAsList(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(SearchHit::getId).collect(Collectors.toList());
    }

    public static String[] getDocIdsAsArray(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(SearchHit::getId).toArray(String[]::new);
    }
    public static String[] getDocIdsAsArray2(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(SearchHit::getSource).toArray(String[]::new);
        //return stream(hits).map(SearchHit::getId).toArray(String[]::new);
    }

    public static List<DocIdScore> getDocIdScoresAsList(SearchRequestBuilder srb) {
        SearchHit[] hits = srb.execute().actionGet().getHits().getHits();
        return stream(hits).map(x -> new DocIdScore(x.getId(),x.getScore())).collect(Collectors.toList());
    }

}
