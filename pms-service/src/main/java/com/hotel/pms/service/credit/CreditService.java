package com.hotel.pms.service.credit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.FolioConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 挂账服务类
 * <p>
 * 负责挂账公司的管理和挂账结算
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class CreditService {
    
    
    @Autowired
    private AgreementPriceMapper agreementPriceMapper;
    @Autowired
    private CreditCompanyMapper creditCompanyMapper;
    
    @Autowired
    private FinTransactionMapper finTransactionMapper;
    
    /**
     * 分页查询挂账公司
     * 
     * @param queryDTO 查询条件
     * @return 挂账公司列表
     */
    public PageResponse<CreditCompanyVO> pageList(CreditCompanyQueryDTO queryDTO) {
        LambdaQueryWrapper<CreditCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, CreditCompany::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getCompanyName()), CreditCompany::getCompanyName, queryDTO.getCompanyName())
               .eq(StringUtils.hasText(queryDTO.getStatus()), CreditCompany::getStatus, queryDTO.getStatus())
               .orderByDesc(CreditCompany::getCreatedAt);
        
        Page<CreditCompany> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<CreditCompany> result = creditCompanyMapper.selectPage(page, wrapper);
        
        List<CreditCompanyVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 根据ID查询挂账公司
     * 
     * @param id 挂账公司ID
     * @return 挂账公司信息
     */
    public CreditCompanyVO getById(Long id) {
        CreditCompany company = creditCompanyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "挂账公司不存在");
        }
        return convertToVO(company);
    }
    
    /**
     * 创建挂账公司
     * 
     * @param dto 挂账公司信息
     * @return 挂账公司信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditCompanyVO create(CreditCompanyDTO dto) {
        // 检查公司名称是否重复
        LambdaQueryWrapper<CreditCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditCompany::getHotelId, dto.getHotelId())
               .eq(CreditCompany::getCompanyName, dto.getCompanyName());
        
        Long count = creditCompanyMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "公司名称已存在");
        }
        
        CreditCompany company = new CreditCompany();
        company.setHotelId(dto.getHotelId());
        company.setCompanyName(dto.getCompanyName());
        company.setContactName(dto.getContactName());
        company.setContactPhone(dto.getContactPhone());
        company.setCreditLimit(dto.getCreditLimit() != null ? dto.getCreditLimit() : BigDecimal.ZERO);
        company.setCurrentBalance(BigDecimal.ZERO);
        company.setStatus(dto.getStatus() != null ? dto.getStatus() : FolioConstants.CREDIT_COMPANY_ACTIVE);
        company.setRemark(dto.getRemark());
        creditCompanyMapper.insert(company);
        
        log.info("创建挂账公司成功：公司名称={}, 信用额度={}", dto.getCompanyName(), dto.getCreditLimit());
        
        return convertToVO(company);
    }
    
    /**
     * 更新挂账公司
     * 
     * @param id 挂账公司ID
     * @param dto 挂账公司信息
     * @return 挂账公司信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditCompanyVO update(Long id, CreditCompanyDTO dto) {
        CreditCompany company = creditCompanyMapper.selectById(id);
        if (company == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "挂账公司不存在");
        }
        
        // 检查公司名称是否重复（排除自身）
        LambdaQueryWrapper<CreditCompany> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CreditCompany::getHotelId, dto.getHotelId())
               .eq(CreditCompany::getCompanyName, dto.getCompanyName())
               .ne(CreditCompany::getId, id);
        
        Long count = creditCompanyMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "公司名称已存在");
        }
        
        company.setCompanyName(dto.getCompanyName());
        company.setContactName(dto.getContactName());
        company.setContactPhone(dto.getContactPhone());
        if (dto.getCreditLimit() != null) {
            company.setCreditLimit(dto.getCreditLimit());
        }
        if (dto.getStatus() != null) {
            company.setStatus(dto.getStatus());
        }
        company.setRemark(dto.getRemark());
        creditCompanyMapper.updateById(company);
        
        log.info("更新挂账公司成功：公司ID={}, 公司名称={}", id, dto.getCompanyName());
        
        return convertToVO(company);
    }
    
    /**
     * 挂账结算
     * 
     * @param companyId 挂账公司ID
     * @param amount 结算金额
     * @param paymentMethod 支付方式
     */
    @Transactional(rollbackFor = Exception.class)
    public void settleCredit(Long companyId, BigDecimal amount, String paymentMethod) {
        CreditCompany company = creditCompanyMapper.selectById(companyId);
        if (company == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "挂账公司不存在");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结算金额必须大于0");
        }
        
        if (amount.compareTo(company.getCurrentBalance()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结算金额不能大于挂账余额");
        }
        
        // 更新挂账公司余额
        company.setCurrentBalance(company.getCurrentBalance().subtract(amount));
        creditCompanyMapper.updateById(company);
        
        log.info("挂账结算成功：公司名称={}, 结算金额={}, 支付方式={}, 剩余余额={}", 
                company.getCompanyName(), amount, paymentMethod, company.getCurrentBalance());
    }
    
    /**
     * 查询挂账明细
     * 
     * @param companyId 挂账公司ID
     * @return 挂账明细列表
     */
    public List<TransactionVO> getCreditTransactions(Long companyId) {
        LambdaQueryWrapper<FinTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinTransaction::getCreditCompanyId, companyId)
               .orderByDesc(FinTransaction::getCreatedAt);
        
        List<FinTransaction> transactions = finTransactionMapper.selectList(wrapper);
        return transactions.stream()
                .map(this::convertTransactionToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     */
    private CreditCompanyVO convertToVO(CreditCompany company) {
        CreditCompanyVO vo = new CreditCompanyVO();
        vo.setId(company.getId());
        vo.setHotelId(company.getHotelId());
        vo.setCompanyName(company.getCompanyName());
        vo.setContactName(company.getContactName());
        vo.setContactPhone(company.getContactPhone());
        vo.setCreditLimit(company.getCreditLimit());
        vo.setCurrentBalance(company.getCurrentBalance());
        vo.setStatus(company.getStatus());
        vo.setStatusName(FolioConstants.CREDIT_COMPANY_ACTIVE.equals(company.getStatus()) ? "活跃" : "停用");
        vo.setRemark(company.getRemark());
        vo.setCreatedAt(company.getCreatedAt());
        vo.setUpdatedAt(company.getUpdatedAt());
        return vo;
    }
    
    /**
     * 转换交易为VO
     */
    private TransactionVO convertTransactionToVO(FinTransaction transaction) {
        TransactionVO vo = new TransactionVO();
        vo.setId(transaction.getId());
        vo.setTransactionNo(transaction.getTransactionNo());
        vo.setFolioId(transaction.getFolioId());
        vo.setType(transaction.getType());
        vo.setAmount(transaction.getAmount());
        vo.setPaymentMethod(transaction.getPaymentMethod());
        vo.setDescription(transaction.getDescription());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }
    
    /**
     * 查询协议价列表
     * <p>
     * 根据协议公司ID查询协议价列表
     * </p>
     * 
     * @param creditCompanyId 协议公司ID
     * @return 协议价列表
     */
    public List<AgreementPriceVO> getAgreementPrices(Long creditCompanyId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<AgreementPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgreementPrice::getCreditCompanyId, creditCompanyId)
               .eq(AgreementPrice::getStatus, "ACTIVE")
               .orderByAsc(AgreementPrice::getRoomTypeId);
        
        // 2. 执行查询
        List<AgreementPrice> prices = agreementPriceMapper.selectList(wrapper);
        
        // 3. 转换为VO
        return prices.stream()
                .map(this::convertAgreementPriceToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建协议价
     * <p>
     * 为协议公司创建房型协议价格
     * </p>
     * 
     * @param dto 协议价信息
     * @return 协议价信息
     */
    @Transactional(rollbackFor = Exception.class)
    public AgreementPriceVO createAgreementPrice(AgreementPriceDTO dto) {
        // 1. 检查协议公司是否存在
        CreditCompany company = creditCompanyMapper.selectById(dto.getCreditCompanyId());
        if (company == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "协议公司不存在");
        }
        
        // 2. 检查是否已存在相同房型和时间段的协议价
        LambdaQueryWrapper<AgreementPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgreementPrice::getCreditCompanyId, dto.getCreditCompanyId())
               .eq(AgreementPrice::getRoomTypeId, dto.getRoomTypeId())
               .eq(AgreementPrice::getStatus, "ACTIVE")
               .le(AgreementPrice::getStartDate, dto.getEndDate())
               .ge(AgreementPrice::getEndDate, dto.getStartDate());
        
        Long count = agreementPriceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该房型在指定时间段已有协议价");
        }
        
        // 3. 创建协议价
        AgreementPrice price = new AgreementPrice();
        price.setHotelId(dto.getHotelId());
        price.setCreditCompanyId(dto.getCreditCompanyId());
        price.setRoomTypeId(dto.getRoomTypeId());
        price.setPrice(dto.getPrice());
        price.setDiscountRate(dto.getDiscountRate());
        price.setStartDate(dto.getStartDate());
        price.setEndDate(dto.getEndDate());
        price.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        price.setRemark(dto.getRemark());
        
        // 4. 保存到数据库
        agreementPriceMapper.insert(price);
        
        // 5. 记录日志
        log.info("创建协议价: 公司ID={}, 房型ID={}, 价格={}", 
                dto.getCreditCompanyId(), dto.getRoomTypeId(), dto.getPrice());
        
        // 6. 返回创建的协议价
        return convertAgreementPriceToVO(price);
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
    @Transactional(rollbackFor = Exception.class)
    public AgreementPriceVO updateAgreementPrice(Long id, AgreementPriceDTO dto) {
        // 1. 查询协议价是否存在
        AgreementPrice price = agreementPriceMapper.selectById(id);
        if (price == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "协议价不存在");
        }
        
        // 2. 更新协议价信息
        price.setPrice(dto.getPrice());
        price.setDiscountRate(dto.getDiscountRate());
        price.setStartDate(dto.getStartDate());
        price.setEndDate(dto.getEndDate());
        price.setStatus(dto.getStatus());
        price.setRemark(dto.getRemark());
        
        // 3. 保存到数据库
        agreementPriceMapper.updateById(price);
        
        // 4. 记录日志
        log.info("更新协议价: ID={}, 价格={}", id, dto.getPrice());
        
        // 5. 返回更新后的协议价
        return convertAgreementPriceToVO(price);
    }
    
    /**
     * 删除协议价
     * <p>
     * 删除指定的协议价（逻辑删除）
     * </p>
     * 
     * @param id 协议价ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgreementPrice(Long id) {
        // 1. 查询协议价是否存在
        AgreementPrice price = agreementPriceMapper.selectById(id);
        if (price == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "协议价不存在");
        }
        
        // 2. 逻辑删除协议价
        price.setDeleted(true);
        agreementPriceMapper.updateById(price);
        
        // 3. 记录日志
        log.info("删除协议价: ID={}", id);
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
    public AgreementPriceVO getAgreementPriceById(Long id) {
        // 1. 查询协议价
        AgreementPrice price = agreementPriceMapper.selectById(id);
        if (price == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "协议价不存在");
        }
        
        // 2. 转换为VO
        return convertAgreementPriceToVO(price);
    }
    
    /**
     * 将协议价实体转换为VO
     * 
     * @param price 协议价实体
     * @return 协议价VO
     */
    private AgreementPriceVO convertAgreementPriceToVO(AgreementPrice price) {
        AgreementPriceVO vo = new AgreementPriceVO();
        vo.setId(price.getId());
        vo.setHotelId(price.getHotelId());
        vo.setCreditCompanyId(price.getCreditCompanyId());
        vo.setRoomTypeId(price.getRoomTypeId());
        vo.setPrice(price.getPrice());
        vo.setDiscountRate(price.getDiscountRate());
        vo.setStartDate(price.getStartDate());
        vo.setEndDate(price.getEndDate());
        vo.setStatus(price.getStatus());
        vo.setRemark(price.getRemark());
        vo.setCreatedAt(price.getCreatedAt());
        vo.setUpdatedAt(price.getUpdatedAt());
        
        // 获取公司名称
        CreditCompany company = creditCompanyMapper.selectById(price.getCreditCompanyId());
        if (company != null) {
            vo.setCompanyName(company.getCompanyName());
        }
        
        // 获取房型名称（需要注入RoomTypeMapper）
        // 这里暂时留空，实际项目中需要注入RoomTypeMapper来获取房型名称
        
        return vo;
    }
}
