package com.hotel.pms.service.master;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.RoomTypeCreateDTO;
import com.hotel.pms.common.dto.RoomTypeQueryDTO;
import com.hotel.pms.common.dto.RoomTypeUpdateDTO;
import com.hotel.pms.common.dto.RoomTypeVO;
import com.hotel.pms.common.enums.StatusEnum;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 房型服务类
 * <p>
 * 负责房型的业务逻辑处理
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoomTypeService extends BaseService<RoomType, RoomTypeMapper> {
    
    /**
     * 分页查询房型列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<RoomTypeVO> pageList(RoomTypeQueryDTO queryDTO) {
        // 【构建查询条件】
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, RoomType::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getName()), RoomType::getName, queryDTO.getName())
               .eq(StringUtils.hasText(queryDTO.getCode()), RoomType::getCode, queryDTO.getCode())
               .eq(StringUtils.hasText(queryDTO.getStatus()), RoomType::getStatus, queryDTO.getStatus())
               .orderByAsc(RoomType::getCode);
        
        // 【执行分页查询】
        Page<RoomType> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<RoomType> result = mapper.selectPage(page, wrapper);
        
        // 【转换为VO】
        List<RoomTypeVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(), 
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 查询房型列表（不分页）
     * 
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    public List<RoomTypeVO> listByHotelId(Long hotelId) {
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomType::getHotelId, hotelId)
               .eq(RoomType::getStatus, StatusEnum.ACTIVE.getCode())
               .orderByAsc(RoomType::getCode);
        
        List<RoomType> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询房型下拉选项
     * 
     * @param hotelId 酒店ID
     * @return 房型列表（只包含ID、名称、编码）
     */
    public List<RoomTypeVO> listOptions(Long hotelId) {
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomType::getHotelId, hotelId)
               .eq(RoomType::getStatus, StatusEnum.ACTIVE.getCode())
               .select(RoomType::getId, RoomType::getName, RoomType::getCode)
               .orderByAsc(RoomType::getCode);
        
        List<RoomType> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID查询房型
     * 
     * @param id 房型ID
     * @return 房型信息
     */
    public RoomTypeVO getById(Long id) {
        RoomType roomType = mapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return convertToVO(roomType);
    }
    
    /**
     * 创建房型
     * 
     * @param dto 创建参数
     * @return 房型信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomTypeVO create(RoomTypeCreateDTO dto) {
        // 【检查房型编码是否重复】
        checkCodeDuplicate(dto.getHotelId(), dto.getCode(), null);
        
        // 【创建房型实体】
        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(dto, roomType);
        roomType.setStatus(StatusEnum.ACTIVE.getCode());
        
        // 【保存到数据库】
        mapper.insert(roomType);
        
        log.info("创建房型成功：hotelId={}, code={}, name={}", dto.getHotelId(), dto.getCode(), dto.getName());
        
        return convertToVO(roomType);
    }
    
    /**
     * 更新房型
     * 
     * @param id 房型ID
     * @param dto 更新参数
     * @return 房型信息
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomTypeVO update(Long id, RoomTypeUpdateDTO dto) {
        // 【查询房型】
        RoomType roomType = mapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 【更新字段】
        if (StringUtils.hasText(dto.getName())) {
            roomType.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getBedType())) {
            roomType.setBedType(dto.getBedType());
        }
        if (dto.getMaxGuests() != null) {
            roomType.setMaxGuests(dto.getMaxGuests());
        }
        if (dto.getBasePrice() != null) {
            roomType.setBasePrice(dto.getBasePrice());
        }
        if (dto.getDescription() != null) {
            roomType.setDescription(dto.getDescription());
        }
        
        // 【保存到数据库】
        mapper.updateById(roomType);
        
        log.info("更新房型成功：id={}, name={}", id, roomType.getName());
        
        return convertToVO(roomType);
    }
    
    /**
     * 删除房型（逻辑删除）
     * 
     * @param id 房型ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 【查询房型】
        RoomType roomType = mapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // TODO: 检查房型下是否有房间
        
        // 【逻辑删除】
        mapper.deleteById(id);
        
        log.info("删除房型成功：id={}", id);
    }
    
    /**
     * 更新房型状态
     * 
     * @param id 房型ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        // 【查询房型】
        RoomType roomType = mapper.selectById(id);
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 【更新状态】
        roomType.setStatus(status);
        mapper.updateById(roomType);
        
        log.info("更新房型状态成功：id={}, status={}", id, status);
    }
    
    /**
     * 检查房型编码是否重复
     * 
     * @param hotelId 酒店ID
     * @param code 房型编码
     * @param excludeId 排除的房型ID（更新时使用）
     */
    private void checkCodeDuplicate(Long hotelId, String code, Long excludeId) {
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomType::getHotelId, hotelId)
               .eq(RoomType::getCode, code)
               .ne(excludeId != null, RoomType::getId, excludeId);
        
        Long count = mapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "房型编码已存在");
        }
    }
    
    /**
     * 转换为VO
     * 
     * @param roomType 房型实体
     * @return 房型VO
     */
    private RoomTypeVO convertToVO(RoomType roomType) {
        RoomTypeVO vo = new RoomTypeVO();
        BeanUtils.copyProperties(roomType, vo);
        // TODO: 查询酒店名称
        vo.setHotelName("默认酒店");
        return vo;
    }
}
