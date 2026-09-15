package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.MetricsVO;
import com.hotel.pms.common.result.Result; // 修改这里：从 R 改为 Result
import com.hotel.pms.service.report.MetricsService; // 注意：这里也更新了包路径，指向 report 子包
import com.hotel.pms.common.dto.ChannelRevenueVO;
import com.hotel.pms.common.dto.TrendDataVO;
import com.hotel.pms.common.dto.ReportVO;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "经营指标", description = "酒店经营数据分析接口")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping
    @Operation(summary = "获取经营指标数据")
    public Result<MetricsVO> getMetrics( // 修改这里：返回类型改为 Result
                                         @RequestParam Long hotelId,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(metricsService.getMetrics(hotelId, startDate, endDate)); // 修改这里：使用 Result.success()
    }
    
    /**
     * 获取渠道收入占比数据
     * <p>
     * 按预订渠道统计收入占比
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 渠道收入占比列表
     */
    @GetMapping("/channel-revenues")
    @Operation(summary = "获取渠道收入占比", description = "按预订渠道统计收入占比")
    public Result<List<ChannelRevenueVO>> getChannelRevenues(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<ChannelRevenueVO> result = metricsService.getChannelRevenues(hotelId, startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取经营趋势数据
     * <p>
     * 按日期统计经营指标趋势
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据列表
     */
    @GetMapping("/trend-data")
    @Operation(summary = "获取经营趋势", description = "按日期统计经营指标趋势")
    public Result<List<TrendDataVO>> getTrendData(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<TrendDataVO> result = metricsService.getTrendData(hotelId, startDate, endDate);
        return Result.success(result);
    }
    
    /**
     * 获取完整报表数据
     * <p>
     * 整合基础指标、渠道收入占比、趋势数据
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 完整报表数据
     */
    @GetMapping("/full-report")
    @Operation(summary = "获取完整报表", description = "整合基础指标、渠道收入占比、趋势数据")
    public Result<ReportVO> getFullReport(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        ReportVO result = metricsService.getFullReport(hotelId, startDate, endDate);
        return Result.success(result);
    }
}
