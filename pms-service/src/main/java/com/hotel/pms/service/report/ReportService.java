package com.hotel.pms.service.report;

import java.time.LocalDate;
import java.util.Map;

/**
 * 报表服务接口
 */
public interface ReportService {
    
    /**
     * 获取收银员交接表数据
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 交接表数据
     */
    Map<String, Object> getShiftReport(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台入账明细
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 入账明细列表
     */
    Map<String, Object> getEntryDetail(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台入账简表
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 入账简表数据
     */
    Map<String, Object> getEntrySummary(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台入账汇总
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 入账汇总数据
     */
    Map<String, Object> getEntryTotal(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台收款明细
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 收款明细列表
     */
    Map<String, Object> getPaymentDetail(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台收款汇总
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 收款汇总数据
     */
    Map<String, Object> getPaymentSummary(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取前台转账报表
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 转账明细列表
     */
    Map<String, Object> getTransferReport(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取冲账调账报表
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 冲账调账列表
     */
    Map<String, Object> getChargeBackAdjust(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取结账实收统计
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 结账实收统计数据
     */
    Map<String, Object> getCheckoutActualStats(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取结账实收明细
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 结账实收明细列表
     */
    Map<String, Object> getCheckoutActualDetail(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取商品销售汇总
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 商品销售汇总数据
     */
    Map<String, Object> getProductSalesSummary(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
    
    /**
     * 获取商品销售明细
     * @param hotelId 酒店ID
     * @param businessDate 营业日期
     * @param shift 班次
     * @param operatorId 操作人ID
     * @return 商品销售明细列表
     */
    Map<String, Object> getProductSalesDetail(Long hotelId, LocalDate businessDate, String shift, Long operatorId);
}
