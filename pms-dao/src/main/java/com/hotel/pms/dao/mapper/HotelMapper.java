package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 酒店Mapper
 */
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
}