<template>
  <!-- 房间选择弹窗 -->
  <el-dialog 
    v-model="visible" 
    title="选择房间" 
    width="900px" 
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterFloor" placeholder="选择楼层" clearable style="width: 150px;">
        <el-option 
          v-for="floor in floorOptions" 
          :key="floor.id" 
          :label="floor.name" 
          :value="floor.id" 
        />
      </el-select>
      <el-tag v-if="selectedRoom" type="success" closable @close="clearSelection">
        已选：{{ selectedRoom.roomNo }}号房（{{ selectedRoom.roomTypeName }}）
      </el-tag>
    </div>

    <!-- 房间列表（按楼层分组） -->
    <div class="room-groups" v-loading="loading">
      <div v-for="group in filteredFloorGroups" :key="group.floorId" class="floor-group">
        <div class="floor-header">
          <span class="floor-name">{{ group.floorName }}楼</span>
          <span class="floor-count">{{ group.rooms.length }}间空闲</span>
        </div>
        <div class="room-grid">
          <div 
            v-for="room in group.rooms" 
            :key="room.id"
            class="room-card"
            :class="{ 
              'selected': selectedRoom && selectedRoom.id === room.id,
              'disabled': !isRoomAvailable(room)
            }"
            @click="selectRoom(room)"
          >
            <div class="room-no">{{ room.roomNo }}</div>
            <div class="room-type">{{ room.roomTypeName }}</div>
            <div class="room-price">¥{{ room.basePrice }}/晚</div>
            <div class="room-floor">{{ room.floorName }}楼</div>
          </div>
        </div>
      </div>
      
      <!-- 无数据提示 -->
      <el-empty v-if="!loading && filteredFloorGroups.length === 0" description="没有找到空闲房间" />
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="!selectedRoom" @click="confirmSelection">
        确认选择
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 房间选择弹窗组件
 * 用于预订排房时可视化选择房间
 */
import { ref, computed, watch, onMounted } from 'vue'
import request from '@/utils/request'

// Props
const props = defineProps({
  /** 是否显示弹窗 */
  modelValue: {
    type: Boolean,
    default: false
  },
  /** 酒店ID */
  hotelId: {
    type: Number,
    default: 1
  },
  /** 房型ID（筛选该房型的房间） */
  roomTypeId: {
    type: Number,
    required: true
  },
  /** 入住日期 */
  checkInDate: {
    type: String,
    default: ''
  },
  /** 离店日期 */
  checkOutDate: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'select'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const rooms = ref([])
const floorOptions = ref([])
const filterFloor = ref(null)
const selectedRoom = ref(null)

// 计算属性：按楼层分组的房间
const filteredFloorGroups = computed(() => {
  const groups = {}
  rooms.value.forEach(room => {
    // 楼层筛选
    if (filterFloor.value && room.floorId !== filterFloor.value) {
      return
    }
    const floorId = room.floorId || 'unknown'
    const floorName = room.floorName || '未知'
    if (!groups[floorId]) {
      groups[floorId] = { floorId, floorName, rooms: [] }
    }
    groups[floorId].rooms.push(room)
  })
  return Object.values(groups).sort((a, b) => {
    const numA = parseInt(a.floorName) || 0
    const numB = parseInt(b.floorName) || 0
    return numA - numB
  })
})

// 方法：检查房间是否可用
const isRoomAvailable = (room) => {
  return room.status === 'AVAILABLE'
}

// 方法：选择房间
const selectRoom = (room) => {
  if (!isRoomAvailable(room)) return
  if (selectedRoom.value && selectedRoom.value.id === room.id) {
    selectedRoom.value = null
  } else {
    selectedRoom.value = room
  }
}

// 方法：清除选择
const clearSelection = () => {
  selectedRoom.value = null
}

// 方法：确认选择
const confirmSelection = () => {
  if (selectedRoom.value) {
    emit('select', selectedRoom.value)
    visible.value = false
  }
}

// 方法：关闭弹窗
const handleClose = () => {
  selectedRoom.value = null
  visible.value = false
}

// 方法：加载可用房间列表
const loadRooms = async () => {
  loading.value = true
  try {
    const res = await request.get('/v1/rooms', {
      params: {
        hotelId: props.hotelId,
        roomTypeId: props.roomTypeId,
        status: 'AVAILABLE',
        size: 200
      }
    })
    rooms.value = res.data?.records || []
  } catch (error) {
    console.error('加载房间列表失败:', error)
    rooms.value = []
  } finally {
    loading.value = false
  }
}

// 方法：加载楼层选项
const loadFloorOptions = async () => {
  try {
    const res = await request.get('/v1/floors/list', {
      params: { hotelId: props.hotelId }
    })
    floorOptions.value = res.data || []
  } catch (error) {
    console.error('加载楼层列表失败:', error)
  }
}

// 监听弹窗显示，加载数据
watch(visible, (val) => {
  if (val && props.roomTypeId) {
    loadRooms()
    loadFloorOptions()
  }
})
</script>

<style scoped lang="scss">
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.room-groups {
  max-height: 500px;
  overflow-y: auto;
}

.floor-group {
  margin-bottom: 20px;
}

.floor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
  
  .floor-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .floor-count {
    font-size: 14px;
    color: #909399;
  }
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.room-card {
  padding: 12px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  
  &:hover:not(.disabled) {
    border-color: #409EFF;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  }
  
  &.selected {
    border-color: #67C23A;
    background-color: #f0f9eb;
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background-color: #f5f7fa;
  }
  
  .room-no {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .room-type {
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }
  
  .room-price {
    font-size: 14px;
    color: #f56c6c;
    font-weight: 500;
  }
  
  .room-floor {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
