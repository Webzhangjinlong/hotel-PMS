# -*- coding: utf-8 -*-
"""后端：预订弹窗增强——协议单位、预付款/押金
1. Flyway V1_37_0：reservation 加 credit_company_id
2. Reservation entity：+ creditCompanyId
3. ReservationCreateDTO：+ creditCompanyId / prepaymentAmount / depositAmount / paymentMethod
4. ReservationVO：+ creditCompanyId
5. ReservationService.create：预付款/押金写入 reservation_prepayment
"""
import io, os

ROOT = r'D:\codex\hotelPMS_doubao\hotel-PMS'

# ---------- 1. Flyway ----------
flyway = ROOT + r'\pms-dao\src\main\resources\db\migration\V1_37_0__add_reservation_credit_company.sql'
sql = '''-- V1_37_0__add_reservation_credit_company.sql
-- 预订增强：预订单关联协议单位（协议价来源）

ALTER TABLE reservation ADD COLUMN IF NOT EXISTS credit_company_id BIGINT;
COMMENT ON COLUMN reservation.credit_company_id IS '协议单位ID（协议价时使用，可为空）';

CREATE INDEX IF NOT EXISTS idx_reservation_credit_company ON reservation(hotel_id, credit_company_id) WHERE deleted = FALSE;
'''
io.open(flyway, 'w', encoding='utf-8', newline='\n').write(sql)
print('1. Flyway done')

# ---------- 2. Reservation entity ----------
p = ROOT + r'\pms-dao\src\main\java\com\hotel\pms\dao\entity\Reservation.java'
s = io.open(p, encoding='utf-8').read()
old = '    private Long memberId;\n    private String specialRequests;'
new = '    private Long memberId;\n    private Long creditCompanyId;\n    private String specialRequests;'
assert old in s, 'ENTITY ANCHOR NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('2. Entity done')

# ---------- 3. ReservationCreateDTO ----------
p = ROOT + r'\pms-common\src\main\java\com\hotel\pms\common\dto\ReservationCreateDTO.java'
s = io.open(p, encoding='utf-8').read()
old = '''    private BigDecimal dailyPrice;
    private String priceSource;'''
new = '''    private BigDecimal dailyPrice;
    private String priceSource;
    /** 协议单位ID（协议价时使用，可选） */
    private Long creditCompanyId;
    /** 预付款金额（可选，>0 时创建预付款记录） */
    @DecimalMin(value = "0", message = "预付款金额不能为负")
    private BigDecimal prepaymentAmount;
    /** 押金金额（可选，>0 时创建押金记录） */
    @DecimalMin(value = "0", message = "押金金额不能为负")
    private BigDecimal depositAmount;
    /** 支付方式：CASH/WECHAT/ALIPAY/POS（预付款/押金时使用，默认 CASH） */
    private String paymentMethod;'''
assert old in s, 'DTO ANCHOR NOT FOUND'
s = s.replace(old, new)
# 确认有 DecimalMin import
if 'import javax.validation.constraints.DecimalMin' not in s and 'import jakarta.validation.constraints.DecimalMin' not in s:
    old = 'import java.math.BigDecimal;'
    assert old in s, 'DTO BigDecimal import NOT FOUND'
    s = s.replace(old, 'import jakarta.validation.constraints.DecimalMin;\nimport java.math.BigDecimal;', 1)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('3. DTO done')

# ---------- 4. ReservationVO ----------
p = ROOT + r'\pms-common\src\main\java\com\hotel\pms\common\dto\ReservationVO.java'
s = io.open(p, encoding='utf-8').read()
old = '    private Long pricePlanId;\n    private String specialRequests;'
new = '    private Long pricePlanId;\n    private Long creditCompanyId;\n    private String specialRequests;'
assert old in s, 'VO ANCHOR NOT FOUND'
s = s.replace(old, new)
io.open(p, 'w', encoding='utf-8', newline='').write(s)
print('4. VO done')

# ---------- 5. ReservationService ----------
p = ROOT + r'\pms-service\src\main\java\com\hotel\pms\service\reservation\ReservationService.java'
s = io.open(p, encoding='utf-8').read()

# 5a. import
old = 'import com.hotel.pms.dao.entity.Reservation;'
if 'ReservationPrepayment' not in s:
    new = ('import com.hotel.pms.dao.entity.Reservation;\n'
           'import com.hotel.pms.dao.entity.ReservationPrepayment;')
    assert old in s, 'SERVICE ENTITY IMPORT NOT FOUND'
    s = s.replace(old, new)
old = 'import com.hotel.pms.dao.mapper.ReservationMapper;'
if 'ReservationPrepaymentMapper' not in s:
    new = ('import com.hotel.pms.dao.mapper.ReservationMapper;\n'
           'import com.hotel.pms.dao.mapper.ReservationPrepaymentMapper;')
    assert old in s, 'SERVICE MAPPER IMPORT NOT FOUND'
    s = s.replace(old, new)
old = 'import java.math.BigDecimal;'
if 'import java.time.LocalDateTime;' not in s:
    new = 'import java.math.BigDecimal;\nimport java.time.LocalDateTime;'
    assert old in s, 'SERVICE BIGDECIMAL IMPORT NOT FOUND'
    s = s.replace(old, new)

# 5b. 注入 prepaymentMapper（在 roomPriceService 注入后）
old = '    @Autowired\n    private RoomPriceService roomPriceService;'
new = '''    @Autowired
    private RoomPriceService roomPriceService;

    @Autowired
    private ReservationPrepaymentMapper prepaymentMapper;'''
assert old in s, 'SERVICE INJECTION ANCHOR NOT FOUND'
s = s.replace(old, new)

# 5c. create 中：保存预付款/押金（在 mapper.insert(reservation) 后、return 前）
old = '''          // 保存到数据库
          mapper.insert(reservation);
  
          log.info("创建预订成功：hotelId={}, reservationNo={}, guestName={}",
                  dto.getHotelId(), reservation.getReservationNo(), dto.getGuestName());
  
          return convertToVO(reservation);'''
new = '''          // 保存到数据库
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

# 5d. 新增 savePrepayment 私有方法（放在 create 方法后）
old = '''      /**
       * 更新预订
       *
       * @param id 预订ID
       * @param dto 更新参数
       * @return 预订信息
       */
      @Transactional(rollbackFor = Exception.class)
      public ReservationVO update(Long id, ReservationUpdateDTO dto) {'''
new = '''      /**
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
print('5. Service done')
