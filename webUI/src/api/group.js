import request from '@/utils/http'

/**
 * 创建群组
 */
export function createGroup(data) {
  return request({ url: '/groups', method: 'post', data })
}

/**
 * 获取群组详情
 */
export function getGroup(code) {
  return request({ url: `/groups/${code}`, method: 'get' })
}

/**
 * 解散群组
 */
export function dismissGroup(code) {
  return request({ url: `/groups/${code}/dismiss`, method: 'post' })
}

/**
 * 添加群成员
 */
export function addGroupMember(code, userCode) {
  return request({ url: `/groups/${code}/members`, method: 'post', data: { userCode } })
}

/**
 * 移除群成员
 */
export function removeGroupMember(code, userCode) {
  return request({ url: `/groups/${code}/members/remove`, method: 'post', data: { userCode } })
}

/**
 * 获取我的群组列表
 */
export function listMyGroups(pageNum = 0, pageSize = 20) {
  return request({ url: '/groups/my', method: 'get', params: { pageNum, pageSize } })
}

/**
 * 搜索群组
 */
export function searchGroups(keyword, pageNum = 0, pageSize = 20) {
  return request({ url: '/groups/search', method: 'get', params: { keyword, pageNum, pageSize } })
}
