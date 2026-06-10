/**
 * WebSocket 全局连接管理器（单例）
 * 
 * 职责：
 * - WebSocket 连接/断线重连
 * - 消息发送与响应追踪（reqCode 机制）
 * - 心跳保活（30s 间隔）
 * - 事件分发（消息、通知、系统消息等）
 * 
 * 消息协议（与后端 ChatMessagePacket 对应）：
 * - cmd: MSG | ACK | ERROR | NOTIFY | HEARTBEAT | SYNC
 * - reqCode: 请求追踪ID（客户端生成，服务端回显）
 * - body: 消息体 { msgId, senderCode, senderName, senderAvatar, roomCode, roomType, msgType, content, fileName, fileSize, timestamp, extra }
 * 
 * 房间类型：
 * - PRIVATE: 私聊，roomCode 格式 'PRIVATE:userCode1:userCode2'
 * - GROUP: 群聊，roomCode 格式 'GROUP:groupCode'
 * - CHANNEL: 频道，roomCode 格式 'CHANNEL:channelCode'
 * 
 * 消息类型：
 * - TEXT: 文本消息
 * - IMAGE: 图片消息（content 为图片URL）
 * - FILE: 文件消息（content 为文件URL）
 * - SYSTEM: 系统消息
 * 
 * 使用示例：
 * ```js
 * import WSManager from '@/utils/ws.js'
 * 
 * // 连接
 * await WSManager.connect(token)
 * 
 * // 监听消息
 * WSManager.on('message', (body) => {
 *   console.log('收到消息:', body.content)
 * })
 * 
 * // 发送消息
 * await WSManager.sendMessage({
 *   roomCode: 'GROUP:abc123',
 *   roomType: 'GROUP',
 *   msgType: 'TEXT',
 *   content: 'Hello'
 * })
 * 
 * // 断开
 * WSManager.disconnect()
 * ```
 * 
 * @see composable: @/composables/useWebSocket.js - Vue 3 组合式函数（推荐在组件中使用）
 */

class WSManager {
  constructor() {
    // WebSocket 实例
    this.ws = null
    
    // 连接状态
    this.status = 'closed' // closed | connecting | connected | reconnecting
    
    // 用户信息
    this.userCode = null
    this.token = null
    
    // 服务器地址
    this.wsUrl = null
    
    // 重连配置
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 10
    this.reconnectDelay = 3000 // 初始重连延迟 3s
    this.reconnectMaxDelay = 30000 // 最大重连延迟 30s
    
    // 心跳配置
    this.heartbeatInterval = 30000 // 30s 发送一次心跳
    this.heartbeatTimer = null
    this.heartbeatTimeout = 10000 // 心跳超时 10s
    this.heartbeatTimer = null
    
    // 消息追踪
    this.pendingRequests = new Map() // reqCode -> { resolve, reject, timer }
    
    // 事件监听器
    this.listeners = {
      message: [],      // 收到聊天消息
      notify: [],       // 收到通知
      system: [],       // 系统消息
      error: [],        // 错误消息
      connect: [],      // 连接成功
      disconnect: [],   // 连接断开
      reconnect: []     // 重连成功
    }
    
    // 是否手动关闭
    this.manualClose = false
  }

  /**
   * 连接 WebSocket
   * 
   * @param {String} token - Sa-Token（从 userStore 或 localStorage 获取）
   * @param {Object} options - 可选配置
   * @param {String} options.wsUrl - WebSocket 服务器地址（默认自动构建）
   * @param {Function} options.onMessage - 消息回调
   * @param {Function} options.onError - 错误回调
   * @param {Function} options.onConnect - 连接成功回调
   * @param {Function} options.onDisconnect - 断开连接回调
   * @returns {Promise<void>}
   * 
   * 示例：
   * ```js
   * // 基础用法
   * await WSManager.connect(token)
   * 
   * // 带回调
   * await WSManager.connect(token, {
   *   onConnect: () => console.log('连接成功'),
   *   onDisconnect: (e) => console.log('断开', e.code, e.reason)
   * })
   * ```
   */
  connect(token, options = {}) {
    if (this.status === 'connected' || this.status === 'connecting') {
      console.warn('[WS] 已在连接中或已连接')
      return Promise.resolve()
    }

    this.token = token
    this.manualClose = false

    // 构建 WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    this.wsUrl = options.wsUrl || `${protocol}//${host}/ws-api`

    // 如果 token 在 query 中传递（握手认证）
    const url = `${this.wsUrl}?token=${token}`

    return new Promise((resolve, reject) => {
      try {
        this.status = 'connecting'
        this.ws = new WebSocket(url)

        // 连接成功
        this.ws.onopen = () => {
          this.status = 'connected'
          this.reconnectAttempts = 0
          console.log('[WS] 连接成功')
          
          // 启动心跳
          this.startHeartbeat()
          
          // 触发连接事件
          this.emit('connect')
          if (options.onConnect) options.onConnect()
          
          resolve()
        }

        // 收到消息
        this.ws.onmessage = (event) => {
          this.handleMessage(event.data, options.onMessage)
        }

        // 连接关闭
        this.ws.onclose = (event) => {
          this.status = 'closed'
          this.stopHeartbeat()
          console.log('[WS] 连接关闭', event.code, event.reason)
          
          // 触发断开事件
          this.emit('disconnect', { code: event.code, reason: event.reason })
          if (options.onDisconnect) options.onDisconnect({ code: event.code, reason: event.reason })
          
          // 非手动关闭时自动重连
          if (!this.manualClose) {
            this.reconnect()
          }
        }

        // 连接错误
        this.ws.onerror = (error) => {
          console.error('[WS] 连接错误', error)
          this.emit('error', error)
          if (options.onError) options.onError(error)
          reject(error)
        }
      } catch (error) {
        console.error('[WS] 创建连接失败', error)
        reject(error)
      }
    })
  }

  /**
   * 断开连接（手动关闭，不会自动重连）
   * 
   * 示例：
   * ```js
   * // 用户登出时
   * WSManager.disconnect()
   * ```
   */
  disconnect() {
    this.manualClose = true
    this.stopHeartbeat()
    
    // 拒绝所有 pending 请求
    this.pendingRequests.forEach(({ reject }) => {
      reject(new Error('连接已关闭'))
    })
    this.pendingRequests.clear()
    
    if (this.ws) {
      this.ws.close(1000, '客户端主动断开')
      this.ws = null
    }
    
    this.status = 'closed'
    console.log('[WS] 手动断开连接')
  }

  /**
   * 发送聊天消息（自动等待 ACK 响应）
   * 
   * @param {Object} message - 消息内容
   * @param {String} message.roomCode - 房间编码
   * @param {String} message.roomType - 房间类型: PRIVATE | GROUP | CHANNEL
   * @param {String} message.msgType - 消息类型: TEXT | IMAGE | FILE（默认 TEXT）
   * @param {String} message.content - 消息内容（文本或文件URL）
   * @param {String} [message.fileName] - 文件名（IMAGE/FILE 消息时）
   * @param {Number} [message.fileSize] - 文件大小（IMAGE/FILE 消息时）
   * @param {Object} [message.extra] - 扩展字段
   * @returns {Promise<Object>} 服务端 ACK 响应
   * 
   * 示例：
   * ```js
   * // 发送文本
   * await WSManager.sendMessage({
   *   roomCode: 'GROUP:abc123',
   *   roomType: 'GROUP',
   *   msgType: 'TEXT',
   *   content: 'Hello World'
   * })
   * 
   * // 发送图片
   * await WSManager.sendMessage({
   *   roomCode: 'PRIVATE:user1:user2',
   *   roomType: 'PRIVATE',
   *   msgType: 'IMAGE',
   *   content: '/api/file/download/image.png',
   *   fileName: 'photo.jpg',
   *   fileSize: 102400
   * })
   * ```
   */
  sendMessage(message) {
    return this.sendWithAck('MSG', {
      roomCode: message.roomCode,
      roomType: message.roomType,
      msgType: message.msgType || 'TEXT',
      content: message.content,
      fileName: message.fileName,
      fileSize: message.fileSize,
      extra: message.extra
    })
  }

  /**
   * 发送心跳
   */
  sendHeartbeat() {
    return this.send('HEARTBEAT', null, false)
  }

  /**
   * 同步消息（断线重连后拉取未收消息）
   * 
   * @param {Number} lastTimestamp - 最后收到的消息时间戳（毫秒）
   * @returns {Promise<Object>} 服务端 ACK 响应
   * 
   * 示例：
   * ```js
   * WSManager.on('reconnect', async () => {
   *   const lastTs = getLastMessageTimestamp()
   *   await WSManager.syncMessages(lastTs)
   * })
   * ```
   */
  syncMessages(lastTimestamp) {
    return this.sendWithAck('SYNC', {
      extra: {
        lastTimestamp: lastTimestamp
      }
    })
  }

  /**
   * 发送消息并等待 ACK
   * @param {String} cmd - 命令类型
   * @param {Object} body - 消息体
   * @param {Number} timeout - 超时时间（默认 5s）
   * @returns {Promise}
   */
  sendWithAck(cmd, body, timeout = 5000) {
    if (this.status !== 'connected') {
      return Promise.reject(new Error('WebSocket 未连接'))
    }

    const reqCode = this.generateReqCode()
    
    return new Promise((resolve, reject) => {
      // 设置超时
      const timer = setTimeout(() => {
        if (this.pendingRequests.has(reqCode)) {
          this.pendingRequests.delete(reqCode)
          reject(new Error('消息发送超时'))
        }
      }, timeout)

      // 保存 pending 请求
      this.pendingRequests.set(reqCode, { resolve, reject, timer })

      // 发送消息
      this.send(cmd, body, true, reqCode)
    })
  }

  /**
   * 发送消息（底层）
   * @param {String} cmd - 命令
   * @param {Object} body - 消息体
   * @param {Boolean} needAck - 是否需要 ACK
   * @param {String} reqCode - 请求追踪ID
   */
  send(cmd, body, needAck = false, reqCode = null) {
    if (this.status !== 'connected') {
      console.warn('[WS] 连接未建立，无法发送消息')
      return false
    }

    const packet = {
      cmd: cmd,
      reqCode: needAck ? (reqCode || this.generateReqCode()) : null,
      body: body
    }

    try {
      this.ws.send(JSON.stringify(packet))
      return true
    } catch (error) {
      console.error('[WS] 发送消息失败', error)
      return false
    }
  }

  /**
   * 处理收到的消息
   * @param {String} data - JSON 字符串
   * @param {Function} customMessageHandler - 自定义消息处理器
   */
  handleMessage(data, customMessageHandler) {
    try {
      const packet = JSON.parse(data)
      const { cmd, code, reqCode, body } = packet

      // ACK 响应
      if (cmd === 'ACK' && reqCode) {
        const pending = this.pendingRequests.get(reqCode)
        if (pending) {
          clearTimeout(pending.timer)
          this.pendingRequests.delete(reqCode)
          pending.resolve(packet)
        }
        return
      }

      // ERROR 响应
      if (cmd === 'ERROR' && reqCode) {
        const pending = this.pendingRequests.get(reqCode)
        if (pending) {
          clearTimeout(pending.timer)
          this.pendingRequests.delete(reqCode)
          pending.reject(new Error(body?.content || '操作失败'))
        }
        this.emit('error', packet)
        return
      }

      // 聊天消息
      if (cmd === 'MSG') {
        this.emit('message', body)
        if (customMessageHandler) customMessageHandler(body)
        return
      }

      // 通知
      if (cmd === 'NOTIFY') {
        const msgType = body?.msgType
        if (msgType === 'SYSTEM') {
          this.emit('system', body)
        } else {
          this.emit('notify', body)
        }
        return
      }

      // 心跳响应
      if (cmd === 'HEARTBEAT') {
        // 心跳正常，无需处理
        return
      }

      console.warn('[WS] 未知消息类型', cmd, packet)
    } catch (error) {
      console.error('[WS] 解析消息失败', error, data)
    }
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[WS] 达到最大重连次数，停止重连')
      this.emit('error', new Error('达到最大重连次数'))
      return
    }

    this.status = 'reconnecting'
    this.reconnectAttempts++

    // 指数退避延迟
    const delay = Math.min(
      this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1),
      this.reconnectMaxDelay
    )

    console.log(`[WS] 准备重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})，延迟 ${delay}ms`)

    setTimeout(() => {
      if (!this.manualClose) {
        console.log('[WS] 开始重连...')
        this.connect(this.token).then(() => {
          console.log('[WS] 重连成功')
          this.emit('reconnect')
        }).catch(err => {
          console.error('[WS] 重连失败', err)
        })
      }
    }, delay)
  }

  /**
   * 启动心跳
   */
  startHeartbeat() {
    this.stopHeartbeat()
    
    this.heartbeatTimer = setInterval(() => {
      if (this.status === 'connected') {
        this.sendHeartbeat()
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 注册事件监听器
   * 
   * @param {String} event - 事件类型
   *   - 'message': 收到聊天消息，回调参数 (body: Object)
   *   - 'notify': 收到通知，回调参数 (body: Object)
   *   - 'system': 系统消息（如被踢出），回调参数 (body: Object)
   *   - 'error': 错误，回调参数 (error: Error|Object)
   *   - 'connect': 连接成功，无参数
   *   - 'disconnect': 连接断开，回调参数 ({ code: Number, reason: String })
   *   - 'reconnect': 重连成功，无参数
   * @param {Function} callback - 回调函数
   * 
   * 示例：
   * ```js
   * WSManager.on('message', (body) => {
   *   console.log('收到消息:', body.senderName, body.content)
   *   // body: { msgId, senderCode, senderName, senderAvatar, roomCode, roomType, msgType, content, timestamp }
   * })
   * 
   * WSManager.on('system', (body) => {
   *   // 系统消息："您的账号在其他设备登录"
   *   alert(body.content)
   * })
   * 
   * WSManager.on('reconnect', () => {
   *   console.log('重连成功')
   * })
   * ```
   */
  on(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event].push(callback)
    }
  }

  /**
   * 移除事件监听器
   * @param {String} event - 事件类型
   * @param {Function} callback - 回调函数
   */
  off(event, callback) {
    if (this.listeners[event]) {
      const index = this.listeners[event].indexOf(callback)
      if (index > -1) {
        this.listeners[event].splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   * @param {String} event - 事件类型
   * @param  {...any} args - 事件参数
   */
  emit(event, ...args) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(...args)
        } catch (error) {
          console.error(`[WS] 事件回调执行失败: ${event}`, error)
        }
      })
    }
  }

  /**
   * 生成请求追踪ID
   */
  generateReqCode() {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 获取连接状态
   * @returns {String} 'closed' | 'connecting' | 'connected' | 'reconnecting'
   */
  getStatus() {
    return this.status
  }

  /**
   * 是否已连接
   */
  isConnected() {
    return this.status === 'connected'
  }
}

// 导出单例
export default new WSManager()
