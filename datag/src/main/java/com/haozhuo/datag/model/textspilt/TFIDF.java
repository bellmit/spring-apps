package com.haozhuo.datag.model.textspilt;


import org.ansj.app.keyword.Keyword;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by Lucius on 10/16/18.
 */
public class TFIDF {
    static MyKeyWordComputer kwc = new MyKeyWordComputer(5);
    private static final int maxScore = 200;

    public static List<MyKeyword> getMyKeywords(String title, String content) {
        List<Keyword> keywords = kwc.computeArticleTfidf(title, content);
        List<MyKeyword> myKeywords =
                keywords.stream()
                        .map(kw -> new MyKeyword(kw.getName(),
                                (int) (kw.getScore() < maxScore ? kw.getScore() : maxScore)))
                        .collect(toList());
        return myKeywords;
    }
}
