import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import pinia from './stores'

// 导入自定义UI组件库样式
import './components/ui/styles/variables.css'
import './components/ui/styles/animations.css'

// 导入聊天组件样式
import './components/chat/styles/chat.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
// 暂时保留ElementPlus，后续逐步替换
// app.use(ElementPlus)

app.mount('#app')