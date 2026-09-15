package com.hotel.pms.api.controller;

import com.hotel.pms.api.config.UserContext;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.Result;
import com.hotel.pms.common.annotation.OperationLog;
import com.hotel.pms.service.folio.FolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 散客账务控制器
 * <p>
 * 处理散客入住的收款、退款、冲账等账务操作
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/folios")
@RequiredArgsConstructor
@Tag(name = "散客账务管理", description = "散客入住的收款、退款、冲账等账务操作")
public class FolioController {

    private final FolioService folioService;

    /**
     * 根据入住单ID查询账务单
     *
     * @param stayId 入住单ID
     * @return 账务单信息
     */
    /**
     * 分页查询散客账务单列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询散客账务单列表", description = "根据条件分页查询散客账务单列表")
    public Result<PageResponse<FolioVO>> getStayFolioList(FolioQueryDTO queryDTO) {
        PageResponse<FolioVO> result = folioService.getStayFolioList(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "统计散客账务数据")
    public Result<FolioStatsVO> getStayFolioStats(FolioQueryDTO queryDTO) {
        FolioStatsVO result = folioService.getStayFolioStats(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/stay/{stayId}")
    @Operation(summary = "查询入住账务", description = "根据入住单ID查询账务单信息")
    public Result<FolioVO> getFolioByStayId(@Parameter(description = "入住单ID") @PathVariable Long stayId) {
        FolioVO result = folioService.getFolioByStayId(stayId);
        return Result.success(result);
    }

    /**
     * 入住时收款（押金/房费/杂费）
     *
     * @param stayId 入住单ID
     * @param dto 收款请求
     * @return 收款结果
     */
    @OperationLog(module = "账务管理", action = "收款", targetType = "账单")
    @PostMapping("/stay/{stayId}/payment")
    @Operation(summary = "入住收款", description = "入住时收取押金、房费或杂费")
    public Result<StayPaymentVO> collectPayment(
            @Parameter(description = "入住单ID") @PathVariable Long stayId,
            @Valid @RequestBody StayPaymentDTO dto) {
        dto.setOperatorId(UserContext.getUserId());
        StayPaymentVO result = folioService.collectPayment(stayId, dto);
        return Result.success(result);
    }

    /**
    /**
     * 加床/杂费入账
     * <p>
     * 为在住客人添加加床费或杂费，支持直接支付或挂账
     * </p>
     *
     * @param stayId 入住单ID
     * @param dto 加床/杂费请求
     * @return 收款结果
     */
    @OperationLog(module = "账务管理", action = "加床/杂费入账", targetType = "账单")
    @PostMapping("/stay/{stayId}/extra-charge")
    @Operation(summary = "加床/杂费入账", description = "为在住客人添加加床费或杂费")
    public Result<StayPaymentVO> addExtraCharge(
            @Parameter(description = "入住单ID") @PathVariable Long stayId,
            @Valid @RequestBody ExtraChargeDTO dto) {
        StayPaymentVO result = folioService.addExtraCharge(stayId, dto);
        return Result.success(result);
    }

    /**
     * 查询入住单的加床/杂费记录
     * <p>
     * 获取指定入住单的所有杂费交易记录
     * </p>
     *
     * @param stayId 入住单ID
     * @return 交易记录列表
     */
    @GetMapping("/stay/{stayId}/extra-charges")
    @Operation(summary = "查询加床/杂费记录", description = "查询入住单的加床/杂费交易记录")
    public Result<List<TransactionVO>> getExtraCharges(
            @Parameter(description = "入住单ID") @PathVariable Long stayId) {
        List<TransactionVO> result = folioService.getExtraChargesByStayId(stayId);
        return Result.success(result);
    }

     /**
     * 退款
     * @param folioId 账务单ID
     * @param dto 退款请求
     * @return 退款结果
     */
    @OperationLog(module = "账务管理", action = "退款", targetType = "账单")
    @PostMapping("/{folioId}/refund")
    @Operation(summary = "退款", description = "对账务单进行退款")
    public Result<TransactionVO> refund(
            @Parameter(description = "账务单ID") @PathVariable Long folioId,
            @Valid @RequestBody RefundDTO dto) {
        TransactionVO result = folioService.refund(folioId, dto);
        return Result.success(result);
    }

    /**
     * 查询账务单交易记录
     *
     * @param folioId 账务单ID
     * @return 交易记录列表
     */
    @GetMapping("/{folioId}/transactions")
    @Operation(summary = "查询交易记录", description = "查询账务单的交易记录")
    public Result<List<TransactionVO>> getTransactions(@Parameter(description = "账务单ID") @PathVariable Long folioId) {
        List<TransactionVO> result = folioService.getTransactionsByFolioId(folioId);
        return Result.success(result);
    }

    /**
     * 冲账（撤销交易）
     *
     * @param transactionId 交易ID
     * @param reason 冲账原因
     * @return 冲账结果
     */
    @OperationLog(module = "账务管理", action = "冲账", targetType = "交易记录", targetIdParam = "transactionId")

    @PostMapping("/transactions/{transactionId}/reverse")
    @Operation(summary = "冲账", description = "撤销指定的交易记录")
    public Result<TransactionVO> reverseTransaction(
            @Parameter(description = "交易ID") @PathVariable Long transactionId,
            @Parameter(description = "冲账原因") @RequestParam String reason) {
        TransactionVO result = folioService.reverseTransaction(transactionId, reason);
        return Result.success(result);
    }

    /**
     * 查询交易记录（分页）
     *
     * @param queryDTO 查询条件
     * @return 交易记录列表
     */
    @GetMapping("/transactions")
    @Operation(summary = "查询交易记录", description = "分页查询交易记录")
    public Result<PageResponse<TransactionVO>> getTransactionList(TransactionQueryDTO queryDTO) {
        PageResponse<TransactionVO> result = folioService.getTransactions(queryDTO);
        return Result.success(result);
    }

    /**
     * 导出交易流水
     * <p>
     * 根据查询条件导出交易记录为CSV文件
     * </p>
     *
     * @param queryDTO 查询条件
     * @return CSV文件
     */

    /**
     * 查询交易流水（支持按客人姓名、房间号查询）
     * <p>
     * 通过关联入住单和账务单，支持按客人姓名和房间号进行模糊查询
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 交易记录分页列表
     */
    @GetMapping("/transactions/flow")
    @Operation(summary = "查询交易流水", description = "支持按客人姓名、房间号查询交易流水")
    public Result<PageResponse<TransactionVO>> getTransactionFlow(TransactionQueryDTO queryDTO) {
        // 设置酒店ID
        queryDTO.setHotelId(UserContext.getHotelId());
        PageResponse<TransactionVO> result = folioService.getTransactionFlow(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/transactions/export")
    @Operation(summary = "导出交易流水", description = "根据条件导出交易记录为CSV文件")
    public ResponseEntity<byte[]> exportTransactions(TransactionQueryDTO queryDTO) {
        // 1. 设置酒店ID
        queryDTO.setHotelId(UserContext.getHotelId());

        // 2. 调用服务导出
        byte[] csvBytes = folioService.exportTransactionFlow(queryDTO);

        // 3. 构建响应
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "交易流水.csv");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}







