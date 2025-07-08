<template>
  <div class="reminder-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑提醒' : '新增提醒' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入提醒标题" />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="请输入提醒内容"
          />
        </el-form-item>

        <el-form-item label="提醒时间" prop="reminderTime">
          <el-date-picker
            v-model="form.reminderTime"
            type="datetime"
            placeholder="选择提醒时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="提醒类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择提醒类型">
            <el-option label="生日" value="BIRTHDAY" />
            <el-option label="纪念日" value="ANNIVERSARY" />
            <el-option label="会议" value="MEETING" />
            <el-option label="任务" value="TASK" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="重复类型" prop="repeatType">
          <el-select v-model="form.repeatType" placeholder="请选择重复类型">
            <el-option label="不重复" value="NONE" />
            <el-option label="每日" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每年" value="YEARLY" />
          </el-select>
        </el-form-item>

        <el-form-item label="提醒方式">
          <el-checkbox-group v-model="form.reminderTypes">
            <el-checkbox label="SMS">短信</el-checkbox>
            <el-checkbox label="WECHAT">微信</el-checkbox>
            <el-checkbox label="EMAIL">邮件</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="收件人">
          <el-select v-model="form.recipients" multiple placeholder="请选择收件人">
            <el-option
              v-for="contact in contacts"
              :key="contact.id"
              :label="contact.name"
              :value="contact.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { remindersApi, contactsApi } from '@/api'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const contacts = ref([])

const isEdit = computed(() => route.params.id !== undefined)

const form = reactive({
  title: '',
  content: '',
  reminderTime: '',
  type: 'OTHER',
  repeatType: 'NONE',
  reminderTypes: ['SMS'],
  recipients: []
})

const rules = {
  title: [
    { required: true, message: '请输入提醒标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入提醒内容', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ],
  reminderTime: [
    { required: true, message: '请选择提醒时间', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择提醒类型', trigger: 'change' }
  ],
  repeatType: [
    { required: true, message: '请选择重复类型', trigger: 'change' }
  ]
}

const loadContacts = async () => {
  try {
    const response = await contactsApi.getContacts()
    contacts.value = response.data.content || response.data
  } catch (error) {
    ElMessage.error('加载联系人失败')
  }
}

const loadReminder = async (id) => {
  try {
    const response = await remindersApi.getReminder(id)
    const reminder = response.data
    form.title = reminder.title
    form.content = reminder.content
    form.reminderTime = reminder.reminderTime
    form.type = reminder.type
    form.repeatType = reminder.repeatType
    form.reminderTypes = reminder.reminderTypes || ['SMS']
    form.recipients = reminder.recipients ? reminder.recipients.map(r => r.contactId) : []
  } catch (error) {
    ElMessage.error('加载提醒信息失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    // 根据后端ReminderRequest期望的格式构造数据
    const data = {
      title: form.title,
      content: form.content,
      reminderTime: form.reminderTime,
      reminderTypes: form.reminderTypes,
      repeatType: form.repeatType,
      recipients: form.recipients.map(contactId => {
        // 根据联系人ID从contacts数组中找到对应的联系人
        const contact = contacts.value.find(c => c.id === contactId)
        if (contact) {
          // 根据联系人的信息决定接收者类型和值
          if (contact.phoneNumber) {
            return {
              recipientType: 'PHONE',
              recipientValue: contact.phoneNumber
            }
          } else if (contact.wechatOpenid) {
            return {
              recipientType: 'WECHAT',
              recipientValue: contact.wechatOpenid
            }
          }
        }
        // 默认使用手机号类型
        return {
          recipientType: 'PHONE',
          recipientValue: contact?.phoneNumber || ''
        }
      })
    }

    if (isEdit.value) {
      await remindersApi.updateReminder(route.params.id, data)
      ElMessage.success('提醒更新成功')
    } else {
      await remindersApi.createReminder(data)
      ElMessage.success('提醒创建成功')
    }

    router.push('/reminders')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push('/reminders')
}

onMounted(async () => {
  await loadContacts()
  if (isEdit.value) {
    await loadReminder(route.params.id)
  }
})
</script>

<style scoped>
.reminder-form {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}
</style> 