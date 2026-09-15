package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.StayGuest;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入住同住人Mapper接口
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface StayGuestMapper extends BaseMapper<StayGuest> {

    /**
     * 根据入住单ID列表批量查询同住人
     * 
     * @param stayIds 入住单ID列表
     * @return 同住人列表
     */
    List<StayGuest> selectByStayIds(@Param("stayIds") List<Long> stayIds);
}
