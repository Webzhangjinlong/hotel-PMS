package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 身份证读取记录查询请求DTO
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdcardReadRecordQueryDTO extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 酒店ID */
    private Long hotelId;
    
    /** 设备ID */
    private Long deviceId;
    
    /** 证件类型：ID_CARD-身份证/PASSPORT-护照/DRIVER_LICENSE-驾驶证 */
    private String cardType;
    
    /** 证件号码 */
    private String cardNo;
    
    /** 姓名 */
    private String name;
    
    /** 关联客人ID */
    private Long guestId;
    
    /** 读取时间开始 */
    private LocalDate readTimeStart;
    
    /** 读取时间结束 */
    private LocalDate readTimeEnd;
}
