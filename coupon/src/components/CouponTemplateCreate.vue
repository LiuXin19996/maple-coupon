<template>
  <div class="container">
    <!-- 动态背景 -->
    <div class="background">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <!-- 页面内容 -->
    <div class="content">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-content">
          <h1 class="title">创建优惠券</h1>
        </div>
      </div>

      <el-card class="create-card">
        <el-form :model="form" label-width="120px" label-position="top" :rules="rules" ref="formRef">
          <!-- 基本信息 -->
          <div class="section-title">基本信息</div>
          <el-row :gutter="24">
            <el-col :md="12" :sm="24">
              <el-form-item label="优惠券名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入优惠券名称">
                  <template #prefix>
                    <el-icon><Ticket /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="优惠券来源" prop="source">
                <el-select v-model="form.source" placeholder="请选择优惠券来源">
                  <el-option label="店铺券" :value="0" />
                  <el-option label="平台券" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 优惠设置 -->
          <div class="section-title">优惠设置</div>
          <el-row :gutter="24">
            <el-col :md="8" :sm="24">
              <el-form-item label="优惠对象" prop="target">
                <el-select v-model="form.target" placeholder="请选择优惠对象">
                  <el-option label="商品专属" :value="0" />
                  <el-option label="全店通用" :value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :md="8" :sm="24">
              <el-form-item label="优惠类型" prop="type">
                <el-select v-model="form.type" placeholder="请选择优惠类型">
                  <el-option label="立减券" :value="0" />
                  <el-option label="满减券" :value="1" />
                  <el-option label="折扣券" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :md="8" :sm="24">
              <el-form-item label="优惠商品编码" prop="goods">
                <el-input v-model="form.goods" placeholder="请输入商品编码">
                  <template #prefix>
                    <el-icon><Goods /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 使用规则 -->
          <div class="section-title">使用规则</div>
          <el-row :gutter="24">
            <el-col :span="24">
              <el-form-item label="有效期" prop="validTimeRange">
                <el-date-picker v-model="form.validTimeRange" type="datetimerange" range-separator="至"
                  start-placeholder="开始日期时间" end-placeholder="结束日期时间" :format="dateFormat" :value-format="dateFormat"
                  :shortcuts="dateShortcuts" style="width: 100%" :locale="locale" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="库存" prop="stock">
                <el-input-number v-model="form.stock" :min="1" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="每人限领次数" prop="limitPerPerson">
                <el-input-number v-model="form.receiveRule.limitPerPerson" :min="1" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="使用说明" prop="usageInstructions">
                <el-input v-model="form.receiveRule.usageInstructions" placeholder="请输入使用说明" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="使用门槛" prop="termsOfUse">
                <el-input-number v-model="form.consumeRule.termsOfUse" :min="0" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="最大优惠金额" prop="maximumDiscountAmount">
                <el-input-number v-model="form.consumeRule.maximumDiscountAmount" :min="0" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="折扣率" prop="discountRate">
                <el-input-number v-model="form.consumeRule.discountRate" :min="0" :max="1" :step="0.1" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="未满足条件说明" prop="explanationOfUnmetConditions">
                <el-input v-model="form.consumeRule.explanationOfUnmetConditions" placeholder="请输入未满足条件说明" />
              </el-form-item>
            </el-col>

            <el-col :md="8" :sm="24">
              <el-form-item label="有效期(小时)" prop="validityPeriod">
                <el-input-number v-model="form.consumeRule.validityPeriod" :min="1" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-actions">
            <el-button type="primary" @click="submitForm" class="submit-button">
              <el-icon><Check /></el-icon>
              <span>创建优惠券</span>
            </el-button>
            <el-button @click="resetForm" class="reset-button">
              <el-icon><Refresh /></el-icon>
              <span>重置表单</span>
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ElDatePicker } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

export default {
  components: {
    ElDatePicker
  },
  data() {
    return {
      locale: zhCn,
      dateFormat: 'YYYY-MM-DD HH:mm:ss',
      dateShortcuts: [
        {
          text: '最近一周',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          },
        },
        {
          text: '最近一个月',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          },
        }
      ],
      form: {
        name: '',
        source: null,
        target: null,
        goods: '',
        type: null,
        validTimeRange: [],
        stock: 1,
        receiveRule: {
          limitPerPerson: 1,
          usageInstructions: ''
        },
        consumeRule: {
          termsOfUse: 0,
          maximumDiscountAmount: 0,
          discountRate: 0.8,
          explanationOfUnmetConditions: '',
          validityPeriod: 48
        }
      },
      rules: {
        name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
        source: [{ required: true, message: '请选择优惠券来源', trigger: 'change' }],
        target: [{ required: true, message: '请选择优惠对象', trigger: 'change' }],
        goods: [{ required: true, message: '请输入优惠商品编码', trigger: 'blur' }],
        type: [{ required: true, message: '请选择优惠类型', trigger: 'change' }],
        validTimeRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
        stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
        'receiveRule.limitPerPerson': [{ required: true, message: '请输入每人限领次数', trigger: 'blur' }],
        'receiveRule.usageInstructions': [{ required: true, message: '请输入使用说明', trigger: 'blur' }],
        'consumeRule.termsOfUse': [{ required: true, message: '请输入使用门槛', trigger: 'blur' }],
        'consumeRule.maximumDiscountAmount': [{ required: true, message: '请输入最大优惠金额', trigger: 'blur' }],
        'consumeRule.discountRate': [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
        'consumeRule.explanationOfUnmetConditions': [{ required: true, message: '请输入未满足条件说明', trigger: 'blur' }],
        'consumeRule.validityPeriod': [{ required: true, message: '请输入有效期', trigger: 'blur' }]
      }
    }
  },
  methods: {
    async submitForm() {
      try {
        await this.$refs.formRef.validate()

        const token = localStorage.getItem('token')
        const username = localStorage.getItem('username')

        // 准备提交数据
        const submitData = {
          ...this.form,
          validStartTime: this.form.validTimeRange[0],
          validEndTime: this.form.validTimeRange[1],
          receiveRule: JSON.stringify(this.form.receiveRule),
          consumeRule: JSON.stringify(this.form.consumeRule)
        }

        const response = await this.$axios.post('/api/merchant-admin/coupon-template/create', submitData, {
          headers: {
            'token': token,
            'username': username
          }
        })

        if (response.data.success) {
          this.$message.success('创建成功')
          this.$router.push('/admin/coupon-template')
        }
      } catch (error) {
        this.$message.error('创建失败：' + (error.response?.data?.message || error.message))
      }
    },
    resetForm() {
      this.$refs.formRef.resetFields()
    }
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

.create-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: none;
  padding: 32px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin: 24px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
  background: linear-gradient(90deg, #2563eb 0%, #4f46e5 100%);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: none;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select .el-input__wrapper:hover) {
  border-color: #3b82f6;
  transform: translateY(-1px);
}

:deep(.el-input__wrapper:focus-within),
:deep(.el-select .el-input__wrapper:focus-within) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #e2e8f0;
}

.submit-button,
.reset-button {
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.submit-button {
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  border: none;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.reset-button {
  background: white;
  border: 2px solid #e5e7eb;
}

.reset-button:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

@keyframes blob {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1); }
}

@media screen and (max-width: 768px) {
  .container {
    padding: 16px;
  }
  
  .create-card {
    padding: 20px;
  }
  
  .title {
    font-size: 1.5rem;
  }
  
  .section-title {
    font-size: 1.1rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>
