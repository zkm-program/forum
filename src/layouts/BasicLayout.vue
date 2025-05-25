<template>
  <van-nav-bar
      :title="title"
      left-arrow
      @click-left="onClickLeft"
  >
    <template #right>
      <van-icon name="link-o" size="25" @click="handleAddClick"/>
    </template>
    <template #left v-if="isSpecialPage">
      <van-image
          v-if="user!=null"
          :src="user.userAvatar"
          width="24"
          height="24"
          class="rounded-avatar"
          @click="goToUserPage"
      />
      <span v-else>未登录</span>
    </template>
  </van-nav-bar>
  <div id="content">
    <router-view/>
  </div>
  <van-tabbar route @change="onChange">
    <van-tabbar-item to="/" icon="eye-o" name="index">帖子</van-tabbar-item>
    <van-tabbar-item to="/calories-page" icon="fire-o" name="user">卡路里</van-tabbar-item>
    <van-tabbar-item to="/match" icon="search" name="team">匹配</van-tabbar-item>
  </van-tabbar>
  <van-dialog v-model:show="showInviteDialog" title="邀请链接" show-cancel-button>
    <div class="invite-link-container">
      <p>{{ inviteLink }}</p>
      <van-button type="primary" @click="copyInviteLink">复制链接</van-button>
    </div>
  </van-dialog>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from "vue-router";
import { ref, computed, onMounted } from "vue";
import routes from "../config/route";
import { getCurrentUser } from "../services/user";
import axios from "axios";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";

const router = useRouter();
const route = useRoute();
const DEFAULT_TITLE = '守夜人';
const title = ref(DEFAULT_TITLE);
const user = ref({});

onMounted(async () => {
  user.value = await getCurrentUser();
});

const showInviteDialog = ref(false);
const inviteLink = ref('');
const handleAddClick = async () => {
  try {
    const response = await myAxios.get('/invite/getInviteLink');
    //console.log('请求成功:', response.data);
    inviteLink.value = response.data;
    showInviteDialog.value = true;
  } catch (error) {
    console.error('请求失败:', error);
    alert('获取邀请链接失败，请稍后再试');
  }
};

const copyInviteLink = () => {
  navigator.clipboard.writeText(inviteLink.value).then(() => {
    Toast.success('邀请链接已复制成功');
  }).catch((error) => {
    console.error('复制失败:', error);
    Toast.fail('复制失败，请重试');
  });
};


router.beforeEach((to, from) => {
  const toPath = to.path;
  const route = routes.find((route) => {
    return toPath == route.path;
  })
  title.value = route?.title ?? DEFAULT_TITLE;
})

const isSpecialPage = computed(() => {
  return ['/','/calories-page','/match'].includes(route.path) || route.path === '/user';
});

const onClickLeft = () => {
  if (isSpecialPage.value) {
    goToUserPage();
  } else {
    router.back();
  }
};
const goToUserPage = () => {
  //console.log('跳转到 UserPage');
  try {
    //console.log('当前路由:', route.path);
    //console.log('目标路由:', '/user');
    router.push('/user').then(() => {
      //console.log('跳转成功');
    }).catch((error) => {
      console.error('跳转失败:', error);
    });
  } catch (error) {
    console.error('路由跳转异常:', error);
  }
};

// const onClickRight = () => {
//   router.push('/search');
// };
</script>

<style scoped>
#content {
  padding-bottom: 50px;
}

.invite-link-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.rounded-avatar {
  border-radius: 50%;
}
</style>