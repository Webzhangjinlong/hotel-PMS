package com.hotel.pms.dao.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置类
 * <p>
 * 配置HikariCP连接池，优化并发性能
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
public class DataSourceConfig {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;
    
    /**
     * 配置数据源
     * <p>
     * HikariCP连接池配置，优化高并发场景
     * </p>
     * 
     * @return DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // 【基础配置】
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 【连接池配置】优化高并发
        config.setMinimumIdle(10);              // 最小空闲连接数
        config.setMaximumPoolSize(50);          // 最大连接数
        config.setConnectionTimeout(30000);     // 连接超时时间：30秒
        config.setIdleTimeout(600000);          // 空闲超时时间：10分钟
        config.setMaxLifetime(1800000);         // 最大生命周期：30分钟
        config.setLeakDetectionThreshold(60000); // 泄漏检测阈值：60秒
        
        // 【连接池名称】便于监控
        config.setPoolName("PMS-HikariPool");
        
        // 【PostgreSQL优化参数】
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(config);
    }
}