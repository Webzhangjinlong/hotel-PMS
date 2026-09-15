package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 积分流水实体类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode
@TableName("member_points_log")
public class MemberPointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;

    /** 会员ID */
    @TableField("member_id")
    private Long memberId;

    /** 变动类型：EARN-获得/EXCHANGE-兑换/REFUND-退还/ADJUST-调整 */
    @TableField("change_type")
    private String changeType;

    /** 变动积分（正数=获得，负数=消耗） */
    @TableField("points")
    private Integer points;

    /** 变动前积分 */
    @TableField("before_points")
    private Integer beforePoints;

    /** 变动后积分 */
    @TableField("after_points")
    private Integer afterPoints;

    /** 关联入住单ID */
    @TableField("related_stay_id")
    private Long relatedStayId;

    /** 关联交易ID */
    @TableField("related_transaction_id")
    private Long relatedTransactionId;

    /** 变动描述 */
    @TableField("description")
    private String description;

    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
