import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/login/index.vue')
    },
    {
      path: '/',
      component: () => import('../layout/index.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/dashboard/index.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('../views/user/index.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('../views/category/index.vue'),
          meta: { title: '分类管理' }
        },
        {
          path: 'products',
          name: 'Products',
          component: () => import('../views/product/index.vue'),
          meta: { title: '商品管理' }
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('../views/order/index.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'order-items',
          name: 'OrderItems',
          component: () => import('../views/order-item/index.vue'),
          meta: { title: '订单明细' }
        },
        {
          path: 'coupons',
          name: 'Coupons',
          component: () => import('../views/coupon/index.vue'),
          meta: { title: '优惠券模板' }
        },
        {
          path: 'user-coupons',
          name: 'UserCoupons',
          component: () => import('../views/user-coupon/index.vue'),
          meta: { title: '用户优惠券' }
        },
        {
          path: 'payments',
          name: 'Payments',
          component: () => import('../views/payment/index.vue'),
          meta: { title: '支付管理' }
        },
        {
          path: 'cart-items',
          name: 'CartItems',
          component: () => import('../views/cart-item/index.vue'),
          meta: { title: '购物车管理' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  if (to.path === '/login') {
    next()
  } else {
    const token = localStorage.getItem('satoken')
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
