package com.hotel.pms.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房卡记录响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCardVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 房卡ID */
    private Long id;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 设备名称 */
    private String deviceName;
    
    /** 卡号 */
    private String cardNo;
    
    /** 卡类型：GUEST_CARD-客人卡/STAFF_CARD-员工卡/MASTER_CARD-总控卡 */
    private String cardType;
    
    /** 卡类型名称 */
    private String cardTypeName;
    
    /** 房间ID */
    private Long roomId;
    
    /** 房间号 */
    private String roomNo;
    
    /** 客人ID */
    private Long guestId;
    
    /** 客人姓名 */
    private String guestName;
    
    /** 入住单ID */
    private Long stayId;
    
    /** 入住单号 */
    private String stayNo;
    
    /** 有效期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validStart;
    
    /** 有效期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validEnd;
    
    /** 卡状态：ACTIVE-有效/EXPIRED-过期/LOST-挂失/CANCELLED-注销 */
    private String cardStatus;
    
    /** 卡状态名称 */
    private String cardStatusName;
    
    /** 操作员ID */
    private Long operatorId;
    
    /** 操作员姓名 */
    private String operatorName;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
