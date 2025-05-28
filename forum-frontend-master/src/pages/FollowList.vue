<template>
  <div class="follow-list">
    <h2>关注的用户</h2>
    <div v-for="user in followList" :key="user.id" class="user-item">
      <van-image round width="48px" height="48px" :src="user.authorAvatar" />
      <div class="user-info">
        <div class="user-name">{{ user.authorName }}</div>
        <div class="user-intro">{{ user.introduction }}</div>
<!--        <van-button type="primary" block @click="" style="height: 35px; width: 100px; margin-top: 8px;">已关注</van-button>-->
      </div>
      <div class="follow-button-container">
        <van-button class="van-button" type="primary" block @click="" style="height: 35px; width: 100px;">已关注</van-button>
      </div>
    </div>
    <van-empty v-if="!followList || followList.length < 1" description="没有关注的用户" />
  </div>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router";
import { ref, onMounted } from "vue";

const route = useRoute();
const followList = ref([]);

onMounted(() => {
  const queryFollowList = route.query.followList;
  if (queryFollowList && typeof queryFollowList === "string") {
    try {
      followList.value = JSON.parse(queryFollowList);
    } catch (error) {
      console.error("解析 followList 失败:", error);
    }
  }
});
</script>

<style scoped>
.follow-list {
  padding: 16px;
}

.user-item {
  display: grid;
  grid-template-rows: auto auto auto;
  grid-template-columns: 48px 1fr;
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: center;
  margin-bottom: 16px;
  gap: 8px;
}

.user-name {
  font-size: 16px;
}

.user-intro {
  font-size: 14px;
  color: #6d757a;
  //grid-column: 2 / 3;
}

.follow-button-container {
  display: flex;
  align-items: flex-start;
  margin-left: 8px;
}
  .follow-button-container .van-button {
    margin-top: 8px;
  }
</style>
