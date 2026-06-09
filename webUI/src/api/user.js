import http from '@/utils/http.js'

/**
 * 用户资料相关 API
 */

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getProfile() {
  return http.get('/user/profile')
}

/**
 * 更新当前用户信息
 * @param {Object} data - 用户信息 { nickname?, avatarUrl?, signature? }
 * @returns {Promise}
 */
export function updateProfile(data) {
  return http.post('/user/profile', data)
}

/**
 * 根据用户 code 获取用户信息
 * @param {String} code - 用户编码
 * @returns {Promise}
 */
export function getUserByCode(code) {
  return http.get(`/user/${code}`)
}

/**
 * 获取在线用户列表（分页）
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function getOnlineUsers(params = {}) {
  return http.get('/user/online', { params })
}

/**
 * 搜索用户（分页）
 * @param {Object} params - { keyword, pageNum?, pageSize? }
 * @returns {Promise}
 */
export function searchUsers(params) {
  return http.get('/user/search', { params })
}
