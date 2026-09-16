# -*- coding: utf-8 -*-
"""后端补丁：Entity + DTO + VO + Service（从断点继续）"""
import io

ROOT = r'D:\codex\hotelPMS_doubao\hotel-PMS'

# ---------- 2. Reservation entity ----------
p = ROOT + r'\pms-dao\src\main\java\com\hotel\pms\dao\entity\Reservation.java'
s = io.open(p, encoding='utf-8').read()
if 'creditCompanyId' not in s:
    old = '    private Long memberId;'
    new = '    private Long memberId;\n\n    /** 协议单位ID（协议价时使用） */\n    @TableField("credit_company_id")\n    private Long creditCompanyId;'
    assert old in s, 'ENTITY memberId NOT FOUND'
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('2. Entity done')
else:
    print('2. Entity already done')

# ---------- 3. ReservationCreateDTO ----------
p = ROOT + r'\pms-common\src\main\java\com\hotel\pms\common\dto\ReservationCreateDTO.java'
s = io.open(p, encoding='utf-8').read()
if 'prepaymentAmount' not in s:
    old = '    private String priceSource;\n}'
    new = '''    private String priceSource;
    /** 协议单位ID（协议价时使用，可选） */
    private Long creditCompanyId;
    /** 预付款金额（可选，>0 时创建预付款记录） */
    @DecimalMin(value = "0", message = "预付款金额不能为负")
    private BigDecimal prepaymentAmount;
    /** 押金金额（可选，>0 时创建押金记录） */
    @DecimalMin(value = "0", message = "押金金额不能为负")
    private BigDecimal depositAmount;
    /** 支付方式：CASH/WECHAT/ALIPAY/POS（预付款/押金时使用，默认 CASH） */
    private String paymentMethod;
}'''
    assert old in s, 'DTO ANCHOR NOT FOUND'
    s = s.replace(old, new)
    if 'DecimalMin' not in s.split('import')[0] and 'jakarta.validation.constraints.DecimalMin' not in s:
        old = 'import java.math.BigDecimal;'
        assert old in s, 'DTO BigDecimal import NOT FOUND'
        s = s.replace(old, 'import jakarta.validation.constraints.DecimalMin;\nimport java.math.BigDecimal;', 1)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('3. DTO done')
else:
    print('3. DTO already done')

# ---------- 4. ReservationVO ----------
p = ROOT + r'\pms-common\src\main\java\com\hotel\pms\common\dto\ReservationVO.java'
s = io.open(p, encoding='utf-8').read()
if 'creditCompanyId' not in s:
    old = '''    /**
     * 房价码ID
     */
    private Long pricePlanId;'''
    new = '''    /**
     * 房价码ID
     */
    private Long pricePlanId;
    /**
     * 协议单位ID
     */
    private Long creditCompanyId;'''
    assert old in s, 'VO ANCHOR NOT FOUND'
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('4. VO done')
else:
    print('4. VO already done')

# ---------- 5. ReservationService ----------
p = ROOT + r'\pms-service\src\main\java\com\hotel\pms\service\reservation\ReservationService.java'
s = io.open(p, encoding='utf-8').read()

if 'ReservationPrepaymentMapper' not in s:
    old = 'import com.hotel.pms.dao.entity.Reservation;'
    new = 'import com.hotel.pms.dao.entity.Reservation;\nimport com.hotel.pms.dao.entity.ReservationPrepayment;'
    assert old in s, 'SERVICE ENTITY IMPORT NOT FOUND'
    s = s.replace(old, new)
    old = 'import com.hotel.pms.dao.mapper.ReservationMapper;'
    new = 'import com.hotel.pms.dao.mapper.ReservationMapper;\nimport com.hotel.pms.dao.mapper.ReservationPrepaymentMapper;'
    assert old in s, 'SERVICE MAPPER IMPORT NOT FOUND'
    s = s.replace(old, new)
    old = 'import java.math.BigDecimal;'
    new = 'import java.math.BigDecimal;\nimport java.time.LocalDateTime;'
    assert old in s, 'SERVICE BIGDECIMAL IMPORT NOT FOUND'
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('5a. Service imports done')

s = io.open(p, encoding='utf-8').read()
if 'prepaymentMapper' not in s:
    old = '    @Autowired\n    private RoomPriceService roomPriceService;'
    new = '''    @Autowired
    private RoomPriceService roomPriceService;

    @Autowired
    private ReservationPrepaymentMapper prepaymentMapper;'''
    assert old in s, 'SERVICE INJECTION ANCHOR NOT FOUND'
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('5b. Service injection done')

s = io.open(p, encoding='utf-8').read()
if 'savePrepayment' not in s:
    old = '''        // 保存到数据库
        mapper.insert(reservation);

        log.info("创建预订成功：hotelId={}, reservationNo={}, guestName={}",
                dto.getHotelId(), reservation.getReservationNo(), dto.getGuestName());

        return convertToVO(reservation);'''
    new = '''        // 保存到数据库
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

        return convertToVO(reservation);'''
    assert old in s, 'SERVICE CREATE ANCHOR NOT FOUND'
    s = s.replace(old, new)

    old = '''    /**
     * 更新预订
     *
     * @param id 预订ID
     * @param dto 更新参数
     * @return 预订信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ReservationVO update(Long id, ReservationUpdateDTO dto) {'''
    new = '''    /**
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
    public ReservationVO update(Long id, ReservationUpdateDTO dto) {'''
    assert old in s, 'SERVICE UPDATE ANCHOR NOT FOUND'
    s = s.replace(old, new)
    io.open(p, 'w', encoding='utf-8', newline='').write(s)
    print('5c/5d. Service create logic done')
else:
    print('5c/5d. Service already done')

print('BACKEND PATCH OK')
