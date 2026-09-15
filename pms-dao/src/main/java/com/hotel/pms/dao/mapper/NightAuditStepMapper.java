package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.NightAuditStep;
import org.apache.ibatis.annotations.Mapper;

/**
 * 夜审步骤Mapper接口
 */
@Mapper
public interface NightAuditStepMapper extends BaseMapper<NightAuditStep> {
}
