package com.haozhuo.datag.config.hbase;


import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.hbase.HbaseTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 把配置加入到hbaseTemplate中，交给spring容器
 * 再把hbaseAdmin交给spring容器
 */
@Configuration
//@EnableConfigurationProperties(HbaseProperties.class)
public class HbaseConfig {

@Bean
public HbaseTemplate hbaseTemplate(@Value("${hbase.zookeeper.quorum}") String quorum,
                                   @Value("${hbase.zookeeper.port}") String port,
                                   @Value("${hbase.rpt.timeout}")  String rpctimeout,
                                   @Value("${hbase.scanner.timeout.period}")  String ctimeout) {
    HbaseTemplate hbaseTemplate = new HbaseTemplate();
    org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
    conf.set("hbase.zookeeper.quorum", quorum);
    conf.set("hbase.zookeeper.port", port);
    conf.set("hbase.rpt.timeout",rpctimeout);
    conf.set("hbase.scanner.timeout.period",ctimeout);
    conf.set("hbase.client.operation.timeout", "180000");
    hbaseTemplate.setConfiguration(conf);
    hbaseTemplate.setAutoFlush(true);
    return hbaseTemplate;
















}

}

