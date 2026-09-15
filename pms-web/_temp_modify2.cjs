const fs = require('fs');
const filePath = 'pms-web/src/views/front-desk/RoomBoard.vue';
let content = fs.readFileSync(filePath, 'utf8');

// Modify the group-header to handle floor groups
const oldHeader = `<div class="group-header">
          <span class="group-title">
            <span class="status-dot" :style="{ backgroundColor: getStatusColor(group.statusCode) }"></span>
            {{ group.statusName }}
          </span>
          <span class="group-count">{{ group.roomCount }}间</span>
        </div>`;

const newHeader = `<div class="group-header" :class="{ 'floor-header': group.isFloorGroup }">
          <span class="group-title">
            <template v-if="group.isFloorGroup">
              <el-icon class="floor-icon"><House /></el-icon>
            </template>
            <template v-else>
              <span class="status-dot" :style="{ backgroundColor: getStatusColor(group.statusCode) }"></span>
            </template>
            {{ group.isFloorGroup ? group.statusName + '楼' : group.statusName }}
          </span>
          <span class="group-count">{{ group.roomCount }}间</span>
        </div>`;

content = content.replace(oldHeader, newHeader);

// Add floor group styles before the closing </style> tag
const floorStyles = `
// 楼层分组样式
.floor-header {
  .floor-icon {
    width: 12px;
    height: 12px;
    margin-right: 8px;
    color: #409EFF;
    font-size: 16px;
  }
}
`;

content = content.replace('</style>', floorStyles + '</style>');

fs.writeFileSync(filePath, content, 'utf8');
console.log('Template and styles updated successfully');
