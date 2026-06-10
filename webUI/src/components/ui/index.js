/**
 * Fish-Chat UI 组件库
 * 清爽蓝色系 · 现代简约 · toC设计
 */

// 样式导入
import './styles/variables.css'
import './styles/animations.css'
import './styles/reset.css'

// 组件导出
export { default as FcButton } from './Button/Button.vue'
export { default as FcInput } from './Input/Input.vue'
export { default as FcModal } from './Modal/Modal.vue'
export { default as FcDrawer } from './Drawer/Drawer.vue'
export { default as FcAvatar } from './Avatar/Avatar.vue'
export { default as FcCard } from './Card/Card.vue'
export { default as FcBadge } from './Badge/Badge.vue'
export { default as FcMessage } from './Message/Message.vue'
export { default as FcSkeleton } from './Skeleton/Skeleton.vue'

// 命令式API
export { default as Message } from './Message/message.js'
