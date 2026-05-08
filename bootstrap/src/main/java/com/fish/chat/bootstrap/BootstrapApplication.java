package com.fish.chat.bootstrap;

import com.fish.chat.common.properties.FileUploadProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import lombok.extern.slf4j.Slf4j;

/**
 * Fish-Chat 应用启动类
 */
@SpringBootApplication(scanBasePackages = {"com.fish.chat"})
@MapperScan("com.fish.chat.core.mapper")
@EnableMongoRepositories(basePackages = "com.fish.chat.core.repository")
@EnableCaching
@EnableConfigurationProperties(FileUploadProperties.class)
@Slf4j
public class BootstrapApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
        log.info("\n" +
                "    _______      __    ________          __  _____ __             __           __\n" +
                "   / ____(_)____/ /_  / ____/ /_  ____ _/ /_/ ___// /_____ ______/ /____  ____/ /\n" +
                "  / /_  / / ___/ __ \\/ /   / __ \\/ __ `/ __/\\__ \\/ __/ __ `/ ___/ __/ _ \\/ __  / \n" +
                " / __/ / (__  ) / / / /___/ / / / /_/ / /_ ___/ / /_/ /_/ / /  / /_/  __/ /_/ /  \n" +
                "/_/   /_/____/_/ /_/\\____/_/ /_/\\__,_/\\__//____/\\__/\\__,_/_/   \\__/\\___/\\__,_/   \n" +
                "                                                                                 ");
    }
}
