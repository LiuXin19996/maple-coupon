<template>
  <div class="coupon-remind-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>优惠券提醒列表</h2>
          <el-button 
            type="primary" 
            size="small"
            @click="fetchData"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>
      </template>

      <el-empty v-if="!loading && remindList.length === 0" description="暂无提醒数据" />

      <div v-else>
        <el-table 
          :data="flattenedRemindList"
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="name" label="优惠券名称" width="200" />
          
          <el-table-column label="有效期" width="220">
            <template #default="{ row }">
              {{ formatDate(row.validStartTime) }} - {{ formatDate(row.validEndTime) }}
            </template>
          </el-table-column>

          <el-table-column label="提醒时间">
            <template #default="{ row }">
              <el-tag
                :type="isExpired(row.remindTime) ? 'danger' : 'success'"
                class="remind-time-tag"
              >
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
              <el-button
                type="danger"
                size="small"
                @click="handleCancel(row)"
                :disabled="isAllExpired(row.remindTime)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
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
.coupon-remind-container {
  padding: 20px;
  background: linear-gradient(120deg, #e0f2fe, #f0f9ff);
  min-height: calc(100vh - 40px);
}

.el-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 16px;
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.el-table-column {
  animation: fadeIn 0.3s ease-out forwards;
}
</style>
