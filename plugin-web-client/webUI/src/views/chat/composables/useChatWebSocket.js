import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'

export function useChatWebSocket(options = {}) {
  const {
    onMessage,
    onNotify,
    onError,
    onConnected,
    onDisconnected
  } = options

  const ws = ref(null)
  const wsConnected = ref(false)
  let heartbeatTimer = null
  let reconnectTimer = null

  function connect(token) {
    if (!token) {
      ElMessage.error('未登录，无法连接')
      return
    }
    if (ws.value) return

    // const url = `ws://127.0.0.1:8081/ws-api/ws?token=${encodeURIComponent(token)}`
    const url = `wss://fish-chat-wss.935577.xyz/ws-api/ws?token=${encodeURIComponent(token)}`
    ws.value = new WebSocket(url)

    ws.value.onopen = () => {
      wsConnected.value = true
      startHeartbeat()
      onConnected?.()
    }

    ws.value.onmessage = (event) => {
      try {
        const pkt = JSON.parse(event.data)
        handlePacket(pkt)
      } catch (e) {
        console.warn('收到非JSON消息', event.data)
      }
    }

    ws.value.onclose = () => {
      wsConnected.value = false
      stopHeartbeat()
      ws.value = null
      onDisconnected?.()
      reconnectTimer = setTimeout(() => {
        if (!wsConnected.value) connect(token)
      }, 3000)
    }

    ws.value.onerror = () => {
      wsConnected.value = false
    }
  }

  function disconnect() {
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
  }

  function handlePacket(pkt) {
    switch (pkt.cmd) {
      case 'MSG':
        onMessage?.(pkt.body || {})
        break
      case 'NOTIFY':
        ElMessage.info(pkt.body?.content || '系统通知')
        onNotify?.(pkt.body)
        break
      case 'ERROR':
        ElMessage.error(pkt.body?.content || '消息发送失败')
        onError?.(pkt.body)
        break
      case 'ACK':
      case 'HEARTBEAT':
        break
    }
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (ws.value && ws.value.readyState === WebSocket.OPEN) {
        ws.value.send(JSON.stringify({ cmd: 'HEARTBEAT' }))
      }
    }, 30000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  function sendRaw(cmd, body) {
    if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
      ElMessage.error('WebSocket 未连接')
      return false
    }
    const pkt = { cmd, reqId: uuidv4(), body }
    ws.value.send(JSON.stringify(pkt))
    return true
  }

  function send(body) {
    return sendRaw('MSG', body)
  }

  onBeforeUnmount(disconnect)

  return {
    wsConnected,
    connect,
    disconnect,
    sendRaw,
    send
  }
}
