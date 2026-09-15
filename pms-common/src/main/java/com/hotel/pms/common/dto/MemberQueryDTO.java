package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 会员查询DTO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQueryDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long hotelId;
    private String phone;
    private String name;
    private String levelCode;
    private String status;
}
