package com.hotel.pms.service.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hotel.pms.common.constant.MemberConstants;
import com.hotel.pms.common.dto.MemberLevelDTO;
import com.hotel.pms.common.dto.MemberLevelVO;
import com.hotel.pms.common.exception.BusinessException;
import com.hotel.pms.common.result.ResultCode;
import com.hotel.pms.dao.entity.MemberLevel;
import com.hotel.pms.dao.mapper.MemberLevelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会员等级服务类
 *
 * @author PMS开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberLevelService {

    private final MemberLevelMapper memberLevelMapper;

    /**
     * 获取酒店所有等级列表
     *
     * @param hotelId 酒店ID
     * @return 等级列表
     */
    public List<MemberLevelVO> getAll(Long hotelId) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getHotelId, hotelId)
               .eq(MemberLevel::getStatus, MemberConstants.STATUS_ACTIVE)
               .orderByAsc(MemberLevel::getSortOrder);
        return memberLevelMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据等级编码查询
     *
     * @param hotelId   酒店ID
     * @param levelCode 等级编码
     * @return 等级信息
     */
    public MemberLevel getByCode(Long hotelId, String levelCode) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getHotelId, hotelId)
               .eq(MemberLevel::getLevelCode, levelCode);
        return memberLevelMapper.selectOne(wrapper);
    }

    /**
     * 初始化酒店默认等级配置
     *
     * @param hotelId 酒店ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void initDefaultLevels(Long hotelId) {
        List<Object[]> defaults = Arrays.asList(
                new Object[]{MemberConstants.LEVEL_NORMAL, "普通会员", BigDecimal.valueOf(100), BigDecimal.valueOf(1.0), BigDecimal.ZERO, 0, "基础权益：积分累计", 0},
                new Object[]{MemberConstants.LEVEL_SILVER, "银卡会员", BigDecimal.valueOf(95), BigDecimal.valueOf(1.5), BigDecimal.valueOf(2000), 0, "银卡权益：95折优惠、1.5倍积分", 1},
                new Object[]{MemberConstants.LEVEL_GOLD, "金卡会员", BigDecimal.valueOf(90), BigDecimal.valueOf(2.0), BigDecimal.valueOf(8000), 0, "金卡权益：9折优惠、2倍积分", 2},
                new Object[]{MemberConstants.LEVEL_DIAMOND, "钻石卡会员", BigDecimal.valueOf(85), BigDecimal.valueOf(3.0), BigDecimal.valueOf(20000), 0, "钻石权益：85折优惠、3倍积分、优先入住", 3}
        );

        for (Object[] d : defaults) {
            MemberLevel level = new MemberLevel();
            level.setHotelId(hotelId);
            level.setLevelCode((String) d[0]);
            level.setLevelName((String) d[1]);
            level.setDiscountRate((BigDecimal) d[2]);
            level.setPointsMultiplier((BigDecimal) d[3]);
            level.setMinTotalConsumption((BigDecimal) d[4]);
            level.setMinStayCount((Integer) d[5]);
            level.setBenefitsDesc((String) d[6]);
            level.setSortOrder((Integer) d[7]);
            level.setStatus(MemberConstants.STATUS_ACTIVE);
            memberLevelMapper.insert(level);
        }
        log.info("酒店[{}]默认会员等级初始化完成", hotelId);
    }

    /**
     * 更新等级配置
     *
     * @param id  等级ID
     * @param dto 等级信息
     * @return 更新后的等级
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberLevelVO update(Long id, MemberLevelDTO dto) {
        MemberLevel level = memberLevelMapper.selectById(id);
        if (level == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "等级不存在");
        }
        BeanUtils.copyProperties(dto, level);
        memberLevelMapper.updateById(level);
        return convertToVO(level);
    }

    /**
     * 根据累计消费金额自动判断应属等级
     *
     * @param hotelId          酒店ID
     * @param totalConsumption 累计消费金额
     * @return 应属等级
     */
    public MemberLevel determineLevel(Long hotelId, BigDecimal totalConsumption) {
        LambdaQueryWrapper<MemberLevel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberLevel::getHotelId, hotelId)
               .eq(MemberLevel::getStatus, MemberConstants.STATUS_ACTIVE)
               .le(MemberLevel::getMinTotalConsumption, totalConsumption)
               .orderByDesc(MemberLevel::getSortOrder);
        wrapper.last("LIMIT 1");
        MemberLevel level = memberLevelMapper.selectOne(wrapper);
        if (level == null) {
            return getByCode(hotelId, MemberConstants.LEVEL_NORMAL);
        }
        return level;
    }

    private MemberLevelVO convertToVO(MemberLevel level) {
        MemberLevelVO vo = new MemberLevelVO();
        BeanUtils.copyProperties(level, vo);
        vo.setStatusName(MemberConstants.STATUS_ACTIVE.equals(level.getStatus()) ? "启用" : "停用");
        return vo;
    }
}
