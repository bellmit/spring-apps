package com.haozhuo.datag.service.Virus;

import com.haozhuo.datag.model.bisys.virus.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        String sql = "select nowtime,hubei_confirm_today,wuhan_confirm_today,hubei_confirm_total,wuhan_confirm_total from Ncov_statis";
        list = whDB.query(sql,(resultSet, i) ->{
            VirusThree virusThree = new VirusThree();
            virusThree.setNowtime(resultSet.getString("nowtime"));
            virusThree.setCountHubeiComfire(resultSet.getInt("hubei_confirm_total"));
            virusThree.setCountWhComfire(resultSet.getInt("wuhan_confirm_total"));
            virusThree.setHubeiNewComfire(resultSet.getInt("hubei_confirm_today"));
            virusThree.setWuhanNewComfir(resultSet.getInt("wuhan_confirm_today"));
/*            System.out.println("湖北新增确诊:"+virusThree.getHubeiNewComfire());
            System.out.println("wh新增确诊:"+virusThree.getWuhanNewComfir());
            System.out.println("湖北总确诊:"+virusThree.getCountHubeiComfire());
            System.out.println("wh总确诊:"+virusThree.getCountWhComfire());*/
            virusThree.setHbexceptWhNewComfir(virusThree.getHubeiNewComfire()-virusThree.getWuhanNewComfir());
            virusThree.setHbexceptWhCountComfire(virusThree.getCountHubeiComfire()-virusThree.getCountWhComfire());
            return virusThree;
        });

        return list;
    }

    private static final String fourSQL="select feiHbSum,feiHbNew,time from (\n" +
            "(SELECT (contry_confirm_total-hubei_confirm_total) as feiHbSum,nowtime FROM `Ncov_statis` where nowtime <=?)a inner join \n" +
            "(SELECT (contry_confirm_today-hubei_confirm_today) as feiHbNew,nowtime as time  FROM `Ncov_statis` where nowtime <=?)b on a.nowtime=b.time\n" +
            ");";
    private static final  String SiWangSQL="select contry_dead_lv,wuhan_dead_lv,hubei_dead_lv,(contry_dead_lv-hubei_dead_lv) as not_hubei_lv,time from (\n" +
            "(select contry_dead_lv,wuhan_dead_lv,time1 from (\n" +
            "(select round((contry_dead_total/contry_confirm_total),2) as contry_dead_lv,nowtime from Ncov_statis where nowtime <=?)a inner join \n" +
            "(select round((wuhan_dead_total/wuhan_confirm_total),2) as wuhan_dead_lv,nowtime as time1 from Ncov_statis where nowtime <=?)b on a.nowtime =b.time1)) c  inner join \n" +
            "(select round(( hubei_dead_total/hubei_confirm_total),2) as hubei_dead_lv,nowtime as time  from Ncov_statis where nowtime <=?) d on c.time1=d.time)";
    public List<NotWH> getNotWhAll() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        //System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        String a = df.format(new Date());
        String time = a.split(" ")[0];
        List<NotWH> list = null;

            list = whDB.query(fourSQL, new Object[]{time,time},
                    (resultSet, i) -> {
                        NotWH notWH = new NotWH();
                        notWH.setFeiHbSum(resultSet.getString("feiHbSum"));
                        notWH.setFeiHbNew(resultSet.getString("feiHbNew"));
                        notWH.setTime(resultSet.getString("time"));
                        return notWH;
                    }
            );

        return list;
    }


    public List<SiWangLv> getAllSiWang(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        //System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        String a  = df.format(new Date());
        String time = a.split(" ")[0];
        List<SiWangLv> list = null;

            list = whDB.query(SiWangSQL, new Object[]{time,time,time},
                    (resultSet, i) -> {
                        SiWangLv siWangLv = new SiWangLv();
                        siWangLv.setContry_dead_lv(resultSet.getString("contry_dead_lv"));
                        siWangLv.setWuhan_dead_lv(resultSet.getString("wuhan_dead_lv"));
                        siWangLv.setHubei_dead_lv(resultSet.getString("hubei_dead_lv"));
                        siWangLv.setNot_hubei_lv(resultSet.getString("not_hubei_lv"));
                        siWangLv.setTime(resultSet.getString("time"));
                        return siWangLv;
                    }
            );


        return list;
    }

}
