const fs = require('fs');
const filePath = 'pms-web/src/views/front-desk/RoomBoard.vue';
let content = fs.readFileSync(filePath, 'utf8');
const lines = content.split('\n');

// Step 1: Add viewMode ref after selectedStatus
const selectedStatusIdx = lines.findIndex(l => l.includes('const selectedStatus = ref(null)'));
if (selectedStatusIdx >= 0) {
  lines.splice(selectedStatusIdx + 1, 0, "const viewMode = ref('floor') // floor or status");
}

// Step 2: Add floorGroups and displayGroups before 状态名称映射
const statusNameMapIdx = lines.findIndex(l => l.includes('// 状态名称映射'));
if (statusNameMapIdx >= 0) {
  const computedCode = `
// 按楼层分组的房间数据（用于默认视图）
const floorGroups = computed(() => {
  const allRooms = []
  // 从 statusGroups 中收集所有房间
  if (dashboardData.value.statusGroups) {
    dashboardData.value.statusGroups.forEach(group => {
      if (group.rooms) {
        allRooms.push(...group.rooms)
      }
    })
  }
  
  // 按楼层分组
  const floorMap = {}
  allRooms.forEach(room => {
    const floorId = room.floorId || 'unknown'
    const floorName = room.floorName || '未知楼层'
    if (!floorMap[floorId]) {
      floorMap[floorId] = {
        floorId,
        floorName,
        rooms: [],
        roomCount: 0
      }
    }
    floorMap[floorId].rooms.push(room)
    floorMap[floorId].roomCount++
  })
  
  // 按楼层号排序
  return Object.values(floorMap).sort((a, b) => {
    const numA = parseInt(a.floorName) || 0
    const numB = parseInt(b.floorName) || 0
    return numA - numB
  })
})

// 当前显示的分组数据（根据 viewMode 切换）
const displayGroups = computed(() => {
  if (viewMode.value === 'floor') {
    // 楼层视图：返回楼层分组格式（兼容模板）
    return floorGroups.value.map(g => ({
      statusCode: 'FLOOR_' + g.floorId,
      statusName: g.floorName,
      roomCount: g.roomCount,
      rooms: g.rooms,
      isFloorGroup: true
    }))
  } else {
    // 状态视图：返回原始状态分组
    return dashboardData.value.statusGroups || []
  }
})
`;
  lines.splice(statusNameMapIdx, 0, computedCode);
}

// Step 3: Modify handleStatusFilter
const handleStatusIdx = lines.findIndex(l => l.includes('const handleStatusFilter = (status) =>'));
if (handleStatusIdx >= 0) {
  // Find the closing brace of this function
  let braceCount = 0;
  let endIdx = handleStatusIdx;
  for (let i = handleStatusIdx; i < lines.length; i++) {
    if (lines[i].includes('{')) braceCount++;
    if (lines[i].includes('}')) braceCount--;
    if (braceCount === 0) { endIdx = i; break; }
  }
  // Remove old function
  lines.splice(handleStatusIdx, endIdx - handleStatusIdx + 1);
  // Insert new function
  const newFunc = `const handleStatusFilter = (status) => {
  if (selectedStatus.value === status) {
    // 取消筛选，回到楼层视图
    selectedStatus.value = null
    viewMode.value = 'floor'
  } else if (status === null) {
    // 点击"全部"卡片，回到楼层视图
    selectedStatus.value = null
    viewMode.value = 'floor'
  } else {
    // 点击某个状态卡片，切换到状态视图
    selectedStatus.value = status
    viewMode.value = 'status'
  }
  loadDashboard()
}`;
  lines.splice(handleStatusIdx, 0, newFunc);
}

// Step 4: Modify handleClearStatusFilter to also reset viewMode
const clearIdx = lines.findIndex(l => l.includes('const handleClearStatusFilter = () =>'));
if (clearIdx >= 0) {
  let braceCount = 0;
  let endIdx = clearIdx;
  for (let i = clearIdx; i < lines.length; i++) {
    if (lines[i].includes('{')) braceCount++;
    if (lines[i].includes('}')) braceCount--;
    if (braceCount === 0) { endIdx = i; break; }
  }
  lines.splice(clearIdx, endIdx - clearIdx + 1);
  const newClear = `const handleClearStatusFilter = () => {
  selectedStatus.value = null
  viewMode.value = 'floor'
  loadDashboard()
}`;
  lines.splice(clearIdx, 0, newClear);
}

// Step 5: Replace template: dashboardData.statusGroups -> displayGroups
const newContent = lines.join('\n')
  .replace(/v-for="group in dashboardData\.statusGroups"/g, 'v-for="group in displayGroups"');

fs.writeFileSync(filePath, newContent, 'utf8');
console.log('All modifications applied successfully');
