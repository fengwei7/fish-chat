import http from '@/utils/http.js'

/**
 * 频道相关 API
 */

/**
 * 创建频道
 * @param {Object} data - { name, avatar?, description? }
 * @returns {Promise}
 */
export function createChannel(data) {
  return http.post('/channels', data)
}

/**
 * 获取频道详情
 * @param {String} code - 频道编码
 * @returns {Promise}
 */
export function getChannel(code) {
  return http.get(`/channels/${code}`)
}

/**
 * 订阅频道
 * @param {String} code - 频道编码
 * @returns {Promise}
 */
export function subscribeChannel(code) {
  return http.post(`/channels/${code}/subscribe`)
}

/**
 * 取消订阅频道
 * @param {String} code - 频道编码
 * @returns {Promise}
 */
export function unsubscribeChannel(code) {
  return http.post(`/channels/${code}/unsubscribe`)
}

/**
 * 获取我订阅的频道列表（分页）
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function listMyChannels(params = {}) {
  return http.get('/channels/my', { params })
}

/**
 * 搜索频道（分页）
 * @param {Object} params - { keyword, pageNum?, pageSize? }
 * @returns {Promise}
 */
export function searchChannels(params) {
  return http.get('/channels/search', { params })
}

/**
 * 转让频道
 * @param {String} code - 频道编码
 * @param {Object} data - { newOwnerCode }
 * @returns {Promise}
 */
export function transferChannel(code, data) {
  return http.post(`/channels/${code}/transfer`, data)
}

/**
 * 设置/取消频道管理员
 * @param {String} code - 频道编码
 * @param {String} userCode - 用户编码
 * @param {Object} data - { isAdmin }
 * @returns {Promise}
 */
export function setChannelAdmin(code, userCode, data) {
  return http.post(`/channels/${code}/admin/${userCode}`, data)
}
