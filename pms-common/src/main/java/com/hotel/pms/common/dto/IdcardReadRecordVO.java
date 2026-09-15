package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 身份证读取记录响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcardReadRecordVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 记录ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 证件类型：ID_CARD-身份证/PASSPORT-护照/DRIVER_LICENSE-驾驶证 */
    private String cardType;
    
    /** 证件类型名称 */
    private String cardTypeName;
    
    /** 证件号码 */
    private String cardNo;
    
    /** 姓名 */
    private String name;
    
    /** 性别 */
    private String gender;
    
    /** 民族 */
    private String nation;
    
    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    /** 地址 */
    private String address;
    
    /** 签发机关 */
    private String issueOrg;
    
    /** 有效期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validStart;
    
    /** 有效期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validEnd;
    
    /** 照片路径 */
    private String photoPath;
    
    /** 照片数据（Base64编码） */
    private String photoData;
    
    /** 关联客人ID */
    private Long guestId;
    
    /** 关联入住单ID */
    private Long stayId;
    
    /** 操作员ID */
    private Long operatorId;
    
    /** 操作员姓名 */
    private String operatorName;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
