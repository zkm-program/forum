<template>
  <template v-if="user">
    <van-uploader :after-read="onAvatarUpload" max-count="1">
      <van-image
        :src="user.userAvatar"
        width="100"
        height="100"
        fit="cover"
        style="margin-bottom: 16px;"
      />
    </van-uploader>
    <van-field v-model="user.userName" label="昵称" placeholder="请输入昵称" />
    <van-field v-model="user.introduction" label="用户简介" placeholder="请输入简介" />
    <van-button type="primary" block @click="updateUserInfo">更新</van-button>
  </template>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from "vue-router";
import { onMounted, ref } from "vue";
import myAxios from "../plugins/myAxios";
import { Toast } from "vant";
import {getCurrentUser} from "../services/user";

const route = useRoute();
const user = ref();

onMounted(async () => {
  const userInfoStr = route.query.userInfo as string;
  if (userInfoStr) {
    try {
      user.value = JSON.parse(userInfoStr);
    } catch (error) {
      console.error('解析用户信息失败:', error);
      Toast.fail('解析用户信息失败');
    }
  } else {
    user.value = await getCurrentUser();
  }
});

const router = useRouter();
const onAvatarUpload = async (file) => {
  const formData = new FormData();
  formData.append('multipartFile', file.file);

  try {
    const res = await myAxios.post('/user/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (res.code === 0) {
      Toast.success('头像上传成功');
      user.value.userAvatar = res.data;
    } else {
      Toast.fail('头像上传失败');
    }
  } catch (error) {
    console.error('上传头像失败:', error);
    Toast.fail('上传头像失败');
  }
};

const updateUserInfo = async () => {
  if (!user.value) {
    Toast.fail('用户信息为空');
    return;
  }

  try {

    const userUpdateMyRequest = {
      id: user.value.id,
      userName: user.value.userName,
      userAvatar: user.value.userAvatar,
      introduction: user.value.introduction,
    };

    const res = await myAxios.post('/user/updatemy', userUpdateMyRequest);

    if (res.code === 0) {
      Toast.success('更新成功');
      router.push({
        path: '/user',
        query: {
          userInfo: JSON.stringify(user.value),
        },
      });
    } else {
      Toast.fail('更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    Toast.fail('更新失败');
  }
};
</script>

<style scoped>
</style>