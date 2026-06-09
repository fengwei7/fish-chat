import { createApp, h } from 'vue'
import MessageComponent from './Message.vue'

/**
 * Message 消息提示 - 命令式API
 * 
 * @example
 * ```javascript
 * import { Message } from '@/components/ui'
 * 
 * Message.success('操作成功')
 * Message.error('操作失败')
 * Message.warning('警告提示')
 * Message.info('信息提示')
 * 
 * // 自定义配置
 * Message({
 *   type: 'success',
 *   message: '自定义消息',
 *   duration: 5000,
 *   showClose: true
 * })
 * ```
 */

// 创建实例
let instance = null
let container = null
let messageMethods = null

function getInstance() {
  if (!instance) {
    container = document.createElement('div')
    document.body.appendChild(container)
    
    const app = createApp({
      render() {
        return h(MessageComponent, { ref: 'message' })
      }
    })
    
    const mountedInstance = app.mount(container)
    instance = mountedInstance
    
    // 保存方法引用
    messageMethods = mountedInstance.$refs.message
  }
  
  return instance
}

function message(options) {
  if (typeof options === 'string') {
    options = {
      type: 'info',
      message: options
    }
  }
  
  const methods = messageMethods || getInstance().$refs.message
  if (!methods || !methods.add) {
    console.error('Message component not properly initialized')
    return
  }
  return methods.add(options)
}

// 快捷方法
const types = ['success', 'error', 'warning', 'info']

types.forEach(type => {
  message[type] = (msg, duration) => {
    return message({
      type,
      message: msg,
      duration
    })
  }
})

// 关闭指定消息
message.close = (id) => {
  const methods = messageMethods || getInstance().$refs.message
  if (methods && methods.close) {
    methods.close(id)
  }
}

// 关闭所有消息
message.closeAll = () => {
  if (messageMethods && messageMethods.messages) {
    messageMethods.messages.forEach(msg => {
      message.close(msg.id)
    })
  }
}

export default message
