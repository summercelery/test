<template>
  <div class="contact-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑联系人' : '新增联系人' }}</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="微信OpenID" prop="wechatOpenid">
          <el-input v-model="form.wechatOpenid" placeholder="请输入微信OpenID（可选）" />
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple placeholder="请选择标签">
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
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
import { contactsApi, tagsApi } from '@/api'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const tags = ref([])

const isEdit = computed(() => route.params.id !== undefined)

const form = reactive({
  name: '',
  phoneNumber: '',
  wechatOpenid: '',
  tags: []
})

const rules = {
  name: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phoneNumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
}

const loadTags = async () => {
  try {
    const response = await tagsApi.getTags()
    tags.value = response.data
  } catch (error) {
    ElMessage.error('加载标签失败')
  }
}

const loadContact = async (id) => {
  try {
    const response = await contactsApi.getContact(id)
    const contact = response.data
    form.name = contact.name || ''
    form.phoneNumber = contact.phoneNumber || ''
    form.wechatOpenid = contact.wechatOpenid || ''
    // 确保标签ID数组的正确处理
    form.tags = contact.tags ? contact.tags.map(tag => tag.id) : []
  } catch (error) {
    ElMessage.error('加载联系人信息失败')
    console.error('Load contact error:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const data = {
      name: form.name,
      phoneNumber: form.phoneNumber,
      wechatOpenid: form.wechatOpenid,
      tagIds: form.tags  // 后端期望的字段名是tagIds，并且是Long数组
    }

    if (isEdit.value) {
      await contactsApi.updateContact(route.params.id, data)
      ElMessage.success('联系人更新成功')
    } else {
      await contactsApi.createContact(data)
      ElMessage.success('联系人创建成功')
    }

    router.push('/contacts')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  router.push('/contacts')
}

onMounted(async () => {
  await loadTags()
  if (isEdit.value) {
    await loadContact(route.params.id)
  }
})
</script>

<style scoped>
.contact-form {
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