package com.haozhuo.rcmd.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 8/17/18.
 */
@Setter
@Getter
@AllArgsConstructor
public class RcmdInfoMsg {
    String userId;
    int rcmdType;

    @Override
    public String toString() {
        return "RcmdInfoMsg{" +
                "userId='" + userId + '\'' +
                ", rcmdType=" + rcmdType +
                '}';
    }
}
