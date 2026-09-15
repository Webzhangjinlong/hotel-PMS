package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入住同住人实体类
 * <p>
 * 对应数据库表：stay_guest
 * 记录入住单关联的所有客人（主客人 + 同住人）
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stay_guest")
public class StayGuest extends BaseEntity {

    /** 入住单ID */
    @TableField("stay_id")
    private Long stayId;

    /** 客人档案ID（可为空，散客未建档时） */
    @TableField("guest_id")
    private Long guestId;

    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;

    /** 证件类型：ID_CARD-身份证/PASSPORT-护照 */
    @TableField("id_type")
    private String idType;

    /** 证件号码 */
    @TableField("id_no")
    private String idNo;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 性别 */
    @TableField("gender")
    private String gender;

    /** 是否主客人（负责结账的人） */
    @TableField("is_primary")
    private Boolean isPrimary;
}
