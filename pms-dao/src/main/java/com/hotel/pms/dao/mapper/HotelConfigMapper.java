package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.HotelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 酒店配置Mapper接口
 * <p>
 * 负责酒店配置的数据访问
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface HotelConfigMapper extends BaseMapper<HotelConfig> {
    
}
