package com.hotel.pms.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PMS酒店管理系统启动类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.hotel.pms")
@EnableScheduling
public class PmsApplication {
    
    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PmsApplication.class, args);
    }
}
