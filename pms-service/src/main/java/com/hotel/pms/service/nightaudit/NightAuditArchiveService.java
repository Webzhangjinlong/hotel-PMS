package com.hotel.pms.service.nightaudit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.NightAuditArchiveVO;
import com.hotel.pms.common.dto.NightAuditQueryDTO;
import com.hotel.pms.common.dto.NightAuditVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 夜审历史数据管理服务类
 */
@Slf4j
@Service
public class NightAuditArchiveService {
    
    @Autowired
    private NightAuditMapper nightAuditMapper;
    
    @Autowired
    private NightAuditStepMapper nightAuditStepMapper;
    
    @Autowired
    private NightAuditArchiveMapper nightAuditArchiveMapper;
    
    /**
     * 归档历史夜审数据
     * <p>
     * 将指定日期之前的夜审数据归档到归档表
     * </p>
     * 
     * @param hotelId 酒店ID
     * @param beforeDate 归档此日期之前的数据
     * @param archivedBy 归档人
     * @return 归档的记录数
     */
    @Transactional(rollbackFor = Exception.class)
    public int archiveData(Long hotelId, LocalDate beforeDate, String archivedBy) {
        log.info("开始归档夜审数据: hotelId={}, beforeDate={}, archivedBy={}", hotelId, beforeDate, archivedBy);
        
        // 查询需要归档的夜审记录
        List<NightAudit> archives = nightAuditMapper.selectList(
            new LambdaQueryWrapper<NightAudit>()
                .eq(NightAudit::getHotelId, hotelId)
                .lt(NightAudit::getAuditDate, beforeDate)
                .in(NightAudit::getStatus, "COMPLETED", "COMPLETED_WITH_ERRORS")
        );
        
        if (archives.isEmpty()) {
            log.info("没有需要归档的夜审数据");
            return 0;
        }
        
        int archivedCount = 0;
        
        for (NightAudit audit : archives) {
            try {
                // 归档夜审记录
                NightAuditArchive archive = new NightAuditArchive();
                archive.setOriginalId(audit.getId());
                archive.setHotelId(audit.getHotelId());
                archive.setAuditDate(audit.getAuditDate());
                archive.setStatus(audit.getStatus());
                archive.setTotalRooms(audit.getTotalRooms());
                archive.setOccupiedRooms(audit.getOccupiedRooms());
                archive.setAvailableRooms(audit.getAvailableRooms());
                archive.setTotalRevenue(audit.getTotalRevenue());
                archive.setRoomRevenue(audit.getRoomRevenue());
                archive.setExtraRevenue(audit.getExtraRevenue());
                archive.setOccupancyRate(audit.getOccupancyRate());
                archive.setAdr(audit.getAdr());
                archive.setRevpar(audit.getRevpar());
                archive.setStartedAt(audit.getStartedAt());
                archive.setCompletedAt(audit.getCompletedAt());
                archive.setErrorMessage(audit.getErrorMessage());
                archive.setArchivedAt(LocalDateTime.now());
                archive.setArchivedBy(archivedBy);
                
                nightAuditArchiveMapper.insert(archive);
                
                // 删除原始夜审步骤记录
                nightAuditStepMapper.delete(
                    new LambdaQueryWrapper<NightAuditStep>()
                        .eq(NightAuditStep::getNightAuditId, audit.getId())
                );
                
                // 删除原始夜审记录
                nightAuditMapper.deleteById(audit.getId());
                
                archivedCount++;
                log.info("归档夜审记录成功: auditId={}, auditDate={}", audit.getId(), audit.getAuditDate());
                
            } catch (Exception e) {
                log.error("归档夜审记录失败: auditId={}", audit.getId(), e);
            }
        }
        
        log.info("夜审数据归档完成: 归档数量={}", archivedCount);
        return archivedCount;
    }
    
    /**
     * 查询归档数据
     */
    public PageResponse<NightAuditArchiveVO> getArchiveList(NightAuditQueryDTO queryDTO) {
        LambdaQueryWrapper<NightAuditArchive> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getHotelId() != null) {
            wrapper.eq(NightAuditArchive::getHotelId, queryDTO.getHotelId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(NightAuditArchive::getAuditDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(NightAuditArchive::getAuditDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(NightAuditArchive::getAuditDate);
        
        Page<NightAuditArchive> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<NightAuditArchive> result = nightAuditArchiveMapper.selectPage(page, wrapper);
        
        List<NightAuditArchiveVO> records = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResponse<>(records, result.getTotal(), 
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 夜审归档记录实体转 VO
     */
    private NightAuditArchiveVO toVO(NightAuditArchive archive) {
        NightAuditArchiveVO vo = new NightAuditArchiveVO();
        BeanUtils.copyProperties(archive, vo);
        return vo;
    }
    
    /**
     * 查询可归档的数据统计
     */
    public ArchiveStatsVO getArchiveStats(Long hotelId, LocalDate beforeDate) {
        // 查询待归档的夜审记录数
        Long pendingCount = nightAuditMapper.selectCount(
            new LambdaQueryWrapper<NightAudit>()
                .eq(NightAudit::getHotelId, hotelId)
                .lt(NightAudit::getAuditDate, beforeDate)
                .in(NightAudit::getStatus, "COMPLETED", "COMPLETED_WITH_ERRORS")
        );
        
        // 查询已归档的记录数
        Long archivedCount = nightAuditArchiveMapper.selectCount(
            new LambdaQueryWrapper<NightAuditArchive>()
                .eq(NightAuditArchive::getHotelId, hotelId)
        );
        
        // 查询最早的未归档记录日期
        NightAudit earliest = nightAuditMapper.selectOne(
            new LambdaQueryWrapper<NightAudit>()
                .eq(NightAudit::getHotelId, hotelId)
                .orderByAsc(NightAudit::getAuditDate)
                .last("LIMIT 1")
        );
        
        return ArchiveStatsVO.builder()
            .pendingCount(pendingCount)
            .archivedCount(archivedCount)
            .earliestDate(earliest != null ? earliest.getAuditDate() : null)
            .build();
    }
    
    /**
     * 归档统计VO
     */
    @lombok.Data
    @lombok.Builder
    public static class ArchiveStatsVO {
        private Long pendingCount;
        private Long archivedCount;
        private LocalDate earliestDate;
    }
}