package com.haozhuo.datag.model.wechat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadNum {
    private String date;
   private String url;
   private int url_pv;
   private int submit_pv;
    private int num;
   private int ios_num;
   private int android_num;
   private String update_time;

}
