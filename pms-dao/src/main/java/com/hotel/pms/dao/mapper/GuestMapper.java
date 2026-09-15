package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Guest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客人Mapper接口
 * <p>
 * 负责客人的数据访问
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface GuestMapper extends BaseMapper<Guest> {
}
