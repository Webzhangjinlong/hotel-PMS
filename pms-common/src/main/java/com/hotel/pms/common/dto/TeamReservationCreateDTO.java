package com.hotel.pms.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

/**
 * 团队预订创建请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class TeamReservationCreateDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;
    
    /** 团队/公司名称 */
    @NotBlank(message = "团队名称不能为空")
    private String teamName;
    
    /** 主联系人姓名 */
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    
    /** 主联系人电话 */
    @NotBlank(message = "联系人电话不能为空")
    private String contactPhone;
    
    /** 主联系人证件号 */
    private String contactIdNo;
    
    /** 入住日期 */
    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;
    
    /** 离店日期 */
    @NotNull(message = "离店日期不能为空")
    private LocalDate checkOutDate;
    
    /** 结算方式：UNIFIED/SEPARATE */
    @NotBlank(message = "结算方式不能为空")
    private String settlementType;
    
    /** 特殊要求 */
    private String specialRequests;
    
    /** 房价码ID（可选，为空则使用默认价格） */
    private Long pricePlanId;
    
    /** 房间明细列表 */
    @NotEmpty(message = "房间明细不能为空")
    @Valid
    private List<RoomDetail> rooms;
    
    @Data
    public static class RoomDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /** 房型ID */
        @NotNull(message = "房型不能为空")
        private Long roomTypeId;
        
        /** 房间ID（可选，预分房时填写） */
        private Long roomId;
        
        /** 客人姓名 */
        @NotBlank(message = "客人姓名不能为空")
        private String guestName;
        
        /** 客人电话 */
        @NotBlank(message = "客人电话不能为空")
        private String guestPhone;
        
        /** 客人证件号 */
        private String guestIdNo;
        
        /** 客人性别 */
        private String guestGender;
        
        /** 金额 */
        private BigDecimal amount;
    }
}
