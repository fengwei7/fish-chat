/**
 * Fish-Chat 聊天组件库
 * 分层设计 · 高度复用 · 现代简约
 */

// 样式导入
import './styles/chat.css'

// 组件导出
export { default as ChatBubble } from './ChatBubble/ChatBubble.vue'
export { default as ChatMessageList } from './ChatMessageList/ChatMessageList.vue'
export { default as ChatInput } from './ChatInput/ChatInput.vue'
export { default as ChatPanel } from './ChatPanel/ChatPanel.vue'
export { default as ChatEmojiPicker } from './ChatEmojiPicker/ChatEmojiPicker.vue'
export { default as ChatFilePreview } from './ChatFilePreview/ChatFilePreview.vue'
