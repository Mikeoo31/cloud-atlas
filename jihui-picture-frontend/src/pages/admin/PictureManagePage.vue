<template>
  <a-config-provider :locale="zhCN">
    <!-- 搜索管理 -->
    <a-space style="width: 100%; justify-content: space-between; margin-bottom: 16px;">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="关键词" name="searchText">
          <a-input
            v-model:value="searchParams.searchText"
            placeholder="从名称和简介搜索"
            allow-clear
            style="width: 240px;"
          />
        </a-form-item>
        <a-form-item label="类型" name="category">
          <a-select
            v-model:value="searchParams.category"
            placeholder="请输入类型"
            :options="categoryList"
            style="min-width: 150px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="searchParams.tags"
            placeholder="请输入标签"
            :options="tagList"
            mode="tags"
            style="min-width: 150px"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>

      <!-- 操作按钮 -->
      <a-space>
        <a-button type="primary" href="/edit_picture" target="_blank">+ 创建图片</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>+ 批量创建图片</a-button>
      </a-space>
    </a-space>

    <!-- 用户管理 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      row-class-name="custom-row"
      class="custom-table"
      :row-selection="rowSelection"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <!-- 图片 -->
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="100" :height="90"/>
        </template>
        <!-- 简介 -->
        <template v-if="column.dataIndex === 'introduction'">
          <template v-if="record.introduction">
            <a-tooltip placement="topLeft">
              <template #title>{{ record.introduction }}</template>
              {{ record.introduction }}
            </a-tooltip>
          </template>
          <span v-else class="empty-tags">----</span>
        </template>
        <!-- 类型 -->
        <template v-if="column.dataIndex === 'category'">
          <template v-if="record.category">
            {{ record.category }}
          </template>
          <span v-else class="empty-tags">----</span>
        </template>
        <!-- 标签 -->
        <template v-if="column.dataIndex === 'tags'">
          <template v-if="record.tags">
            <a-space wrap>
              <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">{{ tag }}</a-tag>
            </a-space>
          </template>
          <span v-else class="empty-tags">无</span>
        </template>
        <!-- 图片信息 -->
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽/高：{{ record.picWidth }}/{{ record.picHeight }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <!-- 审核状态 -->
        <template v-if="column.dataIndex === 'reviewStatus'">
          <div>状态：
            <a-tag color="green" v-if="record.reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS">通过</a-tag>
            <a-tag color="red" v-else-if="record.reviewStatus === PIC_REVIEW_STATUS_ENUM.REJECT">拒绝</a-tag>
            <a-tag color="blue" v-else>待审核</a-tag>
          </div>
          <div>信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
        </template>
        <!-- 创建时间 -->
        <template v-else-if="column.dataIndex === 'createOrEditTime'">
          <div>
            创建：{{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            <div v-if="record.editTime">
              编辑：{{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </div>
        </template>
        <!-- 操作 -->
        <template v-else-if="column.key === 'action'">
          <a-button
            type="primary"
            style="background-color: #0099ff; border-color: #0099ff; margin-bottom: 4px"
            :href="`/edit_picture?id=${record.id}`"
            :size="size"
            target="_blank"
          >编辑
          </a-button>
          <a-button danger :size="size" @click="confirmDelete(record.id)" style="margin-right: 4px">删除</a-button>
          <a-dropdown trigger="['click']" >
            <EllipsisOutlined />
            <template #overlay>
              <a-menu>
                <a-menu-item key="1">
                  <a-button
                    type="primary"
                    style="background-color: #52c41a; border-color: #52c41a; margin-top: 4px"
                    :size="size"
                    target="_blank"
                    @click="handleReview(record,PIC_REVIEW_STATUS_ENUM.PASS)"
                  >通过
                  </a-button>
                </a-menu-item>
                <a-menu-item key="2">
                  <a-button danger :size="size" @click="handleReview(record,PIC_REVIEW_STATUS_ENUM.REJECT)">拒绝</a-button>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
      </template>
    </a-table>
  </a-config-provider>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, unref } from 'vue'
import {
  deletePictureUsingPost, doPictureReviewUsingPost,
  listPictureByPageUsingPost, listPictureTagCategoryUsingGet
} from '@/api/pictureController.ts'
import { message, Modal, Table } from 'ant-design-vue'
import { EllipsisOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import type { SizeType } from 'ant-design-vue/es/config-provider'
import { PIC_REVIEW_STATUS_ENUM } from '@/constants/reviewStatus.ts'

const size = ref<SizeType>('small')

/**
 * 调用后端获取tag和分类
 */
const categoryList = ref<string[]>([])
const tagList = ref<string[]>([])
const getTagAndCategory = async () => {
  try {
    const res = await listPictureTagCategoryUsingGet()
    if (res.data.code === 0 && res.data.data) {
      categoryList.value = (res.data.data.categoryList ?? []).map((data: string) => {
        // 由于下拉框需要数据类型为{
        //   value: string
        //   label: string
        // }
        // 所以需要遍历并以对象的形式返回
        return {
          value: data,
          label: data,
        }
      })
      tagList.value = (res.data.data.tagList ?? []).map((data: string) => {
        return {
          value: data,
          label: data,
        }
      })
    } else {
      message.error('获取标签和分类失败，' + res.data.message)
    }
  } catch (error) {
    message.error('获取标签和分类失败', error.message)
  }
}

// 每次页面刷新时只需要触发一次即可
onMounted(() => {
  getTagAndCategory()
})
// 行头
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
  },
  {
    title: '名称',
    dataIndex: 'name',
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true,
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 120
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo',
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    width: 130,
  },
  {
    title: '创建 / 编辑时间',
    dataIndex: 'createOrEditTime',
  },

  {
    title: '操作 / 审核',
    key: 'action',
  },
]

// 数据
const dataList = ref([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  console.log('adfadasdasfadsfa')
  fetchData()
})

// 获取数据
const doSearch = () => {
  // 重置搜索条件
  searchParams.current = 1
  fetchData()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 弹窗确认删除
const confirmDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据无法恢复，是否继续？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => onDelete(id),
  })
}

// 删除
const onDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({
    pictureId:id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败' + res.data.message)
  }
}

// 审核
const handleReview = async (record: API.Picture,reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员通过' : '管理员拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if (res.data.code === 0) {
    message.success('审核操作成功')
    // 重新获取列表
    fetchData()
  } else {
    message.error('审核操作失败，' + res.data.message)
  }
}

</script>


<style scoped>
.custom-table .ant-table-tbody > tr > td {
  border-bottom: 1px dashed #d9d9d9;
}

.custom-table .ant-table-thead > tr > th {
  text-align: center;
  background-color: #fafafa;
  font-weight: bold;
  border-bottom: 1px solid #d9d9d9;
}

.custom-row {
  text-align: center;
  &:hover {
    background-color: #f5f5f5;
  }
}

.custom-table {
  margin-top: 20px;
}
</style>
