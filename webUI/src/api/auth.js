import http from '@/utils/http.js'

/**
 * 认证相关 API
 */

/**
 * 用户注册
 * @param {Object} data - 注册信息 { username, password, nickname? }
 * @returns {Promise}
 */
export function register(data) {
  return http.post('/auth/register', data)
}

/**
 * 用户登录
 * @param {Object} data - 登录信息 { username, password }
 * @returns {Promise}
 */
export function login(data) {
  return http.post('/auth/login', data)
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return http.post('/auth/logout')
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 * @returns {Promise}
 */
export function changePassword(data) {
  return http.post('/auth/password', data)
}
