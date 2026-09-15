package com.hotel.pms.api.controller;

import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.credit.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 挂账公司控制器
 * <p>
 * 处理挂账公司的管理和挂账结算
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/credit-companies")
@RequiredArgsConstructor
@Tag(name = "挂账公司管理", description = "挂账公司的增删改查和结算")
public class CreditCompanyController {
    
    private final CreditService creditService;
    
    /**
     * 分页查询挂账公司
     * 
     * @param queryDTO 查询条件
     * @return 挂账公司列表
     */
    @GetMapping
    @Operation(summary = "查询挂账公司列表", description = "分页查询挂账公司")
    public Result<PageResponse<CreditCompanyVO>> pageList(CreditCompanyQueryDTO queryDTO) {
        PageResponse<CreditCompanyVO> result = creditService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询挂账公司
     * 
     * @param id 挂账公司ID
     * @return 挂账公司信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询挂账公司详情", description = "根据ID查询挂账公司详情")
    public Result<CreditCompanyVO> getById(@Parameter(description = "挂账公司ID") @PathVariable Long id) {
        CreditCompanyVO result = creditService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建挂账公司
     * 
     * @param dto 挂账公司信息
     * @return 挂账公司信息
     */
    @OperationLog(module = "挂账管理", action = "创建挂账公司", targetType = "挂账公司")

    @PostMapping
    @Operation(summary = "创建挂账公司", description = "创建新的挂账公司")
    public Result<CreditCompanyVO> create(@Valid @RequestBody CreditCompanyDTO dto) {
        CreditCompanyVO result = creditService.create(dto);
        return Result.success(result);
    }
    
    /**
     * 更新挂账公司
     * 
     * @param id 挂账公司ID
     * @param dto 挂账公司信息
     * @return 挂账公司信息
     */
    @OperationLog(module = "挂账管理", action = "修改挂账公司", targetType = "挂账公司", targetIdParam = "id")

    @PutMapping("/{id}")
    @Operation(summary = "更新挂账公司", description = "更新挂账公司信息")
    public Result<CreditCompanyVO> update(
            @Parameter(description = "挂账公司ID") @PathVariable Long id,
            @Valid @RequestBody CreditCompanyDTO dto) {
        CreditCompanyVO result = creditService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 挂账结算
     * 
     * @param id 挂账公司ID
     * @param amount 结算金额
     * @param paymentMethod 支付方式
     * @return 操作结果
     */
    @OperationLog(module = "挂账管理", action = "挂账结算", targetType = "挂账公司", targetIdParam = "id")

    @PostMapping("/{id}/settle")
    @Operation(summary = "挂账结算", description = "对挂账公司进行结算")
    public Result<Void> settleCredit(
            @Parameter(description = "挂账公司ID") @PathVariable Long id,
            @Parameter(description = "结算金额") @RequestParam BigDecimal amount,
            @Parameter(description = "支付方式") @RequestParam String paymentMethod) {
        creditService.settleCredit(id, amount, paymentMethod);
        return Result.success();
    }
    
    /**
     * 查询挂账明细
     * 
     * @param id 挂账公司ID
     * @return 挂账明细列表
     */
    @GetMapping("/{id}/transactions")
    @Operation(summary = "查询挂账明细", description = "查询挂账公司的挂账明细")
    public Result<List<TransactionVO>> getCreditTransactions(
            @Parameter(description = "挂账公司ID") @PathVariable Long id) {
        List<TransactionVO> result = creditService.getCreditTransactions(id);
        return Result.success(result);
    }
    
    /**
     * 查询协议价列表
     * <p>
     * 根据协议公司ID查询协议价列表
     * </p>
     * 
     * @param companyId 协议公司ID
     * @return 协议价列表
     */
    @GetMapping("/{companyId}/agreement-prices")
    @Operation(summary = "查询协议价列表", description = "根据协议公司ID查询协议价列表")
    public Result<List<AgreementPriceVO>> getAgreementPrices(
            @Parameter(description = "协议公司ID") @PathVariable Long companyId) {
        List<AgreementPriceVO> result = creditService.getAgreementPrices(companyId);
        return Result.success(result);
    }
    
    /**
     * 创建协议价
     * <p>
     * 为协议公司创建房型协议价格
     * </p>
     * 
     * @param companyId 协议公司ID
     * @param dto 协议价信息
     * @return 协议价信息
     */
    @PostMapping("/{companyId}/agreement-prices")
    @Operation(summary = "创建协议价", description = "为协议公司创建房型协议价格")
    public Result<AgreementPriceVO> createAgreementPrice(
            @Parameter(description = "协议公司ID") @PathVariable Long companyId,
            @Valid @RequestBody AgreementPriceDTO dto) {
        dto.setCreditCompanyId(companyId);
        AgreementPriceVO result = creditService.createAgreementPrice(dto);
        return Result.success(result);
    }
    
    /**
     * 更新协议价
     * <p>
     * 更新指定协议价的信息
     * </p>
     * 
     * @param id 协议价ID
     * @param dto 协议价信息
     * @return 更新后的协议价信息
     */
    @PutMapping("/agreement-prices/{id}")
    @Operation(summary = "更新协议价", description = "更新指定协议价的信息")
    public Result<AgreementPriceVO> updateAgreementPrice(
            @Parameter(description = "协议价ID") @PathVariable Long id,
            @Valid @RequestBody AgreementPriceDTO dto) {
        AgreementPriceVO result = creditService.updateAgreementPrice(id, dto);
        return Result.success(result);
    }
    
    /**
     * 删除协议价
     * <p>
     * 删除指定的协议价
     * </p>
     * 
     * @param id 协议价ID
     * @return 操作结果
     */
    @DeleteMapping("/agreement-prices/{id}")
    @Operation(summary = "删除协议价", description = "删除指定的协议价")
    public Result<Void> deleteAgreementPrice(
            @Parameter(description = "协议价ID") @PathVariable Long id) {
        creditService.deleteAgreementPrice(id);
        return Result.success();
    }
    
    /**
     * 获取协议价详情
     * <p>
     * 根据ID获取协议价详情
     * </p>
     * 
     * @param id 协议价ID
     * @return 协议价详情
     */
    @GetMapping("/agreement-prices/{id}")
    @Operation(summary = "获取协议价详情", description = "根据ID获取协议价详情")
    public Result<AgreementPriceVO> getAgreementPriceById(
            @Parameter(description = "协议价ID") @PathVariable Long id) {
        AgreementPriceVO result = creditService.getAgreementPriceById(id);
        return Result.success(result);
    }
}
