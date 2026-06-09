<template>
  <div class="ui-showcase">
    <div class="showcase-header">
      <h1>Fish-Chat UI 组件库</h1>
      <p class="showcase-desc">清爽蓝色系 · 现代简约 · toC设计</p>
    </div>

    <div class="showcase-content">
      <!-- Button 按钮 -->
      <section class="showcase-section">
        <h2 class="section-title">Button 按钮</h2>
        
        <div class="showcase-item">
          <h3>基础类型</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton>默认按钮</FcButton>
              <FcButton type="primary">主要按钮</FcButton>
              <FcButton type="secondary">次要按钮</FcButton>
              <FcButton type="outline">边框按钮</FcButton>
              <FcButton type="ghost">幽灵按钮</FcButton>
              <FcButton type="danger">危险按钮</FcButton>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>不同尺寸</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="primary" size="sm">小号按钮</FcButton>
              <FcButton type="primary" size="md">中号按钮</FcButton>
              <FcButton type="primary" size="lg">大号按钮</FcButton>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>特殊样式</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="primary" round>圆角按钮</FcButton>
              <FcButton type="primary" :loading="true">加载中</FcButton>
              <FcButton type="primary" :disabled="true">禁用状态</FcButton>
              <FcButton type="primary" block>块级按钮</FcButton>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>图标按钮</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="primary">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16" height="16">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
                上传
              </FcButton>
              <FcButton type="primary">
                保存
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="16" height="16">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                  <polyline points="17 21 17 13 7 13 7 21" />
                  <polyline points="7 3 7 8 15 8" />
                </svg>
              </FcButton>
              <FcButton type="primary" circle>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" width="20" height="20">
                  <line x1="12" y1="5" x2="12" y2="19" />
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
              </FcButton>
            </div>
          </div>
        </div>
      </section>

      <!-- Input 输入框 -->
      <section class="showcase-section">
        <h2 class="section-title">Input 输入框</h2>
        
        <div class="showcase-item">
          <h3>基础输入</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical">
              <FcInput v-model="inputValue" placeholder="请输入内容" style="max-width: 400px" />
              <FcInput v-model="inputValue" placeholder="禁用状态" disabled style="max-width: 400px" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>带图标输入</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical">
              <FcInput v-model="inputValue" placeholder="搜索..." clearable style="max-width: 400px" />
              <FcInput v-model="passwordValue" type="password" placeholder="密码输入" clearable style="max-width: 400px" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>文本域</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical">
              <FcInput 
                v-model="textareaValue" 
                type="textarea" 
                placeholder="请输入多行文本"
                :rows="4"
                :maxlength="200"
                :show-word-limit="true"
                style="max-width: 600px"
              />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>错误状态</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical">
              <FcInput 
                v-model="errorValue" 
                placeholder="错误状态"
                :error="true"
                error-message="请输入有效的邮箱地址"
                style="max-width: 400px"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- Avatar 头像 -->
      <section class="showcase-section">
        <h2 class="section-title">Avatar 头像</h2>
        
        <div class="showcase-item">
          <h3>不同尺寸</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcAvatar text="张" size="xs" />
              <FcAvatar text="李" size="sm" />
              <FcAvatar text="王" size="md" />
              <FcAvatar text="赵" size="lg" />
              <FcAvatar text="刘" size="xl" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>在线状态</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcAvatar text="在" size="lg" status="online" :show-status="true" />
              <FcAvatar text="离" size="lg" status="offline" :show-status="true" />
              <FcAvatar text="忙" size="lg" status="busy" :show-status="true" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>不同形状</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcAvatar text="圆" size="lg" shape="circle" />
              <FcAvatar text="方" size="lg" shape="square" />
            </div>
          </div>
        </div>
      </section>

      <!-- Card 卡片 -->
      <section class="showcase-section">
        <h2 class="section-title">Card 卡片</h2>
        
        <div class="showcase-item">
          <h3>基础卡片</h3>
          <div class="demo-box">
            <div class="demo-content demo-grid">
              <FcCard title="默认卡片" style="max-width: 300px">
                <p>这是默认样式的卡片内容</p>
              </FcCard>

              <FcCard title="边框卡片" variant="bordered" style="max-width: 300px">
                <p>这是带边框的卡片内容</p>
              </FcCard>

              <FcCard title="阴影卡片" shadow style="max-width: 300px">
                <p>这是带阴影的卡片内容</p>
              </FcCard>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>可悬停卡片</h3>
          <div class="demo-box">
            <div class="demo-content demo-grid">
              <FcCard title="可悬停卡片" shadow hoverable style="max-width: 300px">
                <p>鼠标悬停会有提升效果</p>
              </FcCard>
            </div>
          </div>
        </div>
      </section>

      <!-- Badge 徽章 -->
      <section class="showcase-section">
        <h2 class="section-title">Badge 徽章</h2>
        
        <div class="showcase-item">
          <h3>数字徽章</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcBadge :count="5">
                <FcButton>消息</FcButton>
              </FcBadge>
              <FcBadge :count="100" :max-count="99">
                <FcButton type="primary">通知</FcButton>
              </FcBadge>
              <FcBadge :count="0">
                <FcButton type="secondary">评论</FcButton>
              </FcBadge>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>点状徽章</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcBadge dot>
                <FcButton>新消息</FcButton>
              </FcBadge>
              <FcBadge dot type="success">
                <FcButton type="primary">在线</FcButton>
              </FcBadge>
              <FcBadge dot type="warning">
                <FcButton type="secondary">待处理</FcButton>
              </FcBadge>
            </div>
          </div>
        </div>
      </section>

      <!-- Skeleton 骨架屏 -->
      <section class="showcase-section">
        <h2 class="section-title">Skeleton 骨架屏</h2>
        
        <div class="showcase-item">
          <h3>不同形状</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcSkeleton shape="circle" size="48px" />
              <FcSkeleton shape="rect" width="200px" height="20px" />
              <FcSkeleton shape="rect" width="150px" height="20px" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>文字骨架屏</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical">
              <FcSkeleton rows="3" style="max-width: 400px" />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>组合使用</h3>
          <div class="demo-box">
            <div class="demo-content" style="max-width: 400px">
              <div style="display: flex; gap: 16px; align-items: center; margin-bottom: 20px">
                <FcSkeleton shape="circle" size="48px" />
                <div style="flex: 1">
                  <FcSkeleton shape="rect" width="60%" height="16px" style="margin-bottom: 8px" />
                  <FcSkeleton shape="rect" width="40%" height="16px" />
                </div>
              </div>
              <FcSkeleton rows="2" />
            </div>
          </div>
        </div>
      </section>

      <!-- Modal 弹窗 -->
      <section class="showcase-section">
        <h2 class="section-title">Modal 弹窗</h2>
        
        <div class="showcase-item">
          <h3>基础弹窗</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="primary" @click="showModal1 = true">打开弹窗</FcButton>
              
              <FcModal v-model:visible="showModal1" title="基础弹窗" size="md">
                <p>这是一个基础的弹窗示例</p>
                <p style="margin-top: 12px; color: var(--fc-gray-600)">
                  可以包含任意内容，支持点击遮罩关闭、ESC关闭
                </p>
                
                <template #footer>
                  <FcButton @click="showModal1 = false">取消</FcButton>
                  <FcButton type="primary" @click="showModal1 = false">确定</FcButton>
                </template>
              </FcModal>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>不同尺寸</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton @click="showModalSm = true">小弹窗</FcButton>
              <FcButton type="primary" @click="showModalMd = true">中弹窗</FcButton>
              <FcButton type="secondary" @click="showModalLg = true">大弹窗</FcButton>
              
              <FcModal v-model:visible="showModalSm" title="小弹窗" size="sm">
                <p>这是小型弹窗，宽度400px</p>
                <template #footer>
                  <FcButton @click="showModalSm = false">关闭</FcButton>
                </template>
              </FcModal>

              <FcModal v-model:visible="showModalMd" title="中弹窗" size="md">
                <p>这是中型弹窗，宽度600px</p>
                <template #footer>
                  <FcButton @click="showModalMd = false">关闭</FcButton>
                </template>
              </FcModal>

              <FcModal v-model:visible="showModalLg" title="大弹窗" size="lg">
                <p>这是大型弹窗，宽度800px</p>
                <template #footer>
                  <FcButton @click="showModalLg = false">关闭</FcButton>
                </template>
              </FcModal>
            </div>
          </div>
        </div>
      </section>

      <!-- Message 消息提示 -->
      <section class="showcase-section">
        <h2 class="section-title">Message 消息提示</h2>
        
        <div class="showcase-item">
          <h3>不同类型的消息</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="success" @click="showMessage('success')">
                成功消息
              </FcButton>
              <FcButton @click="showMessage('error')">
                错误消息
              </FcButton>
              <FcButton type="secondary" @click="showMessage('warning')">
                警告消息
              </FcButton>
              <FcButton type="outline" @click="showMessage('info')">
                信息消息
              </FcButton>
            </div>
          </div>
        </div>
      </section>

      <!-- Chat 聊天组件 -->
      <section class="showcase-section">
        <h2 class="section-title">Chat 聊天组件</h2>
        
        <div class="showcase-item">
          <h3>ChatBubble 消息气泡</h3>
          <div class="demo-box">
            <div class="demo-content demo-vertical" style="gap: 20px">
              <ChatBubble
                :message="{
                  type: 'text',
                  content: '你好！这是对方发的消息',
                  senderName: '小明',
                  avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix'
                }"
                :is-mine="false"
                :show-sender="true"
              />
              <ChatBubble
                :message="{
                  type: 'text',
                  content: '这是我发的消息，在右边显示 👋',
                  senderName: '我',
                  avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka'
                }"
                :is-mine="true"
              />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>ChatInput 输入框</h3>
          <div class="demo-box" style="position: relative">
            <ChatInput
              v-model="chatInputText"
              placeholder="输入消息..."
              @send="handleChatSend"
              @emoji="showEmojiPicker = !showEmojiPicker"
              style="max-width: 600px"
            />
            <ChatEmojiPicker
              v-model:visible="showEmojiPicker"
              @select="handleEmojiSelect"
            />
          </div>
        </div>

        <div class="showcase-item">
          <h3>ChatMessageList 消息列表</h3>
          <div class="demo-box">
            <div style="height: 500px">
              <ChatMessageList :messages="chatMessages">
                <template #message="{ message }">
                  <ChatBubble
                    :message="message"
                    :is-mine="message.isMine"
                    :show-sender="!message.isMine"
                    @click-image="handlePreviewImage"
                  />
                </template>
              </ChatMessageList>
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>ChatPanel 完整聊天面板</h3>
          <div class="demo-box">
            <div style="height: 600px">
              <ChatPanel
                :messages="chatMessages"
                title="聊天演示"
                subtitle="在线"
                @send="handleChatSend"
              />
            </div>
          </div>
        </div>

        <div class="showcase-item">
          <h3>ChatFilePreview 文件预览</h3>
          <div class="demo-box">
            <div class="demo-content">
              <FcButton type="primary" @click="handlePreviewImage">
                预览图片
              </FcButton>
              <FcButton @click="handlePreviewFile">
                预览PDF
              </FcButton>
              
              <ChatFilePreview
                v-model:visible="showFilePreview"
                :file-url="previewFileUrl"
                :file-name="previewFileName"
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { FcButton, FcInput, FcModal, FcAvatar, FcCard, FcBadge, FcSkeleton, Message } from '@/components/ui'
import { ChatBubble, ChatMessageList, ChatInput, ChatPanel, ChatEmojiPicker, ChatFilePreview } from '@/components/chat'

// Input 绑定值
const inputValue = ref('')
const passwordValue = ref('')
const textareaValue = ref('')
const errorValue = ref('')

// Modal 控制
const showModal1 = ref(false)
const showModalSm = ref(false)
const showModalMd = ref(false)
const showModalLg = ref(false)

// 聊天组件演示数据
const chatInputText = ref('')
const showEmojiPicker = ref(false)
const showFilePreview = ref(false)
const previewFileUrl = ref('')
const previewFileName = ref('')

// 模拟消息列表
const chatMessages = ref([
  {
    id: 1,
    type: 'text',
    content: '你好！这是聊天组件演示 😊',
    senderName: '小明',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
    isMine: false,
    timestamp: Date.now() - 3600000,
    status: 'sent'
  },
  {
    id: 2,
    type: 'text',
    content: '嗨！看起来不错！',
    senderName: '我',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka',
    isMine: true,
    timestamp: Date.now() - 3500000,
    status: 'sent'
  },
  {
    id: 3,
    type: 'image',
    content: '分享一张图片',
    senderName: '小明',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
    imageUrl: 'https://picsum.photos/400/300',
    isMine: false,
    timestamp: Date.now() - 3400000,
    status: 'sent'
  },
  {
    id: 4,
    type: 'file',
    content: '分享一个文件',
    senderName: '我',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka',
    fileName: '设计文档.pdf',
    fileSize: '2.5 MB',
    isMine: true,
    timestamp: Date.now() - 3300000,
    status: 'sent'
  },
  {
    id: 5,
    type: 'text',
    content: '这个UI组件库真的很棒！清爽的蓝色设计，非常喜欢 👍',
    senderName: '小明',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
    isMine: false,
    timestamp: Date.now() - 3200000,
    status: 'sent'
  }
])

const handleChatSend = (text) => {
  const newMessage = {
    id: chatMessages.value.length + 1,
    type: 'text',
    content: text,
    senderName: '我',
    avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka',
    isMine: true,
    timestamp: Date.now(),
    status: 'sent'
  }
  chatMessages.value.push(newMessage)
}

const handleEmojiSelect = (emoji) => {
  chatInputText.value += emoji
  showEmojiPicker.value = false
}

const handlePreviewImage = () => {
  previewFileUrl.value = 'https://picsum.photos/800/600'
  previewFileName.value = '示例图片.jpg'
  showFilePreview.value = true
}

const handlePreviewFile = () => {
  previewFileUrl.value = 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf'
  previewFileName.value = '示例文档.pdf'
  showFilePreview.value = true
}

// Message 演示
const showMessage = (type) => {
  const messages = {
    success: '操作成功！',
    error: '操作失败，请重试',
    warning: '这是一条警告提示',
    info: '这是一条信息提示'
  }
  Message[type](messages[type])
}
</script>

<style scoped>
.ui-showcase {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 100%);
  padding: 40px 20px;
}

.showcase-header {
  text-align: center;
  margin-bottom: 60px;
}

.showcase-header h1 {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--fc-primary-500) 0%, var(--fc-primary-700) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 16px 0;
}

.showcase-desc {
  font-size: 18px;
  color: var(--fc-gray-500);
  margin: 0;
}

.showcase-content {
  max-width: 1200px;
  margin: 0 auto;
}

.showcase-section {
  margin-bottom: 60px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--fc-gray-900);
  margin: 0 0 32px 0;
  padding-bottom: 16px;
  border-bottom: 3px solid var(--fc-primary-500);
}

.showcase-item {
  margin-bottom: 40px;
}

.showcase-item h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--fc-gray-700);
  margin: 0 0 16px 0;
}

.demo-box {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.demo-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.demo-content.demo-vertical {
  flex-direction: column;
  align-items: flex-start;
}

.demo-content.demo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

/* 响应式 */
@media (max-width: 768px) {
  .ui-showcase {
    padding: 20px 12px;
  }

  .showcase-header h1 {
    font-size: 32px;
  }

  .section-title {
    font-size: 24px;
  }

  .demo-box {
    padding: 20px;
  }

  .demo-content {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
