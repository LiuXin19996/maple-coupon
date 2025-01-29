<template>
  <div class="coupon-template-query">
    <div class="page-header">
      <h2><el-icon class="header-icon">
          <Ticket />
        </el-icon>优惠券兑换预约</h2>
      <div class="header-divider"></div>
    </div>

    <el-row :gutter="20" justify="center">
      <el-col :xs="24" :sm="20" :md="16" :lg="14">
        <el-form :model="queryForm" label-width="120px" class="query-form">
          <el-form-item label="店铺编号" required>
            <el-input v-model="queryForm.shopNumber" placeholder="请输入店铺编号" class="custom-input" />
          </el-form-item>
          <el-form-item label="优惠券ID" required>
            <el-input v-model="queryForm.couponTemplateId" placeholder="请输入优惠券ID" class="custom-input" />
          </el-form-item>
          <el-form-item>
            <div class="form-actions">
              <el-button type="primary" @click="handleQuery" :loading="loading" class="query-button">
                <el-icon>
                  <Search />
                </el-icon>
                查询
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <Transition name="fade">
          <el-card v-if="templateData" class="result-card">
            <template #header>
              <div class="card-header">
                <el-icon>
                  <DocumentChecked />
                </el-icon>
                <span>查询结果</span>
              </div>
            </template>

            <el-skeleton :rows="6" animated v-if="loading" />
            <div v-else class="result-content">
              <el-descriptions :column="1" border class="custom-descriptions">
                <el-descriptions-item v-for="(item, key) in formattedTemplateData" :key="key" :label="item.label"
                  class="custom-description-item">
                  <div :class="['description-content', item.type]">
                    {{ item.value }}
                  </div>
                </el-descriptions-item>

                <el-descriptions-item label="领取规则">
                  <el-card class="rule-card" shadow="never">
                    <div class="rule-detail">
                      <div>每人限领：{{ JSON.parse(templateData.receiveRule).limitPerPerson }} 张</div>
                      <div>使用说明：{{ JSON.parse(templateData.receiveRule).usageInstructions }}</div>
                    </div>
                  </el-card>
                </el-descriptions-item>

                <el-descriptions-item label="使用规则">
                  <el-card class="rule-card" shadow="never">
                    <div class="rule-detail">
                      <div>使用条件：订单满 {{ JSON.parse(templateData.consumeRule).termsOfUse }} 元可用</div>
                      <div>折扣比例：{{ JSON.parse(templateData.consumeRule).discountRate * 100 }}%</div>
                      <div>有效期：{{ JSON.parse(templateData.consumeRule).validityPeriod }} 小时</div>
                      <div>最高优惠：{{ JSON.parse(templateData.consumeRule).maximumDiscountAmount }} 元</div>
                      <div>未满足条件说明：{{ JSON.parse(templateData.consumeRule).explanationOfUnmetConditions }}</div>
                    </div>
                  </el-card>
                </el-descriptions-item>
              </el-descriptions>

              <div class="action-section">
                <div class="primary-actions">
                  <el-button type="success" class="action-button redeem-btn" @click="handleRedeem" :disabled="loading">
                    <el-icon>
                      <ShoppingCart />
                    </el-icon>
                    立即兑换
                  </el-button>
                  <el-button type="primary" class="action-button remind-btn" @click="handleRemind" :disabled="loading">
                    <el-icon>
                      <Bell />
                    </el-icon>
                    设置提醒
                  </el-button>
                </div>

                <el-divider content-position="center">提醒设置</el-divider>

                <el-form :model="remindForm" label-width="80px" class="remind-form">
                  <el-form-item label="提醒类型">
                    <el-radio-group v-model="remindForm.type">
                      <el-radio :label="0">APP提醒</el-radio>
                      <el-radio :label="1">短信提醒</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="提醒时间">
                    <el-select v-model="remindForm.remindTime" placeholder="请选择提醒时间">
                      <el-option v-for="time in remindTimeOptions" :key="time" :label="`提前${time}分钟`" :value="time" />
                    </el-select>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </el-card>
        </Transition>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import {
  Search,
  Ticket,
  DocumentChecked,
  ShoppingCart,
  Bell
} from '@element-plus/icons-vue'
import { couponAPI } from '@/api/coupon'

const queryForm = ref({
  shopNumber: '',
  couponTemplateId: ''
})

const templateData = ref(null)
const loading = ref(false)
const remindForm = ref({
  type: 0,
  remindTime: 5
})
const remindTimeOptions = ref([5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60])

const handleQuery = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录')
    return
  }

  try {
    loading.value = true
    const { shopNumber, couponTemplateId } = queryForm.value
    if (!shopNumber || !couponTemplateId) {
      ElMessage.error('请填写完整的查询条件')
      return
    }

    const response = await couponAPI.findCouponTemplate({
      shopNumber,
      couponTemplateId
    })
    templateData.value = response.data
  } catch (error) {
    console.error('查询失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('查询失败，请稍后重试' + error)
    }
  } finally {
    loading.value = false
  }
}

const handleRedeem = async () => {
  let response
  try {
    response = await couponAPI.redeemCoupon({
      source: templateData.value.source, 
      shopNumber: templateData.value.shopNumber,
      couponTemplateId: templateData.value.id
    })
    
    console.log('兑换响应:', response)
    
    if (!response) {
      ElMessage.error('兑换失败：服务器未响应')
      return
    }
    
    if (response.success) {
      ElMessage.success('兑换成功，请到个人中心查看')
    } else {
      console.error('兑换失败:', response)
      ElMessage.error(response.data.message || '兑换失败')
    }
  } catch (error) {
    console.error('兑换失败:', error)
    ElMessage.error(error)
  } finally {
    loading.value = false
  }
}

const handleRemind = async () => {
  try {
    loading.value = true
    const response = await couponAPI.createCouponTemplateRemind({
      couponTemplateId: templateData.value.id,
      shopNumber: templateData.value.shopNumber,
      type: remindForm.value.type,
      remindTime: remindForm.value.remindTime
    })

    if (response.success) {
      ElMessage.success('设置成功')
    } else {
      ElMessage.error(response.message || '设置失败')
    }
  } catch (error) {
    console.error('设置提醒失败:', error)
    ElMessage.error(error)
  } finally {
    loading.value = false
  }
}

const formattedTemplateData = computed(() => {
  if (!templateData.value) return []

  const typeMapping = {
    0: '立减券',
    1: '满减券',
    2: '折扣券'
  }

  const fields = [
    { label: '模板ID', value: templateData.value.id, type: 'id' },
    { label: '模板名称', value: templateData.value.name, type: 'text' },
    { label: '店铺编号', value: templateData.value.shopNumber, type: 'code' },
    { label: '状态', value: templateData.value.status === 0 ? '生效中' : '已结束', type: 'status' },
    { label: '优惠券来源', value: templateData.value.source === 0 ? '店铺券' : '平台券', type: 'status' },
    { label: '优惠对象', value: templateData.value.target === 0 ? '商品专属' : '全店通用', type: 'status' },
    { label: '优惠类型', value: typeMapping[templateData.value.type] || '未知类型', type: 'status' },
    { label: '有效期', value: `${templateData.value.validStartTime} 至 ${templateData.value.validEndTime}`, type: 'text' }
  ]

  // 如果是商品专属券，添加商品编码字段
  if (templateData.value.target === 0) {
    fields.push({
      label: '优惠商品编码',
      value: templateData.value.goods,
      type: 'code'
    })
  }

  return fields
})
</script>

<style scoped>
.coupon-template-query {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 32px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-divider {
  height: 4px;
  width: 80px;
  background: linear-gradient(to right, #6366f1, #8b5cf6, #d946ef);
  margin: 15px auto;
  border-radius: 2px;
}

.query-form {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.custom-input :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.query-button-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
}

/* 查询按钮样式 */
.query-button {
  min-width: 140px;
  /* 改为与 action-button 一致的最小宽度 */
  padding: 12px 25px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  position: relative;
  overflow: hidden;
}

.query-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.4);
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
}

.query-button:active {
  transform: translateY(0);
}

/* 立即兑换按钮样式 */
.redeem-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  position: relative;
  overflow: hidden;
}

.redeem-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(16, 185, 129, 0.4);
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

.redeem-btn:active {
  transform: translateY(0);
}

/* 设置提醒按钮样式 */
.remind-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  position: relative;
  overflow: hidden;
}

.remind-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.4);
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
}

.remind-btn:active {
  transform: translateY(0);
}

/* 通用按钮动画效果 */
.query-button::after,
.redeem-btn::after,
.remind-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent);
  transition: 0.5s;
}

.query-button:hover::after,
.redeem-btn:hover::after,
.remind-btn:hover::after {
  left: 100%;
}

.action-button {
  min-width: 140px;
  padding: 12px 25px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.action-button :deep(.el-icon) {
  margin-right: 8px;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.action-button:hover :deep(.el-icon) {
  transform: scale(1.1);
}

/* 禁用状态样式 */
.query-button:disabled,
.redeem-btn:disabled,
.remind-btn:disabled {
  background: #e5e7eb;
  box-shadow: none;
  cursor: not-allowed;
  transform: none;
}

.result-card {
  margin-top: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.result-card :deep(.el-card__header) {
  background: var(--el-color-primary-light-8);
  padding: 15px 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.rule-detail {
  padding: 10px;
  background: var(--el-color-primary-light-9);
  border-radius: 4px;
}

.rule-detail div {
  margin: 5px 0;
  color: var(--el-text-color-secondary);
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.remind-form {
  margin-top: 20px;
  padding: 20px;
  background: var(--el-color-primary-light-9);
  border-radius: 8px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .coupon-template-query {
    padding: 15px;
  }

  .query-form {
    padding: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .remind-form {
    padding: 15px;
  }
}

.header-icon {
  margin-right: 12px;
  font-size: 32px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.result-content {
  animation: slideUp 0.3s ease;
}

.custom-descriptions :deep(.el-descriptions__body) {
  background: transparent;
}

.custom-description-item :deep(.el-descriptions__label) {
  background: var(--el-color-primary-light-9);
  padding: 12px 15px;
  font-weight: bold;
}

.description-content {
  padding: 12px 15px;
  background: #fff;
}

.description-content.id {
  color: var(--el-color-primary);
  font-family: monospace;
  font-weight: bold;
}

.description-content.code {
  font-family: monospace;
  background: var(--el-color-info-light-9);
}

.description-content.status {
  color: var(--el-color-success);
  font-weight: bold;
}

.rule-card {
  margin: 0;
  border: none;
  background: var(--el-color-primary-light-9);
}

.rule-card :deep(.el-card__body) {
  padding: 15px;
}

.action-section {
  margin-top: 30px;
  padding: 20px;
  background: var(--el-color-primary-light-9);
  border-radius: 12px;
}

.primary-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
}

.remind-form {
  max-width: 400px;
  margin: 0 auto;
}

:deep(.el-radio-group) {
  display: flex;
  justify-content: space-around;
}

:deep(.el-select) {
  width: 100%;
}

/* 加载动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计优化 */
@media (max-width: 640px) {
  .primary-actions {
    flex-direction: column;
    gap: 10px;
  }

  .action-button {
    width: 100%;
  }

  :deep(.el-radio-group) {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
