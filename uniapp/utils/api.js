// 环境配置：开发环境使用本地地址，生产环境替换为服务器地址
// 开发环境: http://localhost:8080/api
// 测试环境: http://test-server:8080/api
// 生产环境: https://your-domain.com/api
const BASE_URL = 'http://localhost:8080/api'
const FILE_BASE_URL = 'http://localhost:8080'

/**
 * Send HTTP request to backend API
 */
function request(options) {
  const { url, method = 'GET', data, header = {} } = options
  const token = uni.getStorageSync('satoken') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
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

export function login(username, password) {
  return request({ url: '/auth/login', method: 'POST', data: { username, password } })
}

export function logout() {
  return request({ url: '/auth/logout', method: 'POST' })
}

// ==================== User ====================

export function register(user) {
  return request({ url: '/users/add', method: 'POST', data: user })
}

export function getUserById(id) {
  return request({ url: '/users/get/' + id })
}

export function updateUser(user) {
  return request({ url: '/users/update', method: 'PUT', data: user })
}

// ==================== Product ====================

export function getProductList() {
  return request({ url: '/products/onShelf' })
}

export function getProductById(id) {
  return request({ url: '/products/get/' + id })
}

export function getProductsByCategoryId(categoryId) {
  return request({ url: '/products/getByCategoryId/' + categoryId })
}

export function searchProducts(keyword) {
  return request({ url: '/products/searchAll?keyword=' + encodeURIComponent(keyword) })
}

// ==================== Category ====================

export function getCategoryList() {
  return request({ url: '/categories/list' })
}

// ==================== Order ====================

export function getOrdersByUserId(userId) {
  return request({ url: '/orders/getByUserId/' + userId })
}

export function getOrderById(id) {
  return request({ url: '/orders/get/' + id })
}

export function addOrder(userId, productId, quantity, userCouponId, storeId) {
  let url = '/orders/add?userId=' + userId + '&productId=' + productId + '&quantity=' + quantity
  if (userCouponId) {
    url += '&userCouponId=' + userCouponId
  }
  if (storeId) {
    url += '&storeId=' + storeId
  }
  return request({ url: url, method: 'POST' })
}

export function addMultiItemOrder(userId, items, userCouponId, storeId) {
  var data = { userId: userId, items: items }
  if (userCouponId) {
    data.userCouponId = userCouponId
  }
  if (storeId) {
    data.storeId = storeId
  }
  return request({ url: '/orders/addMultiItem', method: 'POST', data: data })
}

export function getOrderItemsByOrderId(orderId) {
  return request({ url: '/order-items/getByOrderId/' + orderId })
}

export function updateOrder(order) {
  return request({ url: '/orders/update', method: 'PUT', data: order })
}

// ==================== Cart ====================

export function getCartByUserId(userId) {
  return request({ url: '/cart-items/getByUserId/' + userId })
}

export function addCartItem(cartItem) {
  return request({ url: '/cart-items/add', method: 'POST', data: cartItem })
}

export function updateCartItem(cartItem) {
  return request({ url: '/cart-items/update', method: 'PUT', data: cartItem })
}

export function deleteCartItem(id) {
  return request({ url: '/cart-items/delete/' + id, method: 'DELETE' })
}

export function clearCart(userId) {
  return request({ url: '/cart-items/clear/' + userId, method: 'DELETE' })
}

// ==================== Coupon ====================

export function getCouponById(id) {
  return request({ url: '/coupons/get/' + id })
}

// ==================== UserCoupon ====================

export function getUserCouponsByUserId(userId) {
  return request({ url: '/user-coupons/getByUserId/' + userId })
}

export function getUserCouponsByUserIdAndStatus(userId, status) {
  return request({ url: '/user-coupons/getByUserIdAndStatus?userId=' + userId + '&status=' + status })
}

export function updateUserCoupon(userCoupon) {
  return request({ url: '/user-coupons/update', method: 'PUT', data: userCoupon })
}

// ==================== Payment ====================

export function addPayment(payment) {
  return request({ url: '/payments/add', method: 'POST', data: payment })
}

export function getPaymentsByOrderId(orderId) {
  return request({ url: '/payments/getByOrderId/' + orderId })
}

// ==================== Store ====================

export function getStoreList() {
  return request({ url: '/stores/list' })
}

export function getStoreById(id) {
  return request({ url: '/stores/get/' + id })
}

// ==================== Message ====================

export function getUserMessages(userId) {
  return request({ url: '/messages/user/' + userId, method: 'GET' })
}

export function getUnreadCount(userId) {
  return request({ url: '/messages/unread/' + userId, method: 'GET' })
}

export function markMessageAsRead(id) {
  return request({ url: '/messages/read/' + id, method: 'PUT' })
}

export function markAllMessagesAsRead(userId) {
  return request({ url: '/messages/readAll/' + userId, method: 'PUT' })
}

// ==================== File ====================

export function getFileUrl(filename) {
  if (!filename) return ''
  // DB stores: /file/xxx.jpg
  // We need: http://localhost:8080/api/file/xxx.jpg
  var name = filename.replace(/^\/file\//, '')
  return BASE_URL + '/file/' + name
}

export default {
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
  getStoreList,
  getStoreById,
  getFileUrl,
  getUserMessages,
  getUnreadCount,
  markMessageAsRead,
  markAllMessagesAsRead
}
