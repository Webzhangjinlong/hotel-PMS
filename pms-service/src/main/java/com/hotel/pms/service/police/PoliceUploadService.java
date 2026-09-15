package com.hotel.pms.service.police;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.PoliceManualUploadDTO;
import com.hotel.pms.common.dto.PoliceUploadQueryDTO;
import com.hotel.pms.common.dto.PoliceUploadStatsVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.PoliceUploadRecord;
import com.hotel.pms.dao.mapper.PoliceUploadRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 公安上传服务类
 * <p>
 * 负责客人信息上传公安系统的业务处理
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PoliceUploadService {

    private final PoliceUploadRecordMapper policeUploadRecordMapper;

    /**
     * 分页查询上传记录
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public IPage<PoliceUploadRecord> getUploadList(PoliceUploadQueryDTO queryDTO) {
        Page<PoliceUploadRecord> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        LambdaQueryWrapper<PoliceUploadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, PoliceUploadRecord::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), PoliceUploadRecord::getStatus, queryDTO.getStatus())
               .like(StringUtils.hasText(queryDTO.getGuestName()), PoliceUploadRecord::getGuestName, queryDTO.getGuestName())
               .ge(queryDTO.getStartDate() != null, PoliceUploadRecord::getCreatedAt, queryDTO.getStartDate().atStartOfDay())
               .le(queryDTO.getEndDate() != null, PoliceUploadRecord::getCreatedAt, queryDTO.getEndDate().atTime(LocalTime.MAX))
               .orderByDesc(PoliceUploadRecord::getCreatedAt);

        return policeUploadRecordMapper.selectPage(page, wrapper);
    }

    /**
     * 查询上传记录详情
     *
     * @param id 记录ID
     * @return 上传记录
     */
    public PoliceUploadRecord getUploadById(Long id) {
        PoliceUploadRecord record = policeUploadRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "上传记录不存在");
        }
        return record;
    }

    /**
     * 重试上传失败的记录
     *
     * @param id 记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void retryUpload(Long id) {
        PoliceUploadRecord record = getUploadById(id);

        if (!"FAILED".equals(record.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只能重试失败的记录");
        }

        // TODO: 调用公安系统接口上传数据
        log.info("重试上传：recordId={}, guestName={}, idNo={}", id, record.getGuestName(), record.getIdNo());

        // 模拟上传成功
        record.setStatus("SUCCESS");
        record.setUploadTime(LocalDateTime.now());
        record.setErrorMessage(null);
        record.setRetryCount(record.getRetryCount() + 1);
        policeUploadRecordMapper.updateById(record);

        log.info("重试上传成功：recordId={}", id);
    }

    /**
     * 批量重试上传失败的记录
     *
     * @param ids 记录ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchRetryUpload(List<Long> ids) {
        for (Long id : ids) {
            try {
                retryUpload(id);
            } catch (Exception e) {
                log.error("批量重试上传失败：recordId={}, error={}", id, e.getMessage());
            }
        }
    }

    /**
     * 人工补传
     *
     * @param dto 补传数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void manualUpload(PoliceManualUploadDTO dto) {
        // TODO: 调用公安系统接口上传数据
        log.info("人工补传：guestName={}, idNo={}", dto.getGuestName(), dto.getGuestIdNo());

        // 创建上传记录
        PoliceUploadRecord record = new PoliceUploadRecord();
        record.setHotelId(dto.getHotelId());
        record.setStayNo(dto.getStayNo());
        record.setGuestName(dto.getGuestName());
        record.setIdNo(dto.getGuestIdNo());
        record.setPhone(dto.getGuestPhone());
        record.setCheckInTime(dto.getCheckInTime());
        record.setStatus("SUCCESS");
        record.setUploadTime(LocalDateTime.now());
        record.setIsManual(true);
        record.setRemark(dto.getRemark());
        record.setRetryCount(0);
        policeUploadRecordMapper.insert(record);

        log.info("人工补传成功：recordId={}", record.getId());
    }

    /**
     * 查询上传统计
     *
     * @param hotelId 酒店ID
     * @return 统计数据
     */
    public PoliceUploadStatsVO getUploadStats(Long hotelId) {
        LocalDate today = LocalDate.now();

        // 今日上传数
        LambdaQueryWrapper<PoliceUploadRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(PoliceUploadRecord::getHotelId, hotelId)
                   .ge(PoliceUploadRecord::getCreatedAt, today.atStartOfDay());
        long todayCount = policeUploadRecordMapper.selectCount(todayWrapper);

        // 成功数
        LambdaQueryWrapper<PoliceUploadRecord> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(PoliceUploadRecord::getHotelId, hotelId)
                     .eq(PoliceUploadRecord::getStatus, "SUCCESS");
        long successCount = policeUploadRecordMapper.selectCount(successWrapper);

        // 失败数
        LambdaQueryWrapper<PoliceUploadRecord> failWrapper = new LambdaQueryWrapper<>();
        failWrapper.eq(PoliceUploadRecord::getHotelId, hotelId)
                  .eq(PoliceUploadRecord::getStatus, "FAILED");
        long failCount = policeUploadRecordMapper.selectCount(failWrapper);

        // 待上传数
        LambdaQueryWrapper<PoliceUploadRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(PoliceUploadRecord::getHotelId, hotelId)
                     .eq(PoliceUploadRecord::getStatus, "PENDING");
        long pendingCount = policeUploadRecordMapper.selectCount(pendingWrapper);

        return PoliceUploadStatsVO.builder()
                .todayCount(todayCount)
                .successCount(successCount)
                .failCount(failCount)
                .pendingCount(pendingCount)
                .build();
    }
}
