package com.hotel.pms.service.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.HotelConfigDTO;
import com.hotel.pms.common.dto.HotelConfigVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.HotelConfig;
import com.hotel.pms.dao.mapper.HotelConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 酒店配置服务类
 */
@Slf4j
@Service
public class HotelConfigService {
    
    @Autowired
    private HotelConfigMapper hotelConfigMapper;
    
    /**
     * 获取酒店所有配置
     */
    public List<HotelConfigVO> getConfigsByHotelId(Long hotelId) {
        List<HotelConfig> configs = hotelConfigMapper.selectList(
            new LambdaQueryWrapper<HotelConfig>()
                .eq(HotelConfig::getHotelId, hotelId)
                .eq(HotelConfig::getDeleted, false)
        );
        
        return configs.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取酒店指定配置
     */
    public HotelConfigVO getConfig(Long hotelId, String configKey) {
        HotelConfig config = hotelConfigMapper.selectOne(
            new LambdaQueryWrapper<HotelConfig>()
                .eq(HotelConfig::getHotelId, hotelId)
                .eq(HotelConfig::getConfigKey, configKey)
                .eq(HotelConfig::getDeleted, false)
        );
        
        if (config == null) {
            return null;
        }
        return convertToVO(config);
    }
    
    /**
     * 批量更新配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigs(Long hotelId, Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            
            HotelConfig existingConfig = hotelConfigMapper.selectOne(
                new LambdaQueryWrapper<HotelConfig>()
                    .eq(HotelConfig::getHotelId, hotelId)
                    .eq(HotelConfig::getConfigKey, configKey)
            );
            
            if (existingConfig != null) {
                existingConfig.setConfigValue(configValue);
                existingConfig.setVersion(existingConfig.getVersion() + 1);
                hotelConfigMapper.updateById(existingConfig);
            } else {
                HotelConfig newConfig = new HotelConfig();
                newConfig.setHotelId(hotelId);
                newConfig.setConfigKey(configKey);
                newConfig.setConfigValue(configValue);
                newConfig.setDeleted(false);
                newConfig.setVersion(0);
                hotelConfigMapper.insert(newConfig);
            }
        }
        
        log.info("酒店配置更新成功: hotelId={}, configKeys={}", hotelId, configs.keySet());
    }
    
    /**
     * 转换为VO
     */
    private HotelConfigVO convertToVO(HotelConfig config) {
        return HotelConfigVO.builder()
            .id(config.getId())
            .hotelId(config.getHotelId())
            .configKey(config.getConfigKey())
            .configValue(config.getConfigValue())
            .description(config.getDescription())
            .build();
    }
}
