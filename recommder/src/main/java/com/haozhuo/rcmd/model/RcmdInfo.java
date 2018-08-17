package com.haozhuo.rcmd.model;

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

    public void add(List<String> ids) {
        for (String id : ids) {
            article.add(id);
        }
    }
}
