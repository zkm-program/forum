<template>
  <div class="goal-progress">
    <h3>目标进度</h3>
    <van-progress :percentage="percentage" :status="status" />
    <p>{{ progressText }}</p>
  </div>
</template>

<script setup>
import { defineProps, computed } from 'vue';

const props = defineProps({
  percentage: {
    type: Number,
    required: true
  },
  status: {
    type: String,
    default: 'success'
  }
});

const progressText = computed(() => {
  return `已达成目标的${props.percentage}%`;
});
</script>

<style scoped>
.goal-progress {
  margin-bottom: 20px;
}
</style>