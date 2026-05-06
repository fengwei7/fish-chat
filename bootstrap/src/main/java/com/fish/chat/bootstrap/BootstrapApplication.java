package com.fish.chat.bootstrap;

import com.fish.chat.common.properties.FileUploadProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Fish-Chat 应用启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fish.chat"})
@MapperScan("com.fish.chat.core.mapper")
@EnableMongoRepositories(basePackages = "com.fish.chat.core.repository")
@EnableCaching
@EnableConfigurationProperties(FileUploadProperties.class)
public class BootstrapApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
        System.out.println("====================================");
        System.out.println("Fish-Chat 启动成功！");
        System.out.println("====================================");
    }
}
