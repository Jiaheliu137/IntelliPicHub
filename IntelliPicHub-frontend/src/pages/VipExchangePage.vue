<template>
  <div id="vipExchange">
    <h2 style="margin-bottom: 16px">
      VIP Membership Activation
    </h2>

    <a-alert
      message="Testing Feature Notice"
      description="The VIP membership function is currently for testing purposes only. There are no exclusive VIP features available on this site at this time."
      type="info"
      show-icon
      style="margin-bottom: 20px"
    />

    <!-- VIP兑换表单 -->
    <a-form
      name="formData"
      layout="vertical"
      :model="formData"
      @finish="handleSubmit"
    >
      <a-form-item name="vipCode" label="VIP Activation Code">
        <a-input
          v-model:value="formData.vipCode"
          placeholder="Enter your VIP activation code"
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          Activate VIP Membership
        </a-button>
      </a-form-item>
    </a-form>

    <!-- VIP特权说明 -->
    <a-card title="VIP Membership Benefits (Coming Soon)" style="margin-top: 24px">
      <p style="color: #999; margin-bottom: 16px">
        These features are planned for future implementation and are not currently available.
      </p>
      <a-list>
        <a-list-item>
          <template #prefix>
            <check-circle-outlined style="color: #52c41a; margin-right: 8px;" />
          </template>
          <span> </span>
        </a-list-item>
        <a-list-item>
          <template #prefix>
            <check-circle-outlined style="color: #52c41a; margin-right: 8px;" />
          </template>
          <span> </span>
        </a-list-item>
        <a-list-item>
          <template #prefix>
            <check-circle-outlined style="color: #52c41a; margin-right: 8px;" />
          </template>
          <span> </span>
        </a-list-item>
        <a-list-item>
          <template #prefix>
            <check-circle-outlined style="color: #52c41a; margin-right: 8px;" />
          </template>
          <span> </span>
        </a-list-item>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { exchangeVipUsingPost } from '@/api/userController.ts'
import { CheckCircleOutlined } from '@ant-design/icons-vue'

// 定义表单数据结构
const formData = reactive<API.VipExchangeRequest>({
  vipCode: ''
})

const loading = ref(false)

/**
 * 表单提交处理
 */
const handleSubmit = async () => {
  // 表单验证
  if (!formData.vipCode) {
    message.warning('Please enter a valid VIP activation code')
    return
  }

  // 设置加载状态
  loading.value = true

  try {
    // 调用API激活VIP会员
    const res = await exchangeVipUsingPost({
      vipCode: formData.vipCode
    })

    // 处理响应
    if (res.data.code === 0 && res.data.data) {
      message.success('VIP membership activated successfully!')
      // 重置表单
      formData.vipCode = ''
      // 延迟1秒后刷新页面以显示新的会员状态
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      message.error('Failed to activate VIP membership: ' + (res.data.message || 'Unknown error'))
    }
  } catch (error) {
    message.error('An error occurred while processing your request')
    console.error('VIP activation error:', error)
  } finally {
    // 恢复按钮状态
    loading.value = false
  }
}
</script>

<style scoped>
#vipExchange {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px;
}
</style>
