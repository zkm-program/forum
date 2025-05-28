<script setup lang="ts">
import PostCardList from "../components/PostCardList.vue";
import { useRoute } from "vue-router";
import { ref, onMounted } from "vue";

const route = useRoute();
const postList = ref([]);
onMounted(() => {
  const queryPostList = route.query.postList;
  if (queryPostList && typeof queryPostList === "string") {
    try {
      postList.value = JSON.parse(queryPostList);
    } catch (error) {
      console.error("解析 postList 失败:", error);
    }
  }
});
</script>

<template>
  <post-card-list :post-list="postList" :loading="false"/>
  <van-empty v-if="!postList || postList.length < 1" description="没有收藏的帖子" />
</template>

<style scoped>

</style>