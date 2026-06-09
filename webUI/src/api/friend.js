import http from '@/utils/http.js'

/**
 * 好友相关 API
 */

/**
 * 添加好友（发送好友申请）
 * @param {Object} data - { friendCode, remark? }
 * @returns {Promise}
 */
export function addFriend(data) {
  return http.post('/friends', data)
}

/**
 * 接受好友申请
 * @param {Object} data - { friendCode }
 * @returns {Promise}
 */
export function acceptFriend(data) {
  return http.post('/friends/accept', data)
}

/**
 * 拒绝好友申请
 * @param {Object} data - { friendCode }
 * @returns {Promise}
 */
export function rejectFriend(data) {
  return http.post('/friends/reject', data)
}

/**
 * 更新好友备注
 * @param {String} friendCode - 好友编码
 * @param {Object} data - { remark }
 * @returns {Promise}
 */
export function updateFriendRemark(friendCode, data) {
  return http.put(`/friends/${friendCode}/remark`, data)
}

/**
 * 删除好友
 * @param {Object} data - { friendCode }
 * @returns {Promise}
 */
export function removeFriend(data) {
  return http.post('/friends/remove', data)
}

/**
 * 获取好友列表（分页）
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function listFriends(params = {}) {
  return http.get('/friends', { params })
}

/**
 * 获取好友申请列表（分页）
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function listFriendRequests(params = {}) {
  return http.get('/friends/requests', { params })
}

/**
 * 搜索用户（用于添加好友）
 * @param {Object} params - { keyword, pageNum?, pageSize? }
 * @returns {Promise}
 */
export function searchFriends(params) {
  return http.get('/friends/search', { params })
}
