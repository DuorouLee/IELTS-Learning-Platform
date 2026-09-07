<script setup lang="ts">
/**
 * RouterLink 是 Vue Router 提供的导航组件。
 *
 * 和普通 <a href=""> 不同，
 * RouterLink 会使用 Vue Router
 * 在前端页面之间切换。
 */
import { RouterLink } from 'vue-router'
</script>

<template>
  <main>
    <!--
      IELTS Learning Platform 首页。

      目前阶段只先提供 Reading 入口，
      不提前做 Vocabulary / Writing 等模块。
    -->
    <h1>IELTS Learning Platform</h1>

    <p>
      当前先完成 IELTS Reading 学习流程。
    </p>

    <!--
      Reading 模块入口。
    -->
    <section>
      <h2>Reading</h2>

      <p>
        A Brief History of Tea
      </p>

      <!--
        点击后进入：

        /reading/tests/3

        Vue Router 会根据：
        /reading/tests/:testId

        打开 ReadingTestView.vue。
      -->
      <RouterLink to="/reading/tests/3">
        开始练习
      </RouterLink>
    </section>
  </main>
</template>