<template>
  <div class="page-container">
    <!-- 背景改为简单渐变 -->
    <div class="page-content">
      <header class="page-header">
        <div class="header-wrapper">
          <div class="header-title-group">
            <h1 class="header-title">优惠券管理</h1>
            <div class="title-decoration"></div>
          </div>
          <p class="header-subtitle">高效便捷的优惠券管理系统</p>
        </div>
        <div class="header-badge">Coupon</div>
      </header>

      <el-card class="main-card">
        <!-- 搜索表单部分 -->
        <div class="search-form">
          <el-form :model="queryParams" label-width="120px" label-position="top" class="search-form">
            <el-row :gutter="24">
              <el-col :span="6">
                <el-form-item label="优惠券名称">
                  <el-input v-model="queryParams.name" placeholder="请输入优惠券名称" clearable class="custom-input">
                    <template #prefix>
                      <el-icon>
                        <Search />
                      </el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="优惠对象">
                  <el-select v-model="queryParams.target" placeholder="请选择优惠对象" clearable class="custom-select">
                    <el-option label="商品专属" :value="0" />
                    <el-option label="全店通用" :value="1" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="优惠商品编码">
                  <el-input v-model="queryParams.goods" placeholder="请输入商品编码" clearable class="custom-input">
                    <template #prefix>
                      <el-icon>
                        <Goods />
                      </el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="优惠类型">
                  <el-select v-model="queryParams.type" placeholder="请选择优惠类型" clearable class="custom-select">
                    <el-option label="立减券" :value="0" />
                    <el-option label="满减券" :value="1" />
                    <el-option label="折扣券" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="button-section">
              <div class="left-buttons">
                <el-button type="success" @click="handleCreate" class="create-button">
                  <el-icon>
                    <Plus />
                  </el-icon>
                  <span>创建优惠券</span>
                </el-button>
              </div>
              <div class="right-buttons">
                <el-button type="primary" @click="handleSearch">
                  <el-icon>
                    <Search />
                  </el-icon>
                  <span>查询</span>
                </el-button>
                <el-button @click="resetSearch">
                  <el-icon>
                    <Refresh />
                  </el-icon>
                  <span>重置</span>
                </el-button>
              </div>
            </div>
          </el-form>
        </div>

        <!-- 表格部分 - 优化表格结构 -->
        <div class="table-container">
          <el-table v-loading="loading" :data="tableData" class="data-table" height="460"
            :row-style="{ height: '48px' }" table-layout="fixed">
            <el-table-column prop="name" label="优惠券名称" width="180" show-overflow-tooltip />
            <el-table-column label="优惠对象" width="120" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="row.target === 0 ? 'tag-exclusive' : 'tag-general'">
                  {{ row.target === 0 ? '商品专属' : '全店通用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="goods" label="优惠商品编码" width="150" show-overflow-tooltip />
            <el-table-column label="优惠类型" width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="'tag-type-' + row.type">
                  {{ ['立减券', '满减券', '折扣券'][row.type] }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="有效期" width="220">
              <template #default="{ row }">
                {{ row.validStartTime }} 至 {{ row.validEndTime }}
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="80" />
            <el-table-column label="领取规则" width="200">
              <template #default="{ row }">
                <el-tooltip effect="dark" placement="top">
                  <template #content>
                    <div v-html="formatReceiveRule(row.receiveRule)" />
                  </template>
                  <el-button type="text">查看规则</el-button>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="使用规则" width="200">
              <template #default="{ row }">
                <el-tooltip effect="dark" placement="top">
                  <template #content>
                    <div v-html="formatConsumeRule(row.consumeRule)" />
                  </template>
                  <el-button type="text">查看规则</el-button>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-tooltip content="查看详情" placement="top">
                    <el-button type="default" link @click="viewDetail(row.id)" class="action-button">
                      <el-icon>
                        <View />
                      </el-icon>
                    </el-button>
                  </el-tooltip>

                  <el-tooltip content="增加发行量" placement="top">
                    <el-button type="success" link @click="openIncreaseDialog(row.id)" class="action-button">
                      <el-icon>
                        <Plus />
                      </el-icon>
                    </el-button>
                  </el-tooltip>

                  <el-tooltip content="终止优惠券" placement="top">
                    <el-button type="danger" link @click="openTerminateDialog(row.id)" class="action-button">
                      <el-icon>
                        <CircleClose />
                      </el-icon>
                    </el-button>
                  </el-tooltip>

                  <el-tooltip content="删除优惠券" placement="top">
                    <el-button type="danger" link @click="openDeleteDialog(row.id)" class="action-button">
                      <el-icon>
                        <Delete />
                      </el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页器 -->
        <div class="pagination-wrapper">
          <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total"
            :page-size="queryParams.size" :current-page="queryParams.current" @size-change="handleSizeChange"
            @current-change="handlePageChange" />
        </div>
      </el-card>

      <!-- 增加发行量对话框 -->
      <el-dialog v-model="increaseDialogVisible" title="增加发行量" width="30%" :before-close="handleClose">
        <el-form :model="increaseForm" :rules="increaseRules" ref="increaseFormRef" label-width="120px">
          <el-form-item label="增加数量" prop="number">
            <el-input v-model.number="increaseForm.number" placeholder="请输入增加数量" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="increaseDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="increaseNumber">确认</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 终止优惠券对话框 -->
      <el-dialog v-model="terminateDialogVisible" title="终止优惠券" width="30%">
        <div class="terminate-content">
          <p>确定要终止该优惠券吗？终止后该优惠券将无法继续使用。</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="terminateDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="terminateCoupon">确认终止</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 删除优惠券对话框 -->
      <el-dialog v-model="deleteDialogVisible" title="删除优惠券" width="30%">
        <div class="delete-content">
          <p>确定要删除该优惠券吗？删除后将无法恢复。</p>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="deleteDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="deleteCoupon">确认删除</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { View, Plus, CircleClose, Search, Goods, Refresh, Delete } from '@element-plus/icons-vue'
import { couponAPI } from '@/api/coupon'

export default {
  components: {
    View,
    Plus,
    CircleClose,
    Search,
    Goods,
    Refresh,
    Delete
  },
  data() {
    return {
      loading: false,
      queryParams: {
        name: '',
        target: null,
        goods: '',
        type: null,
        current: 1,
        size: 10
      },
      total: 0,
      tableData: [],
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
      },
      terminateDialogVisible: false,
      terminateForm: {
        couponTemplateId: ''
      },
      deleteDialogVisible: false,
      deleteForm: {
        couponTemplateId: ''
      }
    }
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const response = await couponAPI.getCouponTemplatePage({
          current: this.queryParams.current,
          size: this.queryParams.size,
          name: this.queryParams.name,
          target: this.queryParams.target,
          goods: this.queryParams.goods,
          type: this.queryParams.type
        })
        console.log('查询优惠券响应:', response.data)
        if (response.data.success) {
          this.tableData = response.data.data.records
          this.total = response.data.data.total
        }
      } catch (error) {
        this.$message.error(error?.data?.data?.message || error.message)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.queryParams.current = 1
      this.fetchData()
    },
    resetSearch() {
      this.queryParams = {
        name: '',
        target: null,
        goods: '',
        type: null,
        current: 1,
        size: 10
      }
      this.fetchData()
    },
    handleSizeChange(size) {
      this.queryParams.size = size
      this.fetchData()
    },
    handlePageChange(page) {
      this.queryParams.current = page
      this.fetchData()
    },
    formatReceiveRule(rule) {
      try {
        const parsed = JSON.parse(rule)
        return `
          <div style="line-height: 1.8;">
            <div>每人限领：${parsed.limitPerPerson} 张</div>
            <div>使用说明：${parsed.usageInstructions}</div>
          </div>
        `
      } catch {
        return '规则解析失败'
      }
    },
    formatConsumeRule(rule) {
      try {
        const parsed = JSON.parse(rule)
        return `
          <div style="line-height: 1.8;">
            <div>使用条件：满 ${parsed.termsOfUse} 元可用</div>
            <div>折扣率：${parsed.discountRate * 100}%</div>
            <div>有效期：${parsed.validityPeriod} 小时</div>
            <div>最大优惠金额：${parsed.maximumDiscountAmount} 元</div>
            <div>未满足条件说明：${parsed.explanationOfUnmetConditions}</div>
          </div>
        `
      } catch {
        return '规则解析失败'
      }
    },
    viewDetail(id) {
      this.$router.push({
        name: 'CouponTemplateDetail',
        params: { id }
      })
    },
    openIncreaseDialog(id) {
      this.increaseForm.couponTemplateId = id
      this.increaseForm.number = null
      this.$nextTick(() => {
        this.increaseDialogVisible = true
      })
    },
    async increaseNumber() {
      try {
        await this.$refs.increaseFormRef.validate()
        const params = {
          couponTemplateId: this.increaseForm.couponTemplateId,
          number: this.increaseForm.number
        }

        const response = await couponAPI.increaseNumberCouponTemplate(params)
        console.log('增加发行量响应:', response.data)
        if (response.data.success) {
          this.$message.success('增加发行量成功')
        } else {
          this.$message.error(response.data.message || '增加失败')
        }
        this.increaseDialogVisible = false
        this.fetchData()
      } catch (error) {
        this.$message.error('增加发行量失败：' + (error?.data?.message || error.message))
      }
    },

    openTerminateDialog(id) {
      this.terminateForm.couponTemplateId = id
      this.terminateDialogVisible = true
    },

    async terminateCoupon() {
      try {
        const response = await couponAPI.terminateCouponTemplate(this.terminateForm.couponTemplateId)
        console.log('终止优惠券响应:', response.data)
        if (response.data.success) {
          this.$message.success('终止优惠券成功')
          this.terminateDialogVisible = false
          this.fetchData()
        } else {
          this.$message.error(response.data.message || '终止失败')
        }
      } catch (error) {
        this.$message.error('终止优惠券失败：' + (error?.data?.message || error.message))
      }
      this.fetchData()
    },
    openDeleteDialog(id) {
      this.deleteForm.couponTemplateId = id
      this.deleteDialogVisible = true
    },

    async deleteCoupon() {
      try {
        const response = await couponAPI.deleteCouponTemplate(this.deleteForm.couponTemplateId)
        console.log('删除优惠券响应:', response.data)
        if (response.data.success) {
          this.$message.success('删除优惠券成功')
          this.deleteDialogVisible = false
          this.fetchData()
        } else {
          this.$message.error(response.data.message || '删除失败')
        }
      } catch (error) {
        this.$message.error('删除优惠券失败：' + (error?.data?.message || error.message))
      }
    },
    handleCreate() {
      this.$router.push('/admin/coupon-template/create')
    },
    tableRowClassName({ row, rowIndex }) {
      return `table-row-${rowIndex % 2 === 0 ? 'even' : 'odd'}`
    },
    tableCellClassName({ row, column }) {
      return 'table-cell'
    },
    tableHeaderClassName() {
      return 'table-header'
    }
  },
  mounted() {
    const token = localStorage.getItem('token')
    if (!token) {
      this.$message.error('请先登录')
      this.$router.push('/')
      return
    }

    this.fetchData()
  }
}
</script>

<style scoped>
/* 核心布局样式 */
.page-container {
  min-height: 100vh;
  padding: 0px;
  background-color: #f8fafc;
}

.page-content {
  position: relative;
  z-index: 1;
}

/* 页头样式 */
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

/* 响应式调整 */
@media (max-width: 768px) {
  .page-header {
    padding: 24px;
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

/* 主卡片样式 */
.main-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.el-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 5px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
}

/* 搜索表单样式 */
.search-form {
  padding: 0px;
  border-bottom: 1px solid #eee;
}

/* 表格容器样式 */
.table-container {
  padding: 0 0px;
}

.data-table {
  --el-table-border-color: #eee;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #f0f7ff;
}

/* 分页器样式 */
.pagination-wrapper {
  padding: 20px;
  text-align: right;
  border-top: 1px solid #eee;
}

/* 状态标签统一样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
}

/* 操作按钮样式 */
.action-btn {
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: #f0f7ff;
}

/* 响应式布局 */
@media (max-width: 1400px) {
  .page-container {
    padding: 0;
  }
}

/* 优化滚动性能 */
:deep(.el-table__body-wrapper) {
  overflow-y: auto;
  scrollbar-width: thin;
}

:deep(.el-table__body) {
  will-change: transform;
  transform: translateZ(0);
}

/* 表格行样式优化 */
:deep(.el-table__row) {
  transition: background-color 0.2s;
}

/* 简化动画效果 */
.fade-transition {
  transition: opacity 0.2s, transform 0.2s;
}

/* 优化按钮样式 */
:deep(.el-button) {
  transition: transform 0.2s, box-shadow 0.2s;
}

/* 移除不必要的动画效果 */
:deep(.el-table__column-resize-proxy) {
  display: none;
}

/* 优化表格行样式 */
:deep(.table-row-even) {
  background-color: #ffffff;
}

:deep(.table-row-odd) {
  background-color: #f9fafb;
}

/* 优化表格单元格样式 */
:deep(.table-cell) {
  padding: 8px 12px;
  transition: background-color 0.2s ease;
}

/* 优化表头样式 */
:deep(.table-header) {
  background: linear-gradient(to bottom, #f8fafc, #f1f5f9);
  color: #1e293b;
  font-weight: 600;
  padding: 12px;
  transition: background-color 0.2s ease;
}

/* 标签样式 */
.tag-exclusive,
.tag-general,
.tag-type-0,
.tag-type-1,
.tag-type-2 {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
}

.tag-exclusive {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.tag-general {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.tag-type-0 {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.tag-type-1 {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.tag-type-2 {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}

/* 优化表格滚动条 */
:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  border-radius: 3px;
  background-color: rgba(144, 147, 153, 0.3);
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background-color: transparent;
}

/* 添加表格动画 */
@keyframes fadeInTable {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.custom-table {
  animation: fadeInTable 0.3s ease-out;
}

/* 优化表格性能 */
:deep(.el-table__body) {
  will-change: transform;
  contain: content;
}

:deep(.el-table__row) {
  contain: content;
}

/* 减少重绘和重排 */
:deep(.el-table__cell) {
  transform: translateZ(0);
  backface-visibility: hidden;
}

/* 替换原有的 button-group 相关样式 */
.button-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  margin-top: 8px;
  border-top: 1px solid #eee;
}

.left-buttons {
  display: flex;
  gap: 12px;
}

.right-buttons {
  display: flex;
  gap: 12px;
}

.create-button {
  background: linear-gradient(135deg, #67C23A, #4CAF50);
  border: none;
  padding: 10px 20px;
  font-weight: 500;
  color: white;
  transition: all 0.3s ease;
}

.create-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
  background: linear-gradient(135deg, #85ce61, #67C23A);
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  padding: 0 20px;
  font-size: 14px;
  border-radius: 6px;
}

:deep(.el-button .el-icon) {
  margin: 0;
  font-size: 16px;
}

/* 查询按钮样式 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF, #3a8ee6);
  border: none;
  color: white;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff, #409EFF);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 重置按钮样式 */
:deep(.el-button--default) {
  border: 1px solid #dcdfe6;
  color: #606266;
  background: #fff;
}

:deep(.el-button--default:hover) {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

@media (max-width: 768px) {
  .button-section {
    flex-direction: column;
    gap: 12px;
  }

  .left-buttons,
  .right-buttons {
    width: 100%;
  }

  .right-buttons {
    justify-content: flex-end;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-button {
  padding: 4px 8px;
}

.delete-content {
  text-align: center;
  color: #606266;
  margin: 20px 0;
}
</style>
