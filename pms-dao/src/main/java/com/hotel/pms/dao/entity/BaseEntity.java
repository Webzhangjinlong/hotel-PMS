package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有实体类都继承此基类，包含公共字段
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public abstract class BaseEntity implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /** 逻辑删除标记：FALSE-未删除/TRUE-已删除 */
    @TableLogic
    private Boolean deleted;
    
    /** 乐观锁版本号 */
    @Version
    private Integer version;
}