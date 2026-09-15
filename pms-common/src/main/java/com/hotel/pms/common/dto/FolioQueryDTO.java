package com.hotel.pms.common.dto;

import com.hotel.pms.common.result.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FolioQueryDTO extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long hotelId;
    private String folioType;
    private String status;
    private String guestName;
    private String teamName;
    private Long stayId;
    /** 付款状态: PAID-已付清, UNPAID-未付清 */
    private String payStatus;
    /** 付款日期-开始 */
    private LocalDate payDateStart;
    /** 付款日期-结束 */
    private LocalDate payDateEnd;
}
