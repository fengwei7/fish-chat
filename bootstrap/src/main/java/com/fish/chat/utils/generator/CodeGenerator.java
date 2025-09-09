package com.fish.chat.utils.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.ibatis.annotations.Mapper;

import java.nio.file.Paths;
import java.util.Collections;

public class CodeGenerator {

    /**
     * MybatisPlus 代码生成器
     * 不怎么好用，待优化
     * 生成内容中Controller没有方法，暂时使用EasyCode单独生成Controller
     * 最优解@Qwen3-coder、@豆包
     * @param args
     * @Author weii
     */
    public static void main(String[] args) {
        String dbUrl = "jdbc:mysql://116.205.110.130:3306/fish_chat?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true";
        String dbUsername = "root";
        String dbPassword = "M7PlDMXRsk9_wPReR";

        FastAutoGenerator.create(dbUrl, dbUsername, dbPassword)
                .globalConfig(builder -> builder
                        .author("fish-chat")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/bootstrap/src/main/java")
                        .commentDate("yyyy-MM-dd")
                        .dateType(DateType.ONLY_DATE)
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent("com.fish.chat")
                        .entity("entity")
                        .mapper("mapper.mysql")
                        .service("service")
                        .serviceImpl("service.impl")
                )
                .strategyConfig(builder -> builder
                        .addTablePrefix("t_", "sys_") // 设置过滤表前缀
                        .addInclude("t_table") // 设置需要生成的表名
                        .entityBuilder()
                        .enableLombok()
                        .logicDeleteColumnName("deleted")
                        .build()
                        .mapperBuilder()
                        .mapperAnnotation(Mapper.class)
                        .build()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .build()
                        .controllerBuilder()
                        .enableRestStyle()
                        .build()

                )
                .templateConfig(builder -> builder
                        .disable(TemplateType.CONTROLLER)
                )
                .injectionConfig(builder -> builder
                        .beforeOutputFile((tableInfo, objectMap) -> {
                            // 自定义配置
                            System.out.println("tableInfo: " + tableInfo.getEntityName());
                        })
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}