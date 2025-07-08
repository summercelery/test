<template>
  <div class="reminders-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提醒管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增提醒
          </el-button>
        </div>
      </template>

      <!-- 搜索和过滤 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchQuery"
              placeholder="搜索提醒内容..."
              @input="handleSearch"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="statusFilter" placeholder="状态筛选" @change="loadReminders">
              <el-option label="全部" value="" />
              <el-option label="待发送" value="PENDING" />
              <el-option label="已发送" value="SENT" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 提醒列表 -->
      <el-table :data="reminders" v-loading="loading" stripe>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reminderTime" label="提醒时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.reminderTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repeatType" label="重复" width="100">
          <template #default="{ row }">
            {{ getRepeatText(row.repeatType) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="warning" size="small" @click="handleCancel(row)" v-if="row.status === 'PENDING'">
              取消
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { remindersApi } from '@/api'

const router = useRouter()

const loading = ref(false)
const reminders = ref([])
const searchQuery = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadReminders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      search: searchQuery.value,
      status: statusFilter.value
    }
    const response = await remindersApi.getReminders(params)
    reminders.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载提醒列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadReminders()
}

const handleCreate = () => {
  router.push('/reminders/create')
}

const handleEdit = (reminder) => {
  router.push(`/reminders/${reminder.id}/edit`)
}

const handleCancel = async (reminder) => {
  try {
    await ElMessageBox.confirm('确定要取消此提醒吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await remindersApi.cancelReminder(reminder.id)
    ElMessage.success('提醒已取消')
    loadReminders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消提醒失败')
    }
  }
}

const handleDelete = async (reminder) => {
  try {
    await ElMessageBox.confirm('确定要删除此提醒吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await remindersApi.deleteReminder(reminder.id)
    ElMessage.success('提醒已删除')
    loadReminders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除提醒失败')
    }
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadReminders()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadReminders()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const getTypeColor = (type) => {
  const colors = {
    BIRTHDAY: 'warning',
    ANNIVERSARY: 'success',
    MEETING: 'info',
    TASK: 'primary',
    OTHER: 'default'
  }
  return colors[type] || 'default'
}

const getTypeText = (type) => {
  const texts = {
    BIRTHDAY: '生日',
    ANNIVERSARY: '纪念日',
    MEETING: '会议',
    TASK: '任务',
    OTHER: '其他'
  }
  return texts[type] || type
}

const getStatusColor = (status) => {
  const colors = {
    PENDING: 'warning',
    SENT: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  }
  return colors[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待发送',
    SENT: '已发送',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getRepeatText = (repeat) => {
  const texts = {
    NONE: '不重复',
    DAILY: '每日',
    WEEKLY: '每周',
    MONTHLY: '每月',
    YEARLY: '每年'
  }
  return texts[repeat] || repeat
}

onMounted(() => {
  loadReminders()
})
</script>

<style scoped>
.reminders-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}

.search-bar {
  margin-bottom: 20px;
}
</style> 