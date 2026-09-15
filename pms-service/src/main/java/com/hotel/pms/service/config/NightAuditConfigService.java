package com.hotel.pms.service.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hotel.pms.common.dto.NightAuditConfigDTO;
import com.hotel.pms.common.dto.NightAuditConfigVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Hotel;
import com.hotel.pms.dao.entity.HotelConfig;
import com.hotel.pms.dao.mapper.HotelConfigMapper;
import com.hotel.pms.dao.mapper.HotelMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 夜审配置服务类
 */
@Slf4j
@Service
public class NightAuditConfigService {

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private HotelConfigMapper hotelConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 配置键常量
    private static final String CONFIG_AUDIT_TIME = "NIGHT_AUDIT_TIME";
    private static final String CONFIG_AUTO_AUDIT_ENABLED = "NIGHT_AUDIT_AUTO_ENABLED";
    private static final String CONFIG_ENABLED_STEPS = "NIGHT_AUDIT_ENABLED_STEPS";
    private static final String CONFIG_NOTIFICATION = "NIGHT_AUDIT_NOTIFICATION";

    // 所有可用的夜审步骤
    private static final Map<String, String> AVAILABLE_STEPS = new LinkedHashMap<>();

    static {
        AVAILABLE_STEPS.put("PRE_CHECK", "预检查");
        AVAILABLE_STEPS.put("AUTO_POST_ROOM_CHARGES", "自动过房费");
        AVAILABLE_STEPS.put("HANDLE_OVERTIME", "处理超时离店");
        AVAILABLE_STEPS.put("TEAM_FOLIO_SUMMARY", "团队账务汇总");
        AVAILABLE_STEPS.put("POLICE_UPLOAD_CHECK", "公安上传检查");
        AVAILABLE_STEPS.put("CALCULATE_STATISTICS", "计算统计指标");
        AVAILABLE_STEPS.put("GENERATE_DAILY_REPORT", "生成营业日报");
        AVAILABLE_STEPS.put("LOCK_DATA", "锁定数据");
    }

    /**
     * 获取夜审配置
     */
    public NightAuditConfigVO getConfig(Long hotelId) {
        // 验证酒店存在
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }

        // 从酒店表获取基本配置
        LocalTime auditTime = hotel.getAuditTime() != null ? hotel.getAuditTime() : LocalTime.of(4, 0);
        Boolean autoAuditEnabled = hotel.getAutoAuditEnabled() != null ? hotel.getAutoAuditEnabled() : true;

        // 从配置表获取详细配置
        List<String> enabledSteps = getEnabledSteps(hotelId);
        NightAuditConfigVO.NotificationConfig notification = getNotificationConfig(hotelId);

        // 构建步骤信息列表
        List<NightAuditConfigVO.StepInfo> stepInfos = AVAILABLE_STEPS.entrySet().stream()
            .map(entry -> NightAuditConfigVO.StepInfo.builder()
                .stepName(entry.getKey())
                .displayName(entry.getValue())
                .enabled(enabledSteps.contains(entry.getKey()))
                .description(getStepDescription(entry.getKey()))
                .build())
            .collect(Collectors.toList());

        return NightAuditConfigVO.builder()
            .hotelId(hotelId)
            .auditTime(auditTime)
            .autoAuditEnabled(autoAuditEnabled)
            .enabledSteps(enabledSteps)
            .availableSteps(stepInfos)
            .notification(notification)
            .build();
    }

    /**
     * 更新夜审配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long hotelId, NightAuditConfigDTO configDTO) throws JsonProcessingException {
        // 验证酒店存在
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }

        // 更新酒店表的基本配置
        if (configDTO.getAuditTime() != null) {
            hotel.setAuditTime(configDTO.getAuditTime());
        }
        if (configDTO.getAutoAuditEnabled() != null) {
            hotel.setAutoAuditEnabled(configDTO.getAutoAuditEnabled());
        }
        hotelMapper.updateById(hotel);

        // 更新配置表的详细配置
        if (configDTO.getEnabledSteps() != null) {
            saveConfig(hotelId, CONFIG_ENABLED_STEPS, objectMapper.writeValueAsString(configDTO.getEnabledSteps()));
        }

        if (configDTO.getNotification() != null) {
            saveConfig(hotelId, CONFIG_NOTIFICATION, objectMapper.writeValueAsString(configDTO.getNotification()));
        }

        log.info("夜审配置更新成功: hotelId={}", hotelId);
    }

    /**
     * 获取启用的步骤列表
     */
    private List<String> getEnabledSteps(Long hotelId) {
        HotelConfig config = getConfigByKey(hotelId, CONFIG_ENABLED_STEPS);
        if (config != null && config.getConfigValue() != null) {
            try {
                return objectMapper.readValue(config.getConfigValue(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("解析启用步骤配置失败: {}", e.getMessage());
            }
        }

        // 默认启用所有步骤
        return new ArrayList<>(AVAILABLE_STEPS.keySet());
    }

    /**
     * 获取通知配置
     */
    private NightAuditConfigVO.NotificationConfig getNotificationConfig(Long hotelId) {
        HotelConfig config = getConfigByKey(hotelId, CONFIG_NOTIFICATION);
        if (config != null && config.getConfigValue() != null) {
            try {
                return objectMapper.readValue(config.getConfigValue(), NightAuditConfigVO.NotificationConfig.class);
            } catch (Exception e) {
                log.warn("解析通知配置失败: {}", e.getMessage());
            }
        }

        // 默认通知配置
        return NightAuditConfigVO.NotificationConfig.builder()
            .enabled(false)
            .notifyOnComplete(true)
            .notifyOnFailure(true)
            .emailRecipients(new ArrayList<>())
            .build();
    }

    /**
     * 获取配置项
     */
    private HotelConfig getConfigByKey(Long hotelId, String configKey) {
        return hotelConfigMapper.selectOne(
            new LambdaQueryWrapper<HotelConfig>()
                .eq(HotelConfig::getHotelId, hotelId)
                .eq(HotelConfig::getConfigKey, configKey)
                .eq(HotelConfig::getDeleted, false)
        );
    }

    /**
     * 保存配置项
     */
    private void saveConfig(Long hotelId, String configKey, String configValue) {
        HotelConfig existingConfig = getConfigByKey(hotelId, configKey);

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

    /**
     * 获取步骤描述
     */
    private String getStepDescription(String stepName) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("PRE_CHECK", "检查酒店状态和数据完整性");
        descriptions.put("AUTO_POST_ROOM_CHARGES", "为在住客人自动过房费");
        descriptions.put("HANDLE_OVERTIME", "处理超时离店客人");
        descriptions.put("TEAM_FOLIO_SUMMARY", "汇总团队账务");
        descriptions.put("POLICE_UPLOAD_CHECK", "检查公安上传状态");
        descriptions.put("CALCULATE_STATISTICS", "计算入住率、ADR、RevPAR等指标");
        descriptions.put("GENERATE_DAILY_REPORT", "生成营业日报");
        descriptions.put("LOCK_DATA", "锁定当日数据防止修改");
        return descriptions.getOrDefault(stepName, "");
    }
}
