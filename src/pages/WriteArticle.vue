
<template>
  <div class="write-container">
    <van-field
        v-model="title"
        label="标题"
        placeholder="请输入标题"
        class="title-input"
    />
    <van-field
        v-model="content"
        label="内容"
        type="textarea"
        placeholder="请输入文章内容"
        rows="10"
        autosize
        class="content-input"
    />
    <van-button
        type="default"
        block
        @click="fetchTags"
        style="margin: 16px 0;"
    >
      选择标签
    </van-button>
    <van-button
        type="primary"
        block
        @click="submitArticle"
        class="submit-btn"
        style="margin-top: 16px;"
    >
      发布文章
    </van-button>
    <van-popup
        v-model:show="showTagPopup"
        position="bottom"
        :style="{ height: '50%' }"
    >
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
          :items="tagItems"
      />

      <van-row gutter="16" style="margin-top: 16px;">
        <van-col span="12">
          <van-button block type="primary" @click="onTagConfirm">确定</van-button>
        </van-col>
        <van-col span="12">
          <van-button block type="default" @click="showTagPopup = false">取消</van-button>
        </van-col>
      </van-row>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import myAxios from '../plugins/myAxios'
import { Toast } from 'vant'
import { getCurrentUserState } from "../states/user";

const router = useRouter()
const title = ref('')
const content = ref('')
const tags = ref('')
const showTagPopup = ref(false)
const tagItems = ref<any[]>([])
const activeIds = ref<string[]>([])
const activeIndex = ref<number>(0)

const currentUserId = ref<number>(0);

onMounted(async () => {
  const currentUser = getCurrentUserState();
  if (currentUser) {
    currentUserId.value = currentUser.id;
  }
});

const fetchTags = async () => {
  try {
    const response = await myAxios.get('/tag/listTagsForAdminVo');
    //console.log("这是打印后端传过来的标签数据", response.data)
    const formattedTagList = response.data.map(parentTag => ({
      text: parentTag.tageName,
      children: parentTag.list.map(childTag => ({
        text: childTag.tageName,
        id: childTag.tageName
      }))
    }));
    tagItems.value = formattedTagList;
    showTagPopup.value = true
  } catch (error) {
    Toast.fail('获取标签失败')
    console.error(error)
  }
}

const doClose = (tag) => {
  activeIds.value = activeIds.value.filter(item => {
    return item !== tag;
  })
}

const onTagConfirm = () => {
  tags.value = activeIds.value
  showTagPopup.value = false
}

const submitArticle = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    Toast.fail('标题和内容不能为空')
    return
  }

  try {
    await myAxios.post('/post/add', {
      title: title.value,
      content: content.value,
      tags: tags.value
    })

    Toast.success('发布成功')
    router.push('/')
  } catch (error) {
    Toast.fail('发布失败，请检查网络')
    console.error(error)
  }
}
</script>

<style scoped>
.write-container {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 50px);
}

.title-input {
  flex: 0 0 50px;
}

.content-input {
  flex: 1;
  margin-top: 16px;
  min-height: 200px;
  overflow-y: auto;
}

.submit-btn {
  margin-top: auto;
  margin-bottom: 16px;
}
</style>
