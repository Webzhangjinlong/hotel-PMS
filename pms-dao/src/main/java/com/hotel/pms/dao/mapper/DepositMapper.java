package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Deposit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 押金Mapper接口
 * <p>
 * 提供押金数据的CRUD操作
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface DepositMapper extends BaseMapper<Deposit> {
    
}
