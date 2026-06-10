import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'

// 导入自定义UI组件库
import {
  FcButton,
  FcInput,
  FcModal,
  FcAvatar,
  FcCard,
  FcBadge,
  FcSkeleton
} from './components/ui/index.js'

// 导入自定义UI组件库样式
import './components/ui/styles/variables.css'
import './components/ui/styles/animations.css'

// 导入聊天组件样式
import './components/chat/styles/chat.css'

const app = createApp(App)

// 注册 Pinia 和 Router
app.use(pinia)
app.use(router)

// 全局注册 UI 组件
app.component('FcButton', FcButton)
app.component('FcInput', FcInput)
app.component('FcModal', FcModal)
app.component('FcAvatar', FcAvatar)
app.component('FcCard', FcCard)
app.component('FcBadge', FcBadge)
app.component('FcSkeleton', FcSkeleton)

app.mount('#app')