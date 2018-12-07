package com.haozhuo.datag.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lucius on 10/23/18.
 */

public class RcmdNewsInfo {
    private static final List<String> allChannelIds = Arrays.asList("1", "2", "3", "4", "5");

    public static List<String> getAllChannelIds() {
        Collections.shuffle(allChannelIds);
        return allChannelIds;
    }

    private boolean initAllChannels = false;
    private final List<String> requestRcmdChannels = new ArrayList<>();
    private List<String> news = new ArrayList<>();


    public boolean isInitAllChannels() {
        return initAllChannels;
    }

    public void setInitAllChannels(boolean initAllChannels) {
        this.initAllChannels = initAllChannels;
    }

    public List<String> getRequestRcmdChannels() {
        return requestRcmdChannels;
    }

    public void addRcmdChannelId(String channelId) {
        requestRcmdChannels.add(channelId);
    }


    public List<String> getNews() {
        return news;
    }

    public void addNews(List<String> some) {
        this.news.addAll(some);
    }
    public void setNews(List<String> some) {
        this.news = some;
    }
}
