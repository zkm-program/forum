<template>
  <div v-if="props.loading">
    <van-skeleton title avatar :row="3" v-for="i in 3" :key="i"/>
  </div>
  <div v-else>
    <van-card
        v-for="post in props.postList"
        :key="post.id"
        @click="handleCardClick(post.id)"
    >
      <template #title>
        <div style="font-size: 20px; color: #6d757a; margin-bottom: 10px;">{{ post.title }}</div>
      </template>

      <template #desc>
        <div style="display: flex; align-items: center; margin-bottom: 0px;">
          <van-image
              round
              width="30px"
              height="30px"
              :src="post.authorAvatar"
              style="margin-right: 8px;"
          />
          <span style="font-size: 12px; color: #1a1a1a;">{{ post.authorName }}</span>
        </div>
      </template>
      <template #price>
        <div v-if="post.original_url" style="font-size: 15px; color: #6d757a; margin-top: 5px;margin-bottom: 5px;">
          {{ post.original_url }}
        </div>
        <div v-else style="font-size: 14px; color: #6d757a; margin-top: 8px;">
          暂无摘要
        </div>
      </template>

      <template #tags>
        <van-tag plain type="primary" v-for="tag in post.tags" :key="tag"
                 style="font-size: 10px;margin-right: 8px; margin-top: 8px; border-radius: 16px; background-color: #f0f2f5; color: #1e80ff;">
          {{ tag }}
        </van-tag>
      </template>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center;" @click.stop="handleLike(post.id, props.currentUserId)">
            <span style="font-size: 14px; color: #6d757a;">{{ post.thumbNum }}点赞</span>
          </div>
          <div style="display: flex; align-items: center;" @click.stop="handleCollect(post.id, props.currentUserId)">
            <span style="font-size: 14px; color: #6d757a;">{{ post.favourNum }}收藏</span>
          </div>
        </div>
      </template>
    </van-card>
  </div>
</template>

<script setup lang="ts">
import {UserType} from "../models/user";
import PostVo = API.PostVo;
import axios from 'axios';
import {Toast} from "vant"
import {useRoute, useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";

interface PostCardListProps {
  loading: boolean;
  postList: PostVo[];
  currentUserId: number;
}

const router = useRouter();
const route = useRoute();
const props = withDefaults(defineProps<PostCardListProps>(), {
  loading: true,
  // @ts-ignore
  postList: [] as PostVo[],
  currentUserId: 0,
});


//console.log('PostCardList props:', props);


// todo 用户点赞收藏过后高亮显示和个数显示

// const handleLike = async (postId: number | undefined, userId: number) => {
//   try {
//     const response = await myAxios.post('/PostThumb/', {postId, userId});
//     console.log('点赞成功:', response.data);

//   } catch (error) {
//     console.error('点赞失败:', error);

//   }
// };

// 收藏按钮点击事件处理函数
// const handleCollect = async (postId: number | undefined, userId: number) => {
//   try {
//     const response = await myAxios.post('/postfavour/', {postId, userId});
//     console.log('收藏成功:', response.data);
//     // 可以在这里更新 UI 或提示用户
//   } catch (error) {
//     console.error('收藏失败:', error);
//     // 处理错误，例如提示用户
//   }
// };
const handleCardClick = async (postId: number | undefined) => {
  //console.log("跳转文章详情页")
  if (!postId) {
    console.error('文章ID无效');
    return;
  }

  // 跳转到文章详情页，将 postId 作为路由参数传递
  router.push({
    name: 'PostDetail', // 确保这里的名称与路由配置中的名称一致
    params: {id: postId},
    // replace: true,
  });
};

</script>

<style scoped>
.van-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.van-card__title {
  font-size: 18px;
  font-weight: bold;
  color: #1a1a1a;
}
.van-card__desc {
  font-size: 14px;
  color: #6d757a;
  margin-top: 8px;
}
.van-tag {
  font-size: 12px;
  padding: 4px 8px;
}

.van-button {
  font-size: 14px;
  margin-top: 16px;
}
</style>