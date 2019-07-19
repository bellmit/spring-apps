package com.haozhuo.datag.model.wechat;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈微信通用接口凭证〉
 *
 * @author Phil
 * @create 12/4/2018 2:13 PM
 * @since 1.0
 */
@Getter
@Setter
public class AccessToken implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5806078615354556552L;

    // 获取到的凭证

    private String accessToken;

    // 凭证有效时间，单位：秒
    private int expires_in;

    /**
     * 设置微信公众号的appid
     */
    private String appId;

    /**
     * 设置微信公众号的app secret
     */
    private String secret;

    /**
     * 设置微信公众号的token
     */
    private String token;

    private String Access_token;

}

