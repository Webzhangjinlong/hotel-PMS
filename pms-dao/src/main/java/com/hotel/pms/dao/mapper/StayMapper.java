package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Stay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住单Mapper接口
 * <p>
 * 负责入住单的数据访问
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface StayMapper extends BaseMapper<Stay> {
}
