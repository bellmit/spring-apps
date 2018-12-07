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
    @ConfigurationProperties("spring.datasource.dataetl")
    public DataSourceProperties dataetlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.dataetl")
    public DataSource dataetlDataSource() {
        return dataetlDataSourceProperties().initializeDataSourceBuilder().build();
    }


    @Bean(name = "bisysJdbc")
    public JdbcTemplate bisysJdbcTemplate() {
        return new JdbcTemplate(bisysDataSource());
    }

    @Bean
    @ConfigurationProperties("spring.datasource.bisys")
    public DataSourceProperties bisysDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.bisys")
    public DataSource bisysDataSource() {
        return bisysDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "yjkMallJdbc")
    public JdbcTemplate yjkMallJdbcTemplate() {
        return new JdbcTemplate(yjkMallDataSource());
    }

    @Bean
    @ConfigurationProperties("spring.datasource.yjk-mall")
    public DataSourceProperties yjkMallDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.yjk-mall")
    public DataSource yjkMallDataSource() {
        return yjkMallDataSourceProperties().initializeDataSourceBuilder().build();
    }
}
