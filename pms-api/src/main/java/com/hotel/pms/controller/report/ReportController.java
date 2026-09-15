package com.hotel.pms.controller.report;

import com.hotel.pms.service.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 报表控制器
 */
@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    /**
     * 获取收银员交接表
     */
    @GetMapping("/shift")
    public Map<String, Object> getShiftReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getShiftReport(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台入账明细
     */
    @GetMapping("/entry/detail")
    public Map<String, Object> getEntryDetail(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getEntryDetail(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台入账简表
     */
    @GetMapping("/entry/summary")
    public Map<String, Object> getEntrySummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getEntrySummary(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台入账汇总
     */
    @GetMapping("/entry/total")
    public Map<String, Object> getEntryTotal(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getEntryTotal(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台收款明细
     */
    @GetMapping("/payment/detail")
    public Map<String, Object> getPaymentDetail(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getPaymentDetail(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台收款汇总
     */
    @GetMapping("/payment/summary")
    public Map<String, Object> getPaymentSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getPaymentSummary(businessDate, shift, operatorId);
    }
    
    /**
     * 获取前台转账报表
     */
    @GetMapping("/transfer")
    public Map<String, Object> getTransferReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getTransferReport(businessDate, shift, operatorId);
    }
    
    /**
     * 获取冲账调账报表
     */
    @GetMapping("/chargeback")
    public Map<String, Object> getChargeBackAdjust(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getChargeBackAdjust(businessDate, shift, operatorId);
    }
    
    /**
     * 获取结账实收统计
     */
    @GetMapping("/checkout/stats")
    public Map<String, Object> getCheckoutActualStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getCheckoutActualStats(businessDate, shift, operatorId);
    }
    
    /**
     * 获取结账实收明细
     */
    @GetMapping("/checkout/detail")
    public Map<String, Object> getCheckoutActualDetail(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getCheckoutActualDetail(businessDate, shift, operatorId);
    }
    
    /**
     * 获取商品销售汇总
     */
    @GetMapping("/product/summary")
    public Map<String, Object> getProductSalesSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getProductSalesSummary(businessDate, shift, operatorId);
    }
    
    /**
     * 获取商品销售明细
     */
    @GetMapping("/product/detail")
    public Map<String, Object> getProductSalesDetail(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate businessDate,
            @RequestParam String shift,
            @RequestParam(required = false) Long operatorId) {
        return reportService.getProductSalesDetail(businessDate, shift, operatorId);
    }
}
