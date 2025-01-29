<template>
  <div class="profile-container">
    <!-- 动态背景 -->
    <div class="background">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>

    <div class="content">
      <div class="card">
        <div class="header">
          <div class="logo-wrapper">
            <img src="/logo.png" alt="MapleCoupon Logo" class="logo"/>
            <div class="logo-glow"></div>
          </div>
          <h1 class="title">个人信息</h1>
          <p class="subtitle">管理您的账户信息</p>
        </div>

        <el-form 
          :model="userForm" 
          label-position="top"
          :rules="rules"
          ref="userForm"
          class="form-wrapper"
        >
          <div class="form-grid">
            <el-form-item label="用户名" prop="username">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                  </svg>
                </div>
                <el-input 
                  v-model="userForm.username" 
                  disabled 
                  class="custom-input"
                  placeholder="用户名"
                />
              </div>
            </el-form-item>

            <el-form-item label="店铺名称" prop="shopName">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 1a1 1 0 011 1v1.323l3.954 1.582 1.599.8a1 1 0 01.547.892v6.834a1 1 0 01-.547.892l-1.599.8L11 16.323V18a1 1 0 11-2 0v-1.677l-3.954-1.582-1.599-.8A1 1 0 013 13.235V6.4a1 1 0 01.547-.892l1.599-.8L9 3.323V2a1 1 0 011-1zM5 7.62v4.76l3.954 1.582 1.599.8a1 1 0 00.894 0l1.599-.8L17 12.38V7.62l-3.954-1.582-1.599-.8a1 1 0 00-.894 0l-1.599.8L5 7.62z" clip-rule="evenodd"/>
                  </svg>
                </div>
                <el-input 
                  v-model="userForm.shopName" 
                  class="custom-input"
                  placeholder="请输入店铺名称"
                />
              </div>
            </el-form-item>

            <el-form-item label="真实姓名" prop="realName">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"/>
                  </svg>
                </div>
                <el-input 
                  v-model="userForm.realName" 
                  class="custom-input"
                  placeholder="请输入真实姓名"
                />
              </div>
            </el-form-item>

            <el-form-item label="电话号码" prop="phone">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
                  </svg>
                </div>
                <el-input 
                  v-model="userForm.phone" 
                  class="custom-input"
                  placeholder="请输入电话号码"
                />
              </div>
            </el-form-item>

            <el-form-item label="邮箱地址" prop="mail" class="span-2">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" class="icon" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/>
                    <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/>
                  </svg>
                </div>
                <el-input 
                  v-model="userForm.mail" 
                  class="custom-input"
                  placeholder="请输入邮箱地址"
                />
              </div>
            </el-form-item>
          </div>

          <div class="button-group">
            <el-button 
              type="primary" 
              @click="updateProfile"
              :loading="loading"
              class="update-btn"
            >
              <span class="button-content">
                <i class="el-icon-check"></i>
                更新信息
              </span>
            </el-button>
            <el-button 
              type="danger" 
              @click="logout"
              class="logout-btn"
            >
              <span class="button-content">
                <i class="el-icon-switch-button"></i>
                退出登录
              </span>
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      userForm: {
        username: '',
        shopName: '',
        realName: '',
        phone: '',
          mail: ''
      },
      rules: {
        shopName: [
          { required: true, message: '请输入店铺名称', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话号码', trigger: 'blur' }
        ],
          mail: [
            { required: true, message: '请输入邮箱地址', trigger: 'blur' },
            { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
          ]
      }
    }
  },
  async created() {
    await this.fetchUserInfo();
  },
  methods: {
    async fetchUserInfo() {
      try {
        const username = localStorage.getItem('username');
        const token = localStorage.getItem('token');
        
        const response = await this.$axios.get(`/api/auth/actual/user/${username}`, {
          headers: { 
            username: username,
            token: token
          }
        });
        
        this.userForm = {
          ...response.data.data,
          username
        };
      } catch (error) {
        this.$message.error('获取用户信息失败');
      }
    },
    async updateProfile() {
      const username = localStorage.getItem('username');
      const token = localStorage.getItem('token');
      try {
        this.loading = true;
        
        const response = await this.$axios.put(`/api/auth/user?token=${token}`, this.userForm, {
          headers: { 
            username: username,
            token: token
          }
        });
        if ( response.data.success ){
          this.$message.success('用户信息更新成功');
        } else {
          this.$message.error(response.data.message || '更新失败');
        }
        
      } catch (error) {
        this.$message.error(error.response?.data?.message || '更新失败');
      } finally {
        this.loading = false;
      }
    },
    async logout() {
      try {
        const username = localStorage.getItem('username');
        const token = localStorage.getItem('token');
        
        await this.$axios.delete(`/api/auth/user/logout?username=${username}&token=${token}`, {
          headers: { 
            username: username,
            token: token
          }
        });
        
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        this.$router.push('/');
        this.$message.success('退出登录成功');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '退出登录失败');
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.background {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8eaff 100%);
}

.blob {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  filter: blur(40px);
  mix-blend-mode: multiply;
  opacity: 0.7;
}

.blob-1 {
  top: -40px;
  left: -40px;
  background: #bcd5ff;
  animation: blob 7s infinite;
}

.blob-2 {
  bottom: -40px;
  right: -40px;
  background: #c4c8ff;
  animation: blob 7s infinite 2s;
}

.blob-3 {
  top: 50%;
  left: 50%;
  background: #e0d5ff;
  animation: blob 7s infinite 4s;
}

.content {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 800px;
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.logo {
  width: 80px;
  height: 80px;
  animation: float 3s ease-in-out infinite;
}

.logo-glow {
  position: absolute;
  inset: -4px;
  background: linear-gradient(135deg, #60a5fa, #818cf8);
  border-radius: 50%;
  filter: blur(8px);
  opacity: 0.3;
}

.title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.subtitle {
  color: #4b5563;
  margin-bottom: 0;
}

.form-wrapper {
  margin-top: 32px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.span-2 {
  grid-column: span 2;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4b5563;
  padding-bottom: 8px;
}

:deep(.el-input__inner) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  padding: 12px 16px;
  height: auto;
  font-size: 1rem;
  transition: all 0.3s;
}

:deep(.el-input__inner:focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.button-group {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  justify-content: center;
}

.update-btn, .logout-btn {
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s;
}

.update-btn {
  background: linear-gradient(135deg, #3b82f6, #4f46e5);
  border: none;
}

.update-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.3);
}

.logout-btn {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  border: none;
  color: white;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.3);
}

.button-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes blob {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0, 0) scale(1); }
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.input-wrapper:hover {
  border-color: #3b82f6;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.input-wrapper:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  color: #6b7280;
  transition: color 0.3s ease;
}

.input-wrapper:hover .input-icon,
.input-wrapper:focus-within .input-icon {
  color: #3b82f6;
}

.icon {
  width: 20px;
  height: 20px;
}

:deep(.custom-input) {
  flex: 1;
}

:deep(.custom-input .el-input__wrapper) {
  box-shadow: none !important;
  padding: 0;
  background: transparent;
}

:deep(.custom-input .el-input__inner) {
  height: 44px;
  line-height: 44px;
  border: none;
  background: transparent;
  padding: 0 16px;
  font-size: 1rem;
  color: #1f2937;
}

:deep(.custom-input .el-input__inner::placeholder) {
  color: #9ca3af;
}

:deep(.custom-input .el-input__inner:focus) {
  box-shadow: none;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #4b5563;
  transition: color 0.3s ease;
}

:deep(.el-form-item:hover .el-form-item__label) {
  color: #3b82f6;
}

/* Disabled input styles */
.input-wrapper:has(.el-input.is-disabled) {
  background: #f3f4f6;
  border-color: #e5e7eb;
  cursor: not-allowed;
}

.input-wrapper:has(.el-input.is-disabled) .input-icon {
  color: #9ca3af;
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: #6b7280;
  -webkit-text-fill-color: #6b7280;
}

@media (max-width: 640px) {
  .card {
    padding: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .span-2 {
    grid-column: auto;
  }

  .button-group {
    flex-direction: column;
  }

  .input-wrapper {
    border-radius: 8px;
  }

  .input-icon {
    width: 40px;
  }

  .icon {
    width: 18px;
    height: 18px;
  }

  :deep(.custom-input .el-input__inner) {
    height: 40px;
    line-height: 40px;
    font-size: 0.875rem;
  }
}
</style>
