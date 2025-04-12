<template>
  <a-config-provider :locale="zhCN">
    <!-- 搜索管理 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="margin-left: 16px">搜索</a-button>
      </a-form-item>
    </a-form>
    <!-- 分界符 -->
    <div style="margin-bottom: 16px"></div>
    <!-- 用户管理 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      row-class-name="custom-row"
      class="custom-table"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <template v-if="record.userAvatar">
            <a-image :src="record.userAvatar" :width="60" />
          </template>
          <template v-else>
            <UserOutlined style="font-size: 60px; color: #ccc;" />
          </template>
        </template>
        <template v-else-if="column.dataIndex === 'userProfile'">
          <a-tooltip :title="record.userProfile">{{ record.userProfile ? record.userProfile : '----' }}</a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div style="display: flex; align-items: center; gap: 12px">
            <a-button size="small" type="primary" style="background-color: #52c41a; border-color: #52c41a;"
                      @click="onEdit(record)">修改</a-button>
            <a-button size="small" danger @click="confirmDelete(record.id)">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
  </a-config-provider>

  <!-- 编辑用户信息模态框 -->
  <a-modal
    v-model:visible="isEditModalVisible"
    ok-text="保存"
    cancel-text="取消"
    @ok="handleEditOk"
    @cancel="handleEditCancel"
    body-style="padding: 24px 0; border-radius: 8px;"
  >
    <template #title>
      <div style="text-align: center; font-weight: bold;">
        编辑用户信息
      </div>
    </template>
    <a-form
      layout="vertical"
      :model="editForm">
      <a-form-item label="账号">
        <a-input v-model:value="editForm.userAccount" placeholder="请输入账号" allow-clear
                 style="border-radius: 6px;"/>
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="editForm.userName" placeholder="请输入用户名"
                 allow-clear
                 style="border-radius: 6px;"/>
      </a-form-item>
      <a-form-item label="用户角色">
        <a-select
          v-model:value="editForm.userRole"
          placeholder="选择角色"
          style="border-radius: 6px; width: 100%;"
        >
          <a-select-option value="admin">管理员</a-select-option>
          <a-select-option value="user">普通用户</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="简介">
        <a-input v-model:value="editForm.userProfile" placeholder="请输入简介"
                 allow-clear
                 style="border-radius: 6px;"/>
      </a-form-item>
      <a-form-item label="头像">
        <div
          style="display: flex; align-items: center; justify-content: space-between;"
        >
          <template v-if="editForm.userAvatar">
            <a-image :src="editForm.userAvatar" :width="60" />
          </template>

          <template v-else>
            <UserOutlined style="font-size: 60px; color: #ccc;" />
          </template>

          <a-upload
            :show-upload-list="false"
            :custom-request="handleUpload"
            :before-upload="beforeUpload"
          >
            <a-button type="primary">上传头像</a-button>
          </a-upload>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>

</template>

<script lang="ts" setup>

import { computed, onMounted, reactive, ref } from 'vue'
import {
  deleteUserUsingPost, editUserUsingPost,
  listUserVoByPageUsingPost,
  updateUserUsingPost
} from '@/api/userController.ts'
import { message, Modal, type UploadProps } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: '10%'
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    width: '20%'
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    filters: [
      { text: '管理员', value: 'admin' },
      { text: '普通用户', value: 'user' },
    ],
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 定义数据
const dataList = ref([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime', // 排序字段
  sortOrder: 'descend', // 排序顺序
  userRole: '', // 用户角色
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

// 表格变化时处理
const doTableChange = (pagination: any, filters: any, sorter: any) => {
  // 更新分页参数
  searchParams.current = pagination.current;
  searchParams.pageSize = pagination.pageSize;

  // 更新过滤条件（假设 filters.userRole 是筛选条件字段）
  if (filters.userRole) {
    searchParams.userRole = filters.userRole[0]; // 选择第一个过滤值
  } else {
    delete searchParams.userRole; // 如果没有选择过滤条件，清除该字段
  }

  // 重新拉取数据
  fetchData();
};

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({
    ...searchParams
  })
  if(res.data.data){
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  }else {
    message.error('获取用户信息失败' + res.data.message)
  }
}

const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 弹窗确认删除
const confirmDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '删除后数据无法恢复，是否继续？',
    okText: '确认',
    cancelText: '取消',
    onOk: () => doDelete(id),
  });
};

const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({id})
  if(res.data.code === 0){
    message.success('删除成功')
    fetchData()
  }else {
    message.error('删除失败' + res.data.message)
  }
}

// 控制编辑弹窗的显示与表单数据
const isEditModalVisible = ref(false);
const editForm = reactive({
  id: "",
  userAccount: "",
  userName: "",
  userRole: "",
  userProfile: "",
  userAvatar: "", // 头像 URL
});

// 编辑用户函数
const onEdit = (record) => {
  // 填充表单数据
  editForm.id = record.id;
  editForm.userAccount = record.userAccount;
  editForm.userName = record.userName;
  editForm.userRole = record.userRole;
  editForm.userProfile = record.userProfile;
  editForm.userAvatar = record.userAvatar;

  // 显示编辑弹窗
  isEditModalVisible.value = true;
};

// 图片参数校验
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('不能上传超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M
}

// 提交编辑表单
const handleEditOk = async () => {
  const res = await updateUserUsingPost(editForm);
  if (res.data.code === 0) {
    message.success("修改成功");
    isEditModalVisible.value = false; // 关闭弹窗
    fetchData(); // 重新加载数据
  } else {
    message.error("修改失败: " + res.data.message);
  }
};

const loading = ref<boolean>(false)

/**
 * 上传
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const res = await editUserUsingPost(editForm, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      editForm.userAvatar = res.data.data.userAvatar
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleEditCancel = () => {
  isEditModalVisible.value = false;
};

// 页面加载请求一次
onMounted(() => {
  fetchData()
})

</script>

<style scoped>
/* 表格虚线样式 */
.custom-table .ant-table-tbody > tr > td {
  border-bottom: 1px dashed #d9d9d9;
}

.custom-table .ant-table-thead > tr > th {
  text-align: center;
  background-color: #fafafa;
  font-weight: bold;
  border-bottom: 1px solid #d9d9d9;
}

/* 自定义行样式 */
.custom-row {
  text-align: center;
  &:hover {
    background-color: #f5f5f5; /* 鼠标悬停效果 */
  }
}
</style>
