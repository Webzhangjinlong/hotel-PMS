package com.hotel.pms.service.master;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Hotel;
import com.hotel.pms.dao.mapper.HotelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;

/**
 * 酒店服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {
    
    private final HotelMapper hotelMapper;
    
    /**
     * 分页查询酒店列表
     */
    public Page<HotelVO> getHotelPage(HotelQueryDTO queryDTO) {
        Page<Hotel> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getName()), Hotel::getName, queryDTO.getName())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Hotel::getStatus, queryDTO.getStatus())
               .orderByDesc(Hotel::getCreatedAt);
        
        Page<Hotel> hotelPage = hotelMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<HotelVO> voPage = new Page<>(hotelPage.getCurrent(), hotelPage.getSize(), hotelPage.getTotal());
        voPage.setRecords(hotelPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    /**
     * 创建酒店
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createHotel(HotelCreateDTO createDTO) {
        Hotel hotel = new Hotel();
          hotel.setHotelCode(createDTO.getHotelCode());
          hotel.setName(createDTO.getName());
        hotel.setAddress(createDTO.getAddress());
        hotel.setPhone(createDTO.getPhone());
        hotel.setTimezone(createDTO.getTimezone() != null ? createDTO.getTimezone() : "Asia/Shanghai");
        hotel.setStatus("ACTIVE");
        // 默认夜审配置
        hotel.setAuditTime(LocalTime.of(4, 0));
        hotel.setAutoAuditEnabled(true);
        
        hotelMapper.insert(hotel);
        
        log.info("创建酒店成功: id={}, name={}", hotel.getId(), hotel.getName());
        return hotel.getId();
    }
    
    /**
     * 更新酒店
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateHotel(HotelUpdateDTO updateDTO) {
        Hotel hotel = hotelMapper.selectById(updateDTO.getId());
        if (hotel == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店不存在");
        }
        
        if (updateDTO.getName() != null) {
            hotel.setName(updateDTO.getName());
        }
        if (updateDTO.getAddress() != null) {
            hotel.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getPhone() != null) {
            hotel.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getTimezone() != null) {
            hotel.setTimezone(updateDTO.getTimezone());
        }
        if (updateDTO.getStatus() != null) {
            hotel.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getAuditTime() != null) {
            hotel.setAuditTime(updateDTO.getAuditTime());
        }
        if (updateDTO.getAutoAuditEnabled() != null) {
            hotel.setAutoAuditEnabled(updateDTO.getAutoAuditEnabled());
        }
        
        hotelMapper.updateById(hotel);
        
        log.info("更新酒店成功: id={}", hotel.getId());
    }
    
    /**
     * 更新夜审配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateNightAuditConfig(Long hotelId, NightAuditConfigDTO configDTO) {
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店不存在");
        }
        
        if (configDTO.getAuditTime() != null) {
            hotel.setAuditTime(configDTO.getAuditTime());
        }
        if (configDTO.getAutoAuditEnabled() != null) {
            hotel.setAutoAuditEnabled(configDTO.getAutoAuditEnabled());
        }
        
        hotelMapper.updateById(hotel);
        
        log.info("更新夜审配置成功: hotelId={}, auditTime={}, autoAuditEnabled={}", 
                hotelId, hotel.getAuditTime(), hotel.getAutoAuditEnabled());
    }
    
    /**
     * 删除酒店
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteHotel(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店不存在");
        }
        
        hotelMapper.deleteById(id);
        
        log.info("删除酒店成功: id={}", id);
    }
    
    /**
     * 根据ID查询酒店
     */
    public HotelVO getHotelById(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        if (hotel == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "酒店不存在");
        }
        return convertToVO(hotel);
    }
    
    /**
     * 转换为VO
     */
    private HotelVO convertToVO(Hotel hotel) {
        return HotelVO.builder()
                .id(hotel.getId())
                .hotelCode(hotel.getHotelCode())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .phone(hotel.getPhone())
                .timezone(hotel.getTimezone())
                .status(hotel.getStatus())
                .auditTime(hotel.getAuditTime())
                .autoAuditEnabled(hotel.getAutoAuditEnabled())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .build();
    }
}
