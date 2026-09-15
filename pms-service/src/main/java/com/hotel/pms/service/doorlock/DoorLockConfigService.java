package com.hotel.pms.service.doorlock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.DoorLockConfig;
import com.hotel.pms.dao.mapper.DoorLockConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 门锁设备配置服务类
 * <p>
 * 负责门锁设备的管理，包括设备配置的增删改查
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class DoorLockConfigService {
    
    @Autowired
    private DoorLockConfigMapper doorLockConfigMapper;
    
    /**
     * 分页查询设备配置列表
     * <p>
     * 根据查询条件分页查询设备配置列表
     * </p>
     * 
     * @param queryDTO 查询条件
     * @return 设备配置列表
     */
    public PageResponse<DoorLockConfigVO> pageList(DoorLockConfigQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<DoorLockConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, DoorLockConfig::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getDeviceName()), DoorLockConfig::getDeviceName, queryDTO.getDeviceName())
               .eq(StringUtils.hasText(queryDTO.getDeviceModel()), DoorLockConfig::getDeviceModel, queryDTO.getDeviceModel())
               .eq(StringUtils.hasText(queryDTO.getDeviceStatus()), DoorLockConfig::getDeviceStatus, queryDTO.getDeviceStatus())
               .eq(StringUtils.hasText(queryDTO.getStatus()), DoorLockConfig::getStatus, queryDTO.getStatus())
               .orderByDesc(DoorLockConfig::getCreatedAt);
        
        // 2. 执行分页查询
        Page<DoorLockConfig> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<DoorLockConfig> result = doorLockConfigMapper.selectPage(page, wrapper);
        
        // 3. 转换为VO
        List<DoorLockConfigVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 4. 返回分页结果
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 根据ID查询设备配置详情
     * <p>
     * 查询设备配置详细信息
     * </p>
     * 
     * @param id 设备配置ID
     * @return 设备配置详情
     */
    public DoorLockConfigVO getById(Long id) {
        // 1. 查询设备配置
        DoorLockConfig config = doorLockConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 转换为VO
        return convertToVO(config);
    }
    
    /**
     * 创建设备配置
     * <p>
     * 创建新的门锁设备配置
     * </p>
     * 
     * @param dto 设备配置信息
     * @return 创建的设备配置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DoorLockConfigVO create(DoorLockConfigDTO dto) {
        // 1. 创建设备配置
        DoorLockConfig config = new DoorLockConfig();
        BeanUtils.copyProperties(dto, config);
        config.setDeviceStatus("OFFLINE");
        config.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        
        // 2. 保存到数据库
        doorLockConfigMapper.insert(config);
        
        // 3. 记录日志
        log.info("创建门锁设备配置: 设备名称={}, 设备型号={}", dto.getDeviceName(), dto.getDeviceModel());
        
        // 4. 返回创建的设备配置
        return convertToVO(config);
    }
    
    /**
     * 更新设备配置
     * <p>
     * 更新门锁设备配置
     * </p>
     * 
     * @param id 设备配置ID
     * @param dto 设备配置信息
     * @return 更新后的设备配置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DoorLockConfigVO update(Long id, DoorLockConfigDTO dto) {
        // 1. 查询设备配置是否存在
        DoorLockConfig config = doorLockConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 更新设备配置信息
        BeanUtils.copyProperties(dto, config);
        
        // 3. 保存到数据库
        doorLockConfigMapper.updateById(config);
        
        // 4. 记录日志
        log.info("更新门锁设备配置: ID={}, 设备名称={}", id, dto.getDeviceName());
        
        // 5. 返回更新后的设备配置
        return convertToVO(config);
    }
    
    /**
     * 删除设备配置
     * <p>
     * 逻辑删除门锁设备配置
     * </p>
     * 
     * @param id 设备配置ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 查询设备配置是否存在
        DoorLockConfig config = doorLockConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 逻辑删除设备配置
        config.setDeleted(true);
        doorLockConfigMapper.updateById(config);
        
        // 3. 记录日志
        log.info("删除门锁设备配置: ID={}, 设备名称={}", id, config.getDeviceName());
    }
    
    /**
     * 更新设备状态
     * <p>
     * 更新门锁设备的在线状态
     * </p>
     * 
     * @param id 设备配置ID
     * @param deviceStatus 设备状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(Long id, String deviceStatus) {
        // 1. 查询设备配置是否存在
        DoorLockConfig config = doorLockConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 更新设备状态
        config.setDeviceStatus(deviceStatus);
        doorLockConfigMapper.updateById(config);
        
        // 3. 记录日志
        log.info("更新门锁设备状态: ID={}, 设备状态={}", id, deviceStatus);
    }
    
    /**
     * 获取所有启用的设备
     * <p>
     * 获取酒店所有启用的门锁设备
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 设备列表
     */
    public List<DoorLockConfigVO> listActiveDevices(Long hotelId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<DoorLockConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoorLockConfig::getHotelId, hotelId)
               .eq(DoorLockConfig::getStatus, "ACTIVE")
               .orderByAsc(DoorLockConfig::getDeviceName);
        
        // 2. 执行查询
        List<DoorLockConfig> devices = doorLockConfigMapper.selectList(wrapper);
        
        // 3. 转换为VO
        return devices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将DoorLockConfig实体转换为VO
     * 
     * @param config 设备配置实体
     * @return 设备配置VO
     */
    private DoorLockConfigVO convertToVO(DoorLockConfig config) {
        DoorLockConfigVO vo = new DoorLockConfigVO();
        BeanUtils.copyProperties(config, vo);
        
        // 设置设备状态名称
        vo.setDeviceStatusName(getDeviceStatusName(config.getDeviceStatus()));
        
        // 设置配置状态名称
        vo.setStatusName(getStatusName(config.getStatus()));
        
        return vo;
    }
    
    /**
     * 获取设备状态名称
     * 
     * @param status 设备状态代码
     * @return 设备状态名称
     */
    private String getDeviceStatusName(String status) {
        switch (status) {
            case "OFFLINE":
                return "离线";
            case "ONLINE":
                return "在线";
            case "ERROR":
                return "故障";
            default:
                return status;
        }
    }
    
    /**
     * 获取配置状态名称
     * 
     * @param status 配置状态代码
     * @return 配置状态名称
     */
    private String getStatusName(String status) {
        switch (status) {
            case "ACTIVE":
                return "启用";
            case "INACTIVE":
                return "停用";
            default:
                return status;
        }
    }
}
