package com.hotel.pms.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 身份证读取记录实体类
 * <p>
 * 对应数据库表：idcard_read_record
 * 存储身份证读取的记录信息
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("idcard_read_record")
public class IdcardReadRecord extends BaseEntity {
    
    /** 酒店ID */
    @TableField("hotel_id")
    private Long hotelId;
    
    /** 设备ID */
    @TableField("device_id")
    private Long deviceId;
    
    /** 证件类型：ID_CARD-身份证/PASSPORT-护照/DRIVER_LICENSE-驾驶证 */
    @TableField("card_type")
    private String cardType;
    
    /** 证件号码 */
    @TableField("card_no")
    private String cardNo;
    
    /** 姓名 */
    @TableField("name")
    private String name;
    
    /** 性别 */
    @TableField("gender")
    private String gender;
    
    /** 民族 */
    @TableField("nation")
    private String nation;
    
    /** 出生日期 */
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    /** 地址 */
    @TableField("address")
    private String address;
    
    /** 签发机关 */
    @TableField("issue_org")
    private String issueOrg;
    
    /** 有效期开始 */
    @TableField("valid_start")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;
    
    /** 有效期结束 */
    @TableField("valid_end")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;
    
    /** 照片路径 */
    @TableField("photo_path")
    private String photoPath;
    
    /** 照片数据（Base64编码） */
    @TableField("photo_data")
    private String photoData;
    
    /** 关联客人ID */
    @TableField("guest_id")
    private Long guestId;
    
    /** 关联入住单ID */
    @TableField("stay_id")
    private Long stayId;
    
    /** 操作员ID */
    @TableField("operator_id")
    private Long operatorId;
    
    /** 操作员姓名 */
    @TableField("operator_name")
    private String operatorName;
}
