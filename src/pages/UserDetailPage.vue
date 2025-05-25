<template>
  <div class="user-detail-page">
    <van-image
      round
      :src="user.userAvatar"
      width="80"
      height="80"
      style="margin-bottom: 16px;"
    />
    <van-icon v-if="user.gender === 'female'" name="female" color="#ff5252" size="16" />
    <van-icon v-else-if="user.gender === 'male'" name="male" color="#409eff" size="16" />
    <h2>{{ user.userName }}</h2>

    <span @click="showMoreOptions" class="more-options">更多&gt;</span>
    <van-button type="primary" @click="followUser" v-if="!isFollowing">+ 关注</van-button>
    <van-button type="default" @click="unfollowUser" v-else>已关注</van-button>
    <van-button type="info" @click="sendMessage">发私信</van-button>
    <div class="user-stats">
      <div>
        <p>{{ user.likeCount }}</p>
        <span>获赞</span>
      </div>
      <div>
        <p>{{ user.followersCount }}</p>
        <span>被关注</span>
      </div>
      <div>
        <p>{{ user.followingCount }}</p>
        <span>关注</span>
      </div>
    </div>
    <van-tabs v-model:active="activeTab">
      <van-tab title="创作">
        <div v-for="(item, index) in user.creations" :key="index">
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
        </div>
      </van-tab>
      <van-tab title="动态">
        <div v-for="(item, index) in user.activities" :key="index" class="dynamic-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
        </div>
      </van-tab>
      <van-tab title="赞同">
        <div v-for="(item, index) in user.likes" :key="index">
          <h3>{{ item.title }}</h3>
          <p>{{ item.content }}</p>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import myAxios from "../plugins/myAxios";
import { Toast } from "vant";

const user = ref({
  userName: "泡泡鱼",
  userAvatar: "https://placehold.co/80x80",
  gender: "female",
  likeCount: 26000,
  followersCount: 5449,
  followingCount: 167,
  creations: [],
  activities: [],
  likes: []
});

const isFollowing = ref(false);
const activeTab = ref(0);

onMounted(async () => {
  const userInfo = await fetchUserInfo();
  if (userInfo) {
    user.value = userInfo;
  }
});
const fetchUserInfo = async () => {
  try {
    const response = await myAxios.get("/user/detail");
    return response.data;
  } catch (error) {
    console.error("获取用户信息失败:", error);
    Toast.fail("获取用户信息失败");
    return null;
  }
};

const followUser = async () => {
  try {
    const response = await myAxios.post("/user/follow");
    if (response.code === 0) {
      Toast.success("关注成功");
      isFollowing.value = true;
    } else {
      Toast.fail("关注失败");
    }
  } catch (error) {
    console.error("关注用户失败:", error);
    Toast.fail("关注失败");
  }
};

const unfollowUser = async () => {
  try {
    const response = await myAxios.post("/user/unfollow");
    if (response.code === 0) {
      Toast.success("取消关注成功");
      isFollowing.value = false;
    } else {
      Toast.fail("取消关注失败");
    }
  } catch (error) {
    console.error("取消关注用户失败:", error);
    Toast.fail("取消关注失败");
  }
};

const sendMessage = () => {
  //  todo 实现发送私信逻辑
  Toast.info("功能暂未实现");
};

const showMoreOptions = () => {
  Toast.info("功能暂未实现");
};
</script>

<style scoped>
.user-detail-page {
  padding: 16px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
}

.user-stats div {
  text-align: center;
}

.user-stats p {
  font-size: 18px;
  margin: 0;
}

.user-stats span {
  font-size: 14px;
  color: #666;
}


.more-options {
  cursor: pointer;
  color: #409eff;
  margin-top: 8px;
}


.van-tabs__nav {
  background-color: transparent;
}

.van-tab--active {
  color: #fff;
  font-weight: bold;
}

.van-tabs__line {
  background-color: #409eff;
}


.dynamic-content {
  margin-top: 16px;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
}

.dynamic-content h3 {
  font-size: 18px;
  margin-bottom: 8px;
}

.dynamic-content p {
  font-size: 14px;
  color: #666;
}
</style>
