import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import WSManager from '@/utils/ws.js'

/**
 * WebSocket Vue 3 组合式函数
 * 
 * 基于 WSManager 封装，提供响应式状态和便捷的 API
 * 推荐在 Vue 组件中使用此函数而非直接使用 WSManager
 * 
 * 功能：
 * - 响应式连接状态（status, isConnected）
 * - 自动从 userStore 获取 token
 * - 便捷的事件监听方法
 * 
 * 使用示例：
 * ```vue
 * <script setup>
 * import { useWebSocket } from '@/composables/useWebSocket.js'
 * 
 * const { 
 *   status,           // 连接状态: 'closed' | 'connecting' | 'connected' | 'reconnecting'
 *   isConnected,      // 是否已连接（响应式布尔值）
 *   connect,          // 连接 WebSocket
 *   disconnect,       // 断开连接
 *   sendMessage,      // 发送消息
 *   onMessage,        // 监听聊天消息
 *   onNotify,         // 监听通知
 *   onSystem          // 监听系统消息
 * } = useWebSocket()
 * 
 * // 连接
 * await connect()
 * 
 * // 监听消息
 * onMessage((msg) => {
 *   console.log('收到消息:', msg.senderName, msg.content)
 *   // msg: { msgId, senderCode, senderName, senderAvatar, roomCode, roomType, msgType, content, timestamp }
 * })
 * 
 * // 发送消息
 * await sendMessage({
 *   roomCode: 'GROUP:abc123',
 *   roomType: 'GROUP',
 *   msgType: 'TEXT',
 *   content: 'Hello World'
 * })
 * 
 * // 在模板中使用状态
 * // <div v-if="isConnected">已连接</div>
 * // <div v-else>连接中...</div>
 * </script>
 * ```
 * 
 * @see WSManager: @/utils/ws.js - 底层 WebSocket 管理器
 */
export function useWebSocket() {
  const userStore = useUserStore()
  
  // 响应式状态
  const status = ref(WSManager.getStatus())
  const isConnected = ref(WSManager.isConnected())

  // 初始化事件监听
  function setupListeners() {
    // 连接成功
    WSManager.on('connect', () => {
      status.value = 'connected'
      isConnected.value = true
    })

    // 连接断开
    WSManager.on('disconnect', () => {
      status.value = 'closed'
      isConnected.value = false
    })

    // 重连成功
    WSManager.on('reconnect', () => {
      status.value = 'connected'
      isConnected.value = true
    })
  }

  /**
   * 连接 WebSocket
   * 自动从 userStore 或 localStorage 获取 token
   * 
   * @returns {Promise<void>}
   * @throws {Error} 如果未找到 token 或连接失败
   * 
   * 示例：
   * ```js
   * try {
   *   await connect()
   *   console.log('连接成功')
   * } catch (error) {
   *   console.error('连接失败:', error.message)
   * }
   * ```
   */
  async function connect(options = {}) {
    try {
      const token = userStore.token || localStorage.getItem('token')
      if (!token) {
        throw new Error('未找到 token')
      }
      await WSManager.connect(token, options)
      status.value = 'connected'
      isConnected.value = true
    } catch (error) {
      console.error('[useWebSocket] 连接失败', error)
      throw error
    }
  }

  /**
   * 断开 WebSocket 连接
   * 手动断开后不会自动重连
   * 
   * 示例：
   * ```js
   * // 用户登出时
   * function handleLogout() {
   *   disconnect()
   *   // 清除用户信息...
   * }
   * ```
   */
  function disconnect() {
    WSManager.disconnect()
    status.value = 'closed'
    isConnected.value = false
  }

  /**
   * 发送聊天消息
   * 
   * @param {Object} message - 消息内容
   * @param {String} message.roomCode - 房间编码
   * @param {String} message.roomType - 房间类型: PRIVATE | GROUP | CHANNEL
   * @param {String} message.msgType - 消息类型: TEXT | IMAGE | FILE（默认 TEXT）
   * @param {String} message.content - 消息内容（文本或文件URL）
   * @param {String} [message.fileName] - 文件名（IMAGE/FILE 消息时）
   * @param {Number} [message.fileSize] - 文件大小（字节，IMAGE/FILE 消息时）
   * @returns {Promise<Object>} 服务端 ACK 响应
   * 
   * 示例：
   * ```js
   * // 发送文本消息
   * await sendMessage({
   *   roomCode: 'GROUP:abc123',
   *   roomType: 'GROUP',
   *   msgType: 'TEXT',
   *   content: 'Hello'
   * })
   * 
   * // 发送图片消息
   * await sendMessage({
   *   roomCode: 'PRIVATE:user1:user2',
   *   roomType: 'PRIVATE',
   *   msgType: 'IMAGE',
   *   content: '/api/file/download/photo.jpg',
   *   fileName: 'photo.jpg',
   *   fileSize: 102400
   * })
   * ```
   */
  function sendMessage(message) {
    return WSManager.sendMessage(message)
  }

  /**
   * 同步消息
   * @param {Number} lastTimestamp - 最后收到的消息时间戳
   * @returns {Promise}
   */
  function syncMessages(lastTimestamp) {
    return WSManager.syncMessages(lastTimestamp)
  }

  /**
   * 监听聊天消息
   * 
   * @param {Function} callback - 消息回调函数
   * @param {Object} callback.body - 消息体
   * @param {String} callback.body.msgId - 消息ID
   * @param {String} callback.body.senderCode - 发送者编码
   * @param {String} callback.body.senderName - 发送者昵称
   * @param {String} callback.body.senderAvatar - 发送者头像
   * @param {String} callback.body.roomCode - 房间编码
   * @param {String} callback.body.roomType - 房间类型
   * @param {String} callback.body.msgType - 消息类型
   * @param {String} callback.body.content - 消息内容
   * @param {Number} callback.body.timestamp - 时间戳（毫秒）
   * 
   * 示例：
   * ```js
   * onMessage((msg) => {
   *   messages.value.push({
   *     id: msg.msgId,
   *     sender: msg.senderName,
   *     content: msg.content,
   *     time: new Date(msg.timestamp)
   *   })
   * })
   * ```
   */
  function onMessage(callback) {
    WSManager.on('message', callback)
  }

  /**
   * 监听通知
   * @param {Function} callback - (body) => void
   */
  function onNotify(callback) {
    WSManager.on('notify', callback)
  }

  /**
   * 监听系统消息
   * 系统消息用于通知重要事件（如被踢出、服务器维护等）
   * 
   * @param {Function} callback - 回调函数
   * @param {Object} callback.body - 消息体
   * @param {String} callback.body.content - 系统消息内容
   * 
   * 示例：
   * ```js
   * onSystem((msg) => {
   *   // "您的账号在其他设备登录"
   *   // "服务器维护中，请稍后重连"
   *   ElMessage.warning(msg.content)
   * })
   * ```
   */
  function onSystem(callback) {
    WSManager.on('system', callback)
  }

  /**
   * 监听错误事件
   * 
   * @param {Function} callback - 错误回调函数
   * @param {Error|Object} callback.error - 错误对象或错误消息
   * 
   * 示例：
   * ```js
   * onError((error) => {
   *   console.error('WebSocket 错误:', error)
   *   ElMessage.error('连接出错')
   * })
   * ```
   */
  function onError(callback) {
    WSManager.on('error', callback)
  }

  // 组件卸载时自动清理
  onUnmounted(() => {
    // 注意：不在这里自动断开连接
    // 因为 WebSocket 是全局的，可能在多个组件间共享
  })

  // 初始化监听器
  setupListeners()

  return {
    status,
    isConnected,
    connect,
    disconnect,
    sendMessage,
    syncMessages,
    onMessage,
    onNotify,
    onSystem,
    onError
  }
}
