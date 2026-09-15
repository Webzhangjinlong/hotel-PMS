package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Room;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间Mapper接口
 * <p>
 * 负责房间的数据访问
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
}