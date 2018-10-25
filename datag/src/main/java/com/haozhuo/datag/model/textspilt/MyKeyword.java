package com.haozhuo.datag.model.textspilt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Created by Lucius on 10/17/18.
 */
public class MyKeyword implements Serializable {
    String name;
    int score;

    public MyKeyword(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<MyKeyword> parseKeywordsFromString(String strKeywords) {
        List<MyKeyword> result;
        if (strKeywords == null || "".equals(strKeywords)) {
            result = new ArrayList<MyKeyword>();
        } else {
            result = stream(strKeywords.split(",")).map(kw -> kw.split(":"))
                    .map(x -> new MyKeyword(x[0], Integer.parseInt(x[1])))
                    .collect(toList());
        }
        return result;
    }
}
