<template>
  <div class="post-detail">
    <!--    <van-nav-bar-->
    <!--      title="文章详情"-->
    <!--      left-arrow-->
    <!--      @click-left="$router.back()"-->
    <!--    />-->
    <div class="post-header">
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="author-info">
        <van-image
            round
            width="32px"
            height="32px"
            :src="post.authorAvatar"
        />
        <span class="author-name">{{ post.authorName }}</span>
        <van-button
            type="primary"
            size="mini"
            @click="handleFollow(post.userId)"
        >
          关注
        </van-button>
      </div>
    </div>
    <div class="post-content" v-html="post.content"></div>
    <div class="post-footer">
      <div class="interaction-buttons">
        <div class="like-button" @click="handleLike(post.id, currentUserId)">
          <van-icon :name="isLiked ? 'like' : 'like-o'" size="20"/>
          <!--          <span>{{ post.thumbNum || 0 }}</span>-->
        </div>
        <div class="comment-button" @click="handleComment(post.id)">
          <van-icon name="comment-o" size="20"/>
          <!--          <span>{{ post.commentNum || 0 }}</span>-->
        </div>
        <div class="collect-button" @click="handleCollect(post.id, currentUserId)">
          <van-icon :name="isCollect ? 'star':'star-o'" size="20"/>
          <!--          <span>{{ post.favourNum || 0 }}</span>-->
        </div>
      </div>


      <transition name="fade">
        <div class="comment-input" v-if="showCommentInput">
          <van-field
              v-model="commentContent"
              type="textarea"
              placeholder="理性发言，友善互动，给文章回复"
              rows="3"
              autosize
          />
          <van-button type="primary" @click="submitComment">发布</van-button>
        </div>
      </transition>
    </div>
    <div class="comments-section" v-if="showComments">
      <div v-for="comment in parentComments" :key="comment.id" class="comment-item">
        <div class="comment-content">
          <van-image round width="32px" height="32px" :src="comment.userAvatar"/>
          <span class="author-name">{{ comment.userName }}</span>
          <p>{{ comment.commentContent }}</p>
          <span class="comment-time">{{ comment.createTime }}</span>
        </div>
        <div class="comment-actions">
          <van-button type="primary" size="mini" @click="handleReply(comment.id)">回复</van-button>
        </div>
        <div v-if="showChildComments[comment.id]" class="child-comments">
          <div v-for="childComment in childComments[comment.id]" :key="childComment.id" class="child-comment-item">
            <van-image round width="24px" height="24px" :src="childComment.userAvatar"/>
            <span class="author-name">
              {{ childComment.userName }}
              <span v-if="childComment.replyUserName" class="reply-arrow">→</span>
              <van-image round width="24px" height="24px" :src="childComment.replyUserAvatar"/>
              {{ childComment.replyUserName }}
            </span>
            <p>{{ childComment.commentContent }}</p>
            <span class="comment-time">{{ childComment.createTime }}</span>
            <van-button type="primary" size="mini" @click="handleReply(childComment.id)">回复</van-button>
          </div>

          <van-button
              v-if="comment.hasChildComment === 1"
              type="info"
              size="mini"
              @click="toggleChildComments(comment.id)"
          >
            {{ showChildComments[comment.id] ? '收起' : '展开' }}评论
          </van-button>
        </div>

        <van-button
            v-else-if="comment.hasChildComment === 1"
            type="info"
            size="mini"
            @click="toggleChildComments(comment.id)"
        >
          {{ showChildComments[comment.id] ? '收起' : '展开' }}评论
        </van-button>
      </div>
    </div>
    <van-dialog
        v-model:show="isReplyDialogVisible"
        title="回复评论"
        show-cancel-button
        @confirm="sendReply"
        @cancel="isReplyDialogVisible = false"
    >
      <van-field
          v-model="replyContent"
          type="textarea"
          placeholder="请输入回复内容"
          rows="3"
          autosize
      />
    </van-dialog>
  </div>


  <!-- 新增：推荐文章区域   todo 不能删，之后会进行优化 -->
  <!--  <div v-if="recommendedPosts.length > 0">-->
  <!--    <h2>为您推荐</h2>-->
  <!--    <div v-for="post in recommendedPosts" :key="post.id" class="post-detail">-->
  <!--      -->
  <!--        <div class="post-header">-->
  <!--          <h1 class="post-title">{{ post.title }}</h1>-->
  <!--          <div class="author-info">-->
  <!--            <van-image-->
  <!--                round-->
  <!--                width="32px"-->
  <!--                height="32px"-->
  <!--                :src="post.authorAvatar"-->
  <!--            />-->
  <!--            <span class="author-name">{{ post.authorName }}</span>-->
  <!--          </div>-->
  <!--        </div>-->
  <!--        &lt;!&ndash; 新增：关注按钮 &ndash;&gt;-->
  <!--        <van-button-->
  <!--            type="primary"-->
  <!--            size="mini"-->
  <!--            @click="handleFollow(post.userId)"-->
  <!--        >-->
  <!--          关注-->
  <!--        </van-button>-->
  <!--        <div class="post-content" v-html="post.content"></div>-->
  <!--        <div class="post-footer">-->
  <!--          <div class="interaction-buttons">-->
  <!--            <div class="like-button" @click="handleLike(post.id, currentUserId)">-->
  <!--              <van-icon name="like-o" size="20"/>-->
  <!--              <span>{{ post.thumbNum || 0 }}</span>-->
  <!--            </div>-->
  <!--            <div class="comment-button" @click="handleComment(post.id)">-->
  <!--              <van-icon name="comment-o" size="20"/>-->
  <!--              <span>{{ post.commentNum || 0 }}</span>-->
  <!--            </div>-->
  <!--            <div class="collect-button" @click="handleCollect(post.id, currentUserId)">-->
  <!--              <van-icon name="star-o" size="20"/>-->
  <!--              <span>{{ post.favourNum || 0 }}</span>-->
  <!--            </div>-->
  <!--          </div>-->
  <!--        </div>-->
  <!--        <div class="comments-section" v-if="showComments">-->
  <!--          <div v-for="comment in parentComments" :key="comment.id" class="comment-item">-->
  <!--            <div class="comment-content">-->
  <!--              <van-image round width="32px" height="32px" :src="comment.userAvatar"/>-->
  <!--              <span class="author-name">{{ comment.userName }}</span>-->
  <!--              <p>{{ comment.commentContent }}</p>-->
  <!--              <span class="comment-time">{{ comment.createTime }}</span>-->
  <!--            </div>-->
  <!--            <div class="comment-actions">-->
  <!--              <van-button type="primary" size="mini" @click="handleReply(comment.id)">回复</van-button>-->
  <!--              <van-button type="info" size="mini" @click="toggleChildComments(comment.id)">-->
  <!--                {{ showChildComments[comment.id] ? '收起' : '展开' }}评论-->
  <!--              </van-button>-->
  <!--            </div>-->
  <!--            <div v-if="showChildComments[comment.id]" class="child-comments">-->
  <!--              <div v-for="childComment in childComments[comment.id]" :key="childComment.id" class="child-comment-item">-->
  <!--                <van-image round width="24px" height="24px" :src="childComment.userAvatar"/>-->
  <!--                <span class="author-name">-->
  <!--              {{ childComment.userName }}-->
  <!--              <span v-if="childComment.replyUserName" class="reply-arrow"></span>-->
  <!--              <van-image round width="24px" height="24px" :src="childComment.replyUserAvatar"/>-->
  <!--              {{ childComment.replyUserName }}-->
  <!--            </span>-->
  <!--                <p>{{ childComment.commentContent }}</p>-->
  <!--                <span class="comment-time">{{ childComment.createTime }}</span>-->
  <!--                <van-button type="primary" size="mini" @click="handleReply(childComment.id)">回复</van-button>-->
  <!--              </div>-->
  <!--            </div>-->
  <!--          </div>-->
  <!--        </div>-->

  <!--        &lt;!&ndash; 新增：回复弹窗 &ndash;&gt;-->
  <!--        <van-dialog-->
  <!--            v-model:show="isReplyDialogVisible"-->
  <!--            title="回复评论"-->
  <!--            show-cancel-button-->
  <!--            @confirm="sendReply"-->
  <!--            @cancel="isReplyDialogVisible = false"-->
  <!--        >-->
  <!--          <van-field-->
  <!--              v-model="replyContent"-->
  <!--              type="textarea"-->
  <!--              placeholder="请输入回复内容"-->
  <!--              rows="3"-->
  <!--              autosize-->
  <!--          />-->
  <!--        </van-dialog>-->


  <!--    </div>-->


  <!--  </div>-->
  <!--  <div v-if="recommendedPosts.length > 0" class="recommended-posts">-->
  <!--    <h2>为你推荐</h2>-->
  <!--    <div v-for="recommendedPost in recommendedPosts" :key="recommendedPost.id" class="recommended-post-item">-->
  <!--      <h3>{{ recommendedPost.title }}</h3>-->
  <!--      <div v-html="recommendedPost.content"></div>-->
  <!--    </div>-->
  <!--  </div>-->
</template>

<script setup lang="ts">
import {ref, onMounted, UnwrapRef, nextTick, onUnmounted} from 'vue';
import {useRoute} from 'vue-router';
import myAxios from "../plugins/myAxios";
import PostVo = API.PostVo;
import CommentVo = API.CommentVo;
import {Dialog, Toast, Field} from 'vant';
import {getCurrentUserState} from '../states/user';

const route = useRoute();
const postId = route.params.id as string;
const post = ref<PostVo>({});
const currentUserId = ref<number>(0);
const showComments = ref(false);
const parentComments = ref<CommentVo[]>([]);
const childComments = ref<Record<number, CommentVo[]>>({});
const showChildComments = ref<Record<number, boolean>>({});
const recommendedPosts = ref<PostVo[]>([]);
const tags = ref<string[]>([]);
const viewedIds = ref<number[]>([]);
const isLoading = ref(false);
const hasMore = ref(true);
const isLiked = ref(false);
const isCollect = ref(false);
onMounted(async () => {
  const currentUser = getCurrentUserState();
  if (currentUser) {
    currentUserId.value = currentUser.id;
  }
  //console.log('PostDetail 组件已挂载');
  if (!postId) {
    console.error('路由参数 postId 为空');
    return;
  }

  if (typeof postId !== 'string' || postId.trim() === '') {
    console.error('路由参数 postId 格式不正确');
    return;
  }

  try {
    //console.log(`正在获取文章详情，postId: ${postId}`);
    const response = await myAxios.get(`/post/${postId}`);
    if (response && response.data) {
      post.value = response.data;
      if (post.value.tags) {
        tags.value = post.value.tags;
      }
      viewedIds.value = [parseInt(postId)];
    } else {
      console.error('获取文章详情失败: 响应数据为空');
    }
  } catch (error) {
    console.error('获取文章详情失败:', error);
    alert('加载文章详情失败，请稍后重试');
  }

  // loadRecommendedPosts();
});


const loadRecommendedPosts = async () => {
  if (isLoading.value || !hasMore.value) return;
  isLoading.value = true;
  Toast.loading({
    message: '加载中...',
    forbidClick: true,
  });
  try {
    // todo 私人推荐请求使用userid=11访问postId=7可以拿到
    const response = await myAxios.post('/post/private/recommend/' + currentUserId.value, {
      tags: tags.value,
      viewedIds: viewedIds.value
    });
    //console.log('推荐接口响应:', response);
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        recommendedPosts.value = [...recommendedPosts.value, ...response.data];
        hasMore.value = response.data.length > 0;
        //console.log('推荐文章加载成功:', recommendedPosts.value);
      } else {
        console.error('推荐接口返回的数据格式不正确:', response.data);
        Toast.fail('推荐数据格式错误');
      }
    } else {
      console.error('推荐接口返回的数据为空:', response);
      Toast.fail('推荐数据为空');
    }
  } catch (error) {
    console.error('加载推荐文章失败:', error);
    Toast.fail('加载失败，请稍后重试');
  } finally {
    isLoading.value = false;
    Toast.clear();
  }
};

const debounce = (func: Function, wait: number) => {
  let timeout: ReturnType<typeof setTimeout> | null = null;
  return function (this: any, ...args: any[]) {
    if (timeout !== null) {
      clearTimeout(timeout);
    }
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
};

onUnmounted(() => {
  //console.log('滚动事件监听器已移除');
});

const handleLike = async (postId: number | undefined, userId: number) => {
  try {
    const response = await myAxios.post('/PostThumb/', {postId, userId});
    if (response.data === 1) {
      isLiked.value = true;
      Toast.success('点赞成功');
    } else if (response.data === -1) {
      isLiked.value = false;
      Toast.fail('取消点赞成功');
    }
  } catch (error) {
    Toast.fail('点赞失败');
  }
};

const showCommentInput = ref(false);
const commentContent = ref('');

const handleComment = async (postId: number | undefined) => {
  if (!postId) return;
  showComments.value = !showComments.value;
  showCommentInput.value = !showCommentInput.value;
  if (showComments.value) {
    try {
      const response = await myAxios.post('/comment/listParent', {postId});
      if (response && response.data) {
        parentComments.value = response.data.records;
      }
    } catch (error) {
      console.error('获取父评论失败:', error);
    }
  }
};

const commentCurrentMap = ref<Record<number, number>>({});

const toggleChildComments = async (parentId: UnwrapRef<API.CommentVo["id"]> | undefined) => {
  if (!parentId) {
    console.error('父评论 ID 无效');
    return;
  }

  const validParentCommentId = Number(parentId);
  if (isNaN(validParentCommentId)) {
    console.error('父评论 ID 转换失败');
    return;
  }

  if (showChildComments.value[validParentCommentId]) {
    showChildComments.value[validParentCommentId] = false;
    commentCurrentMap.value[validParentCommentId] = 1;
    return;
  }

  const current = commentCurrentMap.value[validParentCommentId] || 1;

  try {
    const response = await myAxios.post('/comment/listChildren', {
      parentId: validParentCommentId,
      current: current
    });

    if (response && response.data) {
      const existingChildComments = childComments.value[validParentCommentId] || [];
      const newChildComments = response.data.records.filter(
          (newComment: CommentVo) =>
              !existingChildComments.some(
                  (existingComment: CommentVo) => existingComment.id === newComment.id
              )
      );

      childComments.value[validParentCommentId] = [
        ...existingChildComments,
        ...newChildComments,
      ];
      commentCurrentMap.value[validParentCommentId] = current + 1;
      showChildComments.value[validParentCommentId] = true;
      // todo 为什么childComments.value[validParentCommentId].length=3都能进if语句？这里太抽象了
      if (childComments.value[validParentCommentId].length <= 2) {
        //console.log("开始检查："+typeof childComments.value[validParentCommentId].length);
        //console.log("开始检查："+childComments.value[validParentCommentId].length);
        showChildComments.value[validParentCommentId] = true;
      }
    }
  } catch (error) {
    console.error('获取子评论失败:', error);
  }
};

const isReplyDialogVisible = ref(false);
const replyContent = ref('');
const currentReplyCommentId = ref<number | undefined>(undefined);

const handleReply = (commentId: UnwrapRef<CommentVo["id"]> | undefined) => {
  //console.log('handleReply triggered with commentId:', commentId);
  if (!commentId) {
    console.error('评论 ID 无效');
    return;
  }
  currentReplyCommentId.value = commentId;
  isReplyDialogVisible.value = true;
  replyContent.value = '';
};

const sendReply = async () => {
  if (!currentReplyCommentId.value || !replyContent.value) {
    Toast.fail('回复内容不能为空');
    return;
  }

  try {
    const repliedComment = parentComments.value.find(comment => comment.id === currentReplyCommentId.value) ||
        Object.values(childComments.value).flat().find(comment => comment.id === currentReplyCommentId.value);

    if (!repliedComment || !repliedComment.id) {
      Toast.fail('未找到被回复的评论');
      return;
    }

    const response = await myAxios.post('/comment/save', {
      postId: postId,
      parentId: currentReplyCommentId.value,
      commentContent: replyContent.value,
      replyUserId: repliedComment.userId,
      userId: currentUserId.value,
      topCommentId: repliedComment.topCommentId
    });

    if (response && response.data) {
      Toast.success('回复成功');
      isReplyDialogVisible.value = false;
      const newComment = {
        ...response.data,
        replyUserName: repliedComment.userName,
        replyUserAvatar: repliedComment.userAvatar
      };
      if (repliedComment.parentId) {
        childComments.value[repliedComment.parentId] = childComments.value[repliedComment.parentId] || [];
        childComments.value[repliedComment.parentId].push(newComment);
      } else {
        childComments.value[repliedComment.id] = childComments.value[repliedComment.id] || [];
        childComments.value[repliedComment.id].push(newComment);
      }
    }
  } catch (error) {
    console.error('回复失败:', error);
    Toast.fail('回复失败，请稍后重试');
  }
};

const submitComment = async () => {
  if (!commentContent.value) {
    Toast.fail('评论内容不能为空');
    return;
  }

  try {
    const response = await myAxios.post('/comment/save', {
      postId: postId,
      commentContent: commentContent.value,
      userId: currentUserId.value
    });

    if (response && response.data) {
      Toast.success('评论成功');
      // handleComment(post.value.id);
      commentContent.value = '';
      showCommentInput.value = false;
    }
  } catch (error) {
    console.error('评论失败:', error);
    Toast.fail('评论失败，请稍后重试');
  }
};

const handleCollect = async (postId: number | undefined, userId: number) => {
  try {
    const response = await myAxios.post('/postfavour/', {postId, userId});
    Toast.success('收藏成功');
    if (response.data === 1) {
      isCollect.value = true;
    } else if (response.data === -1) {
      Toast.success('取消收藏成功');
      isCollect.value = false;
    }
    // post.value.favourNum = (post.value.favourNum || 0) + 1;
  } catch (error) {
    Toast.fail('收藏失败，请稍后重试');
  }
};

const handleFollow = async (authorId: number | undefined) => {
  if (!authorId) {
    console.error('作者 ID 无效');
    return;
  }
  try {
    const response = await myAxios.get(`/follow/user/${authorId}`);
    if (response.data === 1) {
      Toast.success('关注成功');
    }
    if (response.data === -1) {
      Toast.fail('取消关注成功');
    }
  } catch (error) {
    Toast.fail('关注失败，请稍后重试');
  }
};

</script>

<style scoped>
.post-detail {
  padding: 16px;
  background-color: #fff;
  overflow: auto;
}

.post-header {
  margin-bottom: 16px;
}

.post-title {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.author-info {
  display: flex;
  align-items: center;
}

.author-name {
  font-size: 14px;
  color: #6d757a;
  margin-left: 8px;
}

.post-content {
  font-size: 16px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 24px;
}

.post-footer {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.interaction-buttons {
  display: flex;
  justify-content: space-between;
}

.like-button,
.collect-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #6d757a;
}

.like-button span,
.collect-button span {
  margin-left: 8px;
}

.comments-section {
  margin-top: 16px;
}

.comment-item {
  margin-bottom: 16px;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
}

.comment-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 8px;
}

.comment-content p {
  margin-left: 0;
  margin-top: 8px;
}

.comment-time {
  font-size: 12px;
  color: #6d757a;
  margin-top: 4px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.child-comments {
  margin-top: 8px;
  padding-left: 16px;
  border-left: 2px solid #f0f0f0;
}

.child-comment-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 8px;
}

.child-comment-item p {
  margin-left: 0;
  margin-top: 8px;
}

.reply-arrow {
  margin: 0 4px;
  color: #6d757a;
}

.comment-button {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #6d757a;
}

.comment-button span {
  margin-left: 8px;
}


.recommended-posts {
  margin-top: 24px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.recommended-posts h2 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.recommended-post-item {
  margin-bottom: 16px;
  padding: 8px;
  background-color: #fff;
  border-radius: 4px;
}

.recommended-post-item h3 {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.loading-indicator {
  text-align: center;
  padding: 16px;
  color: #6d757a;
}


.van-dialog {
  border-radius: 8px;
}

.van-dialog__header {
  background-color: #f0f0f0;
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.van-dialog__content {
  padding: 16px;
}

.van-field__control {
  font-size: 14px;
  color: #333;
}

.van-dialog__footer {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid #e0e0e0;
}

.van-button--primary {
  background-color: #1989fa;
  border-color: #1989fa;
}

.van-button--default {
  color: #333;
  background-color: #fff;
  border-color: #e0e0e0;
}


.comment-input {
  display: flex;
  flex-direction: column;
  margin-top: 16px;
}

.comment-input .van-field {
  padding: 8px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.comment-input .van-button {
  margin-top: 8px;
  align-self: flex-end;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style>