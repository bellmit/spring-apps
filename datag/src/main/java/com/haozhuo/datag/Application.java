package com.haozhuo.datag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class Application{
    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
