package com.haozhuo.datag.model;

import com.haozhuo.datag.common.JavaUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lucius on 8/16/18.
 */
@Setter
@Getter
public class RcmdInfo {
    Set<String> video = new HashSet<>(1);
    Set<String> live = new HashSet<>(1);
    Set<String> article = new HashSet<>(10);

    public int size() {
        return video.size() + live.size() + article.size();
    }

    public void addArticles(List<String> ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            article.add(id);
        }
    }

    public void addArticles(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            article.add(id);
        }
    }

    public void addVideos(List<String> ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            video.add(id);
        }
    }

    public void addVideos(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            video.add(id);
        }
    }

    public void addLives(String[] ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            live.add(id);
        }
    }

    public void addLives(List<String> ids) {
        if (JavaUtils.isEmpty(ids)) return;
        for (String id : ids) {
            live.add(id);
        }
    }
}
