<template>
  <div class="container">
    <!-- 动态背景 -->
    <div class="background">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="content">
      <div class="page-header">
        <div class="back-button" @click="goBack">
          <svg class="arrow-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          返回列表
        </div>
        <h1 class="title">优惠券详情</h1>
      </div>

      <div class="detail-card">
        <!-- 基本信息卡片 -->
        <div class="info-section">
          <h2 class="section-title">基本信息</h2>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="优惠券ID">{{ detailData.id }}</el-descriptions-item>
            <el-descriptions-item label="优惠券名称">{{ detailData.name }}</el-descriptions-item>
            <el-descriptions-item label="店铺编号">{{ detailData.shopNumber }}</el-descriptions-item>
            <el-descriptions-item label="优惠券来源">
              {{ detailData.source === 0 ? '店铺券' : '平台券' }}
            </el-descriptions-item>
            <el-descriptions-item label="优惠对象">
              {{ detailData.target === 0 ? '商品专属' : '全店通用' }}
            </el-descriptions-item>
            <el-descriptions-item label="优惠商品编码">{{ detailData.goods }}</el-descriptions-item>
            <el-descriptions-item label="优惠类型">
              {{ ['立减券', '满减券', '折扣券'][detailData.type] }}
            </el-descriptions-item>
            <el-descriptions-item label="有效期">
              {{ detailData.validStartTime }} 至 {{ detailData.validEndTime }}
            </el-descriptions-item>
            <el-descriptions-item label="库存">{{ detailData.stock }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 0 ? 'success' : 'info'">
                {{ detailData.status === 0 ? '生效中' : '已结束' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 规则信息卡片 -->
        <div class="rules-section">
          <div class="rule-card receive-rule">
            <h2 class="section-title">领取规则</h2>
            <div class="rule-content">
              <pre>{{ formatReceiveRule(detailData.receiveRule) }}</pre>
            </div>
          </div>

          <div class="rule-card consume-rule">
            <h2 class="section-title">使用规则</h2>
            <div class="rule-content">
              <pre>{{ formatConsumeRule(detailData.consumeRule) }}</pre>
            </div>
          </div>
        </div>

        <!-- 操作按钮区 -->
        <div class="actions-section">
          <el-button v-if="detailData.status === 0" type="danger" class="action-button terminate-button"
            @click="openTerminateDialog">
            <i class="el-icon-close"></i>
            终止优惠券
          </el-button>
          <el-button type="success" class="action-button increase-button" :disabled="detailData.status !== 0"
            @click="openIncreaseDialog">
            <i class="el-icon-plus"></i>
            增加发行量
          </el-button>
        </div>
      </div>
    </div>

    <!-- 弹窗部分 -->
    <el-dialog title="终止优惠券" v-model="terminateDialogVisible" width="30%">
      <span>确定要终止该优惠券吗？此操作不可撤销。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="terminateDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="terminateCoupon">确定终止</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 增加发行量弹窗 -->
    <el-dialog title="增加发行量" v-model="increaseDialogVisible" width="30%">
      <el-form :model="increaseForm" label-width="120px" :rules="increaseRules" ref="increaseFormRef">
        <el-form-item label="增加数量" prop="number">
          <el-input-number v-model="increaseForm.number" :min="1" :max="10000" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="increaseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="increaseNumber">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { couponAPI } from '@/api/coupon'

export default {
  created() {
    this.$api = couponAPI
  },
  data() {
    return {
      detailData: {
        id: '',
        name: '',
        shopNumber: '',
        source: 0,
        target: 0,
        goods: '',
        type: 0,
        validStartTime: '',
        validEndTime: '',
        stock: 0,
        receiveRule: '',
        consumeRule: '',
        status: 0
      },
      terminateDialogVisible: false,
      increaseDialogVisible: false,
      increaseForm: {
        couponTemplateId: '',
        number: null
      },
      increaseRules: {
        number: [
          { required: true, message: '请输入增加数量', trigger: 'blur' },
          { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    goBack() {
      this.$router.push('/admin/coupon-template')
    },
    async fetchDetail() {
      const couponTemplateId = this.$route.params.id
      if (!couponTemplateId) {
        this.$message.error('缺少优惠券模板ID')
        this.goBack()
        return
      }

      try {
        const response = await this.$api.getCouponTemplateDetail(couponTemplateId)
        if (response.data.success) {
          this.detailData = response.data.data
        } else {
          this.$message.error(response.data.message || '获取详情失败')
          this.goBack()
        }
      } catch (error) {
        this.$message.error('获取详情失败：' + (error?.data?.message || error.message))
        this.goBack()
      }
    },
    formatReceiveRule(rule) {
      try {
        const parsed = JSON.parse(rule)
        return `• 每人限领：${parsed.limitPerPerson} 张\n• 使用说明：${parsed.usageInstructions}`
      } catch {
        return rule
      }
    },
    formatConsumeRule(rule) {
      try {
        const parsed = JSON.parse(rule)
        return `• 使用门槛：满 ${parsed.termsOfUse} 元可用\n• 折扣比例：${parsed.discountRate * 10} 折\n• 有效期：${parsed.validityPeriod} 小时\n• 最高优惠：${parsed.maximumDiscountAmount} 元\n• 未满足条件说明：${parsed.explanationOfUnmetConditions}`
      } catch {
        return rule
      }
    },

    // 打开终止弹窗
    openTerminateDialog() {
      this.terminateDialogVisible = true
    },

    // 终止优惠券
    async terminateCoupon() {
      try {
        const response = await this.$api.terminateCouponTemplate(this.detailData.id)
        if (response.data.success) {
          this.$message.success('终止优惠券成功')
          this.terminateDialogVisible = false
          this.fetchDetail() // 刷新详情数据
        } else {
          this.$message.error(response.data.message || '终止失败')
        }
      } catch (error) {
        this.$message.error('终止优惠券失败：' + (error?.data?.message || error.message))
      }
    },

    // 打开增加发行量弹窗
    openIncreaseDialog() {
      this.increaseForm.couponTemplateId = this.detailData.id
      this.increaseDialogVisible = true
    },

    // 增加发行量
    async increaseNumber() {
      try {
        await this.$refs.increaseFormRef.validate()
        const response = await this.$api.increaseNumberCouponTemplate({
          couponTemplateId: this.increaseForm.couponTemplateId,
          number: this.increaseForm.number
        })

        if (response.data.success) {
          this.$message.success('增加发行量成功')
          this.increaseDialogVisible = false
          this.fetchDetail() // 刷新详情数据
        } else {
          this.$message.error(response.data.message || '增加发行量失败')
        }
      } catch (error) {
        this.$message.error('增加发行量失败：' + (error?.data?.message || error.message))
      }
    }
  },
  mounted() {
    this.fetchDetail()
  }
}
</script>

<style scoped>
.container {
  position: relative;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8eaff 100%);
}

.background {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  filter: blur(40px);
  mix-blend-mode: multiply;
  opacity: 0.4;
}

.blob-1 {
  top: -100px;
  left: -100px;
  background: #bcd5ff;
  animation: blob 7s infinite;
}

.blob-2 {
  bottom: -100px;
  right: -100px;
  background: #c4c8ff;
  animation: blob 7s infinite 2s;
}

.blob-3 {
  top: 50%;
  left: 50%;
  background: #e0d5ff;
  animation: blob 7s infinite 4s;
}

.content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
  position: relative;
}

.back-button {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 8px;
}

.back-button:hover {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.arrow-icon {
  width: 20px;
  height: 20px;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.subtitle {
  color: #4b5563;
  font-size: 1.1rem;
}

.detail-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 32px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e2e8f0;
  color: #1e293b;
}

.info-section {
  margin-bottom: 32px;
}

.rules-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.rule-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s;
}

.rule-card:hover {
  transform: translateY(-4px);
}

.rule-content {
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  font-family: 'Monaco', monospace;
  line-height: 1.6;
}

.actions-section {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #e2e8f0;
}

.action-button {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.terminate-button {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
}

.increase-button {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  border: none;
}

.terminate-button:hover,
.increase-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

:deep(.el-descriptions) {
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-descriptions__cell) {
  padding: 16px;
}

:deep(.el-tag) {
  border-radius: 8px;
  padding: 8px 16px;
}

@keyframes blob {
  0% {
    transform: translate(0, 0) scale(1);
  }

  33% {
    transform: translate(30px, -50px) scale(1.1);
  }

  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }

  100% {
    transform: translate(0, 0) scale(1);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .detail-card {
    padding: 20px;
  }

  .rules-section {
    grid-template-columns: 1fr;
  }

  .actions-section {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }
}
</style>
