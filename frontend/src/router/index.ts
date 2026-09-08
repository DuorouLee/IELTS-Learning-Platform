import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ReadingPracticeHistoryView from '@/views/ReadingPracticeHistoryView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      /**
       * :testId 是动态路由参数。
       *
       * 例如访问：
       *
       * /reading/tests/3
       *
       * 那么：
       *
       * testId = "3"
       */
      path: '/reading/tests/:testId',

      name: 'reading-test',

      /**
       * 使用懒加载。
       *
       * 只有访问 Reading 页面时，
       * 才加载 ReadingTestView.vue。
       */
      component: () => import('../views/ReadingTestView.vue'),
    },

    {
      path: '/reading/history',
      name: 'reading-history',
      component: ReadingPracticeHistoryView,
    },
  ],
})

export default router
