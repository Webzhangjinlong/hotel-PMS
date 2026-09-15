package com.hotel.pms.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * <p>
 * 配置OpenAPI文档信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 配置OpenAPI
     * 
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PMS 酒店管理系统 API")
                        .description("PMS 酒店管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PMS开发团队")
                                .email("dev@hotel.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .schemaRequirement("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT令牌认证"));
    }
}