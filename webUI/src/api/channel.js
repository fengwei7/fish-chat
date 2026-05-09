import request from '@/utils/http'

/**
 * 创建频道
 */
export function createChannel(data) {
  return request({ url: '/channels', method: 'post', data })
}

/**
 * 获取频道详情
 */
export function getChannel(code) {
  return request({ url: `/channels/${code}`, method: 'get' })
}

/**
 * 订阅频道
 */
export function subscribeChannel(code) {
  return request({ url: `/channels/${code}/subscribe`, method: 'post' })
}

/**
 * 取消订阅频道
 */
export function unsubscribeChannel(code) {
  return request({ url: `/channels/${code}/unsubscribe`, method: 'post' })
}

/**
 * 获取我的频道列表
 */
export function listMyChannels(pageNum = 0, pageSize = 20) {
  return request({ url: '/channels/my', method: 'get', params: { pageNum, pageSize } })
}

/**
 * 搜索频道
 */
export function searchChannels(keyword, pageNum = 0, pageSize = 20) {
  return request({ url: '/channels/search', method: 'get', params: { keyword, pageNum, pageSize } })
}
