package com.hotel.pms.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房卡请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class RoomCardDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 卡号 */
    private String cardNo;
    
    /** 卡类型：GUEST_CARD-客人卡/STAFF_CARD-员工卡/MASTER_CARD-总控卡 */
    private String cardType;
    
    /** 房间ID */
    @NotNull(message = "房间ID不能为空")
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
    @NotNull(message = "有效期开始不能为空")
    private LocalDateTime validStart;
    
    /** 有效期结束 */
    @NotNull(message = "有效期结束不能为空")
    private LocalDateTime validEnd;
}
