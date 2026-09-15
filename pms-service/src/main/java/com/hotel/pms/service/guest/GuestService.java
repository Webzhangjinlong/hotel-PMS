package com.hotel.pms.service.guest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.dto.GuestQueryDTO;
import com.hotel.pms.common.dto.GuestVO;
import com.hotel.pms.common.dto.StayVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Guest;
import com.hotel.pms.dao.entity.Stay;
import com.hotel.pms.dao.mapper.GuestMapper;
import com.hotel.pms.dao.mapper.StayMapper;
import com.hotel.pms.service.config.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客人档案管理服务类
 * <p>
 * 提供客人信息管理、VIP设置、黑名单管理、入住历史查询等功能
 * </p>
 * 
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class GuestService extends BaseService<Guest, GuestMapper> {

    @Autowired
    private StayMapper stayMapper;

    /**
     * 分页查询客人列表
     * <p>
     * 支持按姓名、手机、VIP状态、黑名单状态等条件查询
     * </p>
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResponse<GuestVO> getGuestPage(GuestQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Guest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Guest::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getName()), Guest::getName, queryDTO.getName())
               .like(StringUtils.hasText(queryDTO.getPhone()), Guest::getPhone, queryDTO.getPhone())
               .eq(queryDTO.getIsVip() != null, Guest::getIsVip, queryDTO.getIsVip())
               .eq(queryDTO.getIsBlacklisted() != null, Guest::getIsBlacklisted, queryDTO.getIsBlacklisted())
               .orderByDesc(Guest::getLastStayTime);

        // 2. 执行分页查询
        Page<Guest> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Guest> result = mapper.selectPage(page, wrapper);

        // 3. 转换为VO
        List<GuestVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 4. 返回分页结果
        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 根据ID查询客人详情
     * 
     * @param id 客人ID
     * @return 客人信息
     */
    public GuestVO getGuestById(Long id) {
        // 1. 查询客人信息
        Guest guest = mapper.selectById(id);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 转换为VO
        return convertToVO(guest);
    }

    /**
     * 设置客人VIP状态
     * 
     * @param id 客人ID
     * @param isVip VIP状态
     * @return 更新后的客人信息
     */
    @Transactional
    public GuestVO setVipStatus(Long id, Boolean isVip) {
        // 1. 查询客人信息
        Guest guest = mapper.selectById(id);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 更新VIP状态
        guest.setIsVip(isVip);
        mapper.updateById(guest);
        // 3. 记录日志
        log.info("客人VIP状态更新: id={}, isVip={}", id, isVip);
        // 4. 返回更新后的客人信息
        return convertToVO(guest);
    }

    /**
     * 将客人加入黑名单
     * 
     * @param id 客人ID
     * @param reason 黑名单原因
     * @return 更新后的客人信息
     */
    @Transactional
    public GuestVO addToBlacklist(Long id, String reason) {
        // 1. 查询客人信息
        Guest guest = mapper.selectById(id);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 设置黑名单状态和原因
        guest.setIsBlacklisted(true);
        guest.setBlacklistReason(reason);
        mapper.updateById(guest);
        // 3. 记录日志
        log.info("客人加入黑名单: id={}, reason={}", id, reason);
        // 4. 返回更新后的客人信息
        return convertToVO(guest);
    }

    /**
     * 将客人从黑名单移出
     * 
     * @param id 客人ID
     * @return 更新后的客人信息
     */
    @Transactional
    public GuestVO removeFromBlacklist(Long id) {
        // 1. 查询客人信息
        Guest guest = mapper.selectById(id);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 清除黑名单状态
        guest.setIsBlacklisted(false);
        guest.setBlacklistReason(null);
        mapper.updateById(guest);
        // 3. 记录日志
        log.info("客人移出黑名单: id={}", id);
        // 4. 返回更新后的客人信息
        return convertToVO(guest);
    }

    /**
     * 获取客人入住历史
     * 
     * @param guestId 客人ID
     * @return 入住历史列表
     */
    public List<StayVO> getStayHistory(Long guestId) {
        // 1. 查询客人是否存在
        Guest guest = mapper.selectById(guestId);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 查询该客人的所有入住记录
        LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stay::getGuestId, guestId)
               .orderByDesc(Stay::getCheckInTime);
        List<Stay> stays = stayMapper.selectList(wrapper);
        // 3. 转换为VO
        return stays.stream()
                .map(this::convertStayToVO)
                .collect(Collectors.toList());
    }

    /**
     * 更新客人入住次数
     * <p>
     * 当客人办理入住时调用此方法
     * </p>
     * 
     * @param guestId 客人ID
     */
    @Transactional
    public void updateStayCount(Long guestId) {
        // 1. 查询客人信息
        Guest guest = mapper.selectById(guestId);
        if (guest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "客人不存在");
        }
        // 2. 更新入住次数和最后入住时间
        Integer stayCount = guest.getStayCount() == null ? 0 : guest.getStayCount();
        guest.setStayCount(stayCount + 1);
        guest.setLastStayTime(LocalDateTime.now());
        mapper.updateById(guest);
        // 3. 记录日志
        log.info("客人入住次数更新: guestId={}, stayCount={}", guestId, guest.getStayCount());
    }

    /**
     * 将Guest实体转换为GuestVO
     * 
     * @param guest 客人实体
     * @return 客人VO
     */
    private GuestVO convertToVO(Guest guest) {
        GuestVO vo = new GuestVO();
        BeanUtils.copyProperties(guest, vo);
        // 设置证件类型名称
        if (StringUtils.hasText(guest.getIdType())) {
            vo.setIdTypeName(getIdTypeName(guest.getIdType()));
        }
        return vo;
    }

    /**
     * 将Stay实体转换为StayVO
     * 
     * @param stay 入住实体
     * @return 入住VO
     */
    private StayVO convertStayToVO(Stay stay) {
        StayVO vo = new StayVO();
        BeanUtils.copyProperties(stay, vo);
        return vo;
    }

    /**
     * 获取证件类型名称
     * 
     * @param idType 证件类型代码
     * @return 证件类型名称
     */
    private String getIdTypeName(String idType) {
        switch (idType) {
            case "ID_CARD":
                return "身份证";
            case "PASSPORT":
                return "护照";
            case "DRIVER_LICENSE":
                return "驾驶证";
            default:
                return idType;
        }
    }
}
