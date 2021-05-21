package com.haozhuo.datag.config.jdbc;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by Lucius on 8/13/18.
 */
@SuppressWarnings("ALL")
@Configuration
public class JdbcConfig {
    @Primary
    @Bean(name = "dataetlJdbc")
    public JdbcTemplate dataetlJdbcTemplate() {
        return new JdbcTemplate(dataetlDataSource());
    }

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dataetl")
    public DataSourceProperties dataetlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.dataetl")
    public DataSource dataetlDataSource() {
        return dataetlDataSourceProperties().initializeDataSourceBuilder().build();
    }


    @Bean(name = "bisysJdbc")
    public JdbcTemplate bisysJdbcTemplate() {
        return new JdbcTemplate(bisysDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.bisys")
    public DataSourceProperties bisysDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.bisys")
    public DataSource bisysDataSource() {
        return bisysDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     *
     * @return
     */
    @Bean(name = "yjkMallJdbc")
    public JdbcTemplate yjkMallJdbcTemplate() {
        return new JdbcTemplate(yjkMallDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.yjk-mall")
    public DataSourceProperties yjkMallDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.yjk-mall")
    public DataSource yjkMallDataSource() {
        return yjkMallDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     *
     * @return
     */
    @Bean(name = "whhaozhuoJdbc")
    public JdbcTemplate whhaozhuoJdbcTemplate() {
        return new JdbcTemplate(wuhanhaozhuoDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.whhaozhuo")
    public DataSourceProperties whhaozhuoDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.whhaozhuo")
    public DataSource wuhanhaozhuoDataSource() {
        return whhaozhuoDataSourceProperties().initializeDataSourceBuilder().build();
    }



    @Bean(name = "rptstdJdbc")
    public JdbcTemplate rptStdJdbcTemplate() {
        return new JdbcTemplate(rptstdDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.rptstd")
    public DataSourceProperties rptStdDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.rptstd")
    public DataSource rptstdDataSource() {
        return rptStdDataSourceProperties().initializeDataSourceBuilder().build();
    }


    @Bean(name = "yjkrepot")
    public JdbcTemplate rptStdSdJdbcTemplate() {
        return new JdbcTemplate(rptstdSdDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.yjkrepot")
    public DataSourceProperties rptStdSdDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.yjkrepot")
    public DataSource rptstdSdDataSource() {
        return rptStdSdDataSourceProperties().initializeDataSourceBuilder().build();
    }


    @Bean(name = "newyjkrepot")
    public JdbcTemplate newrptStdSdJdbcTemplate() {
        return new JdbcTemplate(newrptstdSdDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.newyjkrepot")
    public DataSourceProperties newrptStdSdDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.newyjkrepot")
    public DataSource newrptstdSdDataSource() {
        return newrptStdSdDataSourceProperties().initializeDataSourceBuilder().build();
    }





}
