package com.hotel.pms.service.ota;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.OtaChannel;
import com.hotel.pms.dao.mapper.OtaChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OTA渠道配置服务类
 * <p>
 * 负责OTA渠道的管理，包括渠道配置的增删改查
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class OtaChannelService {
    
    @Autowired
    private OtaChannelMapper otaChannelMapper;
    
    /**
     * 分页查询OTA渠道列表
     * <p>
     * 根据查询条件分页查询OTA渠道列表
     * </p>
     * 
     * @param queryDTO 查询条件
     * @return 渠道列表
     */
    public PageResponse<OtaChannelVO> pageList(OtaChannelQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<OtaChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, OtaChannel::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getChannelName()), OtaChannel::getChannelName, queryDTO.getChannelName())
               .eq(StringUtils.hasText(queryDTO.getChannelCode()), OtaChannel::getChannelCode, queryDTO.getChannelCode())
               .eq(StringUtils.hasText(queryDTO.getChannelType()), OtaChannel::getChannelType, queryDTO.getChannelType())
               .eq(StringUtils.hasText(queryDTO.getStatus()), OtaChannel::getStatus, queryDTO.getStatus())
               .orderByDesc(OtaChannel::getCreatedAt);
        
        // 2. 执行分页查询
        Page<OtaChannel> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<OtaChannel> result = otaChannelMapper.selectPage(page, wrapper);
        
        // 3. 转换为VO
        List<OtaChannelVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 4. 返回分页结果
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 根据ID查询渠道详情
     * <p>
     * 查询渠道详细信息
     * </p>
     * 
     * @param id 渠道ID
     * @return 渠道详情
     */
    public OtaChannelVO getById(Long id) {
        // 1. 查询渠道
        OtaChannel channel = otaChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "OTA渠道不存在");
        }
        
        // 2. 转换为VO
        return convertToVO(channel);
    }
    
    /**
     * 创建渠道
     * <p>
     * 创建新的OTA渠道配置
     * </p>
     * 
     * @param dto 渠道信息
     * @return 创建的渠道信息
     */
    @Transactional(rollbackFor = Exception.class)
    public OtaChannelVO create(OtaChannelDTO dto) {
        // 1. 检查渠道编码是否重复
        LambdaQueryWrapper<OtaChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OtaChannel::getHotelId, dto.getHotelId())
               .eq(OtaChannel::getChannelCode, dto.getChannelCode());
        Long count = otaChannelMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "渠道编码已存在");
        }
        
        // 2. 创建渠道
        OtaChannel channel = new OtaChannel();
        BeanUtils.copyProperties(dto, channel);
        channel.setChannelType(dto.getChannelType() != null ? dto.getChannelType() : "OTA");
        channel.setAuthType(dto.getAuthType() != null ? dto.getAuthType() : "API_KEY");
        channel.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        
        // 3. 保存到数据库
        otaChannelMapper.insert(channel);
        
        // 4. 记录日志
        log.info("创建OTA渠道: 渠道名称={}, 渠道编码={}", dto.getChannelName(), dto.getChannelCode());
        
        // 5. 返回创建的渠道
        return convertToVO(channel);
    }
    
    /**
     * 更新渠道
     * <p>
     * 更新OTA渠道配置
     * </p>
     * 
     * @param id 渠道ID
     * @param dto 渠道信息
     * @return 更新后的渠道信息
     */
    @Transactional(rollbackFor = Exception.class)
    public OtaChannelVO update(Long id, OtaChannelDTO dto) {
        // 1. 查询渠道是否存在
        OtaChannel channel = otaChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "OTA渠道不存在");
        }
        
        // 2. 更新渠道信息
        BeanUtils.copyProperties(dto, channel);
        
        // 3. 保存到数据库
        otaChannelMapper.updateById(channel);
        
        // 4. 记录日志
        log.info("更新OTA渠道: ID={}, 渠道名称={}", id, dto.getChannelName());
        
        // 5. 返回更新后的渠道
        return convertToVO(channel);
    }
    
    /**
     * 删除渠道
     * <p>
     * 逻辑删除OTA渠道
     * </p>
     * 
     * @param id 渠道ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 查询渠道是否存在
        OtaChannel channel = otaChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "OTA渠道不存在");
        }
        
        // 2. 逻辑删除渠道
        channel.setDeleted(true);
        otaChannelMapper.updateById(channel);
        
        // 3. 记录日志
        log.info("删除OTA渠道: ID={}, 渠道名称={}", id, channel.getChannelName());
    }
    
    /**
     * 获取所有启用的渠道
     * <p>
     * 获取酒店所有启用的OTA渠道
     * </p>
     * 
     * @param hotelId 酒店ID
     * @return 渠道列表
     */
    public List<OtaChannelVO> listActiveChannels(Long hotelId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<OtaChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OtaChannel::getHotelId, hotelId)
               .eq(OtaChannel::getStatus, "ACTIVE")
               .orderByAsc(OtaChannel::getChannelName);
        
        // 2. 执行查询
        List<OtaChannel> channels = otaChannelMapper.selectList(wrapper);
        
        // 3. 转换为VO
        return channels.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将OtaChannel实体转换为VO
     * 
     * @param channel 渠道实体
     * @return 渠道VO
     */
    private OtaChannelVO convertToVO(OtaChannel channel) {
        OtaChannelVO vo = new OtaChannelVO();
        BeanUtils.copyProperties(channel, vo);
        
        // 设置渠道类型名称
        vo.setChannelTypeName(getChannelTypeName(channel.getChannelType()));
        
        // 设置认证类型名称
        vo.setAuthTypeName(getAuthTypeName(channel.getAuthType()));
        
        // 设置状态名称
        vo.setStatusName(getStatusName(channel.getStatus()));
        
        return vo;
    }
    
    /**
     * 获取渠道类型名称
     * 
     * @param type 渠道类型代码
     * @return 渠道类型名称
     */
    private String getChannelTypeName(String type) {
        switch (type) {
            case "OTA":
                return "在线旅行社";
            case "DIRECT":
                return "直销";
            case "CORPORATE":
                return "协议单位";
            default:
                return type;
        }
    }
    
    /**
     * 获取认证类型名称
     * 
     * @param type 认证类型代码
     * @return 认证类型名称
     */
    private String getAuthTypeName(String type) {
        switch (type) {
            case "API_KEY":
                return "API Key";
            case "OAUTH2":
                return "OAuth 2.0";
            case "BASIC":
                return "Basic Auth";
            default:
                return type;
        }
    }
    
    /**
     * 获取状态名称
     * 
     * @param status 状态代码
     * @return 状态名称
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
