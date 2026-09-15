package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.RoomType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房型Mapper接口
 * <p>
 * 负责房型的数据访问
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface RoomTypeMapper extends BaseMapper<RoomType> {
    
}
