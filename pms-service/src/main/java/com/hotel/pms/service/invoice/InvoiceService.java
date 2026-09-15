package com.hotel.pms.service.invoice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Invoice;
import com.hotel.pms.dao.entity.InvoiceItem;
import com.hotel.pms.dao.mapper.InvoiceItemMapper;
import com.hotel.pms.dao.mapper.InvoiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发票服务类
 * <p>
 * 负责发票的管理，包括发票登记、查询、作废等功能
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class InvoiceService {
    
    @Autowired
    private InvoiceMapper invoiceMapper;
    
    @Autowired
    private InvoiceItemMapper invoiceItemMapper;
    
    /**
     * 分页查询发票列表
     * <p>
     * 根据查询条件分页查询发票列表
     * </p>
     * 
     * @param queryDTO 查询条件
     * @return 发票列表
     */
    public PageResponse<InvoiceVO> pageList(InvoiceQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Invoice::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getInvoiceNo()), Invoice::getInvoiceNo, queryDTO.getInvoiceNo())
               .eq(StringUtils.hasText(queryDTO.getInvoiceType()), Invoice::getInvoiceType, queryDTO.getInvoiceType())
               .eq(StringUtils.hasText(queryDTO.getInvoiceStatus()), Invoice::getInvoiceStatus, queryDTO.getInvoiceStatus())
               .like(StringUtils.hasText(queryDTO.getBuyerName()), Invoice::getBuyerName, queryDTO.getBuyerName())
               .like(StringUtils.hasText(queryDTO.getGuestName()), Invoice::getGuestName, queryDTO.getGuestName())
               .like(StringUtils.hasText(queryDTO.getStayNo()), Invoice::getStayNo, queryDTO.getStayNo())
               .orderByDesc(Invoice::getInvoiceDate);
        
        // 2. 日期条件
        if (queryDTO.getInvoiceDateStart() != null) {
            wrapper.ge(Invoice::getInvoiceDate, queryDTO.getInvoiceDateStart());
        }
        if (queryDTO.getInvoiceDateEnd() != null) {
            wrapper.le(Invoice::getInvoiceDate, queryDTO.getInvoiceDateEnd());
        }
        
        // 3. 执行分页查询
        Page<Invoice> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Invoice> result = invoiceMapper.selectPage(page, wrapper);
        
        // 4. 转换为VO
        List<InvoiceVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 5. 返回分页结果
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 根据ID查询发票详情
     * <p>
     * 查询发票详细信息，包括发票明细
     * </p>
     * 
     * @param id 发票ID
     * @return 发票详情
     */
    public InvoiceVO getById(Long id) {
        // 1. 查询发票主表
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "发票不存在");
        }
        
        // 2. 查询发票明细
        LambdaQueryWrapper<InvoiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoiceItem::getInvoiceId, id)
               .orderByAsc(InvoiceItem::getId);
        List<InvoiceItem> items = invoiceItemMapper.selectList(wrapper);
        
        // 3. 转换为VO
        InvoiceVO vo = convertToVO(invoice);
        vo.setItems(items.stream()
                .map(this::convertItemToVO)
                .collect(Collectors.toList()));
        
        return vo;
    }
    
    /**
     * 创建发票
     * <p>
     * 登记新的发票信息，包括发票明细
     * </p>
     * 
     * @param dto 发票信息
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 发票信息
     */
    @Transactional(rollbackFor = Exception.class)
    public InvoiceVO create(InvoiceDTO dto, Long operatorId, String operatorName) {
        // 1. 检查发票号码是否重复
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getHotelId, dto.getHotelId())
               .eq(Invoice::getInvoiceNo, dto.getInvoiceNo());
        Long count = invoiceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "发票号码已存在");
        }
        
        // 2. 创建发票主表
        Invoice invoice = new Invoice();
        BeanUtils.copyProperties(dto, invoice);
        invoice.setInvoiceStatus("NORMAL");
        invoice.setOperatorId(operatorId);
        invoice.setOperatorName(operatorName);
        
        // 3. 计算发票明细金额
        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        
        for (InvoiceItemDTO itemDTO : dto.getItems()) {
            InvoiceItem item = new InvoiceItem();
            BeanUtils.copyProperties(itemDTO, item);
            item.setHotelId(dto.getHotelId());
            
            // 计算金额
            BigDecimal amount = itemDTO.getItemPrice().multiply(itemDTO.getItemQuantity());
            BigDecimal taxAmount = amount.multiply(itemDTO.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal totalItemAmount = amount.add(taxAmount);
            
            item.setAmount(amount);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalItemAmount);
            
            items.add(item);
            totalAmount = totalAmount.add(amount);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
        }
        
        invoice.setAmount(totalAmount);
        invoice.setTaxAmount(totalTaxAmount);
        invoice.setTotalAmount(totalAmount.add(totalTaxAmount));
        
        // 4. 保存发票主表
        invoiceMapper.insert(invoice);
        
        // 5. 保存发票明细
        for (InvoiceItem item : items) {
            item.setInvoiceId(invoice.getId());
            invoiceItemMapper.insert(item);
        }
        
        // 6. 记录日志
        log.info("创建发票: 发票号码={}, 操作人={}", dto.getInvoiceNo(), operatorName);
        
        // 7. 返回创建的发票
        return getById(invoice.getId());
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
    @Transactional(rollbackFor = Exception.class)
    public InvoiceVO update(Long id, InvoiceDTO dto) {
        // 1. 查询发票是否存在
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "发票不存在");
        }
        
        // 2. 检查发票状态
        if (!"NORMAL".equals(invoice.getInvoiceStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能更新正常状态的发票");
        }
        
        // 3. 更新发票主表
        BeanUtils.copyProperties(dto, invoice);
        
        // 4. 重新计算发票明细金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        
        for (InvoiceItemDTO itemDTO : dto.getItems()) {
            // 计算金额
            BigDecimal amount = itemDTO.getItemPrice().multiply(itemDTO.getItemQuantity());
            BigDecimal taxAmount = amount.multiply(itemDTO.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(amount);
            totalTaxAmount = totalTaxAmount.add(taxAmount);
        }
        
        invoice.setAmount(totalAmount);
        invoice.setTaxAmount(totalTaxAmount);
        invoice.setTotalAmount(totalAmount.add(totalTaxAmount));
        
        invoiceMapper.updateById(invoice);
        
        // 5. 删除原有发票明细
        LambdaQueryWrapper<InvoiceItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(InvoiceItem::getInvoiceId, id);
        invoiceItemMapper.delete(deleteWrapper);
        
        // 6. 保存新的发票明细
        for (InvoiceItemDTO itemDTO : dto.getItems()) {
            InvoiceItem item = new InvoiceItem();
            BeanUtils.copyProperties(itemDTO, item);
            item.setInvoiceId(id);
            item.setHotelId(dto.getHotelId());
            
            // 计算金额
            BigDecimal amount = itemDTO.getItemPrice().multiply(itemDTO.getItemQuantity());
            BigDecimal taxAmount = amount.multiply(itemDTO.getTaxRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal totalItemAmount = amount.add(taxAmount);
            
            item.setAmount(amount);
            item.setTaxAmount(taxAmount);
            item.setTotalAmount(totalItemAmount);
            
            invoiceItemMapper.insert(item);
        }
        
        // 7. 记录日志
        log.info("更新发票: ID={}, 发票号码={}", id, dto.getInvoiceNo());
        
        // 8. 返回更新后的发票
        return getById(id);
    }
    
    /**
     * 作废发票
     * <p>
     * 将发票状态设置为作废，只能作废正常状态的发票
     * </p>
     * 
     * @param id 发票ID
     * @param reason 作废原因
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    @Transactional(rollbackFor = Exception.class)
    public void voidInvoice(Long id, String reason, Long operatorId, String operatorName) {
        // 1. 查询发票是否存在
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "发票不存在");
        }
        
        // 2. 检查发票状态
        if (!"NORMAL".equals(invoice.getInvoiceStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能作废正常状态的发票");
        }
        
        // 3. 更新发票状态
        invoice.setInvoiceStatus("VOID");
        invoice.setVoidReason(reason);
        invoiceMapper.updateById(invoice);
        
        // 4. 记录日志
        log.info("作废发票: ID={}, 发票号码={}, 原因={}, 操作人={}", 
                id, invoice.getInvoiceNo(), reason, operatorName);
    }
    
    /**
     * 红冲发票
     * <p>
     * 将发票状态设置为红冲，只能红冲正常状态的发票
     * </p>
     * 
     * @param id 发票ID
     * @param reason 红冲原因
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    @Transactional(rollbackFor = Exception.class)
    public void redInvoice(Long id, String reason, Long operatorId, String operatorName) {
        // 1. 查询发票是否存在
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "发票不存在");
        }
        
        // 2. 检查发票状态
        if (!"NORMAL".equals(invoice.getInvoiceStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能红冲正常状态的发票");
        }
        
        // 3. 更新发票状态
        invoice.setInvoiceStatus("RED");
        invoice.setVoidReason(reason);
        invoiceMapper.updateById(invoice);
        
        // 4. 记录日志
        log.info("红冲发票: ID={}, 发票号码={}, 原因={}, 操作人={}", 
                id, invoice.getInvoiceNo(), reason, operatorName);
    }
    
    /**
     * 删除发票
     * <p>
     * 逻辑删除发票，只能删除作废或红冲状态的发票
     * </p>
     * 
     * @param id 发票ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 查询发票是否存在
        Invoice invoice = invoiceMapper.selectById(id);
        if (invoice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "发票不存在");
        }
        
        // 2. 检查发票状态
        if ("NORMAL".equals(invoice.getInvoiceStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能删除正常状态的发票，请先作废或红冲");
        }
        
        // 3. 逻辑删除发票
        invoice.setDeleted(true);
        invoiceMapper.updateById(invoice);
        
        // 4. 逻辑删除发票明细
        LambdaQueryWrapper<InvoiceItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoiceItem::getInvoiceId, id);
        List<InvoiceItem> items = invoiceItemMapper.selectList(wrapper);
        for (InvoiceItem item : items) {
            item.setDeleted(true);
            invoiceItemMapper.updateById(item);
        }
        
        // 5. 记录日志
        log.info("删除发票: ID={}, 发票号码={}", id, invoice.getInvoiceNo());
    }
    
    /**
     * 将Invoice实体转换为VO
     * 
     * @param invoice 发票实体
     * @return 发票VO
     */
    private InvoiceVO convertToVO(Invoice invoice) {
        InvoiceVO vo = new InvoiceVO();
        BeanUtils.copyProperties(invoice, vo);
        
        // 设置发票类型名称
        vo.setInvoiceTypeName(getInvoiceTypeName(invoice.getInvoiceType()));
        
        // 设置发票状态名称
        vo.setInvoiceStatusName(getInvoiceStatusName(invoice.getInvoiceStatus()));
        
        return vo;
    }
    
    /**
     * 将InvoiceItem实体转换为VO
     * 
     * @param item 发票明细实体
     * @return 发票明细VO
     */
    private InvoiceItemVO convertItemToVO(InvoiceItem item) {
        InvoiceItemVO vo = new InvoiceItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
    
    /**
     * 获取发票类型名称
     * 
     * @param type 发票类型代码
     * @return 发票类型名称
     */
    private String getInvoiceTypeName(String type) {
        switch (type) {
            case "NORMAL":
                return "普通发票";
            case "SPECIAL":
                return "增值税专用发票";
            case "ELECTRONIC":
                return "电子发票";
            default:
                return type;
        }
    }
    
    /**
     * 获取发票状态名称
     * 
     * @param status 发票状态代码
     * @return 发票状态名称
     */
    private String getInvoiceStatusName(String status) {
        switch (status) {
            case "NORMAL":
                return "正常";
            case "VOID":
                return "已作废";
            case "RED":
                return "已红冲";
            default:
                return status;
        }
    }
}
