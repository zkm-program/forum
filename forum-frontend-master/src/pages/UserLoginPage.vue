<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="userQqEmail"
          name="userQqEmail"
          label="账号"
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
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        登录
      </van-button>
    </div>
    <div style="margin: 16px;">
      <van-button round block type="primary" @click="gotoregist">
        没有账号，去注册
      </van-button>
    </div>
  </van-form>

</template>

<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {
  loginUsingPost,
} from "../api/yonghumokuai";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUser} from "../services/user";


const router = useRouter();
const route = useRoute();

const userQqEmail = ref('');
const userPassword = ref('');


const onSubmit=loginUsingPost(userQqEmail.value,userPassword.value)
const onSubmit = async () => {
  const res = await myAxios.post('/user/login', {
    userQqEmail: userQqEmail.value,
    userPassword: userPassword.value,
  })
  //console.log(res, '用户登录');
  if (res.code === 0 && res.data) {
    Toast.success('登录成功');
    const redirectUrl = route.query?.redirect as string ?? '/';
    window.location.href = redirectUrl;
  } else {
    Toast.fail('登录失败');
  }
};

const gotoregist = () => {
  router.push({
    path: '/user/regist',
    replace: true,
  });

}
</script>

<style scoped>

</style>
