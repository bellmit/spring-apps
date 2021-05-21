package com.haozhuo.datag.service;

import com.google.inject.internal.cglib.core.$DuplicatesPredicate;
import com.haozhuo.datag.com.service.bean.Report;
import com.haozhuo.datag.com.service.bean.ReportContent;
import com.haozhuo.datag.com.service.stdrpt.RptStd;
import com.haozhuo.datag.com.service.stdrpt.UnifMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RptStdService {
    private static final Logger logger = LoggerFactory.getLogger(RptStdService.class);

    public final static Map<String,String[]> indexMap = new HashMap<>();
    public final static Map<String,String[]> sugMap = new HashMap<>();
    public final static Map<String,String> unitMap = new HashMap<>();
    public final static Map<String,String> partMap = new HashMap<>();
    public final static Map<String,String> symptomsMap = new HashMap<>();
    public final static Map<String,String[]> checkIdMap = new HashMap<>();




    //选择jdbc连接池
    private final JdbcTemplate rptstdDB;




    @Scheduled(cron = "0 */30 * * * ?")
    public void execute() {
        inintIndexMap();
        inintSugMap();
        inintCheckIdMap();
    }


    @Autowired
    public RptStdService(@Qualifier("rptstdJdbc") JdbcTemplate jdbcTemplate,Environment env){
        this.rptstdDB = jdbcTemplate;
        inintIndexMap();
        inintSugMap();
        inintPartMap();
        inintSymptomsMap();
        inintUnitMap();
        inintCheckIdMap();
    }


//    @Autowired
//    public RptStdService(JdbcTemplate jdbcTemplate) {
//        this.rptstdDB = jdbcTemplate;
//        inintIndexMap();
//        initSugMap();
//
//    }
    //
    //String indexSql ="select index_name,item_name,index_type,std_index_name,std_item_name,std_type from check_index_name_map ";
    String indexSql = "select * from check_index_name_map  ";
    private void inintIndexMap(){
        //int flag = 0 ;
        logger.info("加载标准化映射表：index_map");
        try{
        List list =rptstdDB.query(indexSql, (resultSet,i)->{
            String[] array = new String[4];
            String std_type;
            String index_name = resultSet.getString("index_name").trim();
            String item_name = resultSet.getString("item_name").trim();
            String index_type = resultSet.getString("index_type").trim();
            String std_index_name = resultSet.getString("std_index_name").trim();
            String std_item_name = resultSet.getString("std_item_name").trim();
            if(resultSet.getString("std_type")==null){
                std_type ="";
                //flag =flag+1;
            }else
            {
                std_type = resultSet.getString("std_type").trim();
                //flag =flag+1;
            }
            array[0] = std_item_name;
            array[1] = std_index_name;
            array[2] = index_type;
            array[3] = std_type;
            indexMap.put(item_name+index_name,array);
            return array;
        });

        }
        catch(Exception ex) {

            logger.debug("initindexmap error", ex);
        }


    }


    String checkIdSql = "select concat(ifnull(item_name,''),'_',ifnull(index_name,'')) as code ,ifnull(check_item_code,'') as check_item_code,state,update_time from data_assets_ms_v2.unif_origin where state in(0,1,3,4,5) and active = 1  ";

    private void inintCheckIdMap(){
        //int flag = 0 ;
        logger.info("加载标准化映射表：checkIdSql");
        try{
            rptstdDB.query(checkIdSql, (resultSet)->{
                String[] array = new String[3];
                String code = resultSet.getString("code").trim();
                String tmpcheckid = resultSet.getString("check_item_code").trim();
                String state = resultSet.getString("state").trim();
                String updatetime = resultSet.getString("update_time").trim();
                array[0] = tmpcheckid;
                array[1] = state;
                array[2] = updatetime;
                checkIdMap.put(code,array);
            });
        }
        catch(Exception ex) {
            logger.debug("symptomsMap error", ex);
        }

    }




    String symptomsSql ="select distinct symptoms from data_assets_ms_v2.nlp_symptoms ";

    private void inintSymptomsMap(){
        //int flag = 0 ;
        logger.info("加载标准化映射表：symptomsMap");
        try{
            rptstdDB.query(symptomsSql, (resultSet)->{
                String part = resultSet.getString("symptoms").trim();
                symptomsMap.put(part,part);
            });
        }
        catch(Exception ex) {
            logger.debug("symptomsMap error", ex);
        }

    }


    String partSql ="select distinct part from data_assets_ms_v2.nlp_part ";
    private void inintPartMap(){
        //int flag = 0 ;
        logger.info("加载标准化映射表：partMap");
        try{
            rptstdDB.query(partSql, (resultSet)->{
                String part = resultSet.getString("part").trim();
                partMap.put(part,part);
            });
        }
        catch(Exception ex) {
            logger.debug("partMap error", ex);
        }

    }


    String unitSql = "select unit,stdunit from data_assets_ms_v2.unif_unit";
    private void inintUnitMap(){
        //int flag = 0 ;
        logger.info("加载标准化映射表：unif_unit");
        try{
            rptstdDB.query(unitSql, (resultSet)->{
                String keystr = resultSet.getString("unit").trim();
                String  rulestr = resultSet.getString("stdunit").trim();
                unitMap.put(keystr,rulestr);
            });
        }
        catch(Exception ex) {
            logger.debug("unif_unit error", ex);
        }

    }

    private void inintSugMap(){

        logger.info("加载标准化映射表：sug_map");
        try {
        rptstdDB.query("select sug_name,std_sug_name,body,check_mode,abnormal_label from check_sug_name_map ",(resultSet,i)->{
            String[] array = new String[4];
            String sug_name = resultSet.getString("sug_name").trim();
            String std_sug_name = resultSet.getString("std_sug_name").trim();
            String body = resultSet.getString("body").trim();
            String check_mode = resultSet.getString("check_mode").trim();
            String abnormal_label = resultSet.getString("abnormal_label").trim();
            array[0] = std_sug_name;
            array[1] = body;
            array[2] = check_mode;
            array[3] = abnormal_label;
            sugMap.put(sug_name,array);
            return array;
        });}
        catch(Exception ex) {
            logger.debug("initsugmap error", ex);
        }

    }

    public Report rptStd(String rptjson){
       return RptStd.rptStd(rptjson);
    }

    public ReportContent rptStdSd(String rptjson){
        return UnifMethod.stdReport(rptjson);
    }

}
