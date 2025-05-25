<template>
  <template v-if="user">
    <van-cell title="修改基本信息" is-link @click="navigateToUpdatePage"/>
    <van-cell title="收藏的帖子" is-link @click="fetchMyPostFavour"/>
    --    <van-cell title="关注的问题" is-link to="/user/team/join" />-->
    <van-cell title="关注的用户" is-link @click="fetchUserFollowList"/>
    <van-cell title="匹配到的用户" is-link @click="fetchMatchResult"/>
    <van-cell title="我的标签" is-link @click="fetchMyTags"/>
       <van-cell title="回复" is-link to="/user/team/join" />-->
       <van-field :value="user.createTime" label="匹配次数" readonly />-->
    <!--    <van-field :value="user.createTime" label="超级匹配次数" readonly />-->
    <!--    <van-field :value="user.createTime" label="健身会员到期日" readonly />-->
    <!--    <van-field :value="user.createTime" label="注册时间" readonly />-->
    <van-cell title="退出登录" @click="logout"/>
  </template>
  <div style="margin: 16px;" class="login" v-if="!user">
    <van-button round block type="primary" @click="login">
      登录
    </van-button>
    <van-button round block type="primary" @click="regist">
      注册
    </van-button>
  </div>
  <van-popup v-model:show="showTagPopup" position="bottom" :style="{ height: '50%' }">
    <div class="popup-content">
      <van-tag
        v-for="(tag, index) in myTags"
        :key="index"
        type="primary"
        class="tag-item"
      >
        {{ tag }}
      </van-tag>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {getCurrentUserState} from "../states/user";

const logout = async () => {
  const res = await myAxios.post("/user/logout");
  if (res.code === 0) {
    Toast.success("退出成功");
  } else {
    Toast.fail("退出失败");
  }
};

const login = async () => {
  router.push({
    path: "/user/login",
    replace: true,
  });
};
const regist = async () => {
  router.push({
    path: "/user/regist",
    replace: true,
  });
};

const user = ref();

const router = useRouter();


onMounted(async () => {
  user.value = await getCurrentUserState();
});

const navigateToUpdatePage = () => {
  if (user.value) {
    router.push({
      path: "/user/update",
      query: {
        userInfo: JSON.stringify(user.value),
      },
    });
  } else {
    Toast.fail("无法获取用户信息");
  }
};

const fetchMyPostFavour = async () => {
  try {
    const response = await myAxios.get("/postfavour/listMyPostFavour");
    if (response.code === 0 && response.data) {
      Toast.success("获取收藏的帖子成功");
      router.push({
        path: "/myPostFavour",
        query: {
          postList: JSON.stringify(response.data),
        },
      });
    } else {
      Toast.fail("获取收藏的帖子失败");
    }
  } catch (error) {
    console.error("请求失败:", error);
    Toast.fail("网络错误，请稍后重试");
  }
};
const fetchUserFollowList = async () => {
  try {
    const response = await myAxios.get("/follow/listUserFollowVo");
    if (response.code === 0 && response.data) {
      //console.log("用户关注列表:", response.data);
      router.push({
        path: "/followList",
        query: {
          followList: JSON.stringify(response.data),
        },
      });
    } else {
      Toast.fail("获取关注列表失败");
    }
  } catch (error) {
    console.error("请求失败:", error);
    Toast.fail("网络错误，请稍后重试");
  }
};

const fetchMatchResult = async () => {
  try {
    const response = await myAxios.get("/matchTags/get");
    if (response.code === 0 && response.data) {
      //console.log("匹配结果:", response.data);
      router.push({
        path: "/matchResult/list",
        query: {
          userList: JSON.stringify(response.data),
        },
      });
    } else {
      Toast.fail("获取匹配结果失败");
    }
  } catch (error) {
    console.error("请求失败:", error);
    Toast.fail("网络错误，请稍后重试");
  }
};

const showTagPopup = ref(false);
const myTags = ref<string[]>([]);

const fetchMyTags = async () => {
  try {
    const response = await myAxios.get("/user/checkMyTags");
    if (response.code === 0 && response.data.length > 0) {
      myTags.value = response.data;
      showTagPopup.value = true;
    } else if (response.code === 0 && response.data.length === 0) {
      Toast.fail("您还没有设置自己标签");
    } else {
      Toast.fail("获取标签失败");
    }
  } catch (error) {
    console.error("请求失败:", error);
    Toast.fail("网络错误，请稍后重试");
  }
};

</script>

<style scoped>
.tag-item {
  margin: 8px;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 14px;
  background-color: #4a90e2;
  color: white;
}
</style>