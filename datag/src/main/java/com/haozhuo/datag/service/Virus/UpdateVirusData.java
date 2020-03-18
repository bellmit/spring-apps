package com.haozhuo.datag.service.Virus;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haozhuo.datag.model.bisys.virus.UpdateData;
import com.haozhuo.datag.model.bisys.virus.VirusAus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


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

    public List getAus(){
        List list = new ArrayList();
        String sql = "SELECT * FROM `fight2feiyan_aus` ORDER BY now_time desc limit 1";
        VirusAus virusAus = new VirusAus();
         whDB.query(sql, (resultSet, i)-> {
            virusAus.setNowtime(resultSet.getString("now_time"));
            virusAus.setJson(resultSet.getString("json_rs"));
            return virusAus;
        });
        System.out.println(virusAus.getJson());
        JSONObject jo = JSONObject.parseObject(new String(virusAus.getJson()));
        System.out.println(jo);
        list.add(jo);
        return list;
    }
}
