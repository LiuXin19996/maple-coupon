<template>
  <div class="page-container">
    <div class="page-content">
      <!-- 页面背景 -->
      <div class="page-background"></div>

      <!-- 页面内容容器 -->
      <div class="page-content">
        <!-- 标题部分 -->
        <header class="page-header">
          <div class="header-wrapper">
            <div class="header-title-group">
              <h1 class="header-title">优惠券结算</h1>
              <div class="title-decoration"></div>
            </div>
            <p class="header-subtitle">快速查询订单可用的优惠券</p>
          </div>
          <div class="header-badge">Settlement</div>
        </header>

        <!-- 查询表单卡片 -->
        <el-card class="query-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <Search />
              </el-icon>
              <span>查询条件</span>
            </div>
          </template>

          <el-form :model="queryForm" label-width="120px" class="query-form">
            <el-form-item label="订单金额">
              <el-input-number v-model="queryForm.orderAmount" :min="0" :precision="2" class="custom-input-number"
                controls-position="right" />
            </el-form-item>

            <el-form-item label="店铺编号">
              <el-input v-model="queryForm.shopNumber" class="custom-input" placeholder="请输入店铺编号" />
            </el-form-item>

            <el-form-item label="商品列表" class="goods-list-item">
              <div class="goods-header">
                <el-button type="primary" @click="openAddGoodsDialog" class="add-goods-btn">
                  <i class="el-icon-plus"></i>
                  添加商品
                </el-button>
              </div>

              <el-table :data="queryForm.goodsList" border class="custom-table">
                <el-table-column prop="goodsNumber" label="商品编号" />
                <el-table-column prop="goodsAmount" label="商品金额">
                  <template #default="scope">
                    <span class="amount">¥{{ scope.row.goodsAmount.toFixed(2) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="goodsPayableAmount" label="应付金额">
                  <template #default="scope">
                    <span class="amount">¥{{ scope.row.goodsPayableAmount.toFixed(2) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button type="danger" size="small" class="delete-btn" @click="removeGoods(scope.$index)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>

            <!-- 更新按钮样式 -->
            <el-form-item class="query-actions">
              <el-button type="primary" @click="handleQuery" class="query-button">
                <el-icon>
                  <Search />
                </el-icon>
                查询优惠券
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 结果展示卡片 -->
        <Transition name="fade">
          <el-card v-if="availableCoupons.length || unavailableCoupons.length" class="result-section">
            <template #header>
              <div class="section-header">
                <el-icon>
                  <List />
                </el-icon>
                <span>查询结果</span>
              </div>
            </template>

            <el-tabs v-model="activeTab" class="custom-tabs">
              <el-tab-pane label="可用优惠券" name="available">
                <el-table :data="availableCoupons" border class="custom-table">
                  <el-table-column prop="id" label="优惠券ID" width="100" />
                  <el-table-column prop="name" label="优惠券名称" />
                  <el-table-column prop="couponAmount" label="优惠金额">
                    <template #default="scope">
                      <span class="discount-amount">¥{{ scope.row.couponAmount }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="consumeRule" label="使用规则" width="120">
                    <template #default="scope">
                      <el-popover placement="right" :width="320" trigger="hover" v-model:visible="popoverVisible">
                        <template #reference>
                          <el-button type="text" class="view-rule-btn">查看规则</el-button>
                        </template>
                        <div class="rule-content">
                          <h4>优惠券使用规则</h4>
                          <div class="rule-item">
                            <span class="rule-label">使用门槛：</span>
                            <span class="rule-value">满{{ formatRule(scope.row.consumeRule).termsOfUse }}元可用</span>
                          </div>
                          <div class="rule-item">
                            <span class="rule-label">最高优惠：</span>
                            <span class="rule-value">{{ formatRule(scope.row.consumeRule).maximumDiscountAmount
                              }}元</span>
                          </div>
                          <div class="rule-item">
                            <span class="rule-label">折扣率：</span>
                            <span class="rule-value">{{ (formatRule(scope.row.consumeRule).discountRate * 10).toFixed(1)
                              }}折</span>
                          </div>
                          <div class="rule-item">
                            <span class="rule-label">有效期：</span>
                            <span class="rule-value">{{ formatRule(scope.row.consumeRule).validityPeriod }}小时</span>
                          </div>
                        </div>
                      </el-popover>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <el-tab-pane label="不可用优惠券" name="unavailable">
                <el-table :data="unavailableCoupons" border class="custom-table">
                  <el-table-column prop="id" label="优惠券ID" width="100" />
                  <el-table-column prop="consumeRule" label="不可用原因">
                    <template #default="scope">
                      <span class="unavailable-reason">
                        {{ JSON.parse(scope.row.consumeRule).explanationOfUnmetConditions }}
                      </span>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </Transition>
      </div>

      <!-- 添加商品对话框 -->
      <el-dialog title="添加商品" v-model="addGoodsDialogVisible" width="500px" class="custom-dialog">
        <el-form :model="newGoods" label-width="100px" class="goods-form">
          <el-form-item label="商品编号">
            <el-input v-model="newGoods.goodsNumber" class="custom-input" />
          </el-form-item>
          <el-form-item label="商品金额">
            <el-input-number v-model="newGoods.goodsAmount" :min="0" :precision="2" class="custom-input-number"
              controls-position="right" />
          </el-form-item>
          <el-form-item label="应付金额">
            <el-input-number v-model="newGoods.goodsPayableAmount" :min="0" :precision="2" class="custom-input-number"
              controls-position="right" />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="addGoodsDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleAddGoods">确定</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { couponAPI } from '@/api/coupon'
import { Search, Wallet } from '@element-plus/icons-vue'
// import { th } from 'element-plus/es/locale';

const queryForm = reactive({
  orderAmount: 0,
  shopNumber: '',
  goodsList: []
})
const popoverVisible = ref(false);

// 查询结果
const availableCoupons = ref([])
const unavailableCoupons = ref([])
const activeTab = ref('available')

// 控制“添加商品”对话框的显示与隐藏
const addGoodsDialogVisible = ref(false)

// 用于临时存放待添加的商品信息
const newGoods = reactive({
  goodsNumber: '',
  goodsAmount: 0,
  goodsPayableAmount: 0
})

// 打开对话框前，重置数据
const openAddGoodsDialog = () => {
  addGoodsDialogVisible.value = true
  newGoods.goodsNumber = ''
  newGoods.goodsAmount = 0
  newGoods.goodsPayableAmount = 0
}

// 确认添加商品
const handleAddGoods = () => {
  // 简单验证（可按需扩展）
  if (!newGoods.goodsNumber) {
    return alert('商品编号不能为空')
  }
  // 将新商品加入 goodsList
  queryForm.goodsList.push({
    goodsNumber: newGoods.goodsNumber,
    goodsAmount: newGoods.goodsAmount,
    goodsPayableAmount: newGoods.goodsPayableAmount
  })
  // 关闭对话框
  addGoodsDialogVisible.value = false
}

// 删除商品
const removeGoods = (index) => {
  queryForm.goodsList.splice(index, 1)
}

// 查询优惠券
const handleQuery = async () => {
  try {
    const res = await couponAPI.settlementCouponQuery(queryForm)
    availableCoupons.value = res.data.data.availableCouponList
    unavailableCoupons.value = res.data.data.notAvailableCouponList
    // 如果查询后要切回可用优惠券页签，可以手动设置
    activeTab.value = 'available'
    if (res.data.success) {
      this.$message.success('查询成功')
    } else {
      this.$message.error(res.data.message || '查询失败')
    }
  } catch (error) {
    console.error(error)
    this.$message.error(error?.data?.message || '查询失败')
  }
}

// 格式化优惠券规则
const formatRule = (ruleString) => {
  if (typeof ruleString === 'string') {
    try {
      return JSON.parse(ruleString);
    } catch (error) {
      console.error('解析规则出错:', error);
      return {
        termsOfUse: 0,
        maximumDiscountAmount: 0,
        discountRate: 0,
        validityPeriod: 0
      };
    }
  }
  return ruleString; // 如果已经是对象则直接返回
}
</script>

<style scoped>
/* 更新页面容器样式 */
.page-container {
  min-height: 100vh;
  padding: 0;
  background-color: #f8fafc;
  width: 100%;
}

.page-content {
  width: 100%;
  margin: 0;
  position: relative;
  padding: 0;
}

/* 调整卡片间距 */
.page-header {
  margin-bottom: 6px;
  padding: 16px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-wrapper {
  position: relative;
  z-index: 2;
}

.header-title-group {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.header-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a90ff;
  position: relative;
  z-index: 1;
  letter-spacing: 1px;
  text-shadow: 2px 2px 4px rgba(26, 144, 255, 0.1);
}

.title-decoration {
  position: absolute;
  bottom: 4px;
  left: 0;
  width: 100%;
  height: 10px;
  background: linear-gradient(90deg, rgba(26, 144, 255, 0.2), rgba(24, 100, 171, 0.1));
  border-radius: 4px;
  z-index: 0;
}

.header-subtitle {
  margin: 0;
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.header-badge {
  position: absolute;
  right: -20px;
  top: -20px;
  background: linear-gradient(135deg, #1a90ff, #1864ab);
  color: rgba(255, 255, 255, 0.9);
  padding: 40px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 2px;
  transform: rotate(45deg);
  opacity: 0.1;
}

/* 添加装饰效果 */
.page-header::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 6px;
  height: 100%;
  background: linear-gradient(to bottom, #1a90ff, #1864ab);
}

.page-header::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(to right, #1a90ff, transparent);
}

/* 标题悬停效果 */
.header-title-group:hover .title-decoration {
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    opacity: 0.5;
    transform: translateX(-10px);
  }

  50% {
    opacity: 0.8;
    transform: translateX(10px);
  }

  100% {
    opacity: 0.5;
    transform: translateX(-10px);
  }
}

.el-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 5px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .page-content {
    padding: 0;
  }

  .query-form {
    padding: 16px 24px;
  }

  .page-header {
    padding: 16px 24px;
  }

  .header-title {
    font-size: 28px;
  }

  .header-subtitle {
    font-size: 14px;
  }

  .header-badge {
    display: none;
  }
}

/* 区段样式 */
.query-section,
.result-section {
  margin-bottom: 0;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  background: white;
  border: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  padding: 16px 32px;
  /* 增加左右内边距 */
  background: linear-gradient(to right, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.08));
}


/* 表单样式优化 */
.query-form {
  padding: 16px;
  /* 统一内边距 */
}

.custom-input :deep(.el-input__wrapper),
.custom-input-number :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover),
.custom-input-number :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.12);
  transform: translateY(-1px);
}

/* 按钮样式增强 */
.query-button,
.add-goods-btn {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  padding: 12px 30px;
  height: auto;
  font-weight: 500;
  letter-spacing: 1px;
  position: relative;
  overflow: hidden;
}

.query-button::after,
.add-goods-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: 0.5s;
}

.query-button:hover::after,
.add-goods-btn:hover::after {
  left: 100%;
}

.query-button:hover,
.add-goods-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.3);
}

/* 表格样式优化 */
.custom-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background: linear-gradient(to right, #f8fafc, #f1f5f9);
  color: #1e293b;
  font-weight: 600;
  padding: 16px;
}

:deep(.el-table td) {
  padding: 16px;
}

/* 标签页样式优化 */
:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: linear-gradient(to right, transparent, #e2e8f0, transparent);
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 25px;
  height: 48px;
  line-height: 48px;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #6366f1;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(to right, #6366f1, #8b5cf6);
  height: 3px;
  border-radius: 3px;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .settlement-coupon-query {
    padding: 20px 15px;
  }

  .header-content {
    padding: 20px;
  }

  .header-content h2 {
    font-size: 24px;
  }

  .section-header {
    padding: 15px;
    font-size: 16px;
  }

  .query-form {
    padding: 12px;
  }

  .query-button,
  .add-goods-btn {
    width: 100%;
  }
}
</style>
