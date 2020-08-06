package com.haozhuo.datag.service;

import com.haozhuo.datag.com.service.bean.Report;
import com.haozhuo.datag.com.service.stdrpt.RptStd;
import lombok.experimental.var;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RptStdService {
    private static final Logger logger = LoggerFactory.getLogger(RptStdService.class);

    public final static List<String> stopWords = new ArrayList<>();
    public final static Map<String,String[]> indexMap = new HashMap<>();
    public final static Map<String,String[]> sugMap = new HashMap<>();
    //@Autowired
    @Qualifier("rptstdJdbc") //选择jdbc连接池
    private  JdbcTemplate rptstdDB;

    public RptStdService(JdbcTemplate jdbcTemplate ,Environment env){
        this.rptstdDB = jdbcTemplate;
        inintIndexMap();
        initSugMap();
    }


//    @Autowired
//    public RptStdService(JdbcTemplate jdbcTemplate) {
//        this.rptstdDB = jdbcTemplate;
//        inintIndexMap();
//        initSugMap();
//
//    }
    //
    String indexSql ="select index_name,item_name,index_type,std_index_name,std_item_name,std_type from check_index_name_map ";
    private void inintIndexMap(){
        logger.info("加载标准化映射表：index_map");
        try{
        rptstdDB.query(indexSql, (resultSet,i)->{
            String[] array = new String[4];
            String std_type;
            String index_name = resultSet.getString("index_name").trim();
            String item_name = resultSet.getString("item_name").trim();
            String index_type = resultSet.getString("index_type").trim();
            String std_index_name = resultSet.getString("std_index_name").trim();
            String std_item_name = resultSet.getString("std_item_name").trim();
            if(resultSet.getString("std_type")==null){
                std_type ="";
            }else
            {
                std_type = resultSet.getString("std_type").trim();

            }
            array[0] = std_item_name;
            array[1] = std_index_name;
            array[2] = index_type;
            array[3] = std_type;

            indexMap.put(item_name+index_name,array);
            return array;
        });}
        catch(Exception ex) {
            logger.debug("initindexmap error", ex);
        }
    }

    private void initSugMap(){

        logger.info("加载标准化映射表：sug_map");
        try {
        rptstdDB.query("select sug_name,std_sug_name,body,check_mode,abnormal_label from check_sug_name_map",(resultSet,i)->{
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

}
