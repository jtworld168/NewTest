const app = getApp()

/**
 * Send HTTP request to backend API
 */
function request(options) {
  const { url, method = 'GET', data, header = {} } = options
  const baseUrl = app.globalData.baseUrl
  const token = wx.getStorageSync('satoken') || ''

  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        'satoken': token,
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
  return request({ url: '/products/onShelf' })
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

function getOrderById(id) {
  return request({ url: '/orders/get/' + id })
}

function addOrder(userId, productId, quantity, userCouponId) {
  let url = '/orders/add?userId=' + userId + '&productId=' + productId + '&quantity=' + quantity
  if (userCouponId) {
    url += '&userCouponId=' + userCouponId
  }
  return request({ url: url, method: 'POST' })
}

function addMultiItemOrder(userId, items, userCouponId, storeId) {
  var data = { userId: userId, items: items }
  if (userCouponId) {
    data.userCouponId = userCouponId
  }
  if (storeId) {
    data.storeId = storeId
  }
  return request({ url: '/orders/addMultiItem', method: 'POST', data: data })
}

function getOrderItemsByOrderId(orderId) {
  return request({ url: '/order-items/getByOrderId/' + orderId })
}

function updateOrder(order) {
  return request({ url: '/orders/update', method: 'PUT', data: order })
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

function updateUserCoupon(userCoupon) {
  return request({ url: '/user-coupons/update', method: 'PUT', data: userCoupon })
}

// ==================== Payment ====================

function addPayment(payment) {
  return request({ url: '/payments/add', method: 'POST', data: payment })
}

function getPaymentsByOrderId(orderId) {
  return request({ url: '/payments/getByOrderId/' + orderId })
}

// ==================== Store ====================

function getStoreList() {
  return request({ url: '/stores/list' })
}

function getStoreById(id) {
  return request({ url: '/stores/get/' + id })
}

// ==================== File ====================

function getFileUrl(filename) {
  if (!filename) return ''
  // DB stores: /file/xxx.jpg
  // We need: http://localhost:8080/api/file/xxx.jpg
  // baseUrl = http://localhost:8080/api
  var name = filename.replace(/^\/file\//, '')
  return app.globalData.baseUrl + '/file/' + name
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
  getOrderById,
  addOrder,
  addMultiItemOrder,
  getOrderItemsByOrderId,
  updateOrder,
  getCartByUserId,
  addCartItem,
  updateCartItem,
  deleteCartItem,
  clearCart,
  getCouponById,
  getUserCouponsByUserId,
  getUserCouponsByUserIdAndStatus,
  updateUserCoupon,
  addPayment,
  getPaymentsByOrderId,
  getFileUrl,
  getStoreList,
  getStoreById
}
