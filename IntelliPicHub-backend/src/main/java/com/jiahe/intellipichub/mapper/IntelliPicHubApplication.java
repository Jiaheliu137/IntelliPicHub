package com.jiahe.intellipichub.mapper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.jiahe.intellipichub.mapper")
@EnableAspectJAutoProxy(exposeProxy=true)
public class IntelliPicHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliPicHubApplication.class, args);
    }

}
