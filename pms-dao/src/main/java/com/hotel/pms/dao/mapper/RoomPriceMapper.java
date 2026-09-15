package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.RoomPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房价Mapper接口
 * <p>
 * 负责房价的数据访问
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface RoomPriceMapper extends BaseMapper<RoomPrice> {
    
    /**
     * 批量插入或更新房价
     * 
     * @param prices 房价列表
     * @return 影响行数
     */
    int batchInsertOrUpdate(@Param("prices") List<RoomPrice> prices);
}
