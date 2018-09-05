package com.haozhuo.datag.model;

import com.haozhuo.datag.service.biz.InfoRcmdService;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucius on 9/5/18.
 */
@Getter
@AllArgsConstructor
public class PushedInfoKeys {
    private String userId;
    private String channelId;
    private String categoryId;


    public String getKey() {
        return String.format("PushedInfo:%s", userId);
    }

    public String getLiveHashKey() {
        return String.format("%s_%s_l", channelId, categoryId);
    }

    public String getVideoHashKey() {
        return String.format("%s_%s_v", channelId, categoryId);
    }

    public String getArticleHashKey() {
        return String.format("%s_%s_a", channelId, categoryId);
    }

    public String getHashKeyByALVIndex(int index) {
        switch (index) {
            case InfoALVArray.articleIndex:
                return getArticleHashKey();
            case InfoALVArray.liveIndex:
                return getLiveHashKey();
            case InfoALVArray.videoIndex:
                return getVideoHashKey();
            default:
                return null;
        }
    }

    public List getALVHashKeys() {
        ArrayList<String> list = new ArrayList<>(3);
        list.add(InfoALVArray.articleIndex, getArticleHashKey());
        list.add(InfoALVArray.liveIndex, getLiveHashKey());
        list.add(InfoALVArray.videoIndex, getVideoHashKey());
        return list;
    }

    public static String getChannelRcmdHashKey() {
        return String.format("%s_%s_a", InfoRcmdService.channelRcmdId, InfoRcmdService.allCategoryId);
    }
}
