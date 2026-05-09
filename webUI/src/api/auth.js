/**
 * 认证相关的API接口
 */
import request from '@/utils/http'

/**
 * 用户登录
 * @param {Object} data - 登录数据 { username, password }
 * @returns {Promise<AuthDTO>}
 */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册数据 { username, password, email, nickname, mobile }
 * @returns {Promise<Boolean>}
 */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}