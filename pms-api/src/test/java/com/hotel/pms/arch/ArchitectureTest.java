package com.hotel.pms.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

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
 * <p>存量违规豁免已全部清零（rule-registry V-01/V-02 治理完成），本文件不再包含任何豁免名单。</p>
 */
@AnalyzeClasses(packages = "com.hotel.pms", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

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
