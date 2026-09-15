package com.hotel.pms.service.master;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.FloorCreateDTO;
import com.hotel.pms.common.dto.FloorQueryDTO;
import com.hotel.pms.common.dto.FloorUpdateDTO;
import com.hotel.pms.common.dto.FloorVO;
import com.hotel.pms.common.enums.StatusEnum;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.HotelFloor;
import com.hotel.pms.dao.mapper.HotelFloorMapper;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 楼层服务类
 * <p>
 * 负责楼层的业务逻辑处理
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class FloorService extends BaseService<HotelFloor, HotelFloorMapper> {
    
    /**
     * 分页查询楼层列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<FloorVO> pageList(FloorQueryDTO queryDTO) {
        // 【构建查询条件】
        LambdaQueryWrapper<HotelFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, HotelFloor::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getName()), HotelFloor::getName, queryDTO.getName())
               .eq(StringUtils.hasText(queryDTO.getStatus()), HotelFloor::getStatus, queryDTO.getStatus())
               .orderByAsc(HotelFloor::getFloorNo);
        
        // 【执行分页查询】
        Page<HotelFloor> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<HotelFloor> result = mapper.selectPage(page, wrapper);
        
        // 【转换为VO】
        List<FloorVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(), 
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 查询楼层列表（不分页）
     * 
     * @param hotelId 酒店ID
     * @return 楼层列表
     */
    public List<FloorVO> listByHotelId(Long hotelId) {
        LambdaQueryWrapper<HotelFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelFloor::getHotelId, hotelId)
               .eq(HotelFloor::getStatus, StatusEnum.ACTIVE.getCode())
               .orderByAsc(HotelFloor::getFloorNo);
        
        List<HotelFloor> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID查询楼层
     * 
     * @param id 楼层ID
     * @return 楼层信息
     */
    public FloorVO getById(Long id) {
        HotelFloor floor = mapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        return convertToVO(floor);
    }
    
    /**
     * 创建楼层
     * 
     * @param dto 创建参数
     * @return 楼层信息
     */
    @Transactional(rollbackFor = Exception.class)
    public FloorVO create(FloorCreateDTO dto) {
        // 【检查楼层号是否重复】
        checkFloorNoDuplicate(dto.getHotelId(), dto.getFloorNo(), null);
        
        // 【创建楼层实体】
        HotelFloor floor = new HotelFloor();
        BeanUtils.copyProperties(dto, floor);
        floor.setStatus(StatusEnum.ACTIVE.getCode());
        
        // 【保存到数据库】
        mapper.insert(floor);
        
        log.info("创建楼层成功：hotelId={}, floorNo={}, name={}", dto.getHotelId(), dto.getFloorNo(), dto.getName());
        
        return convertToVO(floor);
    }
    
    /**
     * 更新楼层
     * 
     * @param id 楼层ID
     * @param dto 更新参数
     * @return 楼层信息
     */
    @Transactional(rollbackFor = Exception.class)
    public FloorVO update(Long id, FloorUpdateDTO dto) {
        // 【查询楼层】
        HotelFloor floor = mapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 【如果修改了楼层号，检查是否重复】
        if (dto.getFloorNo() != null && !dto.getFloorNo().equals(floor.getFloorNo())) {
            checkFloorNoDuplicate(floor.getHotelId(), dto.getFloorNo(), id);
        }
        
        // 【更新字段】
        if (dto.getFloorNo() != null) {
            floor.setFloorNo(dto.getFloorNo());
        }
        if (StringUtils.hasText(dto.getName())) {
            floor.setName(dto.getName());
        }
        
        // 【保存到数据库】
        mapper.updateById(floor);
        
        log.info("更新楼层成功：id={}, floorNo={}, name={}", id, floor.getFloorNo(), floor.getName());
        
        return convertToVO(floor);
    }
    
    /**
     * 删除楼层（逻辑删除）
     * 
     * @param id 楼层ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 【查询楼层】
        HotelFloor floor = mapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // TODO: 检查楼层下是否有房间
        
        // 【逻辑删除】
        mapper.deleteById(id);
        
        log.info("删除楼层成功：id={}", id);
    }
    
    /**
     * 更新楼层状态
     * 
     * @param id 楼层ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        // 【查询楼层】
        HotelFloor floor = mapper.selectById(id);
        if (floor == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        // 【更新状态】
        floor.setStatus(status);
        mapper.updateById(floor);
        
        log.info("更新楼层状态成功：id={}, status={}", id, status);
    }
    
    /**
     * 检查楼层号是否重复
     * 
     * @param hotelId 酒店ID
     * @param floorNo 楼层号
     * @param excludeId 排除的楼层ID（更新时使用）
     */
    private void checkFloorNoDuplicate(Long hotelId, Integer floorNo, Long excludeId) {
        LambdaQueryWrapper<HotelFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelFloor::getHotelId, hotelId)
               .eq(HotelFloor::getFloorNo, floorNo)
               .ne(excludeId != null, HotelFloor::getId, excludeId);
        
        Long count = mapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "楼层号已存在");
        }
    }
    
    /**
     * 转换为VO
     * 
     * @param floor 楼层实体
     * @return 楼层VO
     */
    private FloorVO convertToVO(HotelFloor floor) {
        FloorVO vo = new FloorVO();
        BeanUtils.copyProperties(floor, vo);
        // TODO: 查询酒店名称
        vo.setHotelName("默认酒店");
        return vo;
    }
}
