package com.haozhuo.datag.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * flag:0  生成推荐列表时，不对news_rcmd_channelIds做任何操作。
 * flag:1  生成推荐列表时，将channelId放到news_rcmd_channelIds的最前面
 * flag:2  生成推荐列表时，将channelId放到news_rcmd_channelIds的最后面
 */
@Setter
@Getter
@AllArgsConstructor
public class NewsRcmdMsg {
    String userId;
    String channelId;
    int flag;

    @Override
    public String toString() {
        return "NewsRcmdMsg{" +
                "userId='" + userId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", flag=" + flag +
                '}';
    }
}
