<template>
  <div class="page-container">
    <div class="page-content">
      <header class="page-header">
        <div class="header-wrapper">
          <div class="header-title-group">
            <h1 class="header-title">优惠券提醒</h1>
            <div class="title-decoration"></div>
          </div>
          <p class="header-subtitle">优惠券秒杀提醒</p>
        </div>
        <div class="header-badge">Remind</div>
      </header>

      <el-card class="main-card">
        <template #header>
          <div class="card-header">
            <div class="section-header">
              <el-icon>
                <Bell />
              </el-icon>
              <span>提醒列表</span>
            </div>
            <el-button type="primary" size="small" @click="fetchData" :loading="loading" class="refresh-button">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新列表
            </el-button>
          </div>
        </template>

        <el-empty v-if="!loading && remindList.length === 0" description="暂无提醒数据" />

        <div v-else>
          <el-table :data="flattenedRemindList" v-loading="loading" style="width: 100%">
            <el-table-column prop="name" label="优惠券名称" width="200" />

            <el-table-column label="有效期" width="220">
              <template #default="{ row }">
                {{ formatDate(row.validStartTime) }} - {{ formatDate(row.validEndTime) }}
              </template>
            </el-table-column>

            <el-table-column label="提醒时间">
              <template #default="{ row }">
                <el-tag :type="isExpired(row.remindTime) ? 'danger' : 'success'" class="remind-time-tag">
                  {{ formatDate(row.remindTime) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="提醒方式">
              <template #default="{ row }">
                <el-tag type="info">
                  {{ row.remindType }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="领取规则">
              <template #default="{ row }">
                <div v-if="row.receiveRule">
                  每人限领：{{ parseRule(row.receiveRule).limitPerPerson }} 次
                </div>
              </template>
            </el-table-column>

            <el-table-column label="使用规则">
              <template #default="{ row }">
                <div v-if="row.consumeRule">
                  满 {{ parseRule(row.consumeRule).termsOfUse }} 元
                  减 {{ parseRule(row.consumeRule).maximumDiscountAmount }} 元
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="handleCancel(row)"
                  :disabled="isAllExpired(row.remindTime)">
                  取消
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { couponAPI } from '../api/coupon'
import dayjs from 'dayjs'

export default {
  data() {
    return {
      loading: false,
      remindList: [],
      flattenedRemindList: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await couponAPI.getCouponTemplateRemindList()
        if (res.code === '200') {
          this.remindList = res.data || []
          // 展平数据，为每个提醒时间创建独立的行
          this.flattenedRemindList = this.remindList.reduce((acc, coupon) => {
            const remindTimes = Array.isArray(coupon.remindTime) ? coupon.remindTime : [coupon.remindTime]
            const remindTypes = Array.isArray(coupon.remindType) ? coupon.remindType : [coupon.remindType]

            return acc.concat(
              remindTimes.map((time, index) => ({
                ...coupon,
                remindTime: time,
                remindType: remindTypes[index] || remindTypes[0]
              }))
            )
          }, [])
        } else {
          this.$message.error(res.message || '获取数据失败')
        }
      } catch (error) {
        this.$message.error('请求失败')
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    formatDate(date) {
      return dayjs(date).format('YYYY-MM-DD HH:mm')
    },
    parseRule(rule) {
      try {
        return JSON.parse(rule)
      } catch {
        return {}
      }
    },
    isExpired(time) {
      return dayjs(time).isBefore(dayjs())
    },
    isAllExpired(row) {
      return this.isExpired(row.remindTime)
    },
    async handleCancel(row) {
      try {
        await this.$confirm('确定要取消该提醒吗？', '提示', {
          type: 'warning'
        })

        // 计算提醒时间与开始时间的分钟差
        const startTime = dayjs(row.validStartTime)
        const remindTime = dayjs(row.remindTime)
        const minutesDiff = startTime.diff(remindTime, 'minute')

        // 转换提醒类型为数字
        const typeMap = {
          'App通知': 0,
          '短信提醒': 1
        }

        await couponAPI.cancelCouponTemplateRemind({
          shopNumber: row.shopNumber,
          couponTemplateId: row.id,
          remindTime: minutesDiff, // 传递分钟差
          type: typeMap[row.remindType] || 0 // 将文本类型转换为数字
        })

        this.$message.success('取消成功')
        this.fetchData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.message || '取消失败')
          console.error(error)
        }
      }
    }
  }
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
  /* 移除最大宽度限制 */
  width: 100%;
  margin: 0;
  position: relative;
  padding: 0;
}

/* 更新页头样式 */
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
/* 卡片头部样式更新 */
.main-card {
  border-radius: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-top: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.refresh-button {
  background: linear-gradient(135deg, #1a90ff, #1864ab);
  border: none;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.coupon-remind-container {
  padding: 20px;
  background: linear-gradient(120deg, #e0f2fe, #f0f9ff);
  min-height: calc(100vh - 40px);
}

.el-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 5px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #e6f4ff;
  padding-bottom: 15px;
}

.card-header h2 {
  background: linear-gradient(45deg, #1677ff, #4096ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
}

.el-button--primary {
  background: linear-gradient(45deg, #1677ff, #4096ff);
  border: none;
  padding: 12px 24px;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(22, 119, 255, 0.2);
}

.el-table {
  margin-top: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background: #fafafa !important;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  padding: 16px 12px;
}

:deep(.el-table td) {
  padding: 16px 12px;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #f8fafc;
}

.remind-time-tag {
  margin: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.remind-time-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.el-tag {
  margin: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 500;
}

.el-button--danger {
  background: linear-gradient(45deg, #ff4d4f, #ff7875);
  border: none;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.el-button--danger:hover:not([disabled]) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.2);
}

.el-button--danger[disabled] {
  background: #f5f5f5;
  color: #00000040;
  border: 1px solid #d9d9d9;
}

.el-empty {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 48px 0;
  margin: 20px 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.el-table-column {
  animation: fadeIn 0.3s ease-out forwards;
}
</style>
