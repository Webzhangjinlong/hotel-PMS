package com.hotel.pms.service.price;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.RoomPrice;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.mapper.RoomPriceMapper;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.service.price.RoomPricePlanService;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房价服务类
 * <p>
 * 负责房价的业务逻辑处理
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoomPriceService extends BaseService<RoomPrice, RoomPriceMapper> {
    
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    
    @Autowired
    private RoomPricePlanService roomPricePlanService;
    
    /**
     * 查询指定日期房价
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param date 日期
     * @return 房价信息
     */
    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date) {
        LambdaQueryWrapper<RoomPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPrice::getHotelId, hotelId)
               .eq(RoomPrice::getRoomTypeId, roomTypeId)
               .eq(RoomPrice::getPriceDate, date);
        
        RoomPrice roomPrice = mapper.selectOne(wrapper);
        
        if (roomPrice != null) {
            return convertToVO(roomPrice);
        }
        
        // 如果没有特殊价格，返回房型基础价
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在");
        }
        
        return RoomPriceVO.builder()
                .hotelId(hotelId)
                .roomTypeId(roomTypeId)
                .roomTypeName(roomType.getName())
                .priceDate(date)
                .price(roomType.getBasePrice())
                .status("DEFAULT")
                .build();
    }
    
    /**
     * 查询房价列表（分页）
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<RoomPriceVO> getPriceList(RoomPriceQueryDTO queryDTO) {
        LambdaQueryWrapper<RoomPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, RoomPrice::getHotelId, queryDTO.getHotelId())
               .eq(queryDTO.getRoomTypeId() != null, RoomPrice::getRoomTypeId, queryDTO.getRoomTypeId())
               .ge(queryDTO.getStartDate() != null, RoomPrice::getPriceDate, queryDTO.getStartDate())
               .le(queryDTO.getEndDate() != null, RoomPrice::getPriceDate, queryDTO.getEndDate())
               .orderByAsc(RoomPrice::getPriceDate);
        
        Page<RoomPrice> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<RoomPrice> result = mapper.selectPage(page, wrapper);
        
        List<RoomPriceVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 获取月历价格
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param year 年份
     * @param month 月份
     * @return 月历价格
     */
    public RoomPriceCalendarVO getCalendarPrices(Long hotelId, Long roomTypeId, int year, int month) {
        // 查询房型信息
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在");
        }
        
        // 计算月份的起止日期
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 查询该月的所有特殊价格
        LambdaQueryWrapper<RoomPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPrice::getHotelId, hotelId)
               .eq(RoomPrice::getRoomTypeId, roomTypeId)
               .ge(RoomPrice::getPriceDate, startDate)
               .le(RoomPrice::getPriceDate, endDate);
        
        List<RoomPrice> priceList = mapper.selectList(wrapper);
        Map<LocalDate, BigDecimal> priceMap = priceList.stream()
                .collect(Collectors.toMap(RoomPrice::getPriceDate, RoomPrice::getPrice));
        
        // 构建每日价格列表
        List<RoomPriceCalendarVO.DayPrice> dayPrices = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal price = priceMap.get(currentDate);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            dayPrices.add(RoomPriceCalendarVO.DayPrice.builder()
                    .date(currentDate)
                    .price(price)
                    .hasSpecialPrice(price != null)
                    .dayOfWeek(dayOfWeek.getValue())
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return RoomPriceCalendarVO.builder()
                .roomTypeId(roomTypeId)
                .roomTypeName(roomType.getName())
                .basePrice(roomType.getBasePrice())
                .year(year)
                .month(month)
                .dayPrices(dayPrices)
                .build();
    }
    
    /**
     * 更新单个日期价格
     * 
     * @param dto 更新参数
     * @return 房价信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomPriceVO updatePrice(RoomPriceUpdateDTO dto) {
        // 查询是否已存在
        LambdaQueryWrapper<RoomPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomPrice::getHotelId, dto.getHotelId())
               .eq(RoomPrice::getRoomTypeId, dto.getRoomTypeId())
               .eq(RoomPrice::getPriceDate, dto.getPriceDate());
        
        RoomPrice existingPrice = mapper.selectOne(wrapper);
        
        if (existingPrice != null) {
            // 更新
            existingPrice.setPrice(dto.getPrice());
            mapper.updateById(existingPrice);
            log.info("更新房价成功：hotelId={}, roomTypeId={}, date={}, price={}", 
                    dto.getHotelId(), dto.getRoomTypeId(), dto.getPriceDate(), dto.getPrice());
            return convertToVO(existingPrice);
        } else {
            // 新增
            RoomPrice newPrice = new RoomPrice();
            newPrice.setHotelId(dto.getHotelId());
            newPrice.setRoomTypeId(dto.getRoomTypeId());
            newPrice.setPriceDate(dto.getPriceDate());
            newPrice.setPrice(dto.getPrice());
            newPrice.setStatus("ACTIVE");
            mapper.insert(newPrice);
            log.info("新增房价成功：hotelId={}, roomTypeId={}, date={}, price={}", 
                    dto.getHotelId(), dto.getRoomTypeId(), dto.getPriceDate(), dto.getPrice());
            return convertToVO(newPrice);
        }
    }
    
    /**
     * 批量调价
     * 
     * @param dto 批量调价参数
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdatePrice(RoomPriceBatchDTO dto) {
        // 参数校验
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
        
        int totalUpdated = 0;
        
        for (Long roomTypeId : dto.getRoomTypeIds()) {
            // 查询房型信息
            RoomType roomType = roomTypeMapper.selectById(roomTypeId);
            if (roomType == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在：ID=" + roomTypeId);
            }
            
            // 计算新价格
            BigDecimal newPrice;
            switch (dto.getAdjustType()) {
                case "FIXED":
                    newPrice = dto.getAdjustValue();
                    break;
                case "PERCENT":
                    // 按百分比调整
                    BigDecimal basePrice = roomType.getBasePrice();
                    newPrice = basePrice.multiply(BigDecimal.ONE.add(dto.getAdjustValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                            .setScale(2, RoundingMode.HALF_UP);
                    break;
                case "AMOUNT":
                    // 按金额调整
                    newPrice = roomType.getBasePrice().add(dto.getAdjustValue())
                            .setScale(2, RoundingMode.HALF_UP);
                    break;
                default:
                    throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的调价方式：" + dto.getAdjustType());
            }
            
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "调整后的价格不能小于等于0");
            }
            
            // 构建价格列表
            List<RoomPrice> priceList = new ArrayList<>();
            LocalDate currentDate = dto.getStartDate();
            while (!currentDate.isAfter(dto.getEndDate())) {
                RoomPrice price = new RoomPrice();
                price.setHotelId(dto.getHotelId());
                price.setRoomTypeId(roomTypeId);
                price.setPriceDate(currentDate);
                price.setPrice(newPrice);
                price.setStatus("ACTIVE");
                priceList.add(price);
                currentDate = currentDate.plusDays(1);
            }
            
            // 批量插入或更新
            if (!priceList.isEmpty()) {
                int updated = mapper.batchInsertOrUpdate(priceList);
                totalUpdated += updated;
                log.info("批量调价成功：roomTypeId={}, dateRange={}-{}, newPrice={}, updated={}", 
                        roomTypeId, dto.getStartDate(), dto.getEndDate(), newPrice, updated);
            }
        }
        
        return totalUpdated;
    }
    
    /**
     * 初始化房价
     * 
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int initPrices(Long hotelId, Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        // 查询房型信息
        RoomType roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在");
        }
        
        // 构建价格列表
        List<RoomPrice> priceList = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            RoomPrice price = new RoomPrice();
            price.setHotelId(hotelId);
            price.setRoomTypeId(roomTypeId);
            price.setPriceDate(currentDate);
            price.setPrice(roomType.getBasePrice());
            price.setStatus("ACTIVE");
            priceList.add(price);
            currentDate = currentDate.plusDays(1);
        }
        
        // 批量插入或更新
        if (!priceList.isEmpty()) {
            int updated = mapper.batchInsertOrUpdate(priceList);
            log.info("初始化房价成功：hotelId={}, roomTypeId={}, dateRange={}-{}, count={}", 
                    hotelId, roomTypeId, startDate, endDate, updated);
            return updated;
        }
        
        return 0;
    }
    
    /**
     * 转换为VO
     * 
     * @param roomPrice 房价实体
     * @return 房价VO
     */
    /**
     * 查询指定日期房价（支持房价码优先级）
     *
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param date 日期
     * @param pricePlanId 房价码ID（可选）
     * @return 房价信息
     */
    public RoomPriceVO getPriceByDate(Long hotelId, Long roomTypeId, LocalDate date, Long pricePlanId) {
        // 优先级1：房价码指定价格
        if (pricePlanId != null) {
            BigDecimal planPrice = roomPricePlanService.getPriceByPlanAndRoomType(pricePlanId, roomTypeId);
            if (planPrice != null) {
                RoomType roomType = roomTypeMapper.selectById(roomTypeId);
                return RoomPriceVO.builder()
                        .hotelId(hotelId)
                        .roomTypeId(roomTypeId)
                        .roomTypeName(roomType != null ? roomType.getName() : null)
                        .priceDate(date)
                        .price(planPrice)
                        .status("PLAN")
                        .build();
            }
        }
        
        // 优先级2：房间日历价
        // 优先级3：房型基础价
        return getPriceByDate(hotelId, roomTypeId, date);
    }
        private RoomPriceVO convertToVO(RoomPrice roomPrice) {
        RoomPriceVO vo = new RoomPriceVO();
        BeanUtils.copyProperties(roomPrice, vo);
        
        // 查询房型名称
        RoomType roomType = roomTypeMapper.selectById(roomPrice.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
        }
        
        return vo;
    }
}

