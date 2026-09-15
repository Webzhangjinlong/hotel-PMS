<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
    </div>

    <el-card shadow="never">
      <!-- 角色列表 -->
      <el-table :data="roleList" v-loading="loading" stripe>
        <el-table-column prop="id" label="角色ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="permissionCount" label="权限数" width="80" />
        <el-table-column prop="userCount" label="用户数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="success" link @click="showPermissionDialog(row)">配置权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog 
      :title="isEdit ? '编辑角色' : '新增角色'" 
      v-model="dialogVisible" 
      width="500px"
    >
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="roleForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
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

    <!-- 配置权限弹窗 -->
    <el-dialog title="配置权限" v-model="permissionDialogVisible" width="600px">
      <div style="margin-bottom: 16px;">
        <strong>角色：</strong>{{ currentRole.roleName }}（{{ currentRole.roleCode }}）
      </div>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" size="small" @click="selectAll">全选</el-button>
        <el-button size="small" @click="deselectAll">取消全选</el-button>
        <el-button size="small" @click="expandAll">展开全部</el-button>
        <el-button size="small" @click="collapseAll">折叠全部</el-button>
      </div>
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ label: 'permissionName', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedPermissionIds"
        :default-expand-all="isExpandAll"
        style="max-height: 400px; overflow-y: auto;"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissions" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getRoleList, 
  createRole, 
  updateRole, 
  deleteRole, 
  getRolePermissions, 
  assignRolePermissions, 
  getAllPermissions 
} from '@/api/role'

const loading = ref(false)
const saving = ref(false)
const roleList = ref([])
const permissionList = ref([])
const permissionTree = ref([])

// 角色表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const roleFormRef = ref(null)
const roleForm = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  sortOrder: 0,
  status: 'ACTIVE'
})
const roleRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 权限配置相关
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref(null)
const currentRole = ref({})
const selectedPermissionIds = ref([])
const isExpandAll = ref(true)

// 获取角色列表
const fetchRoleList = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (error) {
    console.error('获取角色列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取权限列表
const fetchPermissionList = async () => {
  try {
    const res = await getAllPermissions()
    permissionList.value = res.data
    permissionTree.value = buildPermissionTree(res.data)
  } catch (error) {
    console.error('获取权限列表失败', error)
  }
}

// 构建权限树
const buildPermissionTree = (list) => {
  const map = {}
  const tree = []
  
  // 创建map
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  // 构建树
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      tree.push(map[item.id])
    }
  })
  
  // 排序
  const sortTree = (nodes) => {
    nodes.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    nodes.forEach(node => {
      if (node.children.length > 0) {
        sortTree(node.children)
      }
    })
  }
  sortTree(tree)
  
  return tree
}

// 显示新增弹窗
const showAddDialog = () => {
  isEdit.value = false
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  roleForm.sortOrder = 0
  roleForm.status = 'ACTIVE'
  dialogVisible.value = true
}

// 显示编辑弹窗
const showEditDialog = (row) => {
  isEdit.value = true
  roleForm.id = row.id
  roleForm.roleName = row.roleName
  roleForm.roleCode = row.roleCode
  roleForm.description = row.description
  roleForm.sortOrder = row.sortOrder
  roleForm.status = row.status
  dialogVisible.value = true
}

// 提交角色表单
const handleSubmit = async () => {
  const valid = await roleFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateRole(roleForm.id, roleForm)
      ElMessage.success('更新成功')
    } else {
      await createRole(roleForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchRoleList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saving.value = false
  }
}

// 删除角色
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？删除后不可恢复。', '确认删除', {
      type: 'warning'
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoleList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 显示权限配置弹窗
const showPermissionDialog = async (row) => {
  currentRole.value = row
  selectedPermissionIds.value = []
  permissionDialogVisible.value = true
  
  try {
    const res = await getRolePermissions(row.id)
    selectedPermissionIds.value = res.data
    // 设置树的选中状态
    nextTick(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(res.data)
      }
    })
  } catch (error) {
    console.error('获取角色权限失败', error)
  }
}

// 分配权限
const handleAssignPermissions = async () => {
  saving.value = true
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]
    
    await assignRolePermissions(currentRole.value.id, permissionIds)
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
    fetchRoleList()
  } catch (error) {
    console.error('配置权限失败', error)
  } finally {
    saving.value = false
  }
}

// 全选
const selectAll = () => {
  const allIds = permissionList.value.map(item => item.id)
  permissionTreeRef.value.setCheckedKeys(allIds)
}

// 取消全选
const deselectAll = () => {
  permissionTreeRef.value.setCheckedKeys([])
}

// 展开全部
const expandAll = () => {
  isExpandAll.value = true
  // 需要重新渲染树
  const temp = permissionTree.value
  permissionTree.value = []
  nextTick(() => {
    permissionTree.value = temp
  })
}

// 折叠全部
const collapseAll = () => {
  isExpandAll.value = false
  const temp = permissionTree.value
  permissionTree.value = []
  nextTick(() => {
    permissionTree.value = temp
  })
}

onMounted(() => {
  fetchRoleList()
  fetchPermissionList()
})
</script>

<style scoped lang="scss">
.role-management {
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
