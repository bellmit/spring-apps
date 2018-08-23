package com.haozhuo.bisys.config.jdbc;

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
@Configuration
public class JdbcConfig {
    @Primary
    @Bean(name = "bisysJdbc")
    public JdbcTemplate dataetlJdbcTemplate() {
        return new JdbcTemplate(dataetlDataSource());
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.bisys")
    public DataSourceProperties dataetlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.bisys")
    public DataSource dataetlDataSource() {
        return dataetlDataSourceProperties().initializeDataSourceBuilder().build();
    }
}
