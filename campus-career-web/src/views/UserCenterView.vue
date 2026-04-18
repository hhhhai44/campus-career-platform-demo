<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { postApi, type PostListItem } from '@/api/post'
import { resourceApi, type ResourceListItem } from '@/api/resource'
import { commentApi, type PostComment } from '@/api/comment'
import { resourceCommentApi, type ResourceComment } from '@/api/resourceComment'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const activeTab = ref('post')

const postLoading = ref(false)
const postPage = ref(1)
const postSize = ref(10)
const postTotal = ref(0)
const myPosts = ref<PostListItem[]>([])

const resourceLoading = ref(false)
const resourcePage = ref(1)
const resourceSize = ref(10)
const resourceTotal = ref(0)
const myResources = ref<ResourceListItem[]>([])

const postCommentLoading = ref(false)
const postCommentPage = ref(1)
const postCommentSize = ref(10)
const postCommentTotal = ref(0)
const myPostComments = ref<PostComment[]>([])

const resourceCommentLoading = ref(false)
const resourceCommentPage = ref(1)
const resourceCommentSize = ref(10)
const resourceCommentTotal = ref(0)
const myResourceComments = ref<ResourceComment[]>([])

const avatarText = computed(() => (auth.username?.slice(0, 1) || 'U').toUpperCase())

function formatTime(value?: string | null) {
  if (!value) return '--'
  return new Date(value).toLocaleString()
}

async function loadMyPosts() {
  postLoading.value = true
  try {
    const data = await postApi.myPage(postPage.value, postSize.value)
    myPosts.value = data.records || []
    postTotal.value = data.total || 0
  } catch {
    ElMessage.error('加载我的帖子失败')
  } finally {
    postLoading.value = false
  }
}

async function loadMyResources() {
  resourceLoading.value = true
  try {
    const data = await resourceApi.myPage(resourcePage.value, resourceSize.value)
    myResources.value = data.records || []
    resourceTotal.value = data.total || 0
  } catch {
    ElMessage.error('加载我的资源失败')
  } finally {
    resourceLoading.value = false
  }
}

async function loadMyPostComments() {
  postCommentLoading.value = true
  try {
    const data = await commentApi.myPage(postCommentPage.value, postCommentSize.value)
    myPostComments.value = data.records || []
    postCommentTotal.value = data.total || 0
  } catch {
    ElMessage.error('加载我的帖子评论失败')
  } finally {
    postCommentLoading.value = false
  }
}

async function loadMyResourceComments() {
  resourceCommentLoading.value = true
  try {
    const data = await resourceCommentApi.myPage(resourceCommentPage.value, resourceCommentSize.value)
    myResourceComments.value = data.records || []
    resourceCommentTotal.value = data.total || 0
  } catch {
    ElMessage.error('加载我的资源评论失败')
  } finally {
    resourceCommentLoading.value = false
  }
}

function onTabChange(name: string | number) {
  const tab = String(name)
  if (tab === 'post' && !myPosts.value.length) loadMyPosts()
  if (tab === 'resource' && !myResources.value.length) loadMyResources()
  if (tab === 'comment' && !myPostComments.value.length && !myResourceComments.value.length) {
    loadMyPostComments()
    loadMyResourceComments()
  }
}

onMounted(() => {
  loadMyPosts()
})
</script>

<template>
  <div class="user-center">
    <div class="header-card">
      <div class="avatar">{{ avatarText }}</div>
      <div class="meta">
        <div class="title">个人中心</div>
        <div class="desc">查看自己发布的帖子、资源和评论记录</div>
        <div class="username">当前用户: {{ auth.username || '--' }}</div>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="我的帖子" name="post">
          <el-table :data="myPosts" v-loading="postLoading" size="small">
            <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="likeCount" label="点赞" width="80" />
            <el-table-column prop="commentCount" label="评论" width="80" />
            <el-table-column label="发布时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pager" v-if="postTotal > postSize">
            <el-pagination
              v-model:current-page="postPage"
              v-model:page-size="postSize"
              :total="postTotal"
              layout="prev, pager, next"
              @current-change="loadMyPosts"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的资源" name="resource">
          <el-table :data="myResources" v-loading="resourceLoading" size="small">
            <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="scoreAvg" label="评分" width="90" />
            <el-table-column prop="favoriteCount" label="收藏" width="80" />
            <el-table-column label="发布时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div class="pager" v-if="resourceTotal > resourceSize">
            <el-pagination
              v-model:current-page="resourcePage"
              v-model:page-size="resourceSize"
              :total="resourceTotal"
              layout="prev, pager, next"
              @current-change="loadMyResources"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的评论" name="comment">
          <div class="comment-block">
            <div class="block-title">帖子评论</div>
            <el-table :data="myPostComments" v-loading="postCommentLoading" size="small">
              <el-table-column prop="postId" label="帖子ID" width="90" />
              <el-table-column prop="content" label="评论内容" min-width="320" show-overflow-tooltip />
              <el-table-column prop="likeCount" label="点赞" width="80" />
              <el-table-column label="时间" width="180">
                <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
              </el-table-column>
            </el-table>
            <div class="pager" v-if="postCommentTotal > postCommentSize">
              <el-pagination
                v-model:current-page="postCommentPage"
                v-model:page-size="postCommentSize"
                :total="postCommentTotal"
                layout="prev, pager, next"
                @current-change="loadMyPostComments"
              />
            </div>
          </div>

          <div class="comment-block">
            <div class="block-title">资源评论</div>
            <el-table :data="myResourceComments" v-loading="resourceCommentLoading" size="small">
              <el-table-column prop="resourceId" label="资源ID" width="90" />
              <el-table-column prop="content" label="评论内容" min-width="320" show-overflow-tooltip />
              <el-table-column prop="likeCount" label="点赞" width="80" />
              <el-table-column label="时间" width="180">
                <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
              </el-table-column>
            </el-table>
            <div class="pager" v-if="resourceCommentTotal > resourceCommentSize">
              <el-pagination
                v-model:current-page="resourceCommentPage"
                v-model:page-size="resourceCommentSize"
                :total="resourceCommentTotal"
                layout="prev, pager, next"
                @current-change="loadMyResourceComments"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.user-center {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header-card {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1890ff;
  color: #fff;
  font-weight: 700;
}

.meta .title {
  font-size: 18px;
  font-weight: 700;
}

.meta .desc,
.meta .username {
  margin-top: 2px;
  color: #6b7280;
  font-size: 13px;
}

.content-card {
  border-radius: 12px;
}

.comment-block {
  margin-bottom: 16px;
}

.block-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
}

.pager {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>

