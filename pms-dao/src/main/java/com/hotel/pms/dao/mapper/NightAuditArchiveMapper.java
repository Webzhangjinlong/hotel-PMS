package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.NightAuditArchive;
import org.apache.ibatis.annotations.Mapper;

/**
 * 夜审归档Mapper接口
 */
@Mapper
public interface NightAuditArchiveMapper extends BaseMapper<NightAuditArchive> {
    
}