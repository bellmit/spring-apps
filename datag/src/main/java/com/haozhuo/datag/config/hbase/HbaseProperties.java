package com.haozhuo.datag.config.hbase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 用来读取hbase配置文件
 */
//@ConfigurationProperties(prefix = "hbase")
public class HbaseProperties {


    private Map<String,String> config;

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {

        this.config = config;

    }
}