<template>
  <div class="settlement-coupon-query">
    <!-- 页面背景 -->
    <div class="page-background"></div>
    
    <!-- 页面内容容器 -->
    <div class="page-content">
      <!-- 标题部分 -->
      <header class="page-header">
        <div class="header-content">
          <h2>
            <el-icon class="header-icon"><Wallet /></el-icon>
            结算优惠券查询
          </h2>
          <div class="header-divider"></div>
          <p class="header-desc">快速查询订单可用的优惠券信息</p>
        </div>
      </header>

      <!-- 查询表单卡片 -->
      <el-card class="query-section">
        <template #header>
          <div class="section-header">
            <el-icon><Search /></el-icon>
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
              <el-icon><Search /></el-icon>
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
              <el-icon><List /></el-icon>
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
                          <span class="rule-value">{{ formatRule(scope.row.consumeRule).maximumDiscountAmount }}元</span>
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
</template>

<script setup>
import { ref, reactive } from 'vue'
import { couponAPI } from '@/api/coupon'
import { Search, Wallet } from '@element-plus/icons-vue'

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
    availableCoupons.value = res.data.availableCouponList
    unavailableCoupons.value = res.data.notAvailableCouponList
    // 如果查询后要切回可用优惠券页签，可以手动设置
    activeTab.value = 'available'
  } catch (error) {
    console.error(error)
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
/* 基础布局 */
.settlement-coupon-query {
  min-height: 100vh;
  position: relative;
  padding: 40px 20px;
}

.page-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #f6f8fd 0%, #f0f3ff 100%);
  z-index: -1;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

/* 页头样式增强 */
.page-header {
  margin-bottom: 40px;
  text-align: center;
  position: relative;
}

.header-content {
  position: relative;
  display: inline-block;
  padding: 30px 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-content h2 {
  font-size: 36px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 15px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
}

.header-desc {
  color: #6b7280;
  font-size: 16px;
  margin: 10px 0 0;
}

.header-divider {
  height: 4px;
  width: 60%;
  margin: 15px auto;
  background: linear-gradient(to right, #6366f1, #8b5cf6, #d946ef);
  border-radius: 2px;
}

/* 区段样式 */
.query-section,
.result-section {
  margin-bottom: 30px;
  border-radius: 16px;
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
  padding: 20px;
  background: linear-gradient(to right, rgba(99, 102, 241, 0.08), rgba(139, 92, 246, 0.08));
}

/* 表单样式优化 */
.query-form {
  padding: 30px;
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
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
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
    padding: 15px;
  }

  .query-button,
  .add-goods-btn {
    width: 100%;
  }
}
</style>
