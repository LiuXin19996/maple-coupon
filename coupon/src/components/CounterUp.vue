<template>
  <span>{{ displayValue }}</span>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  endValue: {
    type: Number,
    required: true
  },
  duration: {
    type: Number,
    default: 2000
  },
  decimals: {
    type: Number,
    default: 0
  }
})

const displayValue = ref(0)

const animate = (start, end, duration) => {
  const startTime = Date.now()
  const updateCounter = () => {
    const currentTime = Date.now()
    const progress = Math.min((currentTime - startTime) / duration, 1)
    
    // 使用 easeOutExpo 缓动函数使动画更流畅
    const easeProgress = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    displayValue.value = Number((start + (end - start) * easeProgress).toFixed(props.decimals))

    if (progress < 1) {
      requestAnimationFrame(updateCounter)
    }
  }
  
  updateCounter()
}

onMounted(() => {
  animate(0, props.endValue, props.duration)
})

// 监听 endValue 的变化以便重新执行动画
watch(() => props.endValue, (newVal, oldVal) => {
  animate(oldVal, newVal, props.duration)
})
</script>

<style scoped>
span {
  display: inline-block;
  min-width: 1.5em;
  text-align: center;
}
</style>
