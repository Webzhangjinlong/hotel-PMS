package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员实体类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member")
public class Member extends BaseEntity {

    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;

    /** 关联客人ID */
    @TableField("guest_id")
    private Long guestId;

    /** 会员编号 */
    @TableField("member_no")
    private String memberNo;

    /** 手机号（电子会员卡号） */
    @TableField("phone")
    private String phone;

    /** 会员姓名 */
    @TableField("name")
    private String name;

    /** 当前等级ID */
    @TableField("level_id")
    private Long levelId;

    /** 冗余等级编码 */
    @TableField("level_code")
    private String levelCode;

    /** 当前可用积分 */
    @TableField("total_points")
    private Integer totalPoints;

    /** 已使用积分 */
    @TableField("used_points")
    private Integer usedPoints;

    /** 累计消费金额 */
    @TableField("total_consumption")
    private BigDecimal totalConsumption;

    /** 累计入住次数 */
    @TableField("total_stay_count")
    private Integer totalStayCount;

    /** 注册来源：AUTO-自动/MANUAL-手动 */
    @TableField("register_source")
    private String registerSource;

    /** 注册时间 */
    @TableField("register_time")
    private LocalDateTime registerTime;

    /** 最后入住时间 */
    @TableField("last_stay_time")
    private LocalDateTime lastStayTime;

    /** 状态：ACTIVE-启用/INACTIVE-停用/FROZEN-冻结 */
    @TableField("status")
    private String status;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
