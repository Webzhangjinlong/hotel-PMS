package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客人实体类
 * <p>
 * 对应数据库表：guest
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("guest")
public class Guest extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 客人姓名 */
    @TableField("name")
    private String name;
    
    /** 证件类型：ID_CARD-身份证/PASSPORT-护照 */
    @TableField("id_type")
    private String idType;
    
    /** 证件号码 */
    @TableField("id_no")
    private String idNo;
    
    /** 手机号码 */
    @TableField("phone")
    private String phone;
    
    /** 性别 */
    @TableField("gender")
    private String gender;
    
    /** 国籍 */
    @TableField("nationality")
    private String nationality;
    
    /** 是否常住客人（VIP） */
    @TableField("is_vip")
    private Boolean isVip;
    
    /** 是否黑名单 */
    @TableField("is_blacklisted")
    private Boolean isBlacklisted;
    
    /** 黑名单原因 */
    @TableField("blacklist_reason")
    private String blacklistReason;
    
    /** 备注 */
    @TableField("remark")
    private String remark;
    
    /** 入住次数 */
    @TableField("stay_count")
    private Integer stayCount;
    
    /** 最后入住时间 */
    @TableField("last_stay_time")
    private LocalDateTime lastStayTime;
}
