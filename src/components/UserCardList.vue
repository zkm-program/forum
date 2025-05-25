<template>
  <van-skeleton title avatar :row="3" :loading="props.loading" v-for="user in props.userList">
    <van-card
        :title="`姓名：${user.userName}（${user.userQqEmail}）`"
        :thumb="user.userAvatar"
    >
      <template #title>
        <div>姓名：{{user.userName}}</div>
        <div>联系方式：{{user.userQqEmail}}</div>
      </template>
      <template #tags>
        <van-tag plain type="danger" v-for="tag in user.tags" style="margin-right: 8px; margin-top: 8px">
          {{ tag }}
        </van-tag>
      </template>
      <template #desc>
        <div>性别：{{user.gender}}</div>
        <div>个人介绍：{{user.introduction}}</div>
        <div>匹配类型：{{ user.kind === 0 ? '基本匹配' : '超级匹配' }}</div>
      </template>
      <template #footer>
        <van-button size="mini" type="primary" @click="copyContact(user.userQqEmail)">
          复制联系方式
        </van-button>
      </template>

    </van-card>
  </van-skeleton>
</template>

<script setup lang="ts">
// todo 点进帖子详情页，加载私人推荐变成异步，不然影响用户体验
import {UserType} from "../models/user";
import {Toast} from "vant";

interface UserCardListProps {
  loading: boolean;
  userList: UserType[];
}

const props = withDefaults(defineProps<UserCardListProps>(), {
  loading: true,
  // @ts-ignore
  userList: [] as UserType[],
});
：复制联系方式的方法
const copyContact = async (contact: string) => {
  try {
    await navigator.clipboard.writeText(contact);
    Toast.success('联系方式已复制');
  } catch (error) {
    console.error('复制失败:', error);
    Toast.fail('复制失败，请重试');
  }
};
</script>

<style scoped>

</style>
