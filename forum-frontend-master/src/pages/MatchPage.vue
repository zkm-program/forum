<template>
  <form action="/">
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入要搜索的标签"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>
  <van-divider content-position="left">已选标签</van-divider>
  <div v-if="activeIds.length === 0">请选择标签</div>
  <van-row gutter="16" style="padding: 0 16px">
    <van-col v-for="tag in activeIds">
      <van-tag closeable size="small" type="primary" @close="doClose(tag)">
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>
  <van-divider content-position="left">选择标签</van-divider>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
  />
  <div style="padding: 12px">
    <van-row gutter="16">
      <van-col span="8">
        <van-button block type="primary" @click="doSearchResult">匹配</van-button>
      </van-col>
      <van-col span="8">
        <van-button block type="info" @click="setMyTags">设置自己标签</van-button>
      </van-col>
      <van-col span="8">
        <van-button block type="warning" @click="superMatch">超级匹配</van-button>
      </van-col>
    </van-row>
  </div>
  <van-popup v-model:show="showSuperMatchPopup" round position="bottom" :style="{ height: '50%' }">
    <div class="popup-content">
      <h3>最契合</h3>
      <div v-if="superMatchResult">
        <p>用户名: {{ superMatchResult.userName }}</p>
        <p>性别: {{ superMatchResult.gender }}</p>
        <p>标签: {{ superMatchResult.tags.join(', ') }}</p>
        <p>简介: {{ superMatchResult.introduction }}</p>
        <p>联系方式: {{ superMatchResult.userQqEmail }}</p>
      </div>
      <div v-else>
        <p>暂无匹配结果</p>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";
import qs from 'qs';
import {Toast} from "vant";

const router = useRouter()

const searchText = ref('');

const originTagList = ref([]);

const tagList = ref<any[]>([]);

onMounted(async () => {
  try {
    const response = await myAxios.get('/tag/listTagsForAdminVo');
    const formattedTagList = response.data.map(parentTag => ({
      text: parentTag.tageName,
      children: parentTag.list.map(childTag => ({
        text: childTag.tageName,
        id: childTag.tageName
      }))
    }));
    tagList.value = formattedTagList;
    originTagList.value = JSON.parse(JSON.stringify(formattedTagList));
    //console.log('原始标签列表已初始化:', originTagList.value);
  } catch (error) {
    console.error('Failed to fetch tags:', error);
  }
});



interface ParentTag {
  text: string;
  children: { text: string; id: string }[];
}

const onSearch = (val: string) => {
  //console.log('onSearch triggered with value:', val);

  if (!searchText.value) {
    console.warn('Search text is empty, skipping filter operation.');
    return;
  }

  tagList.value = originTagList.value.map((parentTag: ParentTag) => {
    const tempChildren = [...parentTag.children];
    const tempParentTag = { ...parentTag };
    tempParentTag.children = tempChildren.filter(item => item.text.includes(searchText.value));
    return tempParentTag;
  });

  const matchedTag = tagList.value.find(parentTag => 
    parentTag.children.some(child => child.text.includes(searchText.value))
  );

  if (matchedTag) {
    const index = tagList.value.indexOf(matchedTag);
    if (index !== -1) {
      activeIndex.value = index;
    }
  }
}


const onCancel = () => {
  //console.log('onCancel triggered');
  searchText.value = '';
  tagList.value = JSON.parse(JSON.stringify(originTagList.value));
  //console.log('标签列表已重置:', tagList.value);
};

const activeIds = ref([]);
const activeIndex = ref(0);

const doClose = (tag) => {
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag;
  })
}


const doSearchResult = async () => {
  try {
    if (!Array.isArray(activeIds.value)) {
      console.error('activeIds is not an array:', activeIds.value);
      return;
    }
    const userListData = await myAxios.get('/user/matchuser/bytags', {
      params: {
        tagList: activeIds.value
      },


      paramsSerializer: params => {

        return qs.stringify(params, {indices: false})
      }
    }).then(function (response) {
      if(response.code === 0&& response.data){
        Toast.success({
          message: "匹配成功，前往我的页面查看" + response.message,
          duration: 8000
        });
      } else if(response.code !== 0){
        Toast.fail(response.message);
      }

      const userList = response?.data || [];
      // router.push({
      //   path: '/matchResult/list',
      //   query: {
      //     userList: JSON.stringify(userList)
      //   }
      // });

      return response?.data;
    })
        .catch(function (error) {
          console.error('/user/matchuser/bytags error', error);
          Toast.fail({
            message: error.description,
            duration: 2000
          });
        });
  } catch (error) {
    console.error('Error during matching users:', error);
  }
};


const setMyTags = async () => {
  try {
    if (!Array.isArray(activeIds.value)) {
      console.error('activeIds is not an array:', activeIds.value);
      return;
    }
    const userListData = await myAxios.get('/user/updateMyTas', {
      params: {
        tagList: activeIds.value
      },


      paramsSerializer: params => {

        return qs.stringify(params, {indices: false})
      }
    }).then(function (response) {
      //console.log('/user/updateMyTas succeed', response);

      Toast.success({
        message: response.message,
        duration: 2000
      });
      const userList = response?.data || [];
      return response?.data;
    })
        .catch(function (error) {
          console.error('/user/updateMyTas error', error);
          Toast.fail({
            message: error.description,
            duration: 2000
          });
        });
  } catch (error) {
    console.error('Error during updateMyTas:', error);
  }
};


const showSuperMatchPopup = ref(false);
const superMatchResult = ref(null);

const superMatch = async () => {
  try {
    const response = await myAxios.get('/user/super/match');
    //console.log('/super/match succeed', response);

    if (response?.data&&response.code===0) {
      Toast.success({
        message: response.message,
        duration: 8000
      });
    } else {
      Toast.fail(response.message);
    }
  } catch (error) {
    console.error('/super/match error', error);
    Toast.fail({
      message: error.description || '超级匹配失败',
      duration: 2000
    });
  }
};
</script>

<style scoped>

.van-row {
  margin-bottom: 16px;
}


.popup-content {
  padding: 20px;
  text-align: center;
}

.popup-content h3 {
  margin-bottom: 16px;
}

.popup-content p {
  margin: 8px 0;
}
</style>