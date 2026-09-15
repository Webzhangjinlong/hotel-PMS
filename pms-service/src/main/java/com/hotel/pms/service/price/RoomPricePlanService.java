package com.hotel.pms.service.price;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.RoomPricePlanCreateDTO;
import com.hotel.pms.common.dto.RoomPricePlanUpdateDTO;
import com.hotel.pms.common.dto.RoomPricePlanVO;
import com.hotel.pms.common.enums.StatusEnum;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.RoomPricePlan;
import com.hotel.pms.dao.entity.RoomPricePlanDetail;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.mapper.RoomPricePlanDetailMapper;
import com.hotel.pms.dao.mapper.RoomPricePlanMapper;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房价码服务类
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoomPricePlanService extends BaseService<RoomPricePlan, RoomPricePlanMapper> {

    @Autowired
    private RoomPricePlanDetailMapper detailMapper;
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;

    public PageResponse<RoomPricePlanVO> pageList(Long hotelId, String name, int page, int size) {
        LambdaQueryWrapper<RoomPricePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(hotelId != null, RoomPricePlan::getHotelId, hotelId)
               .like(StringUtils.hasText(name), RoomPricePlan::getName, name)
               .orderByDesc(RoomPricePlan::getCreatedAt);

        Page<RoomPricePlan> pageParam = new Page<>(page, size);
        IPage<RoomPricePlan> result = mapper.selectPage(pageParam, wrapper);

        List<RoomPricePlanVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    public List<RoomPricePlanVO> listByHotelId(Long hotelId) {
        LambdaQueryWrapper<RoomPricePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPricePlan::getHotelId, hotelId)
               .eq(RoomPricePlan::getStatus, StatusEnum.ACTIVE.getCode());
        return mapper.selectList(wrapper).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public RoomPricePlanVO getById(Long id) {
        RoomPricePlan plan = mapper.selectById(id);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        return convertToVO(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoomPricePlanVO create(RoomPricePlanCreateDTO dto) {
        checkCodeDuplicate(dto.getHotelId(), dto.getCode(), null);
        
        // 创建房价码主表
        RoomPricePlan plan = new RoomPricePlan();
        plan.setHotelId(dto.getHotelId());
        plan.setCode(dto.getCode());
        plan.setName(dto.getName());
        plan.setValidFrom(dto.getValidFrom());
        plan.setValidTo(dto.getValidTo());
        plan.setDescription(dto.getDescription());
        plan.setStatus(StatusEnum.ACTIVE.getCode());
        mapper.insert(plan);
        
        // 创建房型明细
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (RoomPricePlanCreateDTO.Detail detail : dto.getDetails()) {
                RoomPricePlanDetail detailEntity = new RoomPricePlanDetail();
                detailEntity.setPlanId(plan.getId());
                detailEntity.setRoomTypeId(detail.getRoomTypeId());
                detailEntity.setBasePrice(detail.getBasePrice());
                detailEntity.setDiscountType(detail.getDiscountType() != null ? detail.getDiscountType() : "NONE");
                detailEntity.setDiscountValue(detail.getDiscountValue() != null ? detail.getDiscountValue() : BigDecimal.ZERO);
                detailEntity.setFinalPrice(calculateFinalPrice(detail.getBasePrice(), detail.getDiscountType(), detail.getDiscountValue()));
                detailMapper.insert(detailEntity);
            }
        }
        
        log.info("创建房价码成功：hotelId={}, code={}", dto.getHotelId(), dto.getCode());
        return convertToVO(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoomPricePlanVO update(Long id, RoomPricePlanUpdateDTO dto) {
        RoomPricePlan plan = mapper.selectById(id);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        
        if (StringUtils.hasText(dto.getName())) plan.setName(dto.getName());
        if (dto.getValidFrom() != null) plan.setValidFrom(dto.getValidFrom());
        if (dto.getValidTo() != null) plan.setValidTo(dto.getValidTo());
        if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        mapper.updateById(plan);
        
        // 更新房型明细
        if (dto.getDetails() != null) {
            // 删除旧明细（物理删除，避免唯一约束冲突）
            detailMapper.delete(
                new LambdaQueryWrapper<RoomPricePlanDetail>()
                    .eq(RoomPricePlanDetail::getPlanId, id)
            );
            
            // 插入新明细
            for (RoomPricePlanUpdateDTO.Detail detail : dto.getDetails()) {
                RoomPricePlanDetail detailEntity = new RoomPricePlanDetail();
                detailEntity.setPlanId(id);
                detailEntity.setRoomTypeId(detail.getRoomTypeId());
                detailEntity.setBasePrice(detail.getBasePrice());
                detailEntity.setDiscountType(detail.getDiscountType() != null ? detail.getDiscountType() : "NONE");
                detailEntity.setDiscountValue(detail.getDiscountValue() != null ? detail.getDiscountValue() : BigDecimal.ZERO);
                detailEntity.setFinalPrice(calculateFinalPrice(detail.getBasePrice(), detail.getDiscountType(), detail.getDiscountValue()));
                detailMapper.insert(detailEntity);
            }
        }
        
        log.info("更新房价码成功：id={}", id);
        return convertToVO(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RoomPricePlan plan = mapper.selectById(id);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        
        // 删除明细
        LambdaQueryWrapper<RoomPricePlanDetail> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RoomPricePlanDetail::getPlanId, id);
        List<RoomPricePlanDetail> details = detailMapper.selectList(deleteWrapper);
        for (RoomPricePlanDetail detail : details) {
            detailMapper.deleteById(detail.getId());
        }
        
        // 删除主表
        mapper.deleteById(id);
        log.info("删除房价码成功：id={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        RoomPricePlan plan = mapper.selectById(id);
        if (plan == null) throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        plan.setStatus(status);
        mapper.updateById(plan);
        log.info("更新房价码状态成功：id={}, status={}", id, status);
    }

    /**
     * 根据房价码和房型查询价格
     */
    public BigDecimal getPriceByPlanAndRoomType(Long planId, Long roomTypeId) {
        LambdaQueryWrapper<RoomPricePlanDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPricePlanDetail::getPlanId, planId)
               .eq(RoomPricePlanDetail::getRoomTypeId, roomTypeId);
        RoomPricePlanDetail detail = detailMapper.selectOne(wrapper);
        if (detail != null && detail.getFinalPrice() != null) {
            return detail.getFinalPrice();
        }
        return null;
    }

    private void checkCodeDuplicate(Long hotelId, String code, Long excludeId) {
        LambdaQueryWrapper<RoomPricePlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPricePlan::getHotelId, hotelId)
               .eq(RoomPricePlan::getCode, code)
               .ne(excludeId != null, RoomPricePlan::getId, excludeId);
        if (mapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "房价码编码已存在");
        }
    }

    private BigDecimal calculateFinalPrice(BigDecimal basePrice, String discountType, BigDecimal discountValue) {
        if (basePrice == null) return BigDecimal.ZERO;
        if (discountType == null || "NONE".equals(discountType) || discountValue == null) {
            return basePrice;
        }
        switch (discountType) {
            case "PERCENT":
                return basePrice.multiply(BigDecimal.ONE.subtract(discountValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);
            case "FIXED":
                return discountValue.setScale(2, RoundingMode.HALF_UP);
            default:
                return basePrice;
        }
    }

    private RoomPricePlanVO convertToVO(RoomPricePlan plan) {
        RoomPricePlanVO vo = RoomPricePlanVO.builder()
                .id(plan.getId())
                .hotelId(plan.getHotelId())
                .code(plan.getCode())
                .name(plan.getName())
                .validFrom(plan.getValidFrom())
                .validTo(plan.getValidTo())
                .description(plan.getDescription())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
        
        // 查询房型明细
        LambdaQueryWrapper<RoomPricePlanDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(RoomPricePlanDetail::getPlanId, plan.getId());
        List<RoomPricePlanDetail> details = detailMapper.selectList(detailWrapper);
        
        List<RoomPricePlanVO.DetailVO> detailVOs = details.stream()
                .map(this::convertDetailToVO)
                .collect(Collectors.toList());
        vo.setDetails(detailVOs);
        
        return vo;
    }

    private RoomPricePlanVO.DetailVO convertDetailToVO(RoomPricePlanDetail detail) {
        RoomPricePlanVO.DetailVO detailVO = RoomPricePlanVO.DetailVO.builder()
                .id(detail.getId())
                .planId(detail.getPlanId())
                .roomTypeId(detail.getRoomTypeId())
                .basePrice(detail.getBasePrice())
                .discountType(detail.getDiscountType())
                .discountValue(detail.getDiscountValue())
                .finalPrice(detail.getFinalPrice())
                .build();
        
        // 查询房型名称
        RoomType roomType = roomTypeMapper.selectById(detail.getRoomTypeId());
        if (roomType != null) {
            detailVO.setRoomTypeName(roomType.getName());
        }
        
        return detailVO;
    }
}