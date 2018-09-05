package com.haozhuo.datag.model;

import com.haozhuo.datag.common.JavaUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lucius on 8/16/18.
 */
@Setter
@Getter
public class InfoALVArray {
    public static final int articleIndex = 0;
    public static final int liveIndex = 1;
    public static final int videoIndex = 2;

    String[] video = new String[]{};
    String[] live = new String[]{};
    String[] article = new String[]{};

    public int size() {
        return video.length + live.length + article.length;
    }

    public String[] getByIndex(int index) {
        switch (index) {
            case articleIndex:
                return getArticle();
            case liveIndex:
                return getLive();
            case videoIndex:
                return getVideo();
            default:
                return null;
        }
    }

    public void addByIndex(String[] ids, int index) {
        switch (index) {
            case articleIndex:
                addArticle(ids);
            case liveIndex:
                addLive(ids);
            case videoIndex:
                addVideo(ids);
        }
    }

    public void setByIndex(String[] ids, int index) {
        switch (index) {
            case articleIndex:
                setArticle(ids);
            case liveIndex:
                setLive(ids);
            case videoIndex:
                setVideo(ids);
        }
    }

    public void addArticle(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        article = (String[]) ArrayUtils.addAll(article, ids);
    }

    public void addLive(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        live = (String[]) ArrayUtils.addAll(live, ids);
    }

    public void addVideo(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        video = (String[]) ArrayUtils.addAll(video, ids);
    }
}
