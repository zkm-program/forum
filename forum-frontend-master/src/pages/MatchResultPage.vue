<template>
  <user-card-list :user-list="userList" :loading="loading"/>
  <van-empty v-if="!userList || userList.length < 1" description="没有匹配的用户" />
  <van-empty v-if="status === 3" description="没有历史记录" />
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {useRoute} from "vue-router";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import qs from 'qs';
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const {userList: userListStr} = route.query;

const userList = ref([]);
const loading = ref(true);

onMounted(async () => {
  const parsedUserList = JSON.parse(userListStr as string);
  if (parsedUserList) {
    userList.value = parsedUserList;
    loading.value = false;
  } else {
    Toast.fail('用户数据解析失败');
  }
});
</script>

<style scoped>

</style>
