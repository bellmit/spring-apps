package com.haozhuo.datag.service.textspilt;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by Lucius on 10/16/18.
 */
public class SimpleArticle {
    private String informationId;
    private String title;
    private String content;
    private String channelId;
    private List<MyKeyword> keywords;
    private String strKeywords;

    public List<MyKeyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<MyKeyword> keywords) {
        this.keywords = keywords;
        this.strKeywords = keywords.stream().map(kw -> kw.getName() + ":" + kw.getScore()).collect(joining(","));
    }


    public String getInformationIdByString() {
        return String.valueOf(informationId);
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInformationId() {
        return informationId;
    }

    public void setInformationId(String informationId) {
        this.informationId = informationId;
    }

    public String getStrKeywords() {
        return strKeywords;
    }

    public String getChannelIdWithKeywords() {
        return channelId + ";" + strKeywords;
    }

    public boolean isEmpty() {
        return keywords == null || keywords.size() == 0;
    }

    public List<String> getKeywordNameList() {
       return getKeywords().stream().map(MyKeyword::getName).collect(toList());
    }
}
