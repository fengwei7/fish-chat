import http from '@/utils/http.js'

/**
 * 消息相关 API
 */

/**
 * 获取房间历史消息（分页）
 * @param {String} roomCode - 房间编码
 * @param {Object} params - { page?, size? }
 * @returns {Promise}
 */
export function getHistoryMessages(roomCode, params = {}) {
  return http.get(`/messages/${roomCode}`, { params })
}

/**
 * 同步指定时间点之后的消息（断线重连用）
 * @param {String} roomCode - 房间编码
 * @param {Object} params - { after: 时间戳 }
 * @returns {Promise}
 */
export function syncMessages(roomCode, params) {
  return http.get(`/messages/${roomCode}/sync`, { params })
}
