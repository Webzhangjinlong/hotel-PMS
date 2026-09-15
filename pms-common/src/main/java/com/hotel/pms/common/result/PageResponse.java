package com.hotel.pms.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应基类
 * 
 * @param <T> 记录类型
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PageResponse<T> implements Serializable {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;
    
    /** 记录列表 */
    private List<T> records;
    
    /** 总记录数 */
    private long total;
    
    /** 当前页码 */
    private int page;
    
    /** 每页条数 */
    private int size;
    
    /** 总页数 */
    private int pages;
    
    /**
     * 构造分页响应
     * 
     * @param records 记录列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页条数
     */
    public PageResponse(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
    }
    
    /**
     * 创建分页响应
     * 
     * @param records 记录列表
     * @param total 总记录数
     * @param request 分页请求
     * @param <T> 记录类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, long total, PageRequest request) {
        return new PageResponse<>(records, total, request.getPage(), request.getSize());
    }
}