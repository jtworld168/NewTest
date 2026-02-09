const app = getApp()

/**
 * Send HTTP request to backend API
 */
function request(options) {
  const { url, method = 'GET', data, header = {} } = options
  const baseUrl = app.globalData.baseUrl

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        ...header
      },
      success(res) {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(res)
        }
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

// ==================== Auth ====================

function login(username, password) {
  return request({ url: '/auth/login', method: 'POST', data: { username, password } })
}

function logout() {
  return request({ url: '/auth/logout', method: 'POST' })
}

// ==================== User ====================

function register(user) {
  return request({ url: '/users/add', method: 'POST', data: user })
}

function getUserById(id) {
  return request({ url: '/users/get/' + id })
}

function updateUser(user) {
  return request({ url: '/users/update', method: 'PUT', data: user })
}

// ==================== Product ====================

function getProductList() {
  return request({ url: '/products/list' })
}

function getProductById(id) {
  return request({ url: '/products/get/' + id })
}

function getProductsByCategoryId(categoryId) {
  return request({ url: '/products/getByCategoryId/' + categoryId })
}

function searchProducts(keyword) {
  return request({ url: '/products/searchAll?keyword=' + encodeURIComponent(keyword) })
}

// ==================== Category ====================

function getCategoryList() {
  return request({ url: '/categories/list' })
}

// ==================== Order ====================

function getOrdersByUserId(userId) {
  return request({ url: '/orders/getByUserId/' + userId })
}

function addOrder(userId, productId, quantity, userCouponId) {
  let url = '/orders/add?userId=' + userId + '&productId=' + productId + '&quantity=' + quantity
  if (userCouponId) {
    url += '&userCouponId=' + userCouponId
  }
  return request({ url: url, method: 'POST' })
}

// ==================== Cart ====================

function getCartByUserId(userId) {
  return request({ url: '/cart-items/getByUserId/' + userId })
}

function addCartItem(cartItem) {
  return request({ url: '/cart-items/add', method: 'POST', data: cartItem })
}

function updateCartItem(cartItem) {
  return request({ url: '/cart-items/update', method: 'PUT', data: cartItem })
}

function deleteCartItem(id) {
  return request({ url: '/cart-items/delete/' + id, method: 'DELETE' })
}

function clearCart(userId) {
  return request({ url: '/cart-items/clear/' + userId, method: 'DELETE' })
}

// ==================== Coupon ====================

function getCouponById(id) {
  return request({ url: '/coupons/get/' + id })
}

// ==================== UserCoupon ====================

function getUserCouponsByUserId(userId) {
  return request({ url: '/user-coupons/getByUserId/' + userId })
}

function getUserCouponsByUserIdAndStatus(userId, status) {
  return request({ url: '/user-coupons/getByUserIdAndStatus?userId=' + userId + '&status=' + status })
}

// ==================== File ====================

function getFileUrl(filename) {
  return app.globalData.baseUrl.replace('/api', '') + '/file/' + filename
}

module.exports = {
  request,
  login,
  logout,
  register,
  getUserById,
  updateUser,
  getProductList,
  getProductById,
  getProductsByCategoryId,
  searchProducts,
  getCategoryList,
  getOrdersByUserId,
  addOrder,
  getCartByUserId,
  addCartItem,
  updateCartItem,
  deleteCartItem,
  clearCart,
  getCouponById,
  getUserCouponsByUserId,
  getUserCouponsByUserIdAndStatus,
  getFileUrl
}
