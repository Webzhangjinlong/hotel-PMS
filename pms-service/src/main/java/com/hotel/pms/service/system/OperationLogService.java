package com.hotel.pms.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.OperationLogQueryDTO;
import com.hotel.pms.common.dto.OperationLogVO;
import com.hotel.pms.dao.entity.OperationLogEntity;
import com.hotel.pms.dao.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;

/**
 * 操作日志服务
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {
    
    private final OperationLogMapper operationLogMapper;
    
    /**
     * 分页查询操作日志
     */
    public IPage<OperationLogVO> getOperationLogPage(Long hotelId, OperationLogQueryDTO queryDTO) {
        Page<OperationLogEntity> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogEntity::getHotelId, hotelId);
        
        if (StringUtils.hasText(queryDTO.getModule())) {
            wrapper.eq(OperationLogEntity::getModule, queryDTO.getModule());
        }
        
        if (StringUtils.hasText(queryDTO.getAction())) {
            wrapper.eq(OperationLogEntity::getAction, queryDTO.getAction());
        }
        
        if (StringUtils.hasText(queryDTO.getOperatorName())) {
            wrapper.like(OperationLogEntity::getOperatorName, queryDTO.getOperatorName());
        }
        
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(OperationLogEntity::getCreatedAt, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(OperationLogEntity::getCreatedAt, queryDTO.getEndDate().atTime(LocalTime.MAX));
        }
        
        wrapper.orderByDesc(OperationLogEntity::getCreatedAt);
        
        return operationLogMapper.selectPage(page, wrapper).convert(this::toVO);
    }
    
    /**
     * 查询操作日志详情
     */
    public OperationLogVO getOperationLogById(Long id) {
        OperationLogEntity entity = operationLogMapper.selectById(id);
        return entity == null ? null : toVO(entity);
    }

    /**
     * 实体转 VO
     */
    private OperationLogVO toVO(OperationLogEntity entity) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
