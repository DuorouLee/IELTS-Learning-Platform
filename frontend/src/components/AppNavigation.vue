<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

/**
 * 当前路由。
 *
 * 用它判断用户现在位于：
 *
 * Home
 * Reading
 * Vocabulary
 *
 * 哪个模块，
 * 然后自动给对应导航按钮添加 active 状态。
 */
const route = useRoute()

/**
 * 判断当前导航是否应该高亮。
 *
 * 例如：
 *
 * /reading
 * /reading/tests/1
 * /reading/history
 *
 * 都应该让 Reading 保持高亮。
 */
function isActive(path: string) {
    if (path === '/') {
        return route.path === '/'
    }

    return route.path.startsWith(path)
}
</script>

<template>
    <!--
    全站统一顶部导航。

    Home / Reading / Vocabulary
    后面所有页面都复用这里，
    避免每个 View 重复写一份导航。
  -->
    <nav class="app-navigation">

        <!-- 左侧品牌 -->
        <RouterLink to="/" class="brand">
            <span class="brand-mark">
                I
            </span>

            <span class="brand-text">
                IELTS Learning
            </span>
        </RouterLink>


        <!-- 右侧模块入口 -->
        <div class="navigation-links">

            <RouterLink to="/" class="navigation-link" :class="{
                active: isActive('/'),
            }">
                Home
            </RouterLink>


            <RouterLink to="/reading" class="navigation-link" :class="{
                active: isActive('/reading'),
            }">
                Reading
            </RouterLink>


            <RouterLink to="/vocabulary" class="navigation-link" :class="{
                active: isActive('/vocabulary'),
            }">
                Vocabulary
            </RouterLink>

        </div>

    </nav>
</template>

<style scoped>
/* ============================================================
   App Navigation

   这是全站共用的顶部导航。

   当前视觉完全沿用已经确认的：
   - Maple Mono NF CN
   - 淡蓝
   - Liquid Glass
   ============================================================ */

.app-navigation {
    width: min(1240px, 100%);

    min-height: 68px;

    margin:
        0 auto;

    box-sizing: border-box;

    display: flex;

    align-items: center;

    justify-content: space-between;

    gap: 30px;

    padding:
        10px 12px 10px 16px;

    background:
        rgba(255,
            255,
            255,
            0.32);

    border:
        1px solid rgba(255,
            255,
            255,
            0.68);

    border-radius: 24px;

    backdrop-filter:
        blur(22px) saturate(145%);

    -webkit-backdrop-filter:
        blur(22px) saturate(145%);

    box-shadow:
        0 18px 50px rgba(80,
            123,
            161,
            0.075);

    font-family:
        "Maple Mono NF CN",
        "Consolas",
        monospace;
}


/* ============================================================
   Brand
   ============================================================ */

.brand {
    display: flex;

    align-items: center;

    gap: 11px;

    padding: 0;

    color: #26394d;

    text-decoration: none;

    background: transparent;
}


.brand:hover {
    background: transparent;
}


.brand-mark {
    width: 38px;
    height: 38px;

    display: grid;

    place-items: center;

    flex-shrink: 0;

    color: #ffffff;

    background:
        linear-gradient(145deg,
            #8ab2d5,
            #6d97bd);

    border-radius: 13px;

    font-size: 1rem;

    font-weight: 700;

    box-shadow:
        0 8px 20px rgba(79,
            125,
            167,
            0.20);
}


.brand-text {
    font-size: 0.9rem;

    font-weight: 600;

    letter-spacing: -0.03em;
}


/* ============================================================
   Navigation Links
   ============================================================ */

.navigation-links {
    display: flex;

    align-items: center;

    gap: 5px;
}


.navigation-link {
    padding:
        10px 16px;

    color: #667b90;

    text-decoration: none;

    border-radius: 999px;

    font-size: 0.8rem;

    font-weight: 600;

    transition:
        color 0.2s ease,
        background 0.2s ease,
        transform 0.2s ease;
}


.navigation-link:hover {
    color: #314e69;

    background:
        rgba(255,
            255,
            255,
            0.52);

    transform:
        translateY(-1px);
}


/*
  当前所在模块。

  例如：

  /reading/history

  虽然不是 /reading，
  仍然让 Reading 保持高亮。
*/
.navigation-link.active {
    color: #344f69;

    background:
        rgba(255,
            255,
            255,
            0.60);
}


/* ============================================================
   Mobile
   ============================================================ */

@media (max-width: 650px) {

    .app-navigation {
        min-height: 60px;

        padding:
            8px 9px 8px 12px;

        border-radius: 20px;
    }


    /*
    手机上隐藏品牌文字，
    只留下左边 I 图标，
    给导航按钮留出空间。
  */
    .brand-text {
        display: none;
    }


    .navigation-links {
        gap: 0;
    }


    .navigation-link {
        padding:
            9px 10px;

        font-size: 0.7rem;
    }

}
</style>