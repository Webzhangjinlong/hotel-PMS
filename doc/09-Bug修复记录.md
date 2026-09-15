# 09-Bug修复记录

## 文档信息
- **创建时间**: 2026-08-09
- **维护者**: PMS开发团队
- **用途**: 记录项目开发过程中遇到的Bug及解决方案

---

## Bug #1: 前端菜单乱码问题

### 问题描述
页面中的中文文本显示为乱码，如寰呭鐞嗛璁?/span>、鎴块棿绠＄悊等。

### 影响范围
- pms-web/src/layouts/MainLayout.vue - 侧边栏菜单
- pms-web/src/views/dashboard/DashboardView.vue - 仪表盘页面
- pms-web/src/views/master/room/RoomList.vue - 房间管理页面

### 问题原因
文件编码问题导致中文字符被错误编码，保存时产生了乱码字节。

### 解决方案
1. 使用正确的UTF-8编码重新写入文件
2. 逐行检查并替换乱码字符为正确的中文

### 修复示例
`javascript
// 修复前
<template #title>宸ヤ綔鍙?/template>

// 修复后
<template #title>工作台</template>
`

---

## Bug #2: Vue模板标签未闭合

### 问题描述
Vue编译报错：Element is missing end tag

### 影响文件
- pms-web/src/layouts/MainLayout.vue

### 问题原因
1. <el-dropdown> 缺少开始标签
2. <el-breadcrumb> 缺少开始标签
3. <el-dropdown-menu> 缺少结束标签
4. <script setup> 标签缺失

### 解决方案
重写整个模板部分，确保所有标签正确嵌套和闭合。

### 正确的模板结构
`html
<template>
  <div class="layout-container">
    <aside class="layout-sidebar">
      <!-- 侧边栏内容 -->
    </aside>
    
    <div class="layout-main">
      <header class="layout-header">
        <div class="header-left">
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <!-- 用户信息 -->
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <main class="layout-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
// 脚本内容
</script>

<style lang="scss" scoped>
// 样式内容
</style>
`

---

## Bug #3: switch语句缺少break导致穿透

### 问题描述
登录后页面自动弹出"确定要退出登录吗？"对话框。

### 影响文件
- pms-web/src/layouts/MainLayout.vue

### 问题原因
handleCommand函数的switch语句缺少break，导致执行profile或password分支时穿透到logout分支。

### 解决方案
`javascript
// 修复前
async function handleCommand(command) {
  switch (command) {
    case 'profile':
      ElMessage.info('功能开发中...')
    case 'password':
      ElMessage.info('功能开发中...')
    case 'logout':
      await handleLogout()
  }
}

// 修复后
async function handleCommand(command) {
  switch (command) {
    case 'profile':
      ElMessage.info('功能开发中...')
      break  // 添加break
    case 'password':
      ElMessage.info('功能开发中...')
      break  // 添加break
    case 'logout':
      await handleLogout()
      break  // 添加break
  }
}
`

---

## Bug #4: 侧边栏布局问题

### 问题描述
点击菜单后，页面内容显示在菜单下方而不是右侧。

### 影响文件
- pms-web/src/layouts/MainLayout.vue

### 问题原因
.layout-container缺少display: flex样式，导致侧边栏和主内容区垂直堆叠。

### 解决方案
`scss
// 修复前
.layout-container {
  // 缺少display: flex
}

// 修复后
.layout-container {
  display: flex;      // 添加flex布局
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
`

---

## Bug #5: 菜单样式优化

### 问题描述
二级菜单底色是白色，字体是灰色，看起来不够亮眼。

### 影响文件
- pms-web/src/layouts/MainLayout.vue
- pms-web/src/assets/css/variables.scss
- pms-web/src/assets/css/element-theme.scss（新建）

### 解决方案
1. 使用浅色突出风格的二级菜单
2. 将主题色从蓝色(#409eff)改为绿色(#67c23a)

### 样式代码
`scss
.sidebar-menu {
  // 一级菜单项样式
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: #bfcbd9;
    
    &:hover {
      background: #263445;
      color: #ffffff;
    }
  }
  
  // 二级菜单容器背景
  :deep(.el-sub-menu .el-menu) {
    background: #f0f2f5;
  }
  
  // 二级菜单项样式
  :deep(.el-sub-menu .el-menu-item) {
    color: #303133;
    background: #f0f2f5;
    border-left: 3px solid transparent;
    
    &:hover {
      background: #ffffff;
      color: #67c23a;
    }
    
    &.is-active {
      background: #ffffff;
      color: #67c23a;
      border-left-color: #67c23a;
    }
  }
  
  // 一级菜单选中状态
  :deep(.el-menu-item.is-active) {
    background: #67c23a;
    color: #ffffff;
  }
}
`

---

## Bug #6: Java编译未保留参数名

### 问题描述
调用/api/v1/rooms/5接口时后端报错：
`
java.lang.IllegalArgumentException: Name for argument of type [java.lang.Long] not specified
`

### 影响范围
所有使用@PathVariable和@RequestParam注解的接口

### 问题原因
Java编译时没有使用-parameters标志，导致Spring无法通过反射获取方法参数名。

### 解决方案
修改根目录pom.xml，在maven-compiler-plugin中添加<parameters>true</parameters>：

`xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <source></source>
        <target></target>
        <encoding>UTF-8</encoding>
        <parameters>true</parameters>  <!-- 添加此行 -->
    </configuration>
</plugin>
`

### 修复后操作
`ash
mvn clean package -DskipTests
# 重启后端服务
`

---

## Bug #7: PostgreSQL序列未同步导致主键冲突

### 问题描述
新增房间时后端报错：
`
重复键违反唯一约束"room_pkey"
键值(id)=(2) 已经存在
`

### 影响范围
所有通过直接指定id插入初始数据的表

### 问题原因
初始数据脚本通过INSERT INTO room (id, ...) VALUES (2, ...)直接指定id插入，但PostgreSQL的序列（sequence）没有同步更新，导致下次插入时序列还是从旧值开始。

### 解决方案
执行SQL修复序列：

`sql
-- 修复 room 表的序列
SELECT setval('room_id_seq', COALESCE((SELECT MAX(id) FROM room), 1));

-- 修复其他表的序列
SELECT setval('hotel_id_seq', COALESCE((SELECT MAX(id) FROM hotel), 1));
SELECT setval('hotel_floor_id_seq', COALESCE((SELECT MAX(id) FROM hotel_floor), 1));
SELECT setval('room_type_id_seq', COALESCE((SELECT MAX(id) FROM room_type), 1));
SELECT setval('sys_account_id_seq', COALESCE((SELECT MAX(id) FROM sys_account), 1));
`

### 预防措施
在初始数据脚本末尾添加序列重置：

`sql
-- V1_2_0__insert_initial_data.sql 末尾添加
SELECT setval('room_id_seq', COALESCE((SELECT MAX(id) FROM room), 1));
SELECT setval('hotel_floor_id_seq', COALESCE((SELECT MAX(id) FROM hotel_floor), 1));
SELECT setval('room_type_id_seq', COALESCE((SELECT MAX(id) FROM room_type), 1));
`

---


---

## Bug #8: 预订号生成重复

### 问题描述
新增预订时后端报错：
```
重复键违反唯一约束"uk_reservation_no"
键值"(hotel_id, reservation_no)=(1, R202608090001)" 已经存在
```

### 影响范围
- ReservationService.java - 创建预订功能

### 问题原因
预订号生成使用内存中的AtomicInteger计数器，应用重启后会从1开始，导致生成重复的预订号。

### 解决方案
修改ReservationService.generateReservationNo方法，改为从数据库查询当天最大的预订号：

```java
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
        String seqStr = lastNo.substring(lastNo.length() - 4);
        try {
            nextSeq = Integer.parseInt(seqStr) + 1;
        } catch (NumberFormatException e) {
            nextSeq = 1;
        }
    }
    
    return String.format("%s%04d", prefix, nextSeq);
}
```

---

## Bug #9: 入住单号/账务单号生成重复

### 问题描述
预订转入住时后端报错：
```
重复键违反唯一约束"uk_stay_no"
键值"(hotel_id, stay_no)=(1, S202608090002)" 已经存在
```

### 影响范围
- StayService.java - 入住单号和账务单号生成功能

### 问题原因
入住单号和账务单号生成使用内存中的AtomicInteger计数器，应用重启后会从1开始，导致生成重复的单号。

### 解决方案
修改StayService中的generateStayNo和generateFolioNo方法，改为从数据库查询当天最大的单号：

```java
private String generateStayNo(Long hotelId) {
    String dateStr = LocalDate.now().format(DATE_FORMAT);
    String prefix = "S" + dateStr;
    
    // 查询今天最大的入住单号
    LambdaQueryWrapper<Stay> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Stay::getHotelId, hotelId)
           .likeRight(Stay::getStayNo, prefix)
           .orderByDesc(Stay::getStayNo)
           .last("LIMIT 1");
    
    Stay latest = mapper.selectOne(wrapper);
    
    int nextSeq = 1;
    if (latest != null && latest.getStayNo() != null) {
        String lastNo = latest.getStayNo();
        String seqStr = lastNo.substring(lastNo.length() - 4);
        try {
            nextSeq = Integer.parseInt(seqStr) + 1;
        } catch (NumberFormatException e) {
            nextSeq = 1;
        }
    }
    
    return String.format("%s%04d", prefix, nextSeq);
}
```

---

## Bug #10: StayVO缺少roomTypeId字段

### 问题描述
续住功能选择新离店日期后，调用房价查询接口返回500错误：
```
Required request parameter 'roomTypeId' for method parameter type Long is not present
```

### 影响范围
- StayVO.java - 入住响应VO
- StayService.java - convertToVO方法

### 问题原因
StayVO中只有roomTypeName字段，没有roomTypeId字段，导致前端无法获取房型ID。

### 解决方案
1. 在StayVO中添加roomTypeId字段
2. 在StayService.convertToVO方法中设置roomTypeId

```java
// StayVO.java
/** 房型ID */
private Long roomTypeId;

/** 房型名称 */
private String roomTypeName;
```

```java
// StayService.java - convertToVO方法
RoomType roomType = roomTypeMapper.selectById(room.getRoomTypeId());
if (roomType != null) {
    vo.setRoomTypeId(roomType.getId());  // 新增
    vo.setRoomTypeName(roomType.getName());
}
```

---
## 总结

| Bug编号 | 问题类型 | 影响文件 | 严重程度 |
|---------|---------|---------|---------|
| #1 | 编码问题 | 前端多个文件 | 中 |
| #2 | 模板语法 | MainLayout.vue | 高 |
| #3 | 逻辑错误 | MainLayout.vue | 高 |
| #4 | CSS布局 | MainLayout.vue | 高 |
| #5 | UI样式 | MainLayout.vue | 低 |
| #6 | 编译配置 | pom.xml | 高 |
| #7 | 数据库 | PostgreSQL | 高 |
| #8 | 单号生成 | ReservationService.java | 高 |
| #9 | 单号生成 | StayService.java | 高 |
| #10 | 数据字段 | StayVO.java | 高 |

---

## Bug #11: Flyway迁移版本冲突

**日期**：2026-08-14  
**问题描述**：后端启动时报错，Flyway检测到两个相同版本号的迁移文件  
**错误信息**：Found more than one migration with version 1.10.0  
**根本原因**：`V1_10_0__fix_fin_transaction_table.sql` 和 `V1_10_0__add_price_plan_to_team_reservation.sql` 使用了相同的版本号  
**解决方案**：将 `V1_10_0__add_price_plan_to_team_reservation.sql` 重命名为 `V1_14_0__add_price_plan_to_team_reservation.sql`  
**影响范围**：后端启动

---

## Bug #12: 散客入住缺少房价码选择

**日期**：2026-08-14  
**问题描述**：散客入住时没有房价码选择选项，导致房费按房型基础价计算  
**根本原因**：前端散客入住表单缺少房价码选择器，`walkInForm` 中没有 `pricePlanId` 字段  
**解决方案**：
1. 前端 `StayManagement.vue` 添加 `pricePlanId` 字段到 `walkInForm`
2. 添加 `pricePlanOptions` ref 存储房价码列表
3. 添加 `fetchPricePlans` 函数从 API 获取房价码
4. 在散客入住对话框添加房价码下拉选择器
5. `showWalkInDialog` 打开时自动加载房价码列表  
**影响范围**：散客入住功能

---

## Bug #13: 入住管理团队入住标签不显示数据

**日期**：2026-08-13  
**问题描述**：选择"团队入住"标签后，列表显示为空  
**根本原因**：前端缺少 `processTeamData` 函数，无法将入住记录按团队分组  
**解决方案**：
1. 添加 `processTeamData` 函数，按 `teamReservationId` 分组
2. 在 `fetchStayList` 中当 `checkInType === 'TEAM'` 时调用分组函数
3. 添加 `getStatusLabel` 函数显示状态标签  
**影响范围**：入住管理页面团队入住视图

---

## Bug #14: 预订号生成重复

**日期**：2026-08-12  
**问题描述**：应用重启后创建预订，预订号与之前重复  
**根本原因**：预订号生成使用内存计数器，重启后重置为1  
**解决方案**：改为从数据库查询当天最大预订号，在此基础上递增  
**影响范围**：预订创建功能

---

## Bug #15: 入住单号/账务单号生成重复

**日期**：2026-08-12  
**问题描述**：同预订号问题，入住单号和账务单号也会重复  
**根本原因**：使用相同的内存计数器方案  
**解决方案**：同样改为从数据库查询当天最大单号  
**影响范围**：入住和账务功能

---

**文档维护人**：系统管理员  
**最后更新**：2026-08-20
---

## 预防措施

### 16.1 代码质量预防
| 预防措施 | 说明 | 实施方式 |
|----------|------|----------|
| 代码审查 | 每次提交必须经过代码审查 | Git PR审查 |
| 单元测试 | 核心功能必须有单元测试 | 覆盖率检查 |
| 静态分析 | 使用工具检查代码质量 | SonarQube |
| 编码规范 | 遵循编码规范 | IDE插件 |

### 16.2 测试预防
| 预防措施 | 说明 | 实施方式 |
|----------|------|----------|
| 单元测试 | 测试单个方法/类 | JUnit + Mockito |
| 集成测试 | 测试模块间交互 | Spring Boot Test |
| 端到端测试 | 测试完整流程 | Selenium |
| 回归测试 | 确保修复不引入新问题 | 自动化测试 |

### 16.3 部署预防
| 预防措施 | 说明 | 实施方式 |
|----------|------|----------|
| 环境隔离 | 开发、测试、生产环境隔离 | Docker |
| 灰度发布 | 逐步发布到生产环境 | 蓝绿部署 |
| 回滚机制 | 快速回滚到上一版本 | 版本控制 |
| 监控告警 | 实时监控系统状态 | Prometheus + Grafana |

### 16.4 流程预防
| 预防措施 | 说明 | 实施方式 |
|----------|------|----------|
| 需求评审 | 充分理解需求 | 评审会议 |
| 设计评审 | 技术方案评审 | 评审会议 |
| 代码评审 | 代码质量评审 | Git PR |
| 测试评审 | 测试用例评审 | 评审会议 |

### 16.5 常见Bug类型预防

#### 空指针预防
`java
// ❌ 错误：可能空指针
String name = user.getName().toUpperCase();

// ✅ 正确：空值检查
String name = user != null && user.getName() != null 
    ? user.getName().toUpperCase() 
    : "";
`

#### 并发问题预防
`java
// ❌ 错误：非线程安全
private int count = 0;
public void increment() {
    count++;
}

// ✅ 正确：使用原子类
private AtomicInteger count = new AtomicInteger(0);
public void increment() {
    count.incrementAndGet();
}
`

#### 资源泄漏预防
`java
// ❌ 错误：可能资源泄漏
InputStream is = new FileInputStream(file);
// 处理逻辑

// ✅ 正确：使用try-with-resources
try (InputStream is = new FileInputStream(file)) {
    // 处理逻辑
}
`

#### 金额精度问题预防
`java
// ❌ 错误：使用double
double amount = 0.1 + 0.2; // 0.30000000000000004

// ✅ 正确：使用BigDecimal
BigDecimal amount = new BigDecimal("0.1")
    .add(new BigDecimal("0.2")); // 0.3
`

---

## 17. Bug分析报告模板

### 17.1 报告格式
`markdown
## Bug #{编号}: {标题}

**日期**：{发现日期}
**严重程度**：{P0/P1/P2}
**影响范围**：{影响的功能模块}

### 问题描述
{详细描述问题现象}

### 重现步骤
1. {步骤1}
2. {步骤2}
3. {步骤3}

### 预期结果
{期望的正确行为}

### 实际结果
{实际的错误行为}

### 根本原因
{分析问题的根本原因}

### 解决方案
{描述解决方案}

### 预防措施
{如何防止类似问题再次发生}

### 影响评估
- 影响范围：{影响的功能}
- 影响用户：{影响的用户群体}
- 影响时间：{持续时间}
`

### 17.2 严重程度定义
| 级别 | 定义 | 响应时间 |
|------|------|----------|
| P0 | 系统崩溃、数据丢失、核心功能不可用 | 1小时内 |
| P1 | 主要功能异常、性能严重下降 | 4小时内 |
| P2 | 次要功能异常、UI问题 | 24小时内 |
| P3 | 轻微问题、优化建议 | 下个版本 |

---

**文档维护人**：系统管理员  
**最后更新**：2026-08-20

