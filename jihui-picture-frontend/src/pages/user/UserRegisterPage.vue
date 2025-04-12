<template>
  <div class="register-container">
    <!-- 动态背景 -->
    <div class="animated-background"></div>

    <!-- 卡片式注册表单 -->
    <transition name="zoom-in" appear>
      <div class="register-card">
        <div class="brand-header">
          <h1 class="brand-title">加入 Jihui 图库</h1>
          <p class="brand-subtitle">开启您的智能云图库协作体验</p>
        </div>
        <a-form
          :model="formState"
          name="registerForm"
          autocomplete="off"
          @finish="handleSubmit"
          class="register-form"
        >
          <!-- 账号输入 -->
          <a-form-item name="userAccount" :rules="accountRules">
            <a-input
              v-model:value="formState.userAccount"
              placeholder="请输入账号"
              size="large"
            >
              <template #prefix>
                <user-outlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <!-- 密码输入 -->
          <a-form-item name="userPassword" :rules="passwordRules">
            <a-input-password
              v-model:value="formState.userPassword"
              placeholder="请输入密码 (至少8位)"
              size="large"
              @focus="handleInputFocus"
            >
              <template #prefix>
                <lock-outlined class="input-icon" />
              </template>
            </a-input-password>
            <div class="password-strength" v-if="formState.userPassword">
              <div
                class="strength-bar"
                :class="getPasswordStrength(formState.userPassword)"
              ></div>
              <span class="strength-text">{{ getPasswordText(formState.userPassword) }}</span>
            </div>
          </a-form-item>

          <!-- 确认密码 -->
          <a-form-item name="checkPassword" :rules="checkPasswordRules">
            <a-input-password
              v-model:value="formState.checkPassword"
              placeholder="请再次输入密码"
              size="large"
              @focus="handleInputFocus"
            >
              <template #prefix>
                <safety-outlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 注册按钮 -->
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              block
              size="large"
              :loading="submitting"
              class="register-btn"
            >
              {{ submitting ? '注册中...' : '立即注册' }}
            </a-button>
          </a-form-item>

          <!-- 底部链接 -->
          <div class="footer-links">
            <router-link to="/user/login" class="link-item">
              <arrow-left-outlined /> 已有账号？立即登录
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
  SafetyOutlined,
  ArrowLeftOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userRegisterUsingPost } from '@/api/userController'
import router from '@/router'

const formState = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const submitting = ref(false)

// 密码强度计算
const getPasswordStrength = (password: string) => {
  if (!password) return ''
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  const hasSpecial = /[^a-zA-Z0-9]/.test(password)

  const strength = [hasLetter, hasNumber, hasSpecial].filter(Boolean).length
  return ['weak', 'medium', 'strong'][strength - 1] || ''
}

const getPasswordText = (password: string) => {
  const strength = getPasswordStrength(password)
  return {
    'weak': '弱',
    'medium': '中',
    'strong': '强'
  }[strength] || ''
}

// 验证规则
const accountRules = [
  { required: true, message: '请输入账号', trigger: 'blur' },
  { min: 4, message: '账号长度至少4位', trigger: 'blur' },
  { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
]

const passwordRules = [
  { required: true, message: '请输入密码', trigger: 'blur' },
  { min: 8, message: '密码长度不能小于8位', trigger: 'blur' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '需包含字母和数字' },
  { validator: validatePassword, trigger: 'blur' }
]

const checkPasswordRules = [
  { required: true, message: '请再次输入密码', trigger: 'blur' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '需包含字母和数字' },
  { validator: validateCheckPassword, trigger: 'blur' }
]

function validatePassword(_rule: any, value: string) {
  if (value && value.length < 8) {
    return Promise.reject('密码长度不能小于8位')
  }
  return Promise.resolve()
}

function validateCheckPassword(_rule: any, value: string) {
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 输入框聚焦效果
const handleInputFocus = (e: FocusEvent) => {
  const wrapper = (e.target as HTMLElement).closest('.ant-input-affix-wrapper')
  wrapper?.classList.add('active')
}

// 提交处理
const handleSubmit = async (values: any) => {
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }

  try {
    submitting.value = true
    const res = await userRegisterUsingPost({
      userAccount: values.userAccount,
      userPassword: values.userPassword,
      checkPassword: values.checkPassword
    })

    if (res.data.code === 0) {
      message.success({
        content: '注册成功，即将跳转到登录页面',
        duration: 1.5,
        onClose: () => {
          router.push('/user/login')
        }
      })
    } else {
      message.error(res.data.message || '注册失败')
    }
  } catch (error) {
    message.error('网络请求异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.animated-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  opacity: 0.1;
  z-index: 0;
  animation: gradientMove 15s ease infinite;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.register-card {
  width: 100%;
  max-width: 480px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
  z-index: 1;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 16px 32px rgba(0, 0, 0, 0.15);
  }
}

.brand-header {
  text-align: center;
  margin-bottom: 32px;

  .brand-logo {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
    object-fit: contain;
  }

  .brand-title {
    font-size: 24px;
    color: #2c3e50;
    margin: 8px 0;
    font-weight: 600;
  }

  .brand-subtitle {
    color: #7f8c8d;
    font-size: 14px;
    margin: 0;
  }
}

.register-form {
  .ant-form-item {
    margin-bottom: 24px;
  }

  .input-icon {
    color: #7f8c8d;
    transition: color 0.3s;
  }

  :deep(.ant-input-affix-wrapper) {
    border-radius: 8px;
    transition: all 0.3s;

    &.active {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }

  .password-strength {
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 8px;

    .strength-bar {
      height: 4px;
      border-radius: 2px;
      flex-grow: 1;
      background: #e0e0e0;
      position: relative;
      overflow: hidden;

      &::after {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 0;
        transition: width 0.3s ease, background 0.3s ease;
      }

      &.weak::after {
        width: 33%;
        background: #ff4d4f;
      }

      &.medium::after {
        width: 66%;
        background: #faad14;
      }

      &.strong::after {
        width: 100%;
        background: #52c41a;
      }
    }

    .strength-text {
      font-size: 12px;
      color: #7f8c8d;
    }
  }
}

.register-btn {
  height: 48px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 16px;
  transition: all 0.3s;

  &:not([disabled]):hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  }
}

.footer-links {
  margin-top: 24px;
  text-align: center;

  .link-item {
    color: #7f8c8d;
    transition: color 0.3s;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;

    &:hover {
      color: #1890ff;
    }
  }
}

/* 入场动画 */
.zoom-in-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.zoom-in-enter-from {
  opacity: 0;
  transform: scale(0.9);
}

/* 响应式适配 */
@media (max-width: 576px) {
  .register-card {
    padding: 24px;
  }

  .brand-header {
    .brand-logo {
      width: 60px;
      height: 60px;
    }

    .brand-title {
      font-size: 20px;
    }
  }
}
</style>
