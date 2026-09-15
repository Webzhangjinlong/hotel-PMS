package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.common.dto.InvoiceDTO;
import com.hotel.pms.common.dto.InvoiceQueryDTO;
import com.hotel.pms.common.dto.InvoiceVO;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.service.invoice.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 发票控制器
 * <p>
 * 处理发票管理相关的请求，包括发票登记、查询、作废等
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "发票管理", description = "发票的增删改查和作废")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    /**
     * 分页查询发票列表
     * 
     * @param queryDTO 查询条件
     * @return 发票列表
     */
    @GetMapping
    @Operation(summary = "查询发票列表", description = "分页查询发票列表")
    public Result<PageResponse<InvoiceVO>> pageList(InvoiceQueryDTO queryDTO) {
        PageResponse<InvoiceVO> result = invoiceService.pageList(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询发票详情
     * 
     * @param id 发票ID
     * @return 发票详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询发票详情", description = "根据ID查询发票详细信息")
    public Result<InvoiceVO> getById(@Parameter(description = "发票ID") @PathVariable Long id) {
        InvoiceVO result = invoiceService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建发票
     * <p>
     * 登记新的发票信息
     * </p>
     * 
     * @param dto 发票信息
     * @return 创建的发票信息
     */
    @OperationLog(module = "发票管理", action = "创建发票", targetType = "发票")
    @PostMapping
    @Operation(summary = "创建发票", description = "登记新的发票信息")
    public Result<InvoiceVO> create(@Valid @RequestBody InvoiceDTO dto) {
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getUsername();
        InvoiceVO result = invoiceService.create(dto, operatorId, operatorName);
        return Result.success(result);
    }
    
    /**
     * 更新发票
     * <p>
     * 更新发票信息，只能更新正常状态的发票
     * </p>
     * 
     * @param id 发票ID
     * @param dto 发票信息
     * @return 更新后的发票信息
     */
    @OperationLog(module = "发票管理", action = "更新发票", targetType = "发票")
    @PutMapping("/{id}")
    @Operation(summary = "更新发票", description = "更新发票信息")
    public Result<InvoiceVO> update(
            @Parameter(description = "发票ID") @PathVariable Long id,
            @Valid @RequestBody InvoiceDTO dto) {
        InvoiceVO result = invoiceService.update(id, dto);
        return Result.success(result);
    }
    
    /**
     * 作废发票
     * <p>
     * 将发票状态设置为作废
     * </p>
     * 
     * @param id 发票ID
     * @param reason 作废原因
     * @return 操作结果
     */
    @OperationLog(module = "发票管理", action = "作废发票", targetType = "发票")
    @PostMapping("/{id}/void")
    @Operation(summary = "作废发票", description = "将发票状态设置为作废")
    public Result<Void> voidInvoice(
            @Parameter(description = "发票ID") @PathVariable Long id,
            @Parameter(description = "作废原因") @RequestParam String reason) {
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getUsername();
        invoiceService.voidInvoice(id, reason, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 红冲发票
     * <p>
     * 将发票状态设置为红冲
     * </p>
     * 
     * @param id 发票ID
     * @param reason 红冲原因
     * @return 操作结果
     */
    @OperationLog(module = "发票管理", action = "红冲发票", targetType = "发票")
    @PostMapping("/{id}/red")
    @Operation(summary = "红冲发票", description = "将发票状态设置为红冲")
    public Result<Void> redInvoice(
            @Parameter(description = "发票ID") @PathVariable Long id,
            @Parameter(description = "红冲原因") @RequestParam String reason) {
        Long operatorId = UserContext.getUserId();
        String operatorName = UserContext.getUsername();
        invoiceService.redInvoice(id, reason, operatorId, operatorName);
        return Result.success();
    }
    
    /**
     * 删除发票
     * <p>
     * 逻辑删除发票，只能删除作废或红冲状态的发票
     * </p>
     * 
     * @param id 发票ID
     * @return 操作结果
     */
    @OperationLog(module = "发票管理", action = "删除发票", targetType = "发票")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除发票", description = "删除发票")
    public Result<Void> delete(@Parameter(description = "发票ID") @PathVariable Long id) {
        invoiceService.delete(id);
        return Result.success();
    }
}
