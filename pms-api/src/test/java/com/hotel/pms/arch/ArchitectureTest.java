package com.hotel.pms.arch;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.Map;
import java.util.Set;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.doNot;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * 架构守护测试（AGENTS.md 硬约束的机器强制，对应 .harness/rule-registry.md）。
 *
 * <p>规则只约束生产代码（ImportOption.DoNotIncludeTests，测试类不参与分层/命名规则），
 * 任何代码违反依赖方向 / 命名 / 金额类型时，CI（mvn verify）将直接失败。</p>
 *
 * <p>存量违规豁免：V-01/V-02 已全部清零；V-09（Service 跨域 Mapper）按 rule-registry
 * 登记豁免，修复后必须从豁免名单删除。</p>
 */
@AnalyzeClasses(packages = "com.hotel.pms", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // ===== R11 硬约束 13：Service 跨业务域禁止直调他人 Mapper（走对方 Service） =====

    /** 业务域 -> 允许的本域 Mapper 简单名（新 Mapper 归属变化时同步更新） */
    private static final Map<String, Set<String>> DOMAIN_ALLOWED_MAPPERS = Map.ofEntries(
            Map.entry("auth", Set.of("SysAccountMapper", "SysRoleMapper", "SysRolePermissionMapper", "SysUserRoleMapper", "SysPermissionMapper")),
            Map.entry("master", Set.of("HotelMapper", "HotelFloorMapper", "RoomMapper", "RoomTypeMapper", "RoomCardMapper")),
            Map.entry("price", Set.of("RoomPriceMapper", "RoomPricePlanMapper", "RoomPricePlanDetailMapper", "AgreementPriceMapper")),
            Map.entry("reservation", Set.of("ReservationMapper", "ReservationPrepaymentMapper", "TeamReservationMapper", "TeamReservationRoomMapper")),
            Map.entry("stay", Set.of("StayMapper", "StayGuestMapper")),
            Map.entry("folio", Set.of("FolioMapper", "TeamFolioMapper", "TeamFolioPaymentMapper")),
            Map.entry("payment", Set.of("FinTransactionMapper", "DepositMapper")),
            Map.entry("member", Set.of("MemberMapper", "MemberLevelMapper", "MemberPointsLogMapper")),
            Map.entry("guest", Set.of("GuestMapper")),
            Map.entry("nightaudit", Set.of("NightAuditMapper", "NightAuditStepMapper", "NightAuditArchiveMapper")),
            Map.entry("system", Set.of("OperationLogMapper")),
            Map.entry("config", Set.of("HotelConfigMapper")),
            Map.entry("doorlock", Set.of("DoorLockConfigMapper")),
            Map.entry("idcard", Set.of("IdcardReaderConfigMapper", "IdcardReaderLogMapper", "IdcardReadRecordMapper")),
            Map.entry("ota", Set.of("OtaChannelMapper", "OtaEventLogMapper", "OtaOrderMapper")),
            Map.entry("police", Set.of("PoliceUploadRecordMapper")),
            Map.entry("shift", Set.of("SysShiftMapper", "SysShiftMessageMapper", "SysShiftNotifyConfigMapper")),
            Map.entry("invoice", Set.of("InvoiceMapper", "InvoiceItemMapper")),
            Map.entry("credit", Set.of("CreditCompanyMapper")));

    /** 聚合/报表域：允许访问任意 Mapper（设计使然，登记 rule-registry 已知设计差异） */
    private static final Set<String> AGGREGATE_DOMAINS = Set.of("dashboard", "report");

    /** 存量跨域违规（rule-registry V-09）：修复后删除豁免 */
    private static final Set<String> KNOWN_CROSS_DOMAIN_VIOLATIONS = Set.of(
            "AuthService", "NightAuditConfigService", "GuestService", "RoomService",
            "MemberService", "NightAuditService", "RoomPricePlanService", "RoomPriceService",
            "ReservationService", "StayGuestService",
            "CreditService", "DepositService", "FolioService", "PrepaymentService",
            "ShiftService", "StayService", "TeamFolioService", "TeamReservationService");

    /** 排除已知存量违规类（rule-registry V-09 台账） */
    private static DescribedPredicate<JavaClass> notKnownViolation(Set<String> names, String description) {
        return describe(description, c -> !names.contains(c.getSimpleName()));
    }

    // ===== R02 硬约束 10：分层依赖 Controller → Service → Mapper，禁止反向 =====

    @ArchTest
    static final ArchRule layered_dependencies =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .withOptionalLayers(true)
                    .layer("Controller").definedBy("com.hotel.pms..controller..")
                    .layer("Aspect").definedBy("com.hotel.pms.api.aspect..")
                    .layer("Service").definedBy("com.hotel.pms.service..")
                    .layer("Mapper").definedBy("com.hotel.pms.dao.mapper..")
                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Aspect")
                    .whereLayer("Mapper").mayOnlyBeAccessedByLayers("Controller", "Service", "Aspect");

    // ===== R03 硬约束 10：Controller 不得直调 Mapper =====

    @ArchTest
    static final ArchRule controller_never_touches_mapper =
            noClasses().that().resideInAPackage("com.hotel.pms..controller..")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
                    .allowEmptyShould(true);

    // ===== R04 硬约束 10：Controller 不得直用 Entity =====

    @ArchTest
    static final ArchRule controller_never_uses_entity =
            noClasses().that().resideInAPackage("com.hotel.pms..controller..")
                    .should().dependOnClassesThat().resideInAPackage("com.hotel.pms.dao.entity..")
                    .allowEmptyShould(true);

    // ===== R09：Controller 不得依赖其他 Controller =====

    @ArchTest
    static final ArchRule controller_never_touches_controller =
            noClasses().that().resideInAPackage("com.hotel.pms..controller..")
                    .should().dependOnClassesThat(
                            simpleNameEndingWith("Controller")
                                    .and(doNot(simpleName("RestController"))))
                    .allowEmptyShould(true);

    // ===== R08：Service 不得依赖 Controller =====

    @ArchTest
    static final ArchRule service_never_touches_controller =
            noClasses().that().resideInAPackage("com.hotel.pms.service..")
                    .should().dependOnClassesThat(
                            simpleNameEndingWith("Controller")
                                    .and(doNot(simpleName("RestController"))))
                    .allowEmptyShould(true);

    // ===== R11 硬约束 13：Service 跨业务域禁止直调他人 Mapper =====

    @ArchTest
    static final ArchRule service_never_touches_other_domain_mapper =
            classes().that().resideInAPackage("com.hotel.pms.service..")
                    .and(notKnownViolation(KNOWN_CROSS_DOMAIN_VIOLATIONS, "not a known cross-domain violation (V-09)"))
                    .should(new ArchCondition<JavaClass>("only depend on mappers of its own business domain") {
                        @Override
                        public void check(JavaClass clazz, ConditionEvents events) {
                            // 【解析业务域：com.hotel.pms.service.<domain>】
                            String domain = "";
                            String pkg = clazz.getPackageName();
                            if (pkg.startsWith("com.hotel.pms.service.")) {
                                domain = pkg.substring("com.hotel.pms.service.".length()).split("\\.")[0];
                            }
                            // 【聚合/报表域允许访问任意 Mapper】
                            if (AGGREGATE_DOMAINS.contains(domain)) {
                                return;
                            }
                            Set<String> allowed = DOMAIN_ALLOWED_MAPPERS.getOrDefault(domain, Set.of());
                            // 【通道 1：字段类型（覆盖注入的 Mapper 字段）】
                            clazz.getMembers().stream()
                                    .filter(m -> m instanceof JavaField)
                                    .map(m -> ((JavaField) m).getRawType())
                                    .filter(t -> t.getPackageName().startsWith("com.hotel.pms.dao.mapper"))
                                    .filter(t -> !allowed.contains(t.getSimpleName()))
                                    .forEach(t -> events.add(SimpleConditionEvent.violated(clazz,
                                            clazz.getSimpleName() + " 字段依赖跨域 Mapper: " + t.getSimpleName()
                                                    + "（R11，应走对方 Service 或登记 V-09 豁免）")));
                            // 【通道 2：直接依赖（方法调用/签名/泛型/继承等）】
                            clazz.getDirectDependenciesFromSelf().stream()
                                    .filter(d -> d.getTargetClass().getPackageName().startsWith("com.hotel.pms.dao.mapper"))
                                    .filter(d -> !allowed.contains(d.getTargetClass().getSimpleName()))
                                    .forEach(d -> events.add(SimpleConditionEvent.violated(clazz,
                                            clazz.getSimpleName() + " 依赖跨域 Mapper: " + d.getTargetClass().getSimpleName()
                                                    + "（R11，应走对方 Service 或登记 V-09 豁免）")));
                        }
                    });

    // ===== R05：分层类命名规范 Controller =====

    @ArchTest
    static final ArchRule controller_naming =
            classes().that().resideInAPackage("com.hotel.pms..controller..")
                    .should().haveSimpleNameEndingWith("Controller")
                    .allowEmptyShould(true);

    // ===== R07：分层类命名规范 Service（顶层类；config 基础设施 *Helper、实现类 *ServiceImpl 白名单） =====

    @ArchTest
    static final ArchRule service_naming =
            classes().that().resideInAPackage("com.hotel.pms.service..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Service")
                    .orShould().haveSimpleNameEndingWith("ServiceImpl")
                    .orShould().haveSimpleNameEndingWith("Helper")
                    .allowEmptyShould(true);

    // ===== R06：分层类命名规范 Mapper =====

    @ArchTest
    static final ArchRule mapper_naming =
            classes().that().resideInAPackage("com.hotel.pms.dao.mapper..")
                    .should().haveSimpleNameEndingWith("Mapper")
                    .allowEmptyShould(true);

    // ===== R01 硬约束 2：业务数据禁止 float/double（金额一律 BigDecimal） =====

    @ArchTest
    static final ArchRule business_fields_must_not_be_float_or_double =
            noFields()
                    .that().areDeclaredInClassesThat().resideInAnyPackage(
                            "com.hotel.pms.dao.entity..",
                            "com.hotel.pms.common.dto..")
                    .should().haveRawType(float.class)
                    .orShould().haveRawType(double.class)
                    .allowEmptyShould(true);
}
