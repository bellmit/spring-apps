package com.haozhuo.datag.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import java.util.Optional;
import java.util.stream.Stream;
import static java.util.Arrays.stream;


/**
 * Created by Lucius on 8/16/18.
 */
@Setter
@Getter
public class InfoALV {
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

    public void addArticle(String[] newIds) {
        //和下面addLive和addVideo的效果一样
        Optional.ofNullable(newIds).ifPresent(ids -> article = Stream.concat(stream(article), stream(ids)).toArray(String[]::new));
    }

    public void addLive(String[] newIds) {
        Optional.ofNullable(newIds).ifPresent(ids -> live = (String[]) ArrayUtils.addAll(live, ids));
    }

    public void addVideo(String[] newIds) {
        Optional.ofNullable(newIds).ifPresent(ids -> video = (String[]) ArrayUtils.addAll(video, ids));
    }
}
