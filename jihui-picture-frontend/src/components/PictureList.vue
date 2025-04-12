<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card
            hoverable
            @click="doClickPicture(picture)"
            @mouseover="hoveredPicture = picture.id"
            @mouseleave="hoveredPicture = null"
          >
            <template #cover>
              <div class="image-wrapper">
                <img
                  class="picture-image"
                  :class="{ 'image-darken': hoveredPicture === picture.id }"
                  :alt="picture.name"
                  :src="picture.thumbnailUrl ?? picture.url"
                />
                <transition name="fade">
                  <div
                    v-if="hoveredPicture === picture.id"
                    class="hover-overlay"
                  ></div>
                </transition>
                <transition name="slide-down">
                  <a-button
                    v-if="hoveredPicture === picture.id"
                    class="download-btn"
                    type="primary"
                    size="small"
                    @click.stop="onDownload(picture.url)"
                  >
                    立即下载
                  </a-button>
                </transition>
              </div>
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template v-if="showOp" #actions>
              <ShareAltOutlined @click="(e) => doShare(picture, e)" />
              <SearchOutlined @click="(e) => doSearch(picture, e)" />
              <EditOutlined v-if="canEdit" @click="(e) => doEdit(picture, e)" />
              <DeleteOutlined v-if="canDelete" @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  ShareAltOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { reactive, ref, watch } from 'vue'
import { downloadImage,initializeTagColors } from '@/utils'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

interface Props {
  dataList?: API.PictureVO[]
  tagList?: string[]
  loading?: boolean
  showOp?: boolean
  canEdit?: boolean
  canDelete?: boolean
  onReload?: () => void
  showFavorite?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  tagList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
  showFavorite: true, // 默认显示收藏按钮
})

const tagColors = reactive<Record<string, string>>({})
const hoveredPicture = ref<string | null>(null)


const router = useRouter()
// 跳转至图片详情页
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

// 搜索
const doSearch = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  // 跳转时一定要携带 spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除数据
const doDelete = async (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

// ----- 分享操作 ----
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
// 分享
const doShare = (picture, e) => {
  // 阻止冒泡
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

watch(
  () => props.tagList,
  (newTagList) => {
    Object.assign(tagColors, initializeTagColors(newTagList))
  },
  { immediate: true },
)

const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const onDownload = (url: string) => {
  if (!loginUser) {
    router.push('/user/login')
    return
  }
  downloadImage(url)
}

</script>

<style scoped>
/* 图片容器 - 添加相对定位 */
.image-wrapper {
  position: relative;
  overflow: hidden;
}

/* 默认图片样式 */
.picture-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: all 0.3s ease;
}

/* 鼠标悬停时变暗 */
.image-darken {
  filter: brightness(0.7);
}

/* 悬停时的黑色遮罩层 */
.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1;
}

/* 下载按钮样式 */
.download-btn {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  font-size: 12px;
  padding: 4px 8px;
  background-color: rgba(0, 0, 0, 0.7);
  border: none;
  transition: all 0.3s ease;
}

.download-btn:hover {
  background-color: rgba(0, 0, 0, 0.9);
}

/* 动画效果 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active, .slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter, .slide-down-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
