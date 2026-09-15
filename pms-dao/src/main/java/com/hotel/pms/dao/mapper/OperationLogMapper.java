package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
