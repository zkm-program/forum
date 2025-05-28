<template>
  <div class="search-page">
    <van-search
        v-model="searchText"
        placeholder="请输入要搜索的内容"
        show-action
        @search="onSearch"
        @cancel="$router.back()"
    />

    <div v-if="searchHistory.length > 0" class="search-history">
      <h3>历史搜索</h3>
      <div class="history-item" v-for="(item, index) in searchHistory" :key="index" @click="onHistoryClick(item)">
        {{ item }}
      </div>
    </div>

    <div v-if="hotSearch.length > 0" class="hot-search">
      <h3>猜你想搜</h3>
      <div class="hot-item" v-for="(item, index) in hotSearch" :key="index" @click="onHotClick(item)">
        {{ item }}
      </div>
    </div>

    <div v-if="searchDiscover.length > 0" class="search-discover">
      <h3>搜索发现</h3>
      <div class="discover-item" v-for="(item, index) in searchDiscover" :key="index">
        {{ item }}
      </div>
    </div>

    <div v-if="searchResults.length > 0">
      <p>共 {{ searchResults.length }} 条结果</p>
      <post-card-list
        :post-list="searchResults"
        @item-click="handlePostClick"
        :current-user-id="currentUserId"
        :loading="loading"
      />
      <div class="pagination">
        <van-button icon="arrow-left" @click="prevPage" :disabled="current === 1">上一页</van-button>
        <van-button icon="arrow" @click="nextPage" :disabled="searchResults.length < 4">下一页</van-button>
      </div>
    </div>
    <van-empty v-else description="暂无搜索结果" />
  </div>
</template>


<script setup lang="ts">
 todo 从PostDetail返回到Search页面时，恢复搜索结果没有解决，获取热门搜索和搜索发现，历史搜索没有解决
import {onMounted, ref, watch} from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Toast } from 'vant';
import myAxios from '../plugins/myAxios';
import PostCardList from '../components/PostCardList.vue';
import { getCurrentUserState } from '../states/user';

const router = useRouter();
const route = useRoute();
const loading = ref(true);
const currentUser = getCurrentUserState();
const currentUserId = currentUser?.id;
const searchText = ref<string>('');
const searchResults = ref<any[]>([]);
const searchHistory = ref<string[]>([]);
const hotSearch = ref<string[]>([]);
const searchDiscover = ref<string[]>([]);
const current = ref(1);
watch(
    () => route.query.q,
    (newQuery) => {
      if (newQuery) {
        searchText.value = newQuery as string;
        onSearch();
      }
    },
    { immediate: true }
);

const onSearch = async () => {
  loading.value = true;
  if (!searchText.value.trim()) {
    Toast('请输入搜索内容');
    return;
  }

  try {
    const response = await myAxios.post('/post/searchEsPost', {
      keyWords: searchText.value,
      current: current.value
    });
    console.log('搜索接口返回数据:', response.data);

    searchResults.value = response.data.records;
    localStorage.setItem('searchResults', JSON.stringify(searchResults.value));
    loading.value = false;
    const newHistory = [searchText.value, ...searchHistory.value.filter(item => item !== searchText.value)];
    searchHistory.value = newHistory.slice(0, 5);
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value));
  } catch (error) {
    console.error('搜索失败', error);
    Toast('搜索失败，请稍后再试');
  }
};

const nextPage = async () => {
  if (searchResults.value.length < 4) {
    return;
  }
  current.value += 1;
  await onSearch();
};

const prevPage = async () => {
  if (current.value > 1) {
    current.value -= 1;
    await onSearch();
  }
};

watch(
  () => route.path,
  (newPath) => {
    if (newPath !== '/search') {
      current.value = 1;
    }
  },
  { immediate: true }
);

 const fetchHotAndDiscover = async () => {
   try {
    const response = await myAxios.get('/hot-search');
    hotSearch.value = response.data.hotSearch;
//     searchDiscover.value = response.data.searchDiscover;
//   } catch (error) {
//     console.error('获取热门搜索和搜索发现失败', error);
//   }
// };

const restoreSearchResults = () => {
  const savedResults = localStorage.getItem('searchResults');
  if (savedResults) {
    searchResults.value = JSON.parse(savedResults);
  }
};

const restoreSearchHistory = () => {
  const savedHistory = localStorage.getItem('searchHistory');
  if (savedHistory) {
    searchHistory.value = JSON.parse(savedHistory);
  }
};

watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/search') {
      restoreSearchResults();
      restoreSearchHistory();
      loading.value = false;
    }
  },
  { immediate: true }
);

const onHistoryClick = (item: string) => {
  searchText.value = item;
  onSearch();
};

// const onHotClick = (item: string) => {
//   searchText.value = item;
//   onSearch();
// };

const handlePostClick = (post: any) => {
  router.push({
    name: 'PostDetail',
    params: { postId: post.id }
  });
};

</script>


<style scoped>
.search-page {
  padding: 10px;
}

.search-history, .hot-search, .search-discover {
  margin-top: 10px;
}

.search-history h3, .hot-search h3, .search-discover h3 {
  font-size: 16px;
  margin-bottom: 5px;
}

.history-item, .hot-item, .discover-item {
  display: inline-block;
  margin: 5px;
  padding: 5px 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  cursor: pointer;
}

.history-item:hover, .hot-item:hover, .discover-item:hover {
  background-color: #f0f0f0;
}


.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pagination .van-button {
  margin: 0 10px;
}
</style>
