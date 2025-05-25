
<template>
  <van-search
      v-model="searchText"
      placeholder="请输入要搜索的内容"
      @click="goToSearchPage"
  />


  <post-card-list :post-list="postList" :loading="loading" :current-user-id="currentUserId"/>
  <van-empty v-if="!postList || postList.length < 1" description="数据为空"/>
  <van-button class="add-button" type="primary" icon="plus" @click="showPopup = true" />
  <van-popup v-model:show="showPopup" position="bottom"  >
    <div class="popup-content">
      <van-cell title="写文章" @click="onWriteArticleClick" />
      <van-cell title="提问题" @click="onAskQuestionClick" />
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import {onMounted, ref, watchEffect} from 'vue';
import myAxios from "../plugins/myAxios";
import { Toast } from "vant";
import UserCardList from "../components/UserCardList.vue";
import { UserType } from "../models/user";
import { getCurrentUser } from "../services/user";
import BaseResponseListPostVo_ = API.BaseResponseListPostVo_;
import PostVo = API.PostVo;
import PostCardList from "../components/PostCardList.vue";
import { useRouter } from 'vue-router';
import {getCurrentUserState, setCurrentUserState} from "../states/user";

const isMatchMode = ref<boolean>(false);
const searchText = ref('');

const router = useRouter();

const postList = ref([]);
const loading = ref(true);
const currentUserId = ref<number>(0);

onMounted(async () => {
  const currentUser = await getCurrentUser();
  if (currentUser) {
    currentUserId.value = currentUser.id;
  }
});


const loadData = async () => {
  let postListData;
  loading.value = true;
  postListData = await myAxios.get('/post/logout/recommend')
      .then(function (response) {
        console.log('/post/public/login/recommend succeed', response);
        return response?.data;
      })
      .catch(function (error) {
        console.error('/post/logout/recommend error', error);
        Toast.fail('请求失败');
      });

  if (postListData) {
    postList.value = postListData;
  }
  loading.value = false;
};

watchEffect(() => {
  loadData();
});

const goToSearchPage = () => {
  router.push({ path: '/search' });
};

const showPopup = ref(false);

const onWriteArticleClick = () => {
  showPopup.value = false;
  router.push({ path: '/post/write-article' });
};

const onAskQuestionClick = () => {
  showPopup.value = false;
  Toast.fail('正在开发中...');
   router.push({ path: '/ask-question' });
};
</script>

<style scoped>
.add-button {
  position: fixed;
  bottom: 60px;
  right: 15px;
}
.popup-content {
  padding: 10px 0;
}
</style>
