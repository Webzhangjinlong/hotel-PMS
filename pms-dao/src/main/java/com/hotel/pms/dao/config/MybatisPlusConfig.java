package com.hotel.pms.dao.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 * <p>
 * 配置分页插件、乐观锁插件、自动填充等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.hotel.pms.dao.mapper")
public class MybatisPlusConfig {
    
    /**
     * 配置MybatisPlus拦截器
     * <p>
     * 包含分页插件和乐观锁插件
     * </p>
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 【分页插件】支持PostgreSQL分页
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        paginationInterceptor.setMaxLimit(100L); // 单页最大100条
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 【乐观锁插件】防止并发更新冲突
        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
        interceptor.addInnerInterceptor(optimisticLockerInterceptor);
        
        return interceptor;
    }
    
    /**
     * 配置自动填充处理器
     * <p>
     * 自动填充createdAt、updatedAt字段
     * </p>
     * 
     * @return MetaObjectHandler
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 【插入填充】自动填充创建时间和更新时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
            
            @Override
            public void updateFill(MetaObject metaObject) {
                // 【更新填充】自动填充更新时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}