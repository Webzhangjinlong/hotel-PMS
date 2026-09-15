package com.hotel.pms.service.folio;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.StayConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.*;
import com.hotel.pms.dao.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeamFolioService {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Autowired
    private TeamFolioMapper teamFolioMapper;
    
    @Autowired
    private TeamFolioPaymentMapper teamFolioPaymentMapper;
    
    @Autowired
    private TeamReservationMapper teamReservationMapper;
    
    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private StayMapper stayMapper;

    @Autowired
    private TeamReservationRoomMapper teamReservationRoomMapper;
    
    @Autowired
    private FolioMapper folioMapper;
    
    public PageResponse<TeamFolioVO> pageList(TeamFolioQueryDTO queryDTO) {
        LambdaQueryWrapper<TeamFolio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, TeamFolio::getHotelId, queryDTO.getHotelId())
               .eq(queryDTO.getTeamReservationId() != null, TeamFolio::getTeamReservationId, queryDTO.getTeamReservationId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), TeamFolio::getStatus, queryDTO.getStatus());
        
        if (StringUtils.hasText(queryDTO.getTeamName())) {
            wrapper.inSql(TeamFolio::getTeamReservationId,
                "SELECT id FROM team_reservation WHERE team_name LIKE '%" + queryDTO.getTeamName() + "%'");
        }
        if (StringUtils.hasText(queryDTO.getSettlementType())) {
            wrapper.inSql(TeamFolio::getTeamReservationId,
                "SELECT id FROM team_reservation WHERE settlement_type = '" + queryDTO.getSettlementType() + "'");
        }
        if (StringUtils.hasText(queryDTO.getPayStatus())) {
            if ("PAID".equals(queryDTO.getPayStatus())) {
                wrapper.le(TeamFolio::getBalance, BigDecimal.ZERO);
            } else if ("UNPAID".equals(queryDTO.getPayStatus())) {
                wrapper.gt(TeamFolio::getBalance, BigDecimal.ZERO);
            }
        }
        if (queryDTO.getPayDateStart() != null) {
            wrapper.ge(TeamFolio::getCreatedAt, queryDTO.getPayDateStart().atStartOfDay());
        }
        if (queryDTO.getPayDateEnd() != null) {
            wrapper.lt(TeamFolio::getCreatedAt, queryDTO.getPayDateEnd().plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(TeamFolio::getCreatedAt);
        
        Page<TeamFolio> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<TeamFolio> result = teamFolioMapper.selectPage(page, wrapper);
        
        List<TeamFolioVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }
    
    /**
     * 统计团队账务数据
     */
    public FolioStatsVO getStats(TeamFolioQueryDTO queryDTO) {
        LambdaQueryWrapper<TeamFolio> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, TeamFolio::getHotelId, queryDTO.getHotelId())
               .eq(queryDTO.getTeamReservationId() != null, TeamFolio::getTeamReservationId, queryDTO.getTeamReservationId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), TeamFolio::getStatus, queryDTO.getStatus());
        
        if (StringUtils.hasText(queryDTO.getTeamName())) {
            wrapper.inSql(TeamFolio::getTeamReservationId,
                "SELECT id FROM team_reservation WHERE team_name LIKE '%" + queryDTO.getTeamName() + "%'");
        }
        if (StringUtils.hasText(queryDTO.getSettlementType())) {
            wrapper.inSql(TeamFolio::getTeamReservationId,
                "SELECT id FROM team_reservation WHERE settlement_type = '" + queryDTO.getSettlementType() + "'");
        }
        if (StringUtils.hasText(queryDTO.getPayStatus())) {
            if ("PAID".equals(queryDTO.getPayStatus())) {
                wrapper.le(TeamFolio::getBalance, BigDecimal.ZERO);
            } else if ("UNPAID".equals(queryDTO.getPayStatus())) {
                wrapper.gt(TeamFolio::getBalance, BigDecimal.ZERO);
            }
        }
        if (queryDTO.getPayDateStart() != null) {
            wrapper.ge(TeamFolio::getCreatedAt, queryDTO.getPayDateStart().atStartOfDay());
        }
        if (queryDTO.getPayDateEnd() != null) {
            wrapper.lt(TeamFolio::getCreatedAt, queryDTO.getPayDateEnd().plusDays(1).atStartOfDay());
        }
        
        List<TeamFolio> list = teamFolioMapper.selectList(wrapper);
        
        FolioStatsVO stats = new FolioStatsVO();
        stats.setTotalAmount(BigDecimal.ZERO);
        stats.setPaidAmount(BigDecimal.ZERO);
        stats.setBalance(BigDecimal.ZERO);
        stats.setCount(0);
        
        if (list != null && !list.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            BigDecimal balance = BigDecimal.ZERO;
            
            for (TeamFolio folio : list) {
                totalAmount = totalAmount.add(folio.getTotalAmount() != null ? folio.getTotalAmount() : BigDecimal.ZERO);
                paidAmount = paidAmount.add(folio.getPaidAmount() != null ? folio.getPaidAmount() : BigDecimal.ZERO);
                balance = balance.add(folio.getBalance() != null ? folio.getBalance() : BigDecimal.ZERO);
            }
            
            stats.setTotalAmount(totalAmount);
            stats.setPaidAmount(paidAmount);
            stats.setBalance(balance);
            stats.setCount(list.size());
        }
        
        return stats;
    }
        public TeamFolioVO getById(Long id) {
        TeamFolio folio = teamFolioMapper.selectById(id);
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队账务单不存在");
        }
        return convertToVO(folio);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public TeamFolioPaymentVO addPayment(Long folioId, TeamFolioPaymentDTO dto) {
        TeamFolio folio = teamFolioMapper.selectById(folioId);
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队账务单不存在");
        }
        
        if (!StayConstants.FOLIO_STATUS_OPEN.equals(folio.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单已关闭，无法收款");
        }
        
        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收款金额必须大于0");
        }
        
        if (dto.getAmount().compareTo(folio.getBalance()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收款金额不能大于余额");
        }
        
        // 创建收款记录
        TeamFolioPayment payment = new TeamFolioPayment();
        payment.setHotelId(folio.getHotelId());
        payment.setTeamFolioId(folioId);
        payment.setPaymentNo(generatePaymentNo(folio.getHotelId()));
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setRemark(dto.getRemark());
        teamFolioPaymentMapper.insert(payment);
        
        // 更新账务单
        folio.setPaidAmount(folio.getPaidAmount().add(dto.getAmount()));
        folio.setBalance(folio.getBalance().subtract(dto.getAmount()));
        folio.setPaymentMethod(dto.getPaymentMethod());
        teamFolioMapper.updateById(folio);
        
        // 更新该团队预定下所有入住单的已付金额
        updateStayPaidAmount(folio.getTeamReservationId(), dto.getAmount());
        
        // 同步更新该团队下所有 folio
        updateFoliosPaidAmount(folio.getTeamReservationId(), dto.getAmount());
        
        log.info("团队收款成功：folioId={}, amount={}, paymentNo={}", folioId, dto.getAmount(), payment.getPaymentNo());
        
        return convertPaymentToVO(payment);
    }
    
    public List<TeamFolioPaymentVO> getPayments(Long folioId) {
        LambdaQueryWrapper<TeamFolioPayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFolioPayment::getTeamFolioId, folioId)
               .orderByDesc(TeamFolioPayment::getPaymentTime);
        List<TeamFolioPayment> payments = teamFolioPaymentMapper.selectList(wrapper);
        return payments.stream()
                .map(this::convertPaymentToVO)
                .collect(Collectors.toList());
    }    /**
     * 更新团队预定下所有入住单的已付金额
     * 按照房间金额比例分配收款金额
     */
    private void updateStayPaidAmount(Long teamReservationId, BigDecimal paymentAmount) {
        // 查询该团队预定下的所有房间明细
        LambdaQueryWrapper<TeamReservationRoom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(TeamReservationRoom::getTeamReservationId, teamReservationId);
        List<TeamReservationRoom> rooms = teamReservationRoomMapper.selectList(roomWrapper);
        
        if (rooms == null || rooms.isEmpty()) {
            log.warn("未找到团队预定的房间明细：teamReservationId={}", teamReservationId);
            return;
        }
        
        // 计算房间总金额
        BigDecimal totalRoomAmount = rooms.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按比例分配收款金额到各个入住单
        for (TeamReservationRoom room : rooms) {
            if (room.getStayId() == null) {
                continue;
            }
            
            Stay stay = stayMapper.selectById(room.getStayId());
            if (stay == null) {
                continue;
            }
            
            // 计算该房间应分配的收款金额
            BigDecimal roomAmount = room.getAmount() != null ? room.getAmount() : BigDecimal.ZERO;
            BigDecimal allocatedAmount;
            
            if (totalRoomAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 按比例分配
                allocatedAmount = paymentAmount.multiply(roomAmount).divide(totalRoomAmount, 2, BigDecimal.ROUND_HALF_UP);
            } else {
                // 平均分配
                allocatedAmount = paymentAmount.divide(BigDecimal.valueOf(rooms.size()), 2, BigDecimal.ROUND_HALF_UP);
            }
            
            // 更新入住单的已付金额
            stay.setPaidAmount(stay.getPaidAmount().add(allocatedAmount));
            stayMapper.updateById(stay);
            
            log.info("更新入住单已付金额：stayId={}, allocatedAmount={}, totalPaidAmount={}", 
                    stay.getId(), allocatedAmount, stay.getPaidAmount());
        }
    }
    
    /**
     * 同步更新团队下所有 folio 的已付金额（按比例分配）
     */
    private void updateFoliosPaidAmount(Long teamReservationId, BigDecimal paymentAmount) {
        // 查询该团队预定下的所有入住单
        LambdaQueryWrapper<Stay> stayWrapper = new LambdaQueryWrapper<>();
        stayWrapper.eq(Stay::getTeamReservationId, teamReservationId);
        List<Stay> stays = stayMapper.selectList(stayWrapper);
        
        if (stays == null || stays.isEmpty()) {
            return;
        }
        
        // 计算入住单总金额
        BigDecimal totalStayAmount = stays.stream()
                .map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按比例分配收款金额到各个 folio
        for (Stay stay : stays) {
            // 查询该入住单的账务单
            LambdaQueryWrapper<Folio> folioWrapper = new LambdaQueryWrapper<>();
            folioWrapper.eq(Folio::getStayId, stay.getId());
            Folio folio = folioMapper.selectOne(folioWrapper);
            
            if (folio != null) {
                // 计算该入住单应分配的收款金额
                BigDecimal stayAmount = stay.getTotalAmount() != null ? stay.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal allocatedAmount;
                
                if (totalStayAmount.compareTo(BigDecimal.ZERO) > 0) {
                    allocatedAmount = paymentAmount.multiply(stayAmount)
                            .divide(totalStayAmount, 2, BigDecimal.ROUND_HALF_UP);
                } else {
                    allocatedAmount = paymentAmount.divide(
                            BigDecimal.valueOf(stays.size()), 2, BigDecimal.ROUND_HALF_UP);
                }
                
                // 更新 folio 的已付金额
                folio.setPaidAmount(folio.getPaidAmount().add(allocatedAmount));
                folio.setBalance(folio.getBalance().subtract(allocatedAmount));
                folioMapper.updateById(folio);
                
                log.info("同步更新 folio 已付金额：folioId={}, allocatedAmount={}, totalPaidAmount={}", 
                        folio.getId(), allocatedAmount, folio.getPaidAmount());
            }
        }
    }


    
    @Transactional(rollbackFor = Exception.class)
    public void closeFolio(Long folioId) {
        TeamFolio folio = teamFolioMapper.selectById(folioId);
        if (folio == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "团队账务单不存在");
        }
        
        if (folio.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "账务单仍有余额未结清，无法关闭");
        }
        
        folio.setStatus(StayConstants.FOLIO_STATUS_CLOSED);
        teamFolioMapper.updateById(folio);
        
        log.info("团队账务单关闭：folioId={}", folioId);
    }
    
    private TeamFolioVO convertToVO(TeamFolio folio) {
        TeamFolioVO vo = new TeamFolioVO();
        vo.setId(folio.getId());
        vo.setHotelId(folio.getHotelId());
        vo.setTeamReservationId(folio.getTeamReservationId());
        vo.setFolioNo(folio.getFolioNo());
        vo.setTotalAmount(folio.getTotalAmount());
        vo.setPaidAmount(folio.getPaidAmount());
        vo.setBalance(folio.getBalance());
        vo.setStatus(folio.getStatus());
        vo.setPaymentMethod(folio.getPaymentMethod());
        vo.setRemark(folio.getRemark());
        vo.setCreatedAt(folio.getCreatedAt());
        
        // 查询团队预订信息
        TeamReservation reservation = teamReservationMapper.selectById(folio.getTeamReservationId());
        if (reservation != null) {
            vo.setTeamReservationNo(reservation.getTeamReservationNo());
            vo.setTeamName(reservation.getTeamName());
            vo.setSettlementType(reservation.getSettlementType());
        }
        
        return vo;
    }
    
    private TeamFolioPaymentVO convertPaymentToVO(TeamFolioPayment payment) {
        TeamFolioPaymentVO vo = new TeamFolioPaymentVO();
        vo.setId(payment.getId());
        vo.setTeamFolioId(payment.getTeamFolioId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setAmount(payment.getAmount());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setPaymentTime(payment.getPaymentTime());
        vo.setOperatorName(payment.getOperatorName());
        vo.setRemark(payment.getRemark());
        return vo;
    }
    
    private String generatePaymentNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "P" + dateStr;
        
        LambdaQueryWrapper<TeamFolioPayment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamFolioPayment::getHotelId, hotelId)
               .likeRight(TeamFolioPayment::getPaymentNo, prefix)
               .orderByDesc(TeamFolioPayment::getPaymentNo)
               .last("LIMIT 1");
        
        TeamFolioPayment latest = teamFolioPaymentMapper.selectOne(wrapper);
        
        int nextSeq = 1;
        if (latest != null && latest.getPaymentNo() != null) {
            String lastNo = latest.getPaymentNo();
            String seqStr = lastNo.substring(lastNo.length() - 4);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }
        
        return prefix + String.format("%04d", nextSeq);
    }
}


