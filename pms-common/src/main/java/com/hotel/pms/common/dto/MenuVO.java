package com.hotel.pms.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
public class MenuVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 菜单ID */
    private Long id;
    
    /** 菜单名称 */
    private String name;
    
    /** 权限编码 */
    private String code;
    
    /** 菜单路径 */
    private String path;
    
    /** 图标 */
    private String icon;
    
    /** 排序号 */
    private Integer sortOrder;
    
    /** 子菜单 */
    private List<MenuVO> children;
}