<template>
  <div class="login-container">
    <!-- 动态背景粒子效果 -->
    <div class="particles-background"></div>

    <!-- 卡片式登录表单 -->
    <transition name="form-slide" appear>
      <div class="login-card">
        <div class="brand-header">

          <h1 class="brand-title">Jihui 云图库</h1>
        </div>

        <a-form
          :model="formState"
          name="loginForm"
          autocomplete="off"
          @finish="handleSubmit"
          class="login-form"
        >
          <!-- 账号输入带动态图标 -->
          <a-form-item name="userAccount" :rules="accountRules">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号">
              <template #prefix>
                <user-outlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <!-- 密码输入带动画效果 -->
          <a-form-item name="userPassword" :rules="passwordRules">
            <a-input-password
              v-model:value="formState.userPassword"
              placeholder="请输入密码"
              @focus="handlePasswordFocus"
            >
              <template #prefix>
                <lock-outlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 动态提交按钮 -->
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              block
              :loading="submitting"
              class="submit-btn"
            >
              {{ submitting ? '登录中...' : '立即登录' }}
            </a-button>
          </a-form-item>

          <!-- 辅助功能链接 -->
          <div class="action-links">
            <router-link to="/user/register" class="link-item">
              <arrow-right-outlined /> 注册新账号
            </router-link>
          </div>
        </a-form>
      </div>
    </transition>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import {
  UserOutlined,
  LockOutlined,
  ArrowRightOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import router from '@/router'

// 表单状态
const formState = reactive({
  userAccount: '',
  userPassword: ''
})

// 提交状态
const submitting = ref(false)

// 验证规则
const accountRules = [
  { required: true, trigger: 'blur', message: '请输入您的账号' },
  { min: 4, trigger: 'blur', message: '账号长度至少4位' }
]

const passwordRules = [
  { required: true, trigger: 'blur', message: '请输入密码' },
  { min: 8, trigger: 'blur', message: '密码长度不能小于8位' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '需包含字母和数字' }
]

// 密码框聚焦动画
const handlePasswordFocus = (e: FocusEvent) => {
  const inputWrapper = (e.target as HTMLElement).closest('.ant-input-affix-wrapper')
  inputWrapper?.classList.add('active-input')
}

// 提交处理
const handleSubmit = async (values: any) => {
  try {
    submitting.value = true
    const res = await userLoginUsingPost(values)

    if (res.data.code === 0 && res.data.data) {
      await useLoginUserStore().fetchLoginUser()
      message.success({ content: '登录成功', duration: 1.5 })
      router.replace('/')
    } else {
      message.error(res.data.message || '登录失败，请检查输入')
    }
  } catch (error) {
    message.error('网络请求异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.particles-background {
  position: absolute;
  width: 100%;
  height: 100%;
  background: url('@/assets/particles-bg.jpg') no-repeat center/cover;
  opacity: 0.1;
}

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transform: translateY(0);
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-5px);
  }
}

.brand-header {
  text-align: center;
  margin-bottom: 40px;

  .brand-logo {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
    filter: drop-shadow(0 4px 6px rgba(50, 100, 255, 0.2));
  }

  .brand-title {
    color: #2c3e50;
    font-size: 24px;
    letter-spacing: 2px;
    margin: 0;
  }
}

.login-form {
  .input-icon {
    color: rgba(0, 0, 0, 0.25);
    transition: color 0.3s;
  }

  :deep(.ant-input-affix-wrapper) {
    transition: all 0.3s;
    border-radius: 8px;

    &.active-input {
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
}

.submit-btn {
  height: 40px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;

  &:not([disabled]):hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  }
}

.action-links {
  margin-top: 24px;
  display: flex;
  justify-content: space-between;

  .link-item {
    color: #666;
    transition: color 0.3s;
    display: flex;
    align-items: center;
    gap: 6px;

    &:hover {
      color: #1890ff;
    }
  }
}

/* 入场动画 */
.form-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.form-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

/* 响应式适配 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 24px;
  }
}
</style>
