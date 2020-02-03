package com.haozhuo.datag.service.Virus;

import com.haozhuo.datag.model.bisys.virus.Virus;
import com.haozhuo.datag.model.bisys.virus.VirusOne;
import com.haozhuo.datag.model.bisys.virus.VirusThree;
import com.haozhuo.datag.model.bisys.virus.VirusTwo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetVirusGraph {
    @Autowired
    @Qualifier("whhaozhuoJdbc")
    private JdbcTemplate whDB;

    public List<VirusOne> getFirstGraph(){
        List<VirusOne> list = new ArrayList();
        String sql = "select nowtime,contry_confirm_today,contry_suspect_today,contry_confirm_total from Ncov_statis";

        list = whDB.query(sql,(resultSet, i) ->{
            VirusOne virusOne = new VirusOne();
            virusOne.setNowtime(resultSet.getString("nowtime"));
            virusOne.setNewConfirmNum(resultSet.getInt("contry_confirm_today"));
            virusOne.setNewSuspectNum(resultSet.getInt("contry_suspect_today"));
            virusOne.setCountConfirm(resultSet.getInt("contry_confirm_total"));
            return virusOne;
        });

        return list;
    }

    public List<VirusTwo> getSecondGraph(){
        List<VirusTwo> list = new ArrayList();
        String sql = "select nowtime,wuhan_confirm_today,wuhan_confirm_total from Ncov_statis";

        list = whDB.query(sql,(resultSet, i) ->{
            VirusTwo virusTwo = new VirusTwo();
            virusTwo.setNowtime(resultSet.getString("nowtime"));
            virusTwo.setWhNewConfirmNum(resultSet.getInt("wuhan_confirm_today"));
            virusTwo.setWhCountConfirm(resultSet.getInt("wuhan_confirm_total"));
            return virusTwo;
        });
        return list;
    }

    public List<VirusThree> getThridGraph(){
        List<VirusThree> list = new ArrayList();
        String sql = "select nowtime,contry_confirm_today,contry_suspect_today,contry_confirm_total from Ncov_statis";
        list = whDB.query(sql,(resultSet, i) ->{
            VirusThree virusThree = new VirusThree();
            virusThree.setNowtime(resultSet.getString("nowtime"));
            virusThree.setCountHubeiComfire(resultSet.getInt("contry_confirm_today"));
            virusThree.setCountWhComfire(resultSet.getInt("contry_suspect_today"));
            virusThree.setHubeiNewComfire(resultSet.getInt("contry_confirm_total"));
            virusThree.setWuhanNewComfir(resultSet.getInt("contry_confirm_total"));
            virusThree.setHbexceptWhNewComfir(virusThree.getHubeiNewComfire()-virusThree.getWuhanNewComfir());
            virusThree.setHbexceptWhCountComfire(virusThree.getCountHubeiComfire()-virusThree.getCountWhComfire());
            return virusThree;
        });

        return list;
    }
}
