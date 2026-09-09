import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import ReadingPracticeHistoryView from '@/views/ReadingPracticeHistoryView.vue'
import ReadingHistoryDetailView from '@/views/ReadingHistoryDetailView.vue'
import VocabularyView from '@/views/VocabularyView.vue'
import VocabularyPracticeView from '@/views/VocabularyPracticeView.vue'
import ReadingHomeView from '@/views/ReadingHomeView.vue'

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

      /**
       * About 页面使用懒加载。
       *
       * 只有真正访问 /about 时，
       * 才加载 AboutView.vue。
       */
      component: () => import('../views/AboutView.vue'),
    },

    {
      /**
       * Reading Test 答题页面。
       *
       * :testId 是动态路由参数。
       *
       * 例如：
       *
       * /reading/tests/3
       *
       * 那么：
       *
       * route.params.testId = "3"
       */
      path: '/reading/tests/:testId',
      name: 'reading-test',

      /**
       * Reading Test 页面使用懒加载。
       */
      component: () => import('../views/ReadingTestView.vue'),
    },

    {
      /**
       * Reading Practice History 列表页。
       *
       * 用来查看所有历史练习记录。
       */
      path: '/reading/history',
      name: 'reading-history',
      component: ReadingPracticeHistoryView,
    },

    {
      /**
       * Reading Practice History 详情页。
       *
       * :id 表示某一条 Practice Record 的数据库 ID。
       *
       * 例如：
       *
       * /reading/history/15
       *
       * 那么：
       *
       * route.params.id = "15"
       */
      path: '/reading/history/:id',
      name: 'reading-history-detail',
      component: ReadingHistoryDetailView,
    },

    {
      /**
       * Vocabulary 单词列表页面。
       */
      path: '/vocabulary',
      name: 'vocabulary',
      component: VocabularyView,
    },

    {
      /**
       * Vocabulary Practice 页面。
       *
       * 用于实际进行单词复习。
       */
      path: '/vocabulary/practice',
      name: 'vocabulary-practice',
      component: VocabularyPracticeView,
    },

    {
      path: '/reading',
      name: 'reading-home',
      component: ReadingHomeView,
    },
  ],
})

export default router
