package com.jiahe.intellipichub;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

// 用分表spaceId 必须不能为 null；分表后代码很多地方要改，要加spaceId太麻烦了
// 且动态分库分表的实现非常麻烦,不一定提高性能，非必要不分表，所以先注释掉
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAsync
@MapperScan("com.jiahe.intellipichub.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class IntelliPicHubApplication {

    public static void main(String[] args) {

        SpringApplication.run(IntelliPicHubApplication.class, args);
    }

}



