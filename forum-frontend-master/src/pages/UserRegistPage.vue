<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userAccount"
          name="userQqEmail"
          label="QQ邮箱"
          placeholder="请输入邮箱"
          :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
          v-model="userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
      <van-field
          v-model="checkPassword"
          type="password"
          name="checkPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[{ required: true, message: '请再次填写密码' }]"
      />
      <van-field
          v-model="userName"
          name="userName"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
          v-model="gender"
          name="gender"
          label="性别"
          placeholder="请输入性别（男/女）"
          :rules="[{ required: true, message: '请填写性别' }]"
      />
<!--      <van-field name="userAvatar" label="头像">-->
<!--        <template #input>-->
<!--          <van-uploader :after-read="onUpload" />-->
<!--        </template>-->
<!--      </van-field>-->
      <van-field
          v-model="code"
          name="code"
          label="验证码"
          placeholder="请输入验证码"
          :rules="[{ required: true, message: '请填写验证码' }]"
      >
        <template #button>
          <van-button size="small" type="primary" @click="getCode">获取验证码</van-button>
        </template>
      </van-field>
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        注册
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round block type="primary" @click="gotologin">
        去登录
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref, onMounted} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";

const router = useRouter();
const route = useRoute();

const userAccount = ref('');
const userPassword = ref('');
const checkPassword = ref('');
const inviterId = ref('');
const userName = ref('');
const gender = ref('');
const code = ref('');
// const userAvatar = ref('');
// const onUpload = async (file: any) => {
//   const formData = new FormData();
//   formData.append('multipartFile', file.file);
//   try {
//     const res = await myAxios.post('/user/upload', formData, {
//       headers: {
//         'Content-Type': 'multipart/form-data',
//       },
//     });
//     if (res.code === 0 && res.data) {
//       userAvatar.value = res.data;
//       Toast.success('头像上传成功');
//     } else {
//       Toast.fail(res.description);
//     }
//   } catch (error) {
//     Toast.fail('头像上传失败');
//   }
// };

const getCode = async () => {
  if (!userAccount.value) {
    Toast.fail('请先填写邮箱');
    return;
  }
  const res = await myAxios.get(`/user/sendcode/${userAccount.value}`);
  if (res.code === 0) {
    Toast.success('验证码已发送');
  } else {
    Toast.fail(res.message);
  }
};

onMounted(() => {
  inviterId.value = route.query.inviterId as string || '';
});

const onSubmit = async () => {
  const res = await myAxios.post('/user/register', {
    userQqEmail: userAccount.value,
    userPassword: userPassword.value,
    checkPassword: checkPassword.value,
    inviterId: inviterId.value,
    userName: userName.value,
    gender: gender.value,
    userCode: code.value,
    // userAvatar: userAvatar.value,
  });
  //console.log(res, '用户注册');

  if (res.code === 0 && res.data) {
    Toast.success('注册成功');
    router.push({
      path: '/user/login',
      replace: true,
    });
  } else {
    Toast.fail(res.message);
  }
};
const gotologin = () => {
  router.push({
    path: '/user/login',
    replace: true,
  });
};
</script>

<style scoped>
</style>