package com.haozhuo.datag.service.Virus;

import com.haozhuo.datag.model.bisys.virus.UpdateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class UpdateVirusData {
    @Autowired
    @Qualifier("whhaozhuoJdbc")
    private JdbcTemplate whDB;


    public void UpdateData(UpdateData updateData){
        String sql  = "update Ncov_statis set hubei_severe = ? , hubei_server_h = ? , hubei_touch = ?, hubei_observe =?,contry_touch_total =?,contry_observe_total=?,contry_confirm_today=?,contry_suspect_today=?,contry_deobserve_today=?,contry_severe=? where nowtime =?";
        whDB.update(sql,updateData.getHubei_severe(),updateData.getHubei_server_h(),
                updateData.getHubei_touch(),updateData.getHubei_observe(),updateData.getContry_touch_total(),
                updateData.getContry_observe_total(),updateData.getContry_confirm_today(),updateData.getContry_suspect_today(),updateData.getContry_deobserve_today(),updateData.getContry_severe(),updateData.getNowtime());
    }
}
