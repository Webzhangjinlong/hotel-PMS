package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.Folio;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账务单Mapper接口
 * <p>
 * 负责账务单的数据访问
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface FolioMapper extends BaseMapper<Folio> {
}
