<template>
  <div class="account-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        新增员工
      </el-button>
    </div>

    <el-card shadow="never">
      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="role" label="系统角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分配角色" min-width="200">
          <template #default="{ row }">
            <el-tag 
              v-for="roleId in getUserRoleIds(row.id)" 
              :key="roleId"
              style="margin-right: 4px; margin-bottom: 4px;"
            >
              {{ getRoleNameById(roleId) }}
            </el-tag>
            <span v-if="!getUserRoleIds(row.id).length" style="color: #909399;">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="success" link @click="showRoleDialog(row)">分配角色</el-button>
            <el-button type="warning" link @click="showResetPasswordDialog(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog 
      :title="isEdit ? '编辑用户' : '新增员工'" 
      v-model="userDialogVisible" 
      width="500px"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="系统角色">
          <el-select v-model="userForm.role" placeholder="请选择系统角色" style="width: 100%;">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="前台" value="FRONT_DESK" />
            <el-option label="客房" value="HOUSEKEEPING" />
            <el-option label="财务" value="FINANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="userForm.status">
            <el-radio label="ACTIVE">启用</el-radio>
            <el-radio label="INACTIVE">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitUser" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog title="分配角色" v-model="roleDialogVisible" width="500px">
      <div style="margin-bottom: 16px;">
        <strong>用户：</strong>{{ currentUser.username }}（{{ currentUser.realName }}）
      </div>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox 
          v-for="role in roleList" 
          :key="role.id" 
          :label="role.id"
          style="display: block; margin-bottom: 8px;"
        >
          {{ role.roleName }}（{{ role.roleCode }}）
          <span style="color: #909399; margin-left: 8px;">{{ role.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoles" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" v-model="resetPasswordDialogVisible" width="400px">
      <div style="margin-bottom: 16px;">
        <strong>用户：</strong>{{ currentUser.username }}（{{ currentUser.realName }}）
      </div>
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword" :loading="saving">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser, 
  resetPassword,
  getUserRoles, 
  assignUserRoles, 
  getRoleList 
} from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const userList = ref([])
const roleList = ref([])
const userRoleMap = ref({})

// 用户表单相关
const userDialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'FRONT_DESK',
  status: 'ACTIVE'
})
const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

// 角色分配相关
const roleDialogVisible = ref(false)
const currentUser = ref({})
const selectedRoleIds = ref([])

// 重置密码相关
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const resetPasswordRules = {
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    userList.value = res.data
    for (const user of userList.value) {
      await fetchUserRoles(user.id)
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取用户角色
const fetchUserRoles = async (userId) => {
  try {
    const res = await getUserRoles(userId)
    userRoleMap.value[userId] = res.data
  } catch (error) {
    console.error('获取用户角色失败', error)
  }
}

// 获取角色列表
const fetchRoleList = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (error) {
    console.error('获取角色列表失败', error)
  }
}

// 显示新增弹窗
const showAddDialog = () => {
  isEdit.value = false
  userForm.id = null
  userForm.username = ''
  userForm.password = ''
  userForm.realName = ''
  userForm.phone = ''
  userForm.email = ''
  userForm.role = 'FRONT_DESK'
  userForm.status = 'ACTIVE'
  userDialogVisible.value = true
}

// 显示编辑弹窗
const showEditDialog = (row) => {
  isEdit.value = true
  userForm.id = row.id
  userForm.username = row.username
  userForm.password = ''
  userForm.realName = row.realName
  userForm.phone = row.phone
  userForm.email = row.email
  userForm.role = row.role
  userForm.status = row.status
  userDialogVisible.value = true
}

// 提交用户表单
const handleSubmitUser = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateUser(userForm.id, userForm)
      ElMessage.success('更新成功')
    } else {
      await createUser(userForm)
      ElMessage.success('创建成功')
    }
    userDialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saving.value = false
  }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该员工吗？删除后不可恢复。', '确认删除', {
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 显示角色分配弹窗
const showRoleDialog = async (user) => {
  currentUser.value = user
  selectedRoleIds.value = userRoleMap.value[user.id] || []
  roleDialogVisible.value = true
}

// 分配角色
const handleAssignRoles = async () => {
  saving.value = true
  try {
    await assignUserRoles({
      userId: currentUser.value.id,
      roleIds: selectedRoleIds.value
    })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    await fetchUserRoles(currentUser.value.id)
  } catch (error) {
    console.error('分配角色失败', error)
  } finally {
    saving.value = false
  }
}

// 显示重置密码弹窗
const showResetPasswordDialog = (user) => {
  currentUser.value = user
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 重置密码
const handleResetPassword = async () => {
  const valid = await resetPasswordFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await resetPassword(currentUser.value.id, resetPasswordForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error) {
    console.error('重置密码失败', error)
  } finally {
    saving.value = false
  }
}

// 获取用户的角色ID列表
const getUserRoleIds = (userId) => {
  return userRoleMap.value[userId] || []
}

// 根据角色ID获取角色名称
const getRoleNameById = (roleId) => {
  const role = roleList.value.find(r => r.id === roleId)
  return role ? role.roleName : '未知角色'
}

// 获取系统角色名称
const getRoleName = (role) => {
  const map = {
    'ADMIN': '管理员',
    'FRONT_DESK': '前台',
    'HOUSEKEEPING': '客房',
    'FINANCE': '财务'
  }
  return map[role] || role
}

// 获取系统角色标签类型
const getRoleTagType = (role) => {
  const map = {
    'ADMIN': 'danger',
    'FRONT_DESK': '',
    'HOUSEKEEPING': 'success',
    'FINANCE': 'warning'
  }
  return map[role] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchUserList()
  fetchRoleList()
})
</script>

<style scoped lang="scss">
.account-management {
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
