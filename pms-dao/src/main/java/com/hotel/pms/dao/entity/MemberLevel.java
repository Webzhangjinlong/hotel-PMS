package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级配置实体类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_level")
public class MemberLevel extends BaseEntity {

    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;

    /** 等级编码：NORMAL/SILVER/GOLD/DIAMOND */
    @TableField("level_code")
    private String levelCode;

    /** 等级名称 */
    @TableField("level_name")
    private String levelName;

    /** 房价折扣率（100=原价，95=95折） */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /** 积分倍率 */
    @TableField("points_multiplier")
    private BigDecimal pointsMultiplier;

    /** 升级所需最低累计消费金额 */
    @TableField("min_total_consumption")
    private BigDecimal minTotalConsumption;

    /** 升级所需最低入住次数 */
    @TableField("min_stay_count")
    private Integer minStayCount;

    /** 权益描述 */
    @TableField("benefits_desc")
    private String benefitsDesc;

    /** 排序序号 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：ACTIVE-启用/INACTIVE-停用 */
    @TableField("status")
    private String status;
}
