package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预订查询请求DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReservationQueryDTO extends PageRequest implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 酒店ID
     */
    private Long hotelId;

    /**
     * 预订状态
     */
    private String status;

    /**
     * 房型ID
     */
    private Long roomTypeId;

    /**
     * 客人姓名（模糊查询）
     */
    private String guestName;

    /**
     * 客人电话
     */
    private String guestPhone;

    /**
     * 预订号
     */
    private String reservationNo;

    /**
     * 入住日期-开始
     */
    private LocalDate checkInDateStart;

    /**
     * 入住日期-结束
     */
    private LocalDate checkInDateEnd;

    /** 排除的状态（多个用逗号分隔，如：CHECKED_IN） */
    private String excludeStatus;
}

