package com.fish.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fish.chat.mapper.mysql")
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
        System.out.println(" _____                _    ______ _     _     \n"
            + "|_   _|              | |   |  ___(_)   | |    \n"
            + "  | | ___  _   _  ___| |__ | |_   _ ___| |__  \n"
            + "  | |/ _ \\| | | |/ __| '_ \\|  _| | / __| '_ \\ \n"
            + "  | | (_) | |_| | (__| | | | |   | \\__ \\ | | |\n"
            + "  \\_/\\___/ \\__,_|\\___|_| |_\\_|   |_|___/_| |_|\n"
            + "                                              \n"
            + "                                              ");
    }

}
