package com.hotel.pms.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 分页请求基类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PageRequest implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 页码，默认1 */
    private Integer page = 1;
    
    /** 每页条数，默认10 */
    private Integer size = 10;
    
    /** 排序字段 */
    private String sort;
    
    /** 排序方式：asc/desc */
    private String order = "desc";
    
    /**
     * 获取偏移量
     * 
     * @return 偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }
    
    /**
     * 获取起始行号（从1开始）
     * 
     * @return 起始行号
     */
    public long getStartRow() {
        return getOffset() + 1;
    }
    
    /**
     * 获取结束行号
     * 
     * @return 结束行号
     */
    public long getEndRow() {
        return (long) page * size;
    }
}