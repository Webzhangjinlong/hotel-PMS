package com.hotel.pms.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hotel.pms.dao.entity.InvoiceItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票明细Mapper接口
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Mapper
public interface InvoiceItemMapper extends BaseMapper<InvoiceItem> {
}
