package com.haozhuo.datag.model.bisys;

import lombok.Setter;
import lombok.Getter;

/**
 * Created by Lucius on 3/5/19.
 */
@Getter
@Setter
public class UploadInfo {
    private int tableId;
    private String date;
    private String uploadTime;
    private String operateAccount;
}
