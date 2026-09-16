package com.hotel.pms.service.reservation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.ReservationConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Hotel;
import com.hotel.pms.dao.entity.Reservation;
import com.hotel.pms.dao.entity.ReservationPrepayment;
import com.hotel.pms.dao.entity.Room;
import com.hotel.pms.dao.entity.RoomType;
import com.hotel.pms.dao.mapper.HotelMapper;
import com.hotel.pms.dao.mapper.ReservationMapper;
import com.hotel.pms.dao.mapper.ReservationPrepaymentMapper;
import com.hotel.pms.dao.mapper.RoomMapper;
import com.hotel.pms.dao.mapper.RoomTypeMapper;
import com.hotel.pms.service.price.RoomPriceService;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.stream.Collectors;

/**
 * 预订服务类
 * <p>
 * 负责预订的业务逻辑处理
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class ReservationService extends BaseService<Reservation, ReservationMapper> {

    /** 日期格式 */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 预订号序号（简化版，生产环境应使用Redis或数据库序列） */

    @Autowired
    private HotelMapper hotelMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomPriceService roomPriceService;

    @Autowired
    private ReservationPrepaymentMapper prepaymentMapper;

    /**
     * 分页查询预订列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<ReservationVO> pageList(ReservationQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Reservation::getHotelId, queryDTO.getHotelId())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Reservation::getStatus, queryDTO.getStatus())
               .eq(queryDTO.getRoomTypeId() != null, Reservation::getRoomTypeId, queryDTO.getRoomTypeId())
               .like(StringUtils.hasText(queryDTO.getGuestName()), Reservation::getGuestName, queryDTO.getGuestName())
               .eq(StringUtils.hasText(queryDTO.getGuestPhone()), Reservation::getGuestPhone, queryDTO.getGuestPhone())
               .eq(StringUtils.hasText(queryDTO.getReservationNo()), Reservation::getReservationNo, queryDTO.getReservationNo())
               .ge(queryDTO.getCheckInDateStart() != null, Reservation::getCheckInDate, queryDTO.getCheckInDateStart())
               .le(queryDTO.getCheckInDateEnd() != null, Reservation::getCheckInDate, queryDTO.getCheckInDateEnd());

        // 排除指定状态（如已入住的预订）
        if (org.springframework.util.StringUtils.hasText(queryDTO.getExcludeStatus())) {
            java.util.List<String> excludeList = java.util.Arrays.asList(queryDTO.getExcludeStatus().split(","));
            wrapper.notIn(Reservation::getStatus, excludeList);
        }

        wrapper.orderByDesc(Reservation::getCreatedAt);

        // 执行分页查询
        Page<Reservation> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Reservation> result = mapper.selectPage(page, wrapper);

        // 转换为VO
        List<ReservationVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 根据ID查询预订
     *
     * @param id 预订ID
     * @return 预订信息
     */
    public ReservationVO getById(Long id) {
        Reservation reservation = mapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }
        return convertToVO(reservation);
    }



    /**
     * 计算总房费（根据每日房价）
     *
     * @param hotelId 酒店ID
     * @param roomTypeId 房型ID
     * @param checkInDate 入住日期
     * @param checkOutDate 离店日期
     * @return 总房费
     */
    private BigDecimal calculateTotalAmount(Long hotelId, Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate, Long pricePlanId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;

        log.info("开始计算总房费：hotelId={}, roomTypeId={}, 入住={}, 离店={}", hotelId, roomTypeId, checkInDate, checkOutDate);

        while (currentDate.isBefore(checkOutDate)) {
            // 查询当日房价
            RoomPriceVO priceVO = roomPriceService.getPriceByDate(hotelId, roomTypeId, currentDate, pricePlanId);

            if (priceVO != null && priceVO.getPrice() != null) {
                // 使用每日房价
                totalAmount = totalAmount.add(priceVO.getPrice());
                log.info("日期{}: 使用每日房价={}, 累计金额={}", currentDate, priceVO.getPrice(), totalAmount);
            } else {
                // 如果没有房价信息，使用房型基础价格
                RoomType roomType = roomTypeMapper.selectById(roomTypeId);
                if (roomType != null) {
                    totalAmount = totalAmount.add(roomType.getBasePrice());
                    log.info("日期{}: 使用基础价格={}, 累计金额={}", currentDate, roomType.getBasePrice(), totalAmount);
                } else {
                    log.warn("日期{}: 未找到房型信息，roomTypeId={}", currentDate, roomTypeId);
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        log.info("计算总房费完成：总金额={}", totalAmount);
        return totalAmount;
    }

    /**
     * 创建预订
     *
     * @param dto 创建参数
     * @return 预订信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationVO create(ReservationCreateDTO dto) {
        // 验证酒店存在
        Hotel hotel = hotelMapper.selectById(dto.getHotelId());
        if (hotel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "酒店不存在");
        }

        // 验证房型存在
        RoomType roomType = roomTypeMapper.selectById(dto.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "房型不存在");
        }

        // 验证日期
        if (dto.getCheckInDate().isAfter(dto.getCheckOutDate())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "离店日期必须晚于入住日期");
        }

        if (dto.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "入住日期不能早于今天");
        }

        // 计算房晚数
        int nights = dto.getCheckOutDate().compareTo(dto.getCheckInDate());

        // 计算总金额（优先使用前端传递的金额，否则使用每日房价计算）
        BigDecimal totalAmount = dto.getTotalAmount();
        if (totalAmount == null) {
            totalAmount = calculateTotalAmount(dto.getHotelId(), dto.getRoomTypeId(), dto.getCheckInDate(), dto.getCheckOutDate(), dto.getPricePlanId());
        }

        // 创建预订实体
        Reservation reservation = new Reservation();
        BeanUtils.copyProperties(dto, reservation);
        reservation.setReservationNo(generateReservationNo(dto.getHotelId()));
        reservation.setNights(nights);
        reservation.setTotalAmount(totalAmount);
        reservation.setPricePlanId(dto.getPricePlanId());
        reservation.setStatus(ReservationConstants.STATUS_PENDING);
        if (!StringUtils.hasText(reservation.getSource())) {
            reservation.setSource(ReservationConstants.SOURCE_WALK_IN);
        }

        // 保存到数据库
        mapper.insert(reservation);

        // 【预付款/押金：金额>0 时写入预订预付款记录（reservation_prepayment）】
        if (dto.getPrepaymentAmount() != null && dto.getPrepaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
            savePrepayment(reservation.getId(), dto.getHotelId(), "PREPAYMENT", dto.getPrepaymentAmount(), dto.getPaymentMethod());
        }
        if (dto.getDepositAmount() != null && dto.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            savePrepayment(reservation.getId(), dto.getHotelId(), "DEPOSIT", dto.getDepositAmount(), dto.getPaymentMethod());
        }

        log.info("创建预订成功：hotelId={}, reservationNo={}, guestName={}",
                dto.getHotelId(), reservation.getReservationNo(), dto.getGuestName());

        return convertToVO(reservation);
    }

    /**
     * 写入预订预付款/押金记录（reservation_prepayment）
     *
     * @param reservationId 预订ID
     * @param hotelId       酒店ID
     * @param type          类型：PREPAYMENT-预付款 / DEPOSIT-押金
     * @param amount        金额
     * @param paymentMethod 支付方式（空则默认 CASH）
     */
    private void savePrepayment(Long reservationId, Long hotelId, String type, BigDecimal amount, String paymentMethod) {
        ReservationPrepayment prepayment = new ReservationPrepayment();
        prepayment.setHotelId(hotelId);
        prepayment.setReservationId(reservationId);
        prepayment.setPrepaymentType(type);
        prepayment.setAmount(amount);
        prepayment.setPaymentMethod(StringUtils.hasText(paymentMethod) ? paymentMethod : "CASH");
        prepayment.setPaymentTime(LocalDateTime.now());
        prepayment.setStatus("PAID");
        prepayment.setRemark("新增预订时录入");
        prepaymentMapper.insert(prepayment);
        log.info("预订预付款/押金已记录：reservationId={}, type={}, amount={}", reservationId, type, amount);
    }

    /**
     * 更新预订
     *
     * @param id 预订ID
     * @param dto 更新参数
     * @return 预订信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationVO update(Long id, ReservationUpdateDTO dto) {
        // 查询预订
        Reservation reservation = mapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }

        // 只有待确认和已确认的预订可以修改
        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus())
                && !ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许修改");
        }

        // 更新字段
        if (StringUtils.hasText(dto.getGuestName())) {
            reservation.setGuestName(dto.getGuestName());
        }
        if (StringUtils.hasText(dto.getGuestPhone())) {
            reservation.setGuestPhone(dto.getGuestPhone());
        }
        if (dto.getRoomTypeId() != null) {
            reservation.setRoomTypeId(dto.getRoomTypeId());
        }
        if (dto.getRoomId() != null) {
            reservation.setRoomId(dto.getRoomId());
        }
        if (dto.getCheckInDate() != null) {
            reservation.setCheckInDate(dto.getCheckInDate());
        }
        if (dto.getCheckOutDate() != null) {
            reservation.setCheckOutDate(dto.getCheckOutDate());
        }
        if (dto.getTotalAmount() != null) {
            reservation.setTotalAmount(dto.getTotalAmount());
        }
        if (dto.getSpecialRequests() != null) {
            reservation.setSpecialRequests(dto.getSpecialRequests());
        }

        // 重新计算房晚数
        if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
            int nights = reservation.getCheckOutDate().compareTo(reservation.getCheckInDate());
            reservation.setNights(nights);
        }

        // 保存到数据库
        mapper.updateById(reservation);

        log.info("更新预订成功：id={}, reservationNo={}", id, reservation.getReservationNo());

        return convertToVO(reservation);
    }

    /**
     * 取消预订
     *
     * @param id 预订ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        // 查询预订
        Reservation reservation = mapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }

        // 只有待确认和已确认的预订可以取消
        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus())
                && !ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许取消");
        }

        // 更新状态
        reservation.setStatus(ReservationConstants.STATUS_CANCELLED);
        mapper.updateById(reservation);

        log.info("取消预订成功：id={}, reservationNo={}", id, reservation.getReservationNo());
    }

    /**
     * 确认预订
     *
     * @param id 预订ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        // 查询预订
        Reservation reservation = mapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }

        // 只有待确认的预订可以确认
        if (!ReservationConstants.STATUS_PENDING.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许确认");
        }

        // 更新状态
        reservation.setStatus(ReservationConstants.STATUS_CONFIRMED);
        mapper.updateById(reservation);

        log.info("确认预订成功：id={}, reservationNo={}", id, reservation.getReservationNo());
    }

    /**
     * 标记未到店
     *
     * @param id 预订ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markNoShow(Long id) {
        // 查询预订
        Reservation reservation = mapper.selectById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "预订不存在");
        }

        // 只有已确认的预订可以标记未到店
        if (!ReservationConstants.STATUS_CONFIRMED.equals(reservation.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许标记未到店");
        }

        // 更新状态
        reservation.setStatus(ReservationConstants.STATUS_NO_SHOW);
        mapper.updateById(reservation);

        log.info("标记未到店成功：id={}, reservationNo={}", id, reservation.getReservationNo());
    }

    /**
     * 查询今日抵店预订
     *
     * @param hotelId 酒店ID
     * @return 今日抵店列表
     */
    public List<ReservationVO> getTodayArrivals(Long hotelId) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getHotelId, hotelId)
               .eq(Reservation::getCheckInDate, today)
               .in(Reservation::getStatus, ReservationConstants.STATUS_PENDING, ReservationConstants.STATUS_CONFIRMED)
               .orderByAsc(Reservation::getCreatedAt);

        List<Reservation> list = mapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 生成预订号
     * 格式：R + 年月日 + 4位序号
     *
     * @param hotelId 酒店ID
     * @return 预订号
     */
    private String generateReservationNo(Long hotelId) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = "R" + dateStr;

        // 查询今天最大的预订号
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getHotelId, hotelId)
               .likeRight(Reservation::getReservationNo, prefix)
               .orderByDesc(Reservation::getReservationNo)
               .last("LIMIT 1");

        Reservation latest = mapper.selectOne(wrapper);

        int nextSeq = 1;
        if (latest != null && latest.getReservationNo() != null) {
            String lastNo = latest.getReservationNo();
            // 提取序号部分（最后4位）
            String seqStr = lastNo.substring(lastNo.length() - 4);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }

        return String.format("%s%04d", prefix, nextSeq);
    }

    /**
     * 转换为VO
     *
     * @param reservation 预订实体
     * @return 预订VO
     */
    private ReservationVO convertToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        BeanUtils.copyProperties(reservation, vo);

        // 查询酒店信息
        Hotel hotel = hotelMapper.selectById(reservation.getHotelId());
        if (hotel != null) {
            vo.setHotelName(hotel.getName());
        }

        // 查询房型信息
        RoomType roomType = roomTypeMapper.selectById(reservation.getRoomTypeId());
        if (roomType != null) {
            vo.setRoomTypeName(roomType.getName());
        }

        // 查询房间信息
        if (reservation.getRoomId() != null) {
            Room room = roomMapper.selectById(reservation.getRoomId());
            if (room != null) {
                vo.setRoomNo(room.getRoomNo());
            }
        }

        return vo;
    }
}
