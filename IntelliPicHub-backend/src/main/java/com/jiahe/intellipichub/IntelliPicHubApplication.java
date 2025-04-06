package com.jiahe.intellipichub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jiahe.intellipichub.mapper")
public class IntelliPicHubApplication {

    public static void main(String[] args) {

        SpringApplication.run(IntelliPicHubApplication.class, args);
    }

}



