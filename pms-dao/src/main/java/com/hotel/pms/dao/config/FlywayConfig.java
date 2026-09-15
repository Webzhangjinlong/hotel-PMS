package com.hotel.pms.dao.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway数据库迁移配置
 * <p>
 * 配置数据库版本迁移策略
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
public class FlywayConfig {
    
    /**
     * 配置Flyway迁移初始化器
     * <p>
     * 自动执行数据库迁移脚本
     * </p>
     * 
     * @param dataSource 数据源
     * @return FlywayMigrationInitializer
     */
    @Bean
    public FlywayMigrationInitializer flywayInitializer(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(false)
            .outOfOrder(true)
            .load();
        
        return new FlywayMigrationInitializer(flyway);
    }
}