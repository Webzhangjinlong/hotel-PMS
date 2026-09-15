package com.hotel.pms.service.idcard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.IdcardReaderConfig;
import com.hotel.pms.dao.mapper.IdcardReaderConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 身份证阅读器设备配置服务类
 * <p>
 * 负责身份证阅读器设备的管理，包括设备配置的增删改查
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class IdcardReaderConfigService {
    
    @Autowired
    private IdcardReaderConfigMapper idcardReaderConfigMapper;
    
    /**
     * 分页查询设备配置列表
     * <p>
     * 根据查询条件分页查询设备配置列表
     * </p>
     * 
     * @param queryDTO 查询条件
     * @return 设备配置列表
     */
    public PageResponse<IdcardReaderConfigVO> pageList(IdcardReaderConfigQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<IdcardReaderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, IdcardReaderConfig::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getDeviceName()), IdcardReaderConfig::getDeviceName, queryDTO.getDeviceName())
               .eq(StringUtils.hasText(queryDTO.getDeviceModel()), IdcardReaderConfig::getDeviceModel, queryDTO.getDeviceModel())
               .eq(StringUtils.hasText(queryDTO.getDeviceStatus()), IdcardReaderConfig::getDeviceStatus, queryDTO.getDeviceStatus())
               .eq(StringUtils.hasText(queryDTO.getStatus()), IdcardReaderConfig::getStatus, queryDTO.getStatus())
               .orderByDesc(IdcardReaderConfig::getCreatedAt);
        
        // 2. 执行分页查询
        Page<IdcardReaderConfig> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<IdcardReaderConfig> result = idcardReaderConfigMapper.selectPage(page, wrapper);
        
        // 3. 转换为VO
        List<IdcardReaderConfigVO> voList = result.getRecords().stream()
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
    public IdcardReaderConfigVO getById(Long id) {
        // 1. 查询设备配置
        IdcardReaderConfig config = idcardReaderConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 转换为VO
        return convertToVO(config);
    }
    
    /**
     * 创建设备配置
     * <p>
     * 创建新的身份证阅读器设备配置
     * </p>
     * 
     * @param dto 设备配置信息
     * @return 创建的设备配置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public IdcardReaderConfigVO create(IdcardReaderConfigDTO dto) {
        // 1. 创建设备配置
        IdcardReaderConfig config = new IdcardReaderConfig();
        BeanUtils.copyProperties(dto, config);
        config.setDeviceStatus("OFFLINE");
        config.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        
        // 2. 保存到数据库
        idcardReaderConfigMapper.insert(config);
        
        // 3. 记录日志
        log.info("创建身份证阅读器设备配置: 设备名称={}, 设备型号={}", dto.getDeviceName(), dto.getDeviceModel());
        
        // 4. 返回创建的设备配置
        return convertToVO(config);
    }
    
    /**
     * 更新设备配置
     * <p>
     * 更新身份证阅读器设备配置
     * </p>
     * 
     * @param id 设备配置ID
     * @param dto 设备配置信息
     * @return 更新后的设备配置信息
     */
    @Transactional(rollbackFor = Exception.class)
    public IdcardReaderConfigVO update(Long id, IdcardReaderConfigDTO dto) {
        // 1. 查询设备配置是否存在
        IdcardReaderConfig config = idcardReaderConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 更新设备配置信息
        BeanUtils.copyProperties(dto, config);
        
        // 3. 保存到数据库
        idcardReaderConfigMapper.updateById(config);
        
        // 4. 记录日志
        log.info("更新身份证阅读器设备配置: ID={}, 设备名称={}", id, dto.getDeviceName());
        
        // 5. 返回更新后的设备配置
        return convertToVO(config);
    }
    
    /**
     * 删除设备配置
     * <p>
     * 逻辑删除身份证阅读器设备配置
     * </p>
     * 
     * @param id 设备配置ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 查询设备配置是否存在
        IdcardReaderConfig config = idcardReaderConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 逻辑删除设备配置
        config.setDeleted(true);
        idcardReaderConfigMapper.updateById(config);
        
        // 3. 记录日志
        log.info("删除身份证阅读器设备配置: ID={}, 设备名称={}", id, config.getDeviceName());
    }
    
    /**
     * 更新设备状态
     * <p>
     * 更新身份证阅读器的在线状态
     * </p>
     * 
     * @param id 设备配置ID
     * @param deviceStatus 设备状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceStatus(Long id, String deviceStatus) {
        // 1. 查询设备配置是否存在
        IdcardReaderConfig config = idcardReaderConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "设备配置不存在");
        }
        
        // 2. 更新设备状态
        config.setDeviceStatus(deviceStatus);
        idcardReaderConfigMapper.updateById(config);
        
        // 3. 记录日志
        log.info("更新身份证阅读器设备状态: ID={}, 设备状态={}", id, deviceStatus);
    }
    
    /**
     * 获取所有启用的设备
     * <p>
     * 获取酒店所有启用的身份证阅读器设备
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 设备列表
     */
    public List<IdcardReaderConfigVO> listActiveDevices(Long hotelId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<IdcardReaderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdcardReaderConfig::getHotelId, hotelId)
               .eq(IdcardReaderConfig::getStatus, "ACTIVE")
               .orderByAsc(IdcardReaderConfig::getDeviceName);
        
        // 2. 执行查询
        List<IdcardReaderConfig> devices = idcardReaderConfigMapper.selectList(wrapper);
        
        // 3. 转换为VO
        return devices.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将IdcardReaderConfig实体转换为VO
     * 
     * @param config 设备配置实体
     * @return 设备配置VO
     */
    private IdcardReaderConfigVO convertToVO(IdcardReaderConfig config) {
        IdcardReaderConfigVO vo = new IdcardReaderConfigVO();
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
