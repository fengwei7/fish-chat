import http from '@/utils/http.js'

/**
 * 群组相关 API
 */

/**
 * 创建群组
 * @param {Object} data - { name, avatar? }
 * @returns {Promise}
 */
export function createGroup(data) {
  return http.post('/groups', data)
}

/**
 * 获取群组详情
 * @param {String} code - 群组编码
 * @returns {Promise}
 */
export function getGroup(code) {
  return http.get(`/groups/${code}`)
}

/**
 * 获取群组公告
 * @param {String} code - 群组编码
 * @returns {Promise}
 */
export function getGroupNotice(code) {
  return http.get(`/groups/${code}/notice`)
}

/**
 * 更新群组信息
 * @param {String} code - 群组编码
 * @param {Object} data - { name?, avatar?, notice? }
 * @returns {Promise}
 */
export function updateGroup(code, data) {
  return http.post(`/groups/update/${code}`, data)
}

/**
 * 解散群组
 * @param {String} code - 群组编码
 * @returns {Promise}
 */
export function dismissGroup(code) {
  return http.post(`/groups/${code}/dismiss`)
}

/**
 * 退出群组
 * @param {String} code - 群组编码
 * @returns {Promise}
 */
export function leaveGroup(code) {
  return http.post(`/groups/${code}/leave`)
}

/**
 * 设置/取消群组管理员
 * @param {String} code - 群组编码
 * @param {String} userCode - 用户编码
 * @param {Boolean} isAdmin - 是否设为管理员
 * @returns {Promise}
 */
export function setGroupAdmin(code, userCode, isAdmin) {
  return http.post(`/groups/${code}/admin/${userCode}`, null, {
    params: { isAdmin }
  })
}

/**
 * 添加群组成员
 * @param {String} code - 群组编码
 * @param {Object} data - { userCode }
 * @returns {Promise}
 */
export function addGroupMember(code, data) {
  return http.post(`/groups/${code}/members`, data)
}

/**
 * 移除群组成员
 * @param {String} code - 群组编码
 * @param {Object} data - { userCode }
 * @returns {Promise}
 */
export function removeGroupMember(code, data) {
  return http.post(`/groups/${code}/members/remove`, data)
}

/**
 * 获取我加入的群组列表（分页）
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function listMyGroups(params = {}) {
  return http.get('/groups/my', { params })
}

/**
 * 搜索群组（分页）
 * @param {Object} params - { keyword, pageNum?, pageSize? }
 * @returns {Promise}
 */
export function searchGroups(params) {
  return http.get('/groups/search', { params })
}

/**
 * 获取群组成员列表（分页）
 * @param {String} code - 群组编码
 * @param {Object} params - { pageNum?, pageSize? }
 * @returns {Promise}
 */
export function listGroupMembers(code, params = {}) {
  return http.get(`/groups/${code}/members`, { params })
}
