package com.haozhuo.datag.service.textspilt;

import java.io.Serializable;

/**
 * Created by Lucius on 10/17/18.
 */
public class MyKeyword implements Serializable{
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

}
