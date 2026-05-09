import request from '@/utils/http'

/**
 * 上传文件
 * @param {FormData} formData
 */
export function uploadFile(formData) {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 下载文件
 * @param {string} fileName
 */
export function downloadFileUrl(fileName) {
  return `/api/file/download/${fileName}`
}
