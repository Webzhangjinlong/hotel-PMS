<template>
  <div class="permission-management">
    <div class="page-header">
      <h2>权限管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新增权限
      </el-button>
    </div>

    <el-card shadow="never">
      <!-- 筛选条件 -->
      <el-form :model="queryParams" inline style="margin-bottom: 16px;">
        <el-form-item label="资源类型">
          <el-select v-model="queryParams.resourceType" placeholder="全部类型" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPermissionList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 权限列表（树形表格） -->
      <el-table 
        :data="filteredPermissionList" 
        v-loading="loading" 
        stripe
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="permissionName" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" width="180" />
        <el-table-column prop="resourceType" label="资源类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getResourceTypeTag(row.resourceType)">
              {{ getResourceTypeName(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resourcePath" label="资源路径" width="200" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="isVisible" label="可见" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isVisible ? 'success' : 'info'">
              {{ row.isVisible ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="showAddDialog(row)">添加子权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑权限弹窗 -->
    <el-dialog 
      :title="isEdit ? '编辑权限' : '新增权限'" 
      v-model="dialogVisible" 
      width="600px"
    >
      <el-form :model="permissionForm" :rules="permissionRules" ref="permissionFormRef" label-width="100px">
        <el-form-item label="父权限">
          <el-tree-select
            v-model="permissionForm.parentId"
            :data="permissionTreeOptions"
            :props="{ label: 'permissionName', value: 'id', children: 'children' }"
            placeholder="请选择父权限（不选则为顶级）"
            clearable
            check-strictly
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="permissionForm.permissionCode" placeholder="如：user:create、menu:dashboard" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-radio-group v-model="permissionForm.resourceType">
            <el-radio label="MENU">菜单</el-radio>
            <el-radio label="BUTTON">按钮</el-radio>
            <el-radio label="API">接口</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="资源路径">
          <el-input v-model="permissionForm.resourcePath" placeholder="菜单路径或API路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="permissionForm.icon" placeholder="菜单图标" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="permissionForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="permissionForm.isVisible" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="permissionForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getPermissionList, 
  createPermission, 
  updatePermission, 
  deletePermission 
} from '@/api/permission'

const loading = ref(false)
const saving = ref(false)
const permissionList = ref([])

// 查询参数
const queryParams = reactive({
  resourceType: '',
  status: ''
})

// 筛选后的权限列表
const filteredPermissionList = computed(() => {
  let list = permissionList.value
  if (queryParams.resourceType) {
    list = list.filter(item => item.resourceType === queryParams.resourceType)
  }
  if (queryParams.status) {
    list = list.filter(item => item.status === queryParams.status)
  }
  return list
})

// 权限树选项（用于选择父权限）
const permissionTreeOptions = computed(() => {
  return buildTree(permissionList.value)
})

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const permissionFormRef = ref(null)
const permissionForm = reactive({
  id: null,
  parentId: null,
  permissionName: '',
  permissionCode: '',
  resourceType: 'MENU',
  resourcePath: '',
  icon: '',
  sortOrder: 0,
  isVisible: true,
  status: 'ACTIVE'
})
const permissionRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }]
}

// 构建树形结构
const buildTree = (list) => {
  const map = {}
  const tree = []
  
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      tree.push(map[item.id])
    }
  })
  
  return tree
}

// 获取权限列表
const fetchPermissionList = async () => {
  loading.value = true
  try {
    const res = await getPermissionList()
    permissionList.value = res.data
  } catch (error) {
    console.error('获取权限列表失败', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const resetQuery = () => {
  queryParams.resourceType = ''
  queryParams.status = ''
}

// 显示新增弹窗
const showAddDialog = (row) => {
  isEdit.value = false
  permissionForm.id = null
  permissionForm.parentId = row ? row.id : null
  permissionForm.permissionName = ''
  permissionForm.permissionCode = ''
  permissionForm.resourceType = 'MENU'
  permissionForm.resourcePath = ''
  permissionForm.icon = ''
  permissionForm.sortOrder = 0
  permissionForm.isVisible = true
  permissionForm.status = 'ACTIVE'
  dialogVisible.value = true
}

// 显示编辑弹窗
const showEditDialog = (row) => {
  isEdit.value = true
  permissionForm.id = row.id
  permissionForm.parentId = row.parentId
  permissionForm.permissionName = row.permissionName
  permissionForm.permissionCode = row.permissionCode
  permissionForm.resourceType = row.resourceType
  permissionForm.resourcePath = row.resourcePath
  permissionForm.icon = row.icon
  permissionForm.sortOrder = row.sortOrder
  permissionForm.isVisible = row.isVisible
  permissionForm.status = row.status
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await permissionFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updatePermission(permissionForm.id, permissionForm)
      ElMessage.success('更新成功')
    } else {
      await createPermission(permissionForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchPermissionList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saving.value = false
  }
}

// 删除权限
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？删除后不可恢复。', '确认删除', {
      type: 'warning'
    })
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchPermissionList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 获取资源类型名称
const getResourceTypeName = (type) => {
  const map = {
    'MENU': '菜单',
    'BUTTON': '按钮',
    'API': '接口'
  }
  return map[type] || type
}

// 获取资源类型标签类型
const getResourceTypeTag = (type) => {
  const map = {
    'MENU': '',
    'BUTTON': 'success',
    'API': 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  fetchPermissionList()
})
</script>

<style scoped lang="scss">
.permission-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
}
</style>
