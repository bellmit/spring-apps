package com.haozhuo.springcloud;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Created by Lucius on 8/15/18.
 */
@EnableEurekaServer
@SpringBootApplication
class EurekaServerApplication {
    public static void main(String[] args) {
        //noinspection deprecation
        new SpringApplicationBuilder(EurekaServerApplication.class).web(true).run(args);
    }
}
