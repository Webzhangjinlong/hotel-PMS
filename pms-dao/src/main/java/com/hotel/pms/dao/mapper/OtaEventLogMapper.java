package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.OtaEventLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * OTA事件日志Mapper接口
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface OtaEventLogMapper extends BaseMapper<OtaEventLog> {
}
