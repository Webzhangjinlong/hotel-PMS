package com.hotel.pms.service.stay;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.dto.StayGuestDTO;
import com.hotel.pms.common.dto.StayGuestVO;
import com.hotel.pms.dao.entity.Guest;
import com.hotel.pms.dao.entity.StayGuest;
import com.hotel.pms.dao.mapper.GuestMapper;
import com.hotel.pms.dao.mapper.StayGuestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 入住同住人服务类
 * <p>
 * 负责入住单关联的同住人管理，包括添加、查询、删除同住人
 * </p>
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StayGuestService {

    private final StayGuestMapper stayGuestMapper;
    private final GuestMapper guestMapper;

    /**
     * 保存入住单的所有同住人
     * <p>
     * 先删除原有同住人记录，再批量插入新的记录
     * </p>
     *
     * @param stayId     入住单ID
     * @param hotelId    酒店ID
     * @param primaryGuestName  主客人姓名
     * @param primaryGuestIdNo  主客人证件号
     * @param primaryGuestPhone 主客人手机号
     * @param primaryGuestGender 主客人性别
     * @param primaryGuestId    主客人档案ID
     * @param coGuests   同住人列表（不含主客人）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveStayGuests(Long stayId, Long hotelId,
                               String primaryGuestName, String primaryGuestIdNo,
                               String primaryGuestPhone, String primaryGuestGender,
                               Long primaryGuestId,
                               List<StayGuestDTO> coGuests) {
        // 1. 删除该入住单原有的同住人记录
        deleteByStayId(stayId);

        // 2. 插入主客人记录
        StayGuest primary = new StayGuest();
        primary.setStayId(stayId);
        primary.setGuestId(primaryGuestId);
        primary.setGuestName(primaryGuestName);
        primary.setIdType("ID_CARD");
        primary.setIdNo(primaryGuestIdNo);
        primary.setPhone(primaryGuestPhone);
        primary.setGender(primaryGuestGender);
        primary.setIsPrimary(true);
        primary.setCreatedAt(LocalDateTime.now());
        primary.setUpdatedAt(LocalDateTime.now());
        stayGuestMapper.insert(primary);

        // 3. 插入同住人记录
        if (coGuests != null && !coGuests.isEmpty()) {
            for (StayGuestDTO dto : coGuests) {
                // 3.1 如果同住人有手机号，尝试查找或创建客人档案
                Long coGuestId = null;
                if (StringUtils.hasText(dto.getPhone())) {
                    coGuestId = findOrCreateGuest(hotelId, dto.getGuestName(),
                            dto.getIdNo(), dto.getPhone(), dto.getGender());
                }

                // 3.2 构建同住人记录
                StayGuest guest = new StayGuest();
                guest.setStayId(stayId);
                guest.setGuestId(coGuestId);
                guest.setGuestName(dto.getGuestName());
                guest.setIdType(dto.getIdType() != null ? dto.getIdType() : "ID_CARD");
                guest.setIdNo(dto.getIdNo());
                guest.setPhone(dto.getPhone());
                guest.setGender(dto.getGender());
                guest.setIsPrimary(false);
                guest.setCreatedAt(LocalDateTime.now());
                guest.setUpdatedAt(LocalDateTime.now());
                stayGuestMapper.insert(guest);
            }
        }

        log.info("保存入住同住人完成: stayId={}, 主客人={}, 同住人数={}",
                stayId, primaryGuestName, coGuests != null ? coGuests.size() : 0);
    }

    /**
     * 根据入住单ID查询所有同住人
     *
     * @param stayId 入住单ID
     * @return 同住人列表（含主客人）
     */
    public List<StayGuestVO> getByStayId(Long stayId) {
        // 1. 构建查询条件
        LambdaQueryWrapper<StayGuest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StayGuest::getStayId, stayId)
               .orderByDesc(StayGuest::getIsPrimary)
               .orderByAsc(StayGuest::getId);

        // 2. 执行查询
        List<StayGuest> list = stayGuestMapper.selectList(wrapper);

        // 3. 转换为VO
        return list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 查询入住单的同住人姓名列表
     * <p>
     * 用于公安上传等场景，获取所有入住人姓名
     * </p>
     *
     * @param stayId 入住单ID
     * @return 同住人姓名列表
     */
    public List<String> getGuestNames(Long stayId) {
        List<StayGuestVO> guests = getByStayId(stayId);
        return guests.stream()
                .map(StayGuestVO::getGuestName)
                .collect(Collectors.toList());
    }

    /**
     * 添加单个同住人
     *
     * @param stayId  入住单ID
     * @param hotelId 酒店ID
     * @param dto     同住人信息
     * @return 添加的同住人
     */
    @Transactional(rollbackFor = Exception.class)
    public StayGuestVO addCoGuest(Long stayId, Long hotelId, StayGuestDTO dto) {
        // 1. 查找或创建客人档案
        Long guestId = null;
        if (StringUtils.hasText(dto.getPhone())) {
            guestId = findOrCreateGuest(hotelId, dto.getGuestName(),
                    dto.getIdNo(), dto.getPhone(), dto.getGender());
        }

        // 2. 构建同住人记录
        StayGuest guest = new StayGuest();
        guest.setStayId(stayId);
        guest.setGuestId(guestId);
        guest.setGuestName(dto.getGuestName());
        guest.setIdType(dto.getIdType() != null ? dto.getIdType() : "ID_CARD");
        guest.setIdNo(dto.getIdNo());
        guest.setPhone(dto.getPhone());
        guest.setGender(dto.getGender());
        guest.setIsPrimary(false);
        guest.setCreatedAt(LocalDateTime.now());
        guest.setUpdatedAt(LocalDateTime.now());
        stayGuestMapper.insert(guest);

        log.info("添加同住人: stayId={}, guestName={}", stayId, dto.getGuestName());
        return convertToVO(guest);
    }

    /**
     * 删除同住人
     *
     * @param id 同住人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeCoGuest(Long id) {
        StayGuest guest = stayGuestMapper.selectById(id);
        if (guest == null) {
            return;
        }
        // 主客人不能删除
        if (Boolean.TRUE.equals(guest.getIsPrimary())) {
            throw new com.hotel.pms.common.exception.BusinessException(
                    com.hotel.pms.common.result.ResultCode.BAD_REQUEST, "主客人不能删除");
        }
        guest.setDeleted(true);
        stayGuestMapper.updateById(guest);
        log.info("删除同住人: id={}", id);
    }

    /**
     * 根据入住单ID删除所有同住人（逻辑删除）
     *
     * @param stayId 入住单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByStayId(Long stayId) {
        LambdaQueryWrapper<StayGuest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StayGuest::getStayId, stayId);
        List<StayGuest> list = stayGuestMapper.selectList(wrapper);
        for (StayGuest guest : list) {
            guest.setDeleted(true);
            stayGuestMapper.updateById(guest);
        }
    }

    /**
     * 查找或创建客人档案
     * <p>
     * 根据手机号查找已有客人，不存在则自动创建
     * </p>
     *
     * @param hotelId 酒店ID
     * @param name    姓名
     * @param idNo    证件号
     * @param phone   手机号
     * @param gender  性别
     * @return 客人档案ID
     */
    private Long findOrCreateGuest(Long hotelId, String name, String idNo, String phone, String gender) {
        // 1. 根据手机号查找已有客人
        LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Guest::getHotelId, hotelId)
               .eq(Guest::getPhone, phone);
        Guest existing = guestMapper.selectOne(wrapper);

        if (existing != null) {
            return existing.getId();
        }

        // 2. 不存在则创建新客人档案
        Guest guest = new Guest();
        guest.setHotelId(hotelId);
        guest.setName(name);
        guest.setIdType("ID_CARD");
        guest.setIdNo(idNo);
        guest.setPhone(phone);
        guest.setGender(gender);
        guestMapper.insert(guest);
        return guest.getId();
    }

    /**
     * 实体转VO
     *
     * @param guest 同住人实体
     * @return 同住人VO
     */
    private StayGuestVO convertToVO(StayGuest guest) {
        StayGuestVO vo = new StayGuestVO();
        vo.setId(guest.getId());
        vo.setStayId(guest.getStayId());
        vo.setGuestId(guest.getGuestId());
        vo.setGuestName(guest.getGuestName());
        vo.setIdType(guest.getIdType());
        vo.setIdNo(guest.getIdNo());
        vo.setPhone(guest.getPhone());
        vo.setGender(guest.getGender());
        vo.setIsPrimary(guest.getIsPrimary());
        vo.setCreatedAt(guest.getCreatedAt());

        // 设置证件类型名称
        if ("ID_CARD".equals(guest.getIdType())) {
            vo.setIdTypeName("身份证");
        } else if ("PASSPORT".equals(guest.getIdType())) {
            vo.setIdTypeName("护照");
        } else {
            vo.setIdTypeName(guest.getIdType());
        }
        return vo;
    }
}
