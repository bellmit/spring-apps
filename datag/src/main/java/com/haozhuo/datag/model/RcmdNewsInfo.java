package com.haozhuo.datag.model;


import java.util.*;

/**
 * Created by Lucius on 10/23/18.
 */

public class RcmdNewsInfo {
    private static final List<String> allChannelIds = Arrays.asList("1", "2", "3", "4", "5");

    public static List<String> getAllChannelIds() {
        Collections.shuffle(allChannelIds);
        return allChannelIds;
    }

//    private boolean initAllChannels = false;
    private final List<String> channelIdList = new ArrayList<>();
    private List<String> news = new ArrayList<>();


//    public boolean isInitAllChannels() {
//        return initAllChannels;
//    }
//
//    public void setInitAllChannels(boolean initAllChannels) {
//        this.initAllChannels = initAllChannels;
//    }

    public List<String> getChannelIdList() {
        return channelIdList;
    }

    public void addChannelId(String channelId) {
        channelIdList.add(channelId);
    }
    public void addDefaultChannelIds() {
        channelIdList.addAll(getAllChannelIds());
    }

    public List<String> getNews() {
        return news;
    }

    public void addNews(List<String> some) {
        news.addAll(some);
    }
    public void setNews(List<String> some) {
        this.news = some;
    }
}
