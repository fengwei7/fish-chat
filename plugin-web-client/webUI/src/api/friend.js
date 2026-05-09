import request from '@/utils/http'

/**
 * 添加好友
 */
export function addFriend(friendCode, remark) {
  return request({ url: '/friends', method: 'post', data: { friendCode, remark } })
}

/**
 * 接受好友申请
 */
export function acceptFriend(friendCode) {
  return request({ url: '/friends/accept', method: 'post', data: { friendCode } })
}

/**
 * 删除好友
 */
export function removeFriend(friendCode) {
  return request({ url: '/friends/remove', method: 'post', data: { friendCode } })
}

/**
 * 获取好友列表
 */
export function listFriends(pageNum = 0, pageSize = 20) {
  return request({ url: '/friends', method: 'get', params: { pageNum, pageSize } })
}

/**
 * 获取好友申请列表
 */
export function listFriendRequests(pageNum = 0, pageSize = 20) {
  return request({ url: '/friends/requests', method: 'get', params: { pageNum, pageSize } })
}

/**
 * 搜索好友
 */
export function searchFriends(keyword, pageNum = 0, pageSize = 20) {
  return request({ url: '/friends/search', method: 'get', params: { keyword, pageNum, pageSize } })
}
