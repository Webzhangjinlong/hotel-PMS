package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限数据传输对象（创建/更新权限）
 */
@Data
public class SysPermissionDTO {

    /** 权限名称 */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50")
    private String permissionName;

    /** 权限编码，如：user:create、menu:dashboard */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100")
    private String permissionCode;

    /** 资源类型：MENU-菜单/BUTTON-按钮/API-接口 */
    private String resourceType;

    /** 资源路径：菜单路径或API路径 */
    @Size(max = 200, message = "资源路径长度不能超过200")
    private String resourcePath;

    /** 父权限ID，用于菜单层级 */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 菜单图标 */
    @Size(max = 100, message = "图标长度不能超过100")
    private String icon;

    /** 是否可见：TRUE-可见/FALSE-隐藏 */
    private Boolean isVisible;

    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    private String status;
}
