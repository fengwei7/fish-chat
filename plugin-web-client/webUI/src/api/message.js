import request from '@/utils/http'

/**
 * 获取房间历史消息
 */
export function getHistoryMessages(roomCode, page = 0, size = 20) {
  return request({ url: `/messages/${roomCode}`, method: 'get', params: { page, size } })
}

/**
 * 同步指定时间之后的消息（断线重连用）
 */
export function syncMessages(roomCode, after) {
  return request({ url: `/messages/${roomCode}/sync`, method: 'get', params: { after } })
}
