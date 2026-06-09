import http from '@/utils/http.js'

/**
 * 会话相关 API
 */

/**
 * 获取会话列表
 * @param {Object} params - { limit? }
 * @returns {Promise}
 */
export function listConversations(params = {}) {
  return http.get('/conversations', { params })
}

/**
 * 标记会话为已读
 * @param {String} roomCode - 房间编码
 * @returns {Promise}
 */
export function markAsRead(roomCode) {
  return http.post(`/conversations/${roomCode}/read`)
}

/**
 * 获取总未读消息数
 * @returns {Promise}
 */
export function getTotalUnreadCount() {
  return http.get('/conversations/unread/total')
}

/**
 * 删除会话
 * @param {String} roomCode - 房间编码
 * @returns {Promise}
 */
export function removeConversation(roomCode) {
  return http.delete(`/conversations/${roomCode}`)
}
