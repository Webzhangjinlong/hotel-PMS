package com.hotel.pms.common.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 公安上传记录查询DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
public class PoliceUploadQueryDTO {

    /** 酒店ID */
    private Long hotelId;

    /** 上传状态 */
    private String status;

    /** 客人姓名 */
    private String guestName;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 20;
}
