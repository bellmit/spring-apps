package com.haozhuo.datag.service.Virus;

import com.haozhuo.datag.model.bisys.virus.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class GetVirusGraph {
    @Autowired
    @Qualifier("whhaozhuoJdbc")
    private JdbcTemplate whDB;


    private static final String fourSQL = "select * from (select feiHbSum,feiHbNew,time from (\n" +
            "(SELECT (contry_confirm_total-hubei_confirm_total) as feiHbSum,nowtime FROM `Ncov_statis` ORDER BY nowtime desc limit 14)a inner join \n" +
            "(SELECT (contry_confirm_today-hubei_confirm_today) as feiHbNew,nowtime as time  FROM `Ncov_statis` ORDER BY nowtime desc limit 14)b on a.nowtime=b.time\n" +
            "))c  order by c.time;";
    private static final String SiWangSQL = "select * from (select contry_dead_lv,wuhan_dead_lv,hubei_dead_lv, round((contry_dead_total-hubei_dead_total)/(contry_confirm_total-hubei_confirm_total),3) as not_hubei_lv,time from (\n" +
            "            (select contry_dead_lv,wuhan_dead_lv,time1,contry_dead_total,contry_confirm_total from (\n" +
            "            (select contry_dead_total,contry_confirm_total,round((contry_dead_total/contry_confirm_total),2) as contry_dead_lv,nowtime from Ncov_statis ORDER BY nowtime desc limit 14)a inner join \n" +
            "            (select round((wuhan_dead_total/wuhan_confirm_total),2) as wuhan_dead_lv,nowtime as time1 from Ncov_statis ORDER BY nowtime desc limit 14)b on a.nowtime =b.time1)) c  inner join \n" +
            "            (select  hubei_dead_total,hubei_confirm_total,round(( hubei_dead_total/hubei_confirm_total),2) as hubei_dead_lv,nowtime as time  from Ncov_statis ORDER BY nowtime desc limit 14) d on c.time1=d.time)) f order by f.time;";

    public List<VirusOne> getFirstGraph() {
        List<VirusOne> list = new ArrayList();
        String sql = "select * from (select nowtime,contry_confirm_today,contry_suspect_today,contry_confirm_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";

        list = whDB.query(sql, (resultSet, i) -> {
            VirusOne virusOne = new VirusOne();
            virusOne.setNowtime(resultSet.getString("nowtime"));
            virusOne.setNewConfirmNum(resultSet.getInt("contry_confirm_today"));
            virusOne.setNewSuspectNum(resultSet.getInt("contry_suspect_today"));
            virusOne.setCountConfirm(resultSet.getInt("contry_confirm_total"));
            return virusOne;
        });

        return list;
    }

    public List<VirusTwo> getSecondGraph() {
        List<VirusTwo> list = new ArrayList();
        String sql = "select * from (select nowtime,wuhan_confirm_today,wuhan_confirm_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";

        list = whDB.query(sql, (resultSet, i) -> {
            VirusTwo virusTwo = new VirusTwo();
            virusTwo.setNowtime(resultSet.getString("nowtime"));
            virusTwo.setWhNewConfirmNum(resultSet.getInt("wuhan_confirm_today"));
            virusTwo.setWhCountConfirm(resultSet.getInt("wuhan_confirm_total"));
            return virusTwo;
        });
        return list;
    }

    public List<VirusThree> getThridGraph() {
        List<VirusThree> list = new ArrayList();
        String sql = "select * from (select nowtime,hubei_confirm_today,wuhan_confirm_today,hubei_confirm_total,wuhan_confirm_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";
        list = whDB.query(sql, (resultSet, i) -> {
            VirusThree virusThree = new VirusThree();
            virusThree.setNowtime(resultSet.getString("nowtime"));
            virusThree.setCountHubeiComfire(resultSet.getInt("hubei_confirm_total"));
            virusThree.setCountWhComfire(resultSet.getInt("wuhan_confirm_total"));
            virusThree.setHubeiNewComfire(resultSet.getInt("hubei_confirm_today"));
            virusThree.setWuhanNewComfir(resultSet.getInt("wuhan_confirm_today"));

            virusThree.setHbexceptWhNewComfir(virusThree.getHubeiNewComfire() - virusThree.getWuhanNewComfir());
            virusThree.setHbexceptWhCountComfire(virusThree.getCountHubeiComfire() - virusThree.getCountWhComfire());
            return virusThree;
        });

        return list;
    }

    public List<VirusFifth> getFifthGraph() {
        List<VirusFifth> list = new ArrayList<>();
        String sql = "select * from (select nowtime,contry_observe_total,contry_observe_today,contry_deobserve_today from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";
        list = whDB.query(sql, (resultSet, i) -> {
            VirusFifth virusFifth = new VirusFifth();
            virusFifth.setNowtime(resultSet.getString("nowtime"));
            virusFifth.setObservingNum(resultSet.getInt("contry_observe_total"));
            virusFifth.setNewObesrveNum(resultSet.getInt("contry_observe_today"));
            virusFifth.setReleaseObserve(resultSet.getInt("contry_deobserve_today"));
            return virusFifth;
        });

        return list;
    }

    public List<VirusSixth> getSixthGraph() {
        DecimalFormat df = new DecimalFormat("0.00");
        List<VirusSixth> list = new ArrayList<>();
        String sql = "select * from (select nowtime,contry_severe,contry_confirm_total,hubei_severe,hubei_confirm_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";
        list = whDB.query(sql, (resultSet, i) -> {
            VirusSixth virusSixth = new VirusSixth();
            virusSixth.setNowtime(resultSet.getString("nowtime"));
            virusSixth.setNationalServer(resultSet.getInt("contry_severe"));
            virusSixth.setNationalConfirm(resultSet.getInt("contry_confirm_total"));
            virusSixth.setHubeiServer(resultSet.getInt("hubei_severe"));
            virusSixth.setHubeiConfirm(resultSet.getInt("hubei_confirm_total"));
            if (virusSixth.getNationalConfirm() == 0) {
                virusSixth.setNationalSeverity(0.0);
            } else {
                virusSixth.setNationalSeverity(Double.valueOf(df.format((double)virusSixth.getNationalServer() / (double)virusSixth.getNationalConfirm())));
            }

            if (virusSixth.getHubeiConfirm() == 0) {
                virusSixth.setHbSeverity(0.0);
            } else {
                virusSixth.setHbSeverity(Double.valueOf(df.format((double)virusSixth.getHubeiServer() / (double)virusSixth.getHubeiConfirm())));
            }

            if ((virusSixth.getNationalConfirm() - virusSixth.getHubeiConfirm()) <= 0) {
                virusSixth.setExceptHbSeverity(0.0);
            } else {
                virusSixth.setExceptHbSeverity(Double.valueOf(df.format((double)(virusSixth.getNationalServer() - virusSixth.getHubeiServer()) / (double)(virusSixth.getNationalConfirm() - virusSixth.getHubeiConfirm()))));
            }

            return virusSixth;
        });

        return list;
    }

    public List<VirusSeventh> getSeventhGraph() {
        DecimalFormat df = new DecimalFormat("0.00");
        List<VirusSeventh> list = new ArrayList<>();
        String sql = "select * from (select nowtime,hubei_severe,hubei_server_h,hubei_confirm_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";
        list = whDB.query(sql, (resultSet, i) -> {
            VirusSeventh virusSeventh = new VirusSeventh();
            virusSeventh.setNowtime(resultSet.getString("nowtime"));
            virusSeventh.setHbServer(resultSet.getInt("hubei_severe"));
            virusSeventh.setHbh(resultSet.getInt("hubei_server_h"));
            virusSeventh.setHbConfirm(resultSet.getInt("hubei_confirm_total"));
            if (virusSeventh.getHbServer() == 0 || (virusSeventh.getHbServer() + virusSeventh.getHbh() == 0)) {
                virusSeventh.setFirst(0.0);
            } else {
                virusSeventh.setFirst(Double.valueOf(df.format((double) virusSeventh.getHbServer() / (double)(virusSeventh.getHbServer() + virusSeventh.getHbh()))));
            }

            if (virusSeventh.getHbConfirm() == 0) {
                virusSeventh.setSecond(0.0);
            } else {
                virusSeventh.setSecond(Double.valueOf(df.format((double)virusSeventh.getHbh() / (double)virusSeventh.getHbConfirm())));
            }
            return virusSeventh;
        });

        return list;
    }

    public List<VirusEighth> getEighthGraph() {
        List<VirusEighth> list = new ArrayList<>();
        String sql = "select * from (select nowtime,contry_cure_total,contry_dead_total from Ncov_statis ORDER BY nowtime desc limit 14) aa ORDER BY aa.nowtime";
        list = whDB.query(sql, (resultSet, i) -> {
            VirusEighth virusEighth = new VirusEighth();
            virusEighth.setNowtime(resultSet.getString("nowtime"));
            virusEighth.setNationCure(resultSet.getInt("contry_cure_total"));
            virusEighth.setNationDead(resultSet.getInt("contry_dead_total"));

            return virusEighth;
        });

        return list;
    }


    public List<NotWH> getNotWhAll() {
        List<NotWH> list = null;
        list = whDB.query(fourSQL, (resultSet, i) -> {
                    NotWH notWH = new NotWH();
                    notWH.setFeiHbSum(resultSet.getString("feiHbSum"));
                    notWH.setFeiHbNew(resultSet.getString("feiHbNew"));
                    notWH.setTime(resultSet.getString("time"));
                    return notWH;
                }
        );

        return list;
    }


    public List<SiWangLv> getAllSiWang() {
        List<SiWangLv> list = null;
        list = whDB.query(SiWangSQL, (resultSet, i) -> {
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

    private static final String NineSql="select * from (select hubei_lv,round((contry_confirm_total-hubei_confirm_total)/(contry_touch_total-hubei_touch) ,2) as not_hubei_lv,time from (\n" +
            "(select hubei_confirm_total,hubei_touch,round(hubei_confirm_total/hubei_touch,2) as hubei_lv,nowtime from Ncov_statis ORDER BY nowtime desc limit 14)a INNER JOIN\n" +
            "(select contry_confirm_total,contry_touch_total ,nowtime as time from Ncov_statis ORDER BY nowtime desc limit 14)b on a.nowtime=b.time\n" +
            "))c order by c.time;";
    public List<VirusNine> getBl(){
        List<VirusNine> list = null;
        list = whDB.query(NineSql, (resultSet, i) -> {
                    VirusNine virusNine = new VirusNine();
                    virusNine.setHubei_lv(resultSet.getString("hubei_lv"));
                    virusNine.setNot_hubei_lv(resultSet.getString("not_hubei_lv"));
                    virusNine.setTime(resultSet.getString("time"));
                    return virusNine;
                }
        );
        return list;
    }

    private static final String TweleSQL="select * from (select contry_cure_total,contry_confirm_today,nowtime from Ncov_statis ORDER BY nowtime desc limit 14)a order by a.nowtime;";
    public List<VirusTwelve> getNum(){
        List<VirusTwelve> list = null;
        list = whDB.query(TweleSQL, (resultSet, i) -> {
                    VirusTwelve virusTwelve = new VirusTwelve();
                    virusTwelve.setContry_cure_total(resultSet.getString("contry_cure_total"));
                    virusTwelve.setContry_confirm_today(resultSet.getString("contry_confirm_today"));
                    virusTwelve.setTime(resultSet.getString("nowtime"));
                    return virusTwelve;
                }
        );
        return list;
    }

    private static final String ThirteenSql="select * from (select contry_lv,hubei_lv,round((contry_cure_total-hubei_cure_total)/(contry_confirm_total-hubei_confirm_total),3) as not_hb_lv,time1 from (\n" +
            "(select contry_cure_total,if(contry_confirm_total is null or contry_confirm_total=0,1,contry_confirm_total) as contry_confirm_total ,round(contry_cure_total/contry_confirm_total,2) as contry_lv,nowtime from Ncov_statis  ORDER BY nowtime desc limit 14)a INNER JOIN \n" +
            "(select hubei_cure_total, if(hubei_confirm_total is null or hubei_confirm_total=0,1,hubei_confirm_total) as hubei_confirm_total,round(hubei_cure_total/hubei_confirm_total,2) as hubei_lv,nowtime as time1 from Ncov_statis  ORDER BY nowtime desc limit 14)b on a.nowtime=b.time1\n" +
            " ))c order by c.time1;";

    public List<VirusThirteen> getNums(){
        List<VirusThirteen> list = null;
        list = whDB.query(ThirteenSql, (resultSet, i) -> {
                    VirusThirteen virusThirteen = new VirusThirteen();
                    virusThirteen.setContry_lv(resultSet.getString("contry_lv"));
                    virusThirteen.setHubei_lv(resultSet.getString("hubei_lv"));
                    virusThirteen.setNot_hb_lv(resultSet.getString("not_hb_lv"));
                    virusThirteen.setTime(resultSet.getString("time1"));
                    return virusThirteen;
                }
        );
        return list;
    }

    public static void main(String[] args) {
        int a = 120;
        int b = 1345;
        double c = a/b;
        System.out.println(c);
    }

}
