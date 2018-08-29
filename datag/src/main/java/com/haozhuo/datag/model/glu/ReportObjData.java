package com.haozhuo.datag.model.glu;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Created by Lucius on 8/27/18.
 */
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportObjData {
    private UserInfo userInfo;
    private Report report;
}
