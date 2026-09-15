package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客人响应VO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long hotelId;
    private String name;
    private String idType;
    private String idTypeName;
    private String idNo;
    private String phone;
    private String gender;
    private String nationality;
    private Boolean isVip;
    private Boolean isBlacklisted;
    private String blacklistReason;
    private String remark;
    private Integer stayCount;
    private LocalDateTime lastStayTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 会员信息 */
    private Long memberId;
    private String memberNo;
    private String memberLevelCode;
    private String memberLevelName;
    private Integer availablePoints;
}
