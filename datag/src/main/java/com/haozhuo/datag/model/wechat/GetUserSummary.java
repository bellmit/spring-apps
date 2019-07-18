package com.haozhuo.datag.model.wechat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserSummary {
    private String ref_date;
    private String user_source;
    private String new_user;
    private String cancel_user;
}
