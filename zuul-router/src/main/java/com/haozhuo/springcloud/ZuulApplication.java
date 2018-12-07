package com.haozhuo.springcloud;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Created by Lucius on 8/15/18.
 */
@SuppressWarnings("deprecation")
@EnableZuulProxy
@SpringBootApplication
class ZuulApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ZuulApplication.class).web(true).run(args);
    }
}
