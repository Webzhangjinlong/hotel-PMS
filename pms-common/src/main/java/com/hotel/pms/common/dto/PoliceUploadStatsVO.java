package com.hotel.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 公安上传统计VO
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliceUploadStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日上传数 */
    private Long todayCount;

    /** 成功数 */
    private Long successCount;

    /** 失败数 */
    private Long failCount;

    /** 待上传数 */
    private Long pendingCount;
}
