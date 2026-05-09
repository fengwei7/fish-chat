import request from '@/utils/http'

/**
 * 获取当前用户信息
 */
export function getProfile() {
  return request({ url: '/user/profile', method: 'get' })
}

/**
 * 更新当前用户信息
 */
export function updateProfile(data) {
  return request({ url: '/user/profile', method: 'post', data })
}

/**
 * 根据code获取用户信息
 */
export function getUserByCode(code) {
  return request({ url: `/user/${code}`, method: 'get' })
}

/**
 * 获取在线用户列表
 */
export function getOnlineUsers(pageNum = 0, pageSize = 20) {
  return request({ url: '/user/online', method: 'get', params: { pageNum, pageSize } })
}

/**
 * 搜索用户
 */
export function searchUsers(keyword, pageNum = 0, pageSize = 20) {
  return request({ url: '/user/search', method: 'get', params: { keyword, pageNum, pageSize } })
}
