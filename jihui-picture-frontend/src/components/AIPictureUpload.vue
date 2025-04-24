<template>
  <div class="ai-picture-upload">
    <div v-if="!resultImageUrl">
      <a-textarea
        v-model:value="prompt"
        placeholder="请描述你想生成图片内容"
        :rows="4"
      />
      <div class="btn-wrapper">
        <a-button
          type="primary"
          style="width: 120px"
          :loading="loading"
          :disabled="loading"
          @click="createTask"
        >
          {{ loading ? '生成中...' : '生成图片' }}
        </a-button>
      </div>
    </div>
    <div class="img-wrapper" v-if="resultImageUrl">
      <img :src="resultImageUrl" alt="AI生成图片" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onUnmounted, ref, toRaw } from 'vue'
import { message } from 'ant-design-vue'
import {
  aiGeneratePaintingTaskUsingPost,
  getAiGeneratePictureTaskUsingGet, uploadPictureByUrlUsingPost
} from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}
const props = defineProps<Props>()

const prompt = ref<string>('')
const loading = ref<boolean>(false)
const resultImageUrl = ref<string>('')
const taskId = ref<string>()

// 轮询定时器
let pollingTimer: NodeJS.Timeout | null = null

/**
 * 创建任务
 */
const createTask = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入图片描述')
    return
  }

  loading.value = true
  try {
    const params = {
      prompt: prompt.value,
      parameters: {
        n: 1,
        size: '1024x1024'
      }
    }

    const res = await aiGeneratePaintingTaskUsingPost(params)
    const rawData = toRaw(res.data)

    if (rawData.code === 0 && rawData.data) {
      message.success('创建任务成功，请耐心等待')
      taskId.value = rawData.data.output.taskId
      startPolling()
    } else {
      message.error('任务创建失败: ' + (rawData.message || '未知错误'))
    }
  } catch (error) {
    console.error('任务创建异常:', error)
    message.error('任务创建异常: ' + error.message)
  }
  // 注意：不要在成功后立刻关闭 loading，要在 polling 完成后处理
}

/**
 * 开始轮询任务状态
 */
const startPolling = () => {
  if (!taskId.value) return
  clearPolling()

  pollingTimer = setInterval(async () => {
    try {
      const res = await getAiGeneratePictureTaskUsingGet({ taskId: taskId.value })
      const rawData = toRaw(res.data)

      if (rawData.code === 0 && rawData.data) {
        const taskStatus = rawData.data.output.taskStatus
        const results = rawData.data.output.results

        if (taskStatus === 'SUCCEEDED' && results?.length) {
          message.success('图片生成成功')
          resultImageUrl.value = results[0].url
          try {
            const params: API.PictureUploadRequest = { fileUrl: results[0].url }
            params.spaceId = props.spaceId;
            const res = await uploadPictureByUrlUsingPost(params)
            if (res.data.code === 0 && res.data.data) {
              //message.success('图片上传成功')
              // 将上传成功的图片信息传递给父组件
              props.onSuccess?.(res.data.data)
            } else {
              //message.error('图片上传失败，' + res.data.message)
            }
          } catch (error) {
            //console.error('图片上传失败', error)
            //message.error('图片上传失败，' + error.message)
          }
          clearPolling()
          loading.value = false
        } else if (taskStatus === 'FAILED') {
          message.error('图片生成失败')
          clearPolling()
          loading.value = false
        }
      }
    } catch (error) {
      console.error('轮询异常:', error)
      message.error('查询任务状态失败')
      clearPolling()
      loading.value = false
    }
  }, 3000)
}

/**
 * 清除轮询
 */
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 卸载时清除轮询
onUnmounted(() => {
  clearPolling()
})
</script>

<style scoped>
.ai-picture-upload {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.btn-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.img-wrapper {
  margin-top: 16px;
}

.img-wrapper img {
  max-width: 100%;
  border-radius: 4px;
}
</style>
