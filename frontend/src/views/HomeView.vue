<script setup lang="ts">
import { RouterLink } from 'vue-router'
import AppNavigation from '@/components/AppNavigation.vue'
</script>

<template>
  <main class="home-page">
    <!-- =====================================================
         顶部导航

         首页只负责导航，不再承载 Reading 题库列表。
         ===================================================== -->
    <AppNavigation />

    <!-- =====================================================
         首页 Hero

         首页不显示：
         - Good morning
         - 今日打卡
         - 今日目标

         只保留一句真正有辨识度的学习主题。
         ===================================================== -->
    <section class="hero">
      <!-- 背景柔光 -->
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>

      <!-- 左侧文字 -->
      <div class="hero-content">
        <p class="hero-label">
          IELTS LEARNING PLATFORM
        </p>

        <!--
          中文古文单独使用宋体。

          每一句单独一行，
          避免浏览器自动出现很丑的断句。
        -->
        <h1 class="hero-title">
          <span class="hero-title-line">
            路漫漫其修远兮，
          </span>

          <span class="hero-title-line">
            吾将上下而求索。
          </span>
        </h1>

        <p class="hero-description">
          Reading · Vocabulary · Review
        </p>

        <!-- 模块入口 -->
        <div class="hero-actions">
          <RouterLink to="/reading" class="primary-button">
            Reading
            <span>→</span>
          </RouterLink>

          <RouterLink to="/vocabulary" class="secondary-button">
            Vocabulary
            <span>→</span>
          </RouterLink>
        </div>
      </div>

      <!-- =================================================
           右侧液态玻璃视觉区域

           不放统计数据，
           只作为视觉主体。
           ================================================= -->
      <div class="visual-stage">
        <!-- 后层玻璃 -->
        <div class="glass-card glass-card-back">
          <span class="glass-back-title">
            Vocabulary
          </span>
        </div>

        <!-- 主玻璃 -->
        <div class="glass-card glass-card-main">
          <div class="glass-card-top">
            <span class="glass-number">
              01
            </span>

            <span class="glass-small-text">
              Reading
            </span>
          </div>

          <div class="glass-card-center">
            <p>
              Read.
            </p>

            <p>
              Understand.
            </p>

            <p>
              Remember.
            </p>
          </div>

          <div class="glass-card-bottom">
            IELTS Learning Platform
          </div>
        </div>

        <!-- 装饰性液态圆 -->
        <div class="floating-orb orb-one"></div>
        <div class="floating-orb orb-two"></div>
      </div>
    </section>

    <!-- =====================================================
         模块入口区域

         Home 下面只说明 Reading / Vocabulary 是什么，
         不直接显示 Reading Test。
         ===================================================== -->
    <section class="module-section">
      <div class="module-heading">
        <p class="section-label">
          EXPLORE
        </p>

        <h2>
          Choose your path.
        </h2>
      </div>

      <div class="module-grid">
        <!-- Reading -->
        <RouterLink to="/reading" class="module-card">
          <div class="module-top">
            <span class="module-index">
              01
            </span>

            <span class="module-arrow">
              ↗
            </span>
          </div>

          <div class="module-content">
            <p class="module-english">
              Reading
            </p>

            <h3>
              阅读练习
            </h3>

            <p>
              完整完成 Reading Test、答题、判分、
              Review、History 和 Statistics。
            </p>
          </div>

          <div class="module-line"></div>
        </RouterLink>

        <!-- Vocabulary -->
        <RouterLink to="/vocabulary" class="module-card">
          <div class="module-top">
            <span class="module-index">
              02
            </span>

            <span class="module-arrow">
              ↗
            </span>
          </div>

          <div class="module-content">
            <p class="module-english">
              Vocabulary
            </p>

            <h3>
              词汇学习
            </h3>

            <p>
              通过学习、复习和长期积累，
              建立属于自己的 IELTS Vocabulary。
            </p>
          </div>

          <div class="module-line"></div>
        </RouterLink>
      </div>
    </section>

    <!-- Footer -->
    <footer class="home-footer">
      <span>
        IELTS Learning Platform
      </span>

      <span>
        Keep moving.
      </span>
    </footer>
  </main>
</template>

<style scoped>
/* ============================================================
   Font System

   英文 UI：
   Maple Mono NF CN
   → Consolas
   → monospace

   中文普通正文：
   Microsoft YaHei / PingFang SC

   中文诗句：
   SimSun / STSong
   ============================================================ */


/* ============================================================
   Home Page
   ============================================================ */

.home-page {
  min-height: 100vh;

  box-sizing: border-box;

  padding:
    24px 34px 54px;

  overflow: hidden;

  color: #243448;

  /*
    淡蓝色主背景。

    不使用纯白，
    保留非常轻的蓝色空间感。
  */
  background:
    radial-gradient(circle at 14% 15%,
      rgba(184, 218, 245, 0.62),
      transparent 28%),
    radial-gradient(circle at 88% 8%,
      rgba(211, 227, 249, 0.78),
      transparent 31%),
    linear-gradient(180deg,
      var(--color-bg-start) 0%,
      var(--color-bg-middle) 48%,
      var(--color-bg-end) 100%);

  font-family: var(--font-main);
}


/* ============================================================
   英文 UI 字体
   ============================================================ */

.brand,
.navigation-link,
.hero-label,
.hero-description,
.primary-button,
.secondary-button,
.section-label,
.module-english,
.module-index,
.glass-card,
.home-footer {
  font-family:
    "Maple Mono NF CN",
    "Consolas",
    monospace;
}



/* ============================================================
   Hero
   ============================================================ */

.hero {
  position: relative;

  width: min(1240px, 100%);

  min-height: 620px;

  margin:
    22px auto 0;

  box-sizing: border-box;

  display: grid;

  grid-template-columns:
    minmax(0, 1.05fr) minmax(380px, 0.8fr);

  align-items: center;

  gap: 70px;

  padding:
    74px 72px;

  overflow: hidden;

  border:
    1px solid rgba(255,
      255,
      255,
      0.72);

  border-radius: 42px;

  background:
    linear-gradient(135deg,
      rgba(255, 255, 255, 0.50),
      rgba(222, 239, 251, 0.38));

  backdrop-filter:
    blur(25px) saturate(130%);

  -webkit-backdrop-filter:
    blur(25px) saturate(130%);

  box-shadow:
    0 32px 90px rgba(70,
      111,
      151,
      0.11),
    inset 0 1px 0 rgba(255,
      255,
      255,
      0.90);
}


/* ============================================================
   Hero Glow
   ============================================================ */

.hero-glow {
  position: absolute;

  border-radius: 50%;

  pointer-events: none;
}


.hero-glow-left {
  width: 480px;
  height: 480px;

  left: -220px;
  bottom: -250px;

  background:
    radial-gradient(circle,
      rgba(173, 211, 240, 0.36),
      rgba(173, 211, 240, 0));
}


.hero-glow-right {
  width: 430px;
  height: 430px;

  right: -130px;
  top: -160px;

  background:
    radial-gradient(circle,
      rgba(203, 218, 247, 0.52),
      rgba(203, 218, 247, 0));
}


/* ============================================================
   Hero Content
   ============================================================ */

.hero-content {
  position: relative;

  z-index: 2;
}


.hero-label {
  margin:
    0 0 24px;

  color: #7896b0;

  font-size: 0.74rem;

  font-weight: 600;

  letter-spacing: 0.13em;
}


/* ============================================================
   首页中文主题句

   中文和英文全部统一使用 Maple Mono NF CN。

   这样整个项目会有更加明显的一致性和个人风格。
   ============================================================ */
.hero-title {
  margin: 0;

  color: var(--color-text-main);

  font-family:
    "Maple Mono NF CN",
    "Consolas",
    monospace;

  font-size:
    clamp(2.75rem,
      4vw,
      4.45rem);

  font-weight: 600;

  /*
    Maple Mono 本身比较规整，
    中文不需要使用很大的负字间距。
  */
  letter-spacing: 0.015em;

  line-height: 1.45;
}


/*
  每一句完整占一行，
  避免浏览器乱断句。
*/
.hero-title-line {
  display: block;

  white-space: nowrap;
}


.hero-title-line+.hero-title-line {
  margin-top: 4px;
}


/*
  每一句独占一行。

  浏览器不能再在句子中间随便换行。
*/
.hero-title-line {
  display: block;

  white-space: nowrap;
}


.hero-title-line+.hero-title-line {
  margin-top: 4px;
}


.hero-description {
  margin:
    28px 0 0;

  color: var(--color-text-muted);

  font-size: 0.82rem;

  letter-spacing: 0.04em;
}


/* ============================================================
   Hero Buttons
   ============================================================ */

.hero-actions {
  display: flex;

  align-items: center;

  flex-wrap: wrap;

  gap: 12px;

  margin-top: 38px;
}


.primary-button,
.secondary-button {
  display: inline-flex;

  align-items: center;

  justify-content: center;

  gap: 12px;

  min-height: 48px;

  box-sizing: border-box;

  padding:
    0 21px;

  border-radius: 999px;

  text-decoration: none;

  font-size: 0.8rem;

  font-weight: 600;

  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}


.primary-button {
  color: #ffffff;

  background:
    linear-gradient(135deg,
      var(--color-primary),
      var(--color-primary-dark));

  box-shadow:
    0 13px 30px rgba(82,
      133,
      178,
      0.24);
}


.primary-button:hover {
  color: #ffffff;

  transform:
    translateY(-2px);

  box-shadow:
    0 17px 35px rgba(82,
      133,
      178,
      0.28);
}


.secondary-button {
  color: #55738e;

  background:
    rgba(255,
      255,
      255,
      0.48);

  border:
    1px solid rgba(255,
      255,
      255,
      0.76);

  backdrop-filter:
    blur(16px);

  -webkit-backdrop-filter:
    blur(16px);
}


.secondary-button:hover {
  color: #435f79;

  background:
    rgba(255,
      255,
      255,
      0.68);

  transform:
    translateY(-2px);
}


/* ============================================================
   Visual Stage
   ============================================================ */

.visual-stage {
  position: relative;

  z-index: 2;

  width: 100%;
  height: 430px;
}


/* ============================================================
   Liquid Glass
   ============================================================ */

.glass-card {
  position: absolute;

  box-sizing: border-box;

  border:
    1px solid rgba(255,
      255,
      255,
      0.76);

  backdrop-filter:
    blur(30px) saturate(150%);

  -webkit-backdrop-filter:
    blur(30px) saturate(150%);

  box-shadow:
    0 30px 70px rgba(69,
      108,
      148,
      0.14),
    inset 0 1px 0 rgba(255,
      255,
      255,
      0.94);
}


.glass-card-back {
  width: 76%;
  height: 68%;

  right: -5px;
  top: 30px;

  padding: 30px;

  color:
    rgba(74,
      108,
      141,
      0.48);

  background:
    linear-gradient(145deg,
      rgba(191, 220, 242, 0.30),
      rgba(255, 255, 255, 0.28));

  border-radius: 38px;

  transform:
    rotate(7deg);
}


.glass-back-title {
  font-size: 0.75rem;

  letter-spacing: 0.06em;
}


.glass-card-main {
  width: 82%;
  height: 79%;

  left: 2%;
  bottom: 12px;

  display: flex;

  flex-direction: column;

  justify-content: space-between;

  padding:
    28px 30px;

  color: #466884;

  background:
    linear-gradient(145deg,
      rgba(255, 255, 255, 0.55),
      rgba(195, 224, 247, 0.25));

  border-radius: 40px;

  transform:
    rotate(-3deg);
}


.glass-card-top {
  display: flex;

  justify-content: space-between;

  align-items: flex-start;
}


.glass-number {
  font-size: 1.75rem;

  color: #6387a5;
}


.glass-small-text {
  font-size: 0.7rem;

  letter-spacing: 0.05em;
}


.glass-card-center p {
  margin: 0;

  color: #41617e;

  font-size:
    clamp(1.4rem,
      2.7vw,
      2.25rem);

  font-weight: 600;

  letter-spacing: -0.06em;

  line-height: 1.3;
}


.glass-card-bottom {
  color: #849db2;

  font-size: 0.67rem;

  letter-spacing: 0.05em;
}


/* ============================================================
   Floating Orbs
   ============================================================ */

.floating-orb {
  position: absolute;

  border:
    1px solid rgba(255,
      255,
      255,
      0.80);

  border-radius: 50%;

  backdrop-filter:
    blur(15px);

  -webkit-backdrop-filter:
    blur(15px);

  box-shadow:
    inset 0 1px 0 rgba(255,
      255,
      255,
      0.90),
    0 20px 40px rgba(81,
      123,
      162,
      0.11);
}


.orb-one {
  width: 78px;
  height: 78px;

  right: 5px;
  bottom: 15px;

  background:
    rgba(190,
      220,
      244,
      0.44);
}


.orb-two {
  width: 44px;
  height: 44px;

  left: -14px;
  top: 80px;

  background:
    rgba(255,
      255,
      255,
      0.48);
}


/* ============================================================
   Modules
   ============================================================ */

.module-section {
  width: min(1240px, 100%);

  margin:
    110px auto 0;
}


.module-heading {
  margin-bottom: 30px;
}


.section-label {
  margin:
    0 0 9px;

  color: #83a0b9;

  font-size: 0.7rem;

  font-weight: 600;

  letter-spacing: 0.12em;
}


.module-heading h2 {
  margin: 0;

  color: #314b62;

  font-family:
    "Maple Mono NF CN",
    "Consolas",
    monospace;

  font-size:
    clamp(1.7rem,
      3vw,
      2.35rem);

  font-weight: 600;

  letter-spacing: -0.06em;
}


.module-grid {
  display: grid;

  grid-template-columns:
    repeat(2,
      minmax(0, 1fr));

  gap: 18px;
}


.module-card {
  position: relative;

  min-height: 280px;

  box-sizing: border-box;

  display: flex;

  flex-direction: column;

  justify-content: space-between;

  padding: 30px;

  overflow: hidden;

  color: #314b62;

  text-decoration: none;

  background:
    rgba(255,
      255,
      255,
      0.42);

  border:
    1px solid rgba(255,
      255,
      255,
      0.72);

  border-radius: 30px;

  backdrop-filter:
    blur(20px);

  -webkit-backdrop-filter:
    blur(20px);

  box-shadow:
    0 20px 55px rgba(67,
      109,
      146,
      0.075);

  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background 0.25s ease;
}


.module-card:hover {
  color: #314b62;

  background:
    rgba(255,
      255,
      255,
      0.58);

  transform:
    translateY(-5px);

  box-shadow:
    0 28px 65px rgba(67,
      109,
      146,
      0.11);
}


.module-top {
  display: flex;

  align-items: center;

  justify-content: space-between;
}


.module-index {
  color: #9ab0c3;

  font-size: 0.76rem;
}


.module-arrow {
  width: 42px;
  height: 42px;

  display: grid;

  place-items: center;

  color: #567895;

  background:
    rgba(255,
      255,
      255,
      0.50);

  border-radius: 50%;
}


.module-content {
  max-width: 430px;
}


.module-english {
  margin:
    0 0 7px;

  color: #7596b1;

  font-size: 1rem;
}


.module-content h3 {
  margin:
    0 0 12px;

  color: #344e65;

  font-size: 1.55rem;

  font-weight: 600;
}


.module-content>p:last-child {
  margin: 0;

  color: #8093a4;

  font-size: 0.86rem;

  line-height: 1.7;
}


.module-line {
  width: 100%;
  height: 1px;

  background:
    linear-gradient(90deg,
      rgba(108, 148, 181, 0.34),
      rgba(108, 148, 181, 0));
}


/* ============================================================
   Footer
   ============================================================ */

.home-footer {
  width: min(1240px, 100%);

  margin:
    110px auto 0;

  box-sizing: border-box;

  display: flex;

  justify-content: space-between;

  gap: 20px;

  padding-top: 25px;

  color: #95a9ba;

  border-top:
    1px solid rgba(128,
      161,
      190,
      0.15);

  font-size: 0.7rem;
}


/* ============================================================
   Tablet
   ============================================================ */

@media (max-width: 900px) {

  .hero {
    grid-template-columns: 1fr;

    min-height: auto;

    padding:
      60px 42px;
  }


  .visual-stage {
    width: min(500px, 100%);

    height: 390px;

    margin:
      0 auto;
  }


  .module-grid {
    grid-template-columns: 1fr;
  }

}


/* ============================================================
   Mobile
   ============================================================ */

@media (max-width: 650px) {

  .home-page {
    padding:
      14px 14px 40px;
  }

  .hero {
    margin-top: 14px;

    padding:
      48px 24px;

    border-radius: 30px;
  }


  .hero-title {
    font-size: 2.1rem;

    letter-spacing: 0.01em;
  }


  /*
    小屏幕不再强制 nowrap。

    否则太窄的手机可能横向溢出。
  */
  .hero-title-line {
    white-space: normal;
  }


  .hero-actions {
    align-items: stretch;

    flex-direction: column;
  }


  .primary-button,
  .secondary-button {
    width: 100%;
  }


  .visual-stage {
    height: 320px;
  }


  .glass-card-main {
    width: 88%;
    height: 77%;

    padding: 23px;
  }


  .glass-card-back {
    width: 76%;
  }


  .module-section {
    margin-top: 75px;
  }


  .module-card {
    min-height: 250px;

    padding: 25px;
  }


  .home-footer {
    align-items: flex-start;

    flex-direction: column;

    margin-top: 75px;
  }

}
</style>