<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { postApi, type CreatePostReq } from '@/api/post'
import { resourceApi, type UploadResourceReq } from '@/api/resource'
import { categoryApi, type CategoryItem } from '@/api/category'

const router = useRouter()

const activeTab = ref('post')
const postCategories = ref<CategoryItem[]>([])
const resourceCategories = ref<CategoryItem[]>([])
const loadingPostCat = ref(false)
const loadingResourceCat = ref(false)

const postForm = ref<CreatePostReq>({
  title: '',
  content: '',
  categoryId: 0,
})
const posting = ref(false)

const resourceForm = ref<UploadResourceReq>({
  title: '',
  description: '',
  categoryId: 0,
  fileUrl: '',
  tags: '',
})
const uploading = ref(false)

async function fetchPostCategories() {
  loadingPostCat.value = true
  try {
    postCategories.value = await categoryApi.list()
  } finally {
    loadingPostCat.value = false
  }
}

async function fetchResourceCategories() {
  loadingResourceCat.value = true
  try {
    resourceCategories.value = await categoryApi.resourceList()
  } finally {
    loadingResourceCat.value = false
  }
}

async function submitPost() {
  if (!postForm.value.title.trim()) {
    ElMessage.warning('请输入帖子标题')
    return
  }
  if (!postForm.value.content.trim()) {
    ElMessage.warning('请输入帖子内容')
    return
  }
  if (!postForm.value.categoryId) {
    ElMessage.warning('请选择帖子分类')
    return
  }
  posting.value = true
  try {
    const id = await postApi.create({
      title: postForm.value.title.trim(),
      content: postForm.value.content.trim(),
      categoryId: postForm.value.categoryId,
    })
    ElMessage.success('帖子发布成功')
    router.push({ name: 'forum-detail', params: { id } })
  } catch {
    ElMessage.error('帖子发布失败，请稍后重试')
  } finally {
    posting.value = false
  }
}

async function submitResource() {
  if (!resourceForm.value.title.trim()) {
    ElMessage.warning('请输入资源标题')
    return
  }
  if (!resourceForm.value.categoryId) {
    ElMessage.warning('请选择资源分类')
    return
  }
  if (!resourceForm.value.fileUrl.trim()) {
    ElMessage.warning('请输入资源地址')
    return
  }
  uploading.value = true
  try {
    const id = await resourceApi.upload({
      title: resourceForm.value.title.trim(),
      description: resourceForm.value.description?.trim() || '',
      categoryId: resourceForm.value.categoryId,
      fileUrl: resourceForm.value.fileUrl.trim(),
      tags: resourceForm.value.tags?.trim() || '',
    })
    ElMessage.success('资源上传成功')
    router.push({ name: 'resource-detail', params: { id } })
  } catch {
    ElMessage.error('资源上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  fetchPostCategories()
  fetchResourceCategories()
})
</script>

<template>
  <div class="upload">
    <div class="header">
      <div class="title">上传中心</div>
      <div class="sub">发布帖子或上传学习资源，帮助更多同学</div>
    </div>

    <el-card class="card" shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="发布帖子" name="post">
          <el-form label-width="80">
            <el-form-item label="标题">
              <el-input
                v-model="postForm.title"
                placeholder="例如：2026 届暑期实习经验分享"
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select
                v-model="postForm.categoryId"
                placeholder="选择帖子分类"
                :loading="loadingPostCat"
              >
                <el-option
                  :key="0"
                  label="请选择分类"
                  :value="0"
                  disabled
                />
                <el-option
                  v-for="cat in postCategories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="postForm.content"
                type="textarea"
                :rows="6"
                placeholder="详细描述你的经历、踩坑与建议..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="posting" @click="submitPost">
                发布帖子
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="上传资源" name="resource">
          <el-form label-width="80">
            <el-form-item label="标题">
              <el-input
                v-model="resourceForm.title"
                placeholder="例如：数据结构期末复习笔记"
              />
            </el-form-item>
            <el-form-item label="分类">
              <el-select
                v-model="resourceForm.categoryId"
                placeholder="选择资源分类"
                :loading="loadingResourceCat"
              >
                <el-option
                    :key="0"
                    label="请选择资源分类"
                    :value="0"
                    disabled
                  />
                <el-option
                  v-for="cat in resourceCategories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="地址">
              <el-input
                v-model="resourceForm.fileUrl"
                placeholder="填入网盘链接、直链或存储路径"
              />
            </el-form-item>
            <el-form-item label="简介">
              <el-input
                v-model="resourceForm.description"
                type="textarea"
                :rows="4"
                placeholder="简单介绍资源内容与适用场景"
              />
            </el-form-item>
            <el-form-item label="标签">
              <el-input
                v-model="resourceForm.tags"
                placeholder="例如：数据结构, 期末, 复习"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="uploading" @click="submitResource">
                上传资源
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.upload {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.header {
  margin-bottom: 4px;
}

.title {
  font-size: var(--ccp-title-size);
  font-weight: var(--ccp-title-weight);
}

.sub {
  margin-top: 2px;
  font-size: var(--ccp-sub-size);
  color: var(--ccp-sub-color);
}

.card {
  border-radius: var(--ccp-card-radius);
}
</style>
