package com.haozhuo.datag.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 8/17/18.
 */
@Setter
@Getter
@AllArgsConstructor
public class RcmdRequestMsg {
    String userId;
    String channelId;

    @Override
    public String toString() {
        return "RcmdRequestMsg{" +
                "userId='" + userId + '\'' +
                ", channelId=" + channelId +
                '}';
    }
}
