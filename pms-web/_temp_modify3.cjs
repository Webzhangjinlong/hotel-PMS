const fs = require('fs');
const filePath = 'pms-web/src/views/front-desk/RoomBoard.vue';
let content = fs.readFileSync(filePath, 'utf8');

// More robust replacement using regex
const oldPattern = /(<div class="group-header">)\s*(<span class="group-title">)\s*(<span class="status-dot"[^>]*><\/span>)\s*(\{\{ group\.statusName \}\})\s*(<\/span>)\s*(<span class="group-count">\{\{ group\.roomCount \}\}间<\/span>)\s*(<\/div>)/;

const newTemplate = `$1" :class="{ 'floor-header': group.isFloorGroup }">
          $2
            <template v-if="group.isFloorGroup">
              <el-icon class="floor-icon"><House /></el-icon>
            </template>
            <template v-else>
              $3
            </template>
            {{ group.isFloorGroup ? group.statusName + '楼' : group.statusName }}
          </span>
          $6
        </div>`;

const result = content.replace(oldPattern, newTemplate);
if (result === content) {
  console.log('WARNING: Regex replacement did not match. Trying simpler approach...');
  
  // Try a simpler line-by-line approach
  const lines = content.split('\n');
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].includes('class="group-header"') && !lines[i].includes('floor-header')) {
      // Found the template line - replace the next few lines
      // Line i: <div class="group-header">
      // Line i+1: <span class="group-title">
      // Line i+2: <span class="status-dot" ...></span>
      // Line i+3: {{ group.statusName }}
      // Line i+4: </span>
      // Line i+5: <span class="group-count">...
      // Line i+6: </div>
      
      lines[i] = lines[i].replace('class="group-header"', 'class="group-header" :class="{ \'floor-header\': group.isFloorGroup }"');
      
      // Replace lines i+2 to i+3 with conditional template
      const indent = '            ';
      lines.splice(i + 2, 2, 
        `${indent}<template v-if="group.isFloorGroup">`,
        `${indent}  <el-icon class="floor-icon"><House /></el-icon>`,
        `${indent}</template>`,
        `${indent}<template v-else>`,
        `${indent}  <span class="status-dot" :style="{ backgroundColor: getStatusColor(group.statusCode) }"></span>`,
        `${indent}</template>`,
        `${indent}{{ group.isFloorGroup ? group.statusName + '楼' : group.statusName }}`
      );
      
      console.log('Template modified using line-by-line approach');
      break;
    }
  }
  
  fs.writeFileSync(filePath, lines.join('\n'), 'utf8');
} else {
  fs.writeFileSync(filePath, result, 'utf8');
  console.log('Template replaced using regex');
}
