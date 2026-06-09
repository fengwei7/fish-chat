import http from '@/utils/http.js'

/**
 * 文件上传相关 API
 */

/**
 * 上传文件
 * @param {FormData} formData - 包含文件的 FormData 对象
 * @returns {Promise}
 */
export function uploadFile(formData) {
  return http.post('/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 下载文件
 * @param {String} fileName - 文件名
 * @returns {Promise<Blob>}
 */
export function downloadFile(fileName) {
  return http.get(`/file/download/${fileName}`, {
    responseType: 'blob'
  })
}
