package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公安上传记录实体类
 * <p>
 * 记录客人信息上传公安系统的情况
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("police_upload_record")
public class PoliceUploadRecord extends BaseEntity {

    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;

    /** 入住单ID */
    @TableField("stay_id")
    private Long stayId;

    /** 入住单号 */
    @TableField("stay_no")
    private String stayNo;

    /** 客人ID */
    @TableField("guest_id")
    private Long guestId;

    /** 客人姓名 */
    @TableField("guest_name")
    private String guestName;

    /** 证件类型 */
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

    /** 入住时间 */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    /** 预计离店时间 */
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    /** 房间号 */
    @TableField("room_no")
    private String roomNo;

    /** 上传状态：PENDING-待上传/UPLOADING-上传中/SUCCESS-成功/FAILED-失败 */
    @TableField("status")
    private String status;

    /** 上传时间 */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /** 失败原因 */
    @TableField("error_message")
    private String errorMessage;

    /** 重试次数 */
    @TableField("retry_count")
    private Integer retryCount;

    /** 是否人工补传 */
    @TableField("is_manual")
    private Boolean isManual;

    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
