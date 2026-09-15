package com.hotel.pms.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hotel.pms.common.constant.MemberConstants;
import com.hotel.pms.common.dto.*;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.PageResponse;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.Guest;
import com.hotel.pms.dao.entity.Member;
import com.hotel.pms.dao.entity.MemberLevel;
import com.hotel.pms.dao.entity.MemberPointsLog;
import com.hotel.pms.dao.mapper.GuestMapper;
import com.hotel.pms.dao.mapper.MemberLevelMapper;
import com.hotel.pms.dao.mapper.MemberMapper;
import com.hotel.pms.dao.mapper.MemberPointsLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员服务类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberPointsLogMapper pointsLogMapper;
    private final GuestMapper guestMapper;
    private final MemberLevelService memberLevelService;
    private final MemberLevelMapper memberLevelMapper;


    /**
     * 手动注册会员
     *
     * @param dto     会员信息
     * @param hotelId 酒店ID
     * @return 会员信息
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberVO register(MemberCreateDTO dto, Long hotelId) {
        // 1. 检查手机号是否已注册
        Member existing = getByPhone(hotelId, dto.getPhone());
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "该手机号已注册会员");
        }

        // 2. 查找或创建客人档案
        LambdaQueryWrapper<Guest> guestWrapper = new LambdaQueryWrapper<>();
        guestWrapper.eq(Guest::getHotelId, hotelId)
                    .eq(Guest::getPhone, dto.getPhone());
        Guest guest = guestMapper.selectOne(guestWrapper);
        if (guest == null) {
            guest = new Guest();
            guest.setHotelId(hotelId);
            guest.setName(dto.getName());
            guest.setPhone(dto.getPhone());
            guest.setGender(dto.getGender());
            guest.setIdNo(dto.getIdNo());
            guestMapper.insert(guest);
        }

        // 3. 获取默认等级
        MemberLevel defaultLevel = memberLevelService.getByCode(hotelId, MemberConstants.LEVEL_NORMAL);
        if (defaultLevel == null) {
            memberLevelService.initDefaultLevels(hotelId);
            defaultLevel = memberLevelService.getByCode(hotelId, MemberConstants.LEVEL_NORMAL);
        }

        // 4. 生成会员编号
        String memberNo = generateMemberNo(hotelId);

        // 5. 创建会员
        Member member = new Member();
        member.setHotelId(hotelId);
        member.setGuestId(guest.getId());
        member.setMemberNo(memberNo);
        member.setPhone(dto.getPhone());
        member.setName(dto.getName());
        member.setLevelId(defaultLevel.getId());
        member.setLevelCode(defaultLevel.getLevelCode());
        member.setTotalPoints(0);
        member.setUsedPoints(0);
        member.setTotalConsumption(BigDecimal.ZERO);
        member.setTotalStayCount(0);
        member.setRegisterSource(MemberConstants.SOURCE_MANUAL);
        member.setRegisterTime(LocalDateTime.now());
        member.setStatus(MemberConstants.STATUS_ACTIVE);
        member.setRemark(dto.getRemark());
        memberMapper.insert(member);

        log.info("会员注册成功: 手机号={}, 会员编号={}", dto.getPhone(), memberNo);
        return convertToVO(member);
    }

    /**
     * 入住时自动注册会员
     * 如果客人已是会员则直接返回，否则自动注册
     *
     * @param guestId    客人ID
     * @param hotelId    酒店ID
     * @param guestName  客人姓名
     * @param guestPhone 客人手机号
     * @return 会员信息（可能为null，如果手机号为空）
     */
    @Transactional(rollbackFor = Exception.class)
    public Member autoRegister(Long guestId, Long hotelId, String guestName, String guestPhone) {
        if (!StringUtils.hasText(guestPhone)) {
            return null;
        }

        // 1. 检查是否已是会员
        Member existing = getByPhone(hotelId, guestPhone);
        if (existing != null) {
            return existing;
        }

        // 2. 获取默认等级
        MemberLevel defaultLevel = memberLevelService.getByCode(hotelId, MemberConstants.LEVEL_NORMAL);
        if (defaultLevel == null) {
            memberLevelService.initDefaultLevels(hotelId);
            defaultLevel = memberLevelService.getByCode(hotelId, MemberConstants.LEVEL_NORMAL);
        }

        // 3. 生成会员编号
        String memberNo = generateMemberNo(hotelId);

        // 4. 创建会员
        Member member = new Member();
        member.setHotelId(hotelId);
        member.setGuestId(guestId);
        member.setMemberNo(memberNo);
        member.setPhone(guestPhone);
        member.setName(guestName);
        member.setLevelId(defaultLevel.getId());
        member.setLevelCode(defaultLevel.getLevelCode());
        member.setTotalPoints(0);
        member.setUsedPoints(0);
        member.setTotalConsumption(BigDecimal.ZERO);
        member.setTotalStayCount(0);
        member.setRegisterSource(MemberConstants.SOURCE_AUTO);
        member.setRegisterTime(LocalDateTime.now());
        member.setStatus(MemberConstants.STATUS_ACTIVE);
        memberMapper.insert(member);

        log.info("入住自动注册会员: 客人={}, 手机号={}, 会员编号={}", guestName, guestPhone, memberNo);
        return member;
    }

    /**
     * 根据ID查询会员
     */
    public MemberVO getById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "会员不存在");
        }
        return convertToVO(member);
    }

    /**
     * 根据客人ID查询会员
     */
    public Member getByGuestId(Long guestId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getGuestId, guestId)
               .eq(Member::getStatus, MemberConstants.STATUS_ACTIVE);
        return memberMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查询会员
     */
    public Member getByPhone(Long hotelId, String phone) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getHotelId, hotelId)
               .eq(Member::getPhone, phone);
        return memberMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查询会员（返回 VO，供 Controller 直接使用）
     *
     * @param hotelId 酒店ID
     * @param phone   手机号
     * @return 会员 VO，不存在返回 null
     */
    public MemberVO getByPhoneVO(Long hotelId, String phone) {
        Member member = getByPhone(hotelId, phone);
        return member == null ? null : getById(member.getId());
    }

    /**
     * 分页查询会员
     */
    public PageResponse<MemberVO> pageList(MemberQueryDTO queryDTO) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getHotelId() != null, Member::getHotelId, queryDTO.getHotelId())
               .like(StringUtils.hasText(queryDTO.getPhone()), Member::getPhone, queryDTO.getPhone())
               .like(StringUtils.hasText(queryDTO.getName()), Member::getName, queryDTO.getName())
               .eq(StringUtils.hasText(queryDTO.getLevelCode()), Member::getLevelCode, queryDTO.getLevelCode())
               .eq(StringUtils.hasText(queryDTO.getStatus()), Member::getStatus, queryDTO.getStatus())
               .orderByDesc(Member::getCreatedAt);

        Page<Member> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        IPage<Member> result = memberMapper.selectPage(page, wrapper);

        List<MemberVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(),
                (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 根据累计消费自动升级等级
     *
     * @param memberId 会员ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberLevel(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) return;

        MemberLevel newLevel = memberLevelService.determineLevel(member.getHotelId(), member.getTotalConsumption());
        if (newLevel != null && !newLevel.getLevelCode().equals(member.getLevelCode())) {
            String oldLevel = member.getLevelCode();
            member.setLevelId(newLevel.getId());
            member.setLevelCode(newLevel.getLevelCode());
            memberMapper.updateById(member);
            log.info("会员等级升级: 会员={}, {} -> {}", member.getName(), oldLevel, newLevel.getLevelCode());
        }
    }

    /**
     * 累计消费时积分
     *
     * @param memberId    会员ID
     * @param amount      消费金额
     * @param stayId      入住单ID
     * @param transactionId 交易ID
     * @param operatorId  操作员ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void earnPoints(Long memberId, BigDecimal amount, Long stayId, Long transactionId, Long operatorId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) return;

        // 查询等级获取积分倍率
        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        BigDecimal multiplier = level != null ? level.getPointsMultiplier() : BigDecimal.ONE;

        // 计算积分：消费金额 × 倍率（取整）
        int earnedPoints = amount.multiply(multiplier).setScale(0, RoundingMode.FLOOR).intValue();
        if (earnedPoints <= 0) return;

        // 记录积分流水
        int beforePoints = member.getTotalPoints();
        member.setTotalPoints(beforePoints + earnedPoints);
        member.setTotalConsumption(member.getTotalConsumption().add(amount));
        member.setTotalStayCount(member.getTotalStayCount() + 1);
        member.setLastStayTime(LocalDateTime.now());
        memberMapper.updateById(member);

        // 写积分流水
        MemberPointsLog pointsLog = new MemberPointsLog();
        pointsLog.setHotelId(member.getHotelId());
        pointsLog.setMemberId(memberId);
        pointsLog.setChangeType(MemberConstants.POINTS_EARN);
        pointsLog.setPoints(earnedPoints);
        pointsLog.setBeforePoints(beforePoints);
        pointsLog.setAfterPoints(beforePoints + earnedPoints);
        pointsLog.setRelatedStayId(stayId);
        pointsLog.setRelatedTransactionId(transactionId);
        pointsLog.setDescription("消费积分：消费" + amount + "元，获得" + earnedPoints + "积分");
        pointsLog.setOperatorId(operatorId);
        pointsLog.setCreatedAt(LocalDateTime.now());
        pointsLogMapper.insert(pointsLog);

        // 等级自动升级
        updateMemberLevel(memberId);

        log.info("会员积分: 会员={}, 消费={}, 获得积分={}, 倍率={}", member.getName(), amount, earnedPoints, multiplier);
    }

    /**
     * 积分抵扣房费
     *
     * @param memberId 会员ID
     * @param points   使用积分数
     * @param stayId   关联入住单ID
     * @return 抵扣金额
     */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal exchangePoints(Long memberId, Integer points, Long stayId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "会员不存在");
        }

        int availablePoints = member.getTotalPoints() - member.getUsedPoints();
        if (points > availablePoints) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "可用积分不足，当前可用: " + availablePoints);
        }

        // 计算抵扣金额
        BigDecimal exchangeAmount = BigDecimal.valueOf(points)
                .divide(BigDecimal.valueOf(MemberConstants.POINTS_EXCHANGE_RATE), 2, RoundingMode.HALF_UP);

        // 更新积分
        int beforePoints = member.getTotalPoints() - member.getUsedPoints();
        member.setUsedPoints(member.getUsedPoints() + points);
        memberMapper.updateById(member);

        // 写积分流水
        MemberPointsLog pointsLog = new MemberPointsLog();
        pointsLog.setHotelId(member.getHotelId());
        pointsLog.setMemberId(memberId);
        pointsLog.setChangeType(MemberConstants.POINTS_EXCHANGE);
        pointsLog.setPoints(-points);
        pointsLog.setBeforePoints(beforePoints);
        pointsLog.setAfterPoints(beforePoints - points);
        pointsLog.setRelatedStayId(stayId);
        pointsLog.setDescription("积分抵扣：" + points + "积分抵扣" + exchangeAmount + "元");
        pointsLogMapper.insert(pointsLog);

        log.info("积分抵扣: 会员={}, 积分={}, 金额={}", member.getName(), points, exchangeAmount);
        return exchangeAmount;
    }

    /**
     * 退还积分（冲账/退款时调用）
     *
     * @param memberId  会员ID
     * @param points    退还积分数
     * @param stayId    关联入住单ID
     * @param description 描述
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundPoints(Long memberId, Integer points, Long stayId, String description) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) return;

        int beforePoints = member.getTotalPoints() - member.getUsedPoints();
        member.setUsedPoints(Math.max(0, member.getUsedPoints() - points));
        memberMapper.updateById(member);

        MemberPointsLog pointsLog = new MemberPointsLog();
        pointsLog.setHotelId(member.getHotelId());
        pointsLog.setMemberId(memberId);
        pointsLog.setChangeType(MemberConstants.POINTS_REFUND);
        pointsLog.setPoints(points);
        pointsLog.setBeforePoints(beforePoints);
        pointsLog.setAfterPoints(beforePoints + points);
        pointsLog.setRelatedStayId(stayId);
        pointsLog.setDescription(StringUtils.hasText(description) ? description : "积分退还");
        pointsLogMapper.insert(pointsLog);

        log.info("积分退还: 会员={}, 积分={}", member.getName(), points);
    }

    /**
     * 手动调整积分
     *
     * @param memberId   会员ID
     * @param points     调整积分数（正数增加，负数减少）
     * @param description 描述
     * @param operatorId 操作员ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(Long memberId, Integer points, String description, Long operatorId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "会员不存在");
        }

        int beforePoints = member.getTotalPoints() - member.getUsedPoints();
        if (points < 0 && member.getTotalPoints() + points < member.getUsedPoints()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "调整后积分不能低于已使用积分");
        }

        member.setTotalPoints(member.getTotalPoints() + points);
        memberMapper.updateById(member);

        MemberPointsLog pointsLog = new MemberPointsLog();
        pointsLog.setHotelId(member.getHotelId());
        pointsLog.setMemberId(memberId);
        pointsLog.setChangeType(MemberConstants.POINTS_ADJUST);
        pointsLog.setPoints(points);
        pointsLog.setBeforePoints(beforePoints);
        pointsLog.setAfterPoints(member.getTotalPoints() - member.getUsedPoints());
        pointsLog.setDescription(StringUtils.hasText(description) ? description : "手动调整积分");
        pointsLog.setOperatorId(operatorId);
        pointsLogMapper.insert(pointsLog);

        log.info("手动调整积分: 会员={}, 调整={}", member.getName(), points);
    }

    /**
     * 查询积分流水
     */
    public List<MemberPointsLogVO> getPointsLogs(Long memberId) {
        LambdaQueryWrapper<MemberPointsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPointsLog::getMemberId, memberId)
               .orderByDesc(MemberPointsLog::getCreatedAt);
        return pointsLogMapper.selectList(wrapper).stream()
                .map(this::convertLogToVO)
                .collect(Collectors.toList());
    }

    /**
     * 会员统计
     */
    public MemberStatisticsVO getStatistics(Long hotelId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getHotelId, hotelId)
               .eq(Member::getStatus, MemberConstants.STATUS_ACTIVE);

        List<Member> members = memberMapper.selectList(wrapper);

        MemberStatisticsVO stats = new MemberStatisticsVO();
        stats.setTotalMembers(members.size());
        stats.setNormalCount((int) members.stream().filter(m -> MemberConstants.LEVEL_NORMAL.equals(m.getLevelCode())).count());
        stats.setSilverCount((int) members.stream().filter(m -> MemberConstants.LEVEL_SILVER.equals(m.getLevelCode())).count());
        stats.setGoldCount((int) members.stream().filter(m -> MemberConstants.LEVEL_GOLD.equals(m.getLevelCode())).count());
        stats.setDiamondCount((int) members.stream().filter(m -> MemberConstants.LEVEL_DIAMOND.equals(m.getLevelCode())).count());
        stats.setTotalPoints(members.stream().mapToInt(m -> m.getTotalPoints() - m.getUsedPoints()).sum());
        stats.setTotalConsumption(members.stream().map(Member::getTotalConsumption).reduce(BigDecimal.ZERO, BigDecimal::add));
        return stats;
    }

    /**
     * 更新会员信息
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberVO update(Long id, MemberCreateDTO dto) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "会员不存在");
        }
        if (StringUtils.hasText(dto.getName())) member.setName(dto.getName());
        if (StringUtils.hasText(dto.getRemark())) member.setRemark(dto.getRemark());
        memberMapper.updateById(member);
        return convertToVO(member);
    }

    /**
     * 生成会员编号：M + 酒店ID后4位 + 6位序号
     */
    private String generateMemberNo(Long hotelId) {
        String prefix = "M" + String.format("%04d", hotelId % 10000);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getHotelId, hotelId)
               .likeRight(Member::getMemberNo, prefix)
               .orderByDesc(Member::getMemberNo)
               .last("LIMIT 1");
        Member last = memberMapper.selectOne(wrapper);
        long nextSeq = 1;
        if (last != null && last.getMemberNo() != null) {
            String lastNo = last.getMemberNo();
            try {
                nextSeq = Long.parseLong(lastNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%06d", nextSeq);
    }

    private MemberVO convertToVO(Member member) {
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);

        // 计算可用积分
        vo.setAvailablePoints(member.getTotalPoints() - member.getUsedPoints());

        // 填充等级信息
        MemberLevel level = memberLevelMapper.selectById(member.getLevelId());
        if (level != null) {
            vo.setLevelName(level.getLevelName());
            vo.setDiscountRate(level.getDiscountRate());
            vo.setPointsMultiplier(level.getPointsMultiplier());
        }

        // 注册来源名称
        vo.setRegisterSourceName(MemberConstants.SOURCE_AUTO.equals(member.getRegisterSource()) ? "自动注册" : "手动注册");

        // 状态名称
        switch (member.getStatus()) {
            case MemberConstants.STATUS_ACTIVE:
                vo.setStatusName("正常");
                break;
            case MemberConstants.STATUS_INACTIVE:
                vo.setStatusName("停用");
                break;
            case MemberConstants.STATUS_FROZEN:
                vo.setStatusName("冻结");
                break;
            default:
                vo.setStatusName(member.getStatus());
        }

        return vo;
    }

    private MemberPointsLogVO convertLogToVO(MemberPointsLog log) {
        MemberPointsLogVO vo = new MemberPointsLogVO();
        BeanUtils.copyProperties(log, vo);

        switch (log.getChangeType()) {
            case MemberConstants.POINTS_EARN:
                vo.setChangeTypeName("消费获得");
                break;
            case MemberConstants.POINTS_EXCHANGE:
                vo.setChangeTypeName("积分兑换");
                break;
            case MemberConstants.POINTS_REFUND:
                vo.setChangeTypeName("积分退还");
                break;
            case MemberConstants.POINTS_ADJUST:
                vo.setChangeTypeName("手动调整");
                break;
            default:
                vo.setChangeTypeName(log.getChangeType());
        }

        return vo;
    }

    /** 获取内部Member对象（供其他Service调用） */
    public Member getMemberById(Long memberId) {
        return memberMapper.selectById(memberId);
    }

    /** 获取MemberLevelMapper（供其他Service调用） */
    public MemberLevelMapper getMemberLevelMapper() {
        return memberLevelService != null ? null : null;
    }

    /** 根据等级ID获取等级信息 */
    public MemberLevel getLevelById(Long levelId) {
        return memberLevelService.getByCode(null, null);
    }
}
