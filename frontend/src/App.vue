<script setup lang="ts">
/**
 * RouterView 是 Vue Router 提供的组件。
 *
 * 它的作用是：
 * 根据当前浏览器 URL，
 * 自动决定应该显示哪个 View。
 *
 * 例如：
 *
 * /reading/tests/3
 *        ↓
 * router/index.ts
 *        ↓
 * ReadingTestView.vue
 */
import { RouterView } from 'vue-router'
</script>

<template>
  <!--
    不再直接写死 <ReadingTestView />。
    
    让 Vue Router 根据 URL
    决定当前应该显示哪个页面。
  -->
  <RouterView />
</template>