import request from '@/utils/request'

/**
 * 查询挂账公司/协议单位列表
 * @param {Object} params - 查询参数
 * @returns {Promise} 挂账公司列表
 */
export function getCreditCompanyList(params) {
  return request({
    url: '/v1/credit-companies',
    method: 'get',
    params
  })
}

/**
 * 查询挂账公司/协议单位详情
 * @param {number} id - 挂账公司ID
 * @returns {Promise} 挂账公司信息
 */
export function getCreditCompanyById(id) {
  return request({
    url: `/v1/credit-companies/${id}`,
    method: 'get'
  })
}

/**
 * 创建挂账公司/协议单位
 * @param {Object} data - 挂账公司信息
 * @returns {Promise} 挂账公司信息
 */
export function createCreditCompany(data) {
  return request({
    url: '/v1/credit-companies',
    method: 'post',
    data
  })
}

/**
 * 更新挂账公司/协议单位
 * @param {number} id - 挂账公司ID
 * @param {Object} data - 挂账公司信息
 * @returns {Promise} 挂账公司信息
 */
export function updateCreditCompany(id, data) {
  return request({
    url: `/v1/credit-companies/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除挂账公司/协议单位
 * @param {number} id - 挂账公司ID
 * @returns {Promise} 操作结果
 */
export function deleteCreditCompany(id) {
  return request({
    url: `/v1/credit-companies/${id}`,
    method: 'delete'
  })
}

/**
 * 查询挂账明细
 * @param {number} companyId - 挂账公司ID
 * @returns {Promise} 挂账明细列表
 */
export function getCreditTransactions(companyId) {
  return request({
    url: `/v1/credit-companies/${companyId}/transactions`,
    method: 'get'
  })
}

/**
 * 查询协议价列表
 * @param {number} companyId - 协议公司ID
 * @returns {Promise} 协议价列表
 */
export function getAgreementPrices(companyId) {
  return request({
    url: `/v1/credit-companies/${companyId}/agreement-prices`,
    method: 'get'
  })
}

/**
 * 创建协议价
 * @param {number} companyId - 协议公司ID
 * @param {Object} data - 协议价信息
 * @returns {Promise} 协议价信息
 */
export function createAgreementPrice(companyId, data) {
  return request({
    url: `/v1/credit-companies/${companyId}/agreement-prices`,
    method: 'post',
    data
  })
}

/**
 * 更新协议价
 * @param {number} id - 协议价ID
 * @param {Object} data - 协议价信息
 * @returns {Promise} 协议价信息
 */
export function updateAgreementPrice(id, data) {
  return request({
    url: `/v1/credit-companies/agreement-prices/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除协议价
 * @param {number} id - 协议价ID
 * @returns {Promise} 操作结果
 */
export function deleteAgreementPrice(id) {
  return request({
    url: `/v1/credit-companies/agreement-prices/${id}`,
    method: 'delete'
  })
}

/**
 * 获取协议价详情
 * @param {number} id - 协议价ID
 * @returns {Promise} 协议价详情
 */
export function getAgreementPriceById(id) {
  return request({
    url: `/v1/credit-companies/agreement-prices/${id}`,
    method: 'get'
  })
}
