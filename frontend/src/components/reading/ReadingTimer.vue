<script setup lang="ts">
import {
  computed,
  onBeforeUnmount,
  ref,
} from 'vue'

/**
 * Reading 做题计时器。
 *
 * 第一版只保存在当前页面中，
 * 不写入后端，也不会影响提交答案逻辑。
 */

const DEFAULT_MINUTES = 60

const selectedMinutes = ref(DEFAULT_MINUTES)
const remainingSeconds = ref(
  DEFAULT_MINUTES * 60,
)

const running = ref(false)
const settingsVisible = ref(false)

let timerId: number | null = null

const formattedTime = computed(() => {
  const minutes = Math.floor(
    remainingSeconds.value / 60,
  )

  const seconds =
    remainingSeconds.value % 60

  return `${String(minutes).padStart(
    2,
    '0',
  )}:${String(seconds).padStart(
    2,
    '0',
  )}`
})

const timerStatus = computed(() => {
  if (remainingSeconds.value === 0) {
    return '时间到'
  }

  return running.value ? '计时中' : '已暂停'
})

/**
 * 真正开始 interval。
 *
 * 每秒减 1；
 * 到 0 后自动停止。
 */
function startInterval() {
  if (timerId !== null) {
    return
  }

  timerId = window.setInterval(() => {
    if (remainingSeconds.value <= 1) {
      remainingSeconds.value = 0
      running.value = false
      stopInterval()
      return
    }

    remainingSeconds.value -= 1
  }, 1000)
}

function stopInterval() {
  if (timerId === null) {
    return
  }

  window.clearInterval(timerId)
  timerId = null
}

function toggleTimer() {
  if (remainingSeconds.value === 0) {
    resetTimer()
  }

  running.value = !running.value

  if (running.value) {
    startInterval()
  } else {
    stopInterval()
  }
}

/**
 * 应用新的总时长。
 *
 * 设置时间后回到暂停状态，
 * 由用户自己决定什么时候开始。
 */
function applyMinutes() {
  selectedMinutes.value = Math.min(
    180,
    Math.max(
      1,
      Math.round(selectedMinutes.value || 1),
    ),
  )

  running.value = false
  stopInterval()

  remainingSeconds.value =
    selectedMinutes.value * 60

  settingsVisible.value = false
}

function resetTimer() {
  running.value = false
  stopInterval()

  remainingSeconds.value =
    selectedMinutes.value * 60
}

onBeforeUnmount(() => {
  stopInterval()
})
</script>

<template>
  <div class="reading-timer">
    <div class="timer-main">
      <span
        class="timer-time"
        :class="{ finished: remainingSeconds === 0 }"
      >
        ⏱ {{ formattedTime }}
      </span>

      <span class="timer-status">
        {{ timerStatus }}
      </span>

      <button
        type="button"
        class="timer-control-button"
        @click="toggleTimer"
      >
        {{ running ? '暂停' : '开始' }}
      </button>

      <button
        type="button"
        class="timer-control-button secondary"
        @click="resetTimer"
      >
        重置
      </button>

      <button
        type="button"
        class="timer-setting-button"
        @click="
          settingsVisible = !settingsVisible
        "
      >
        设置
      </button>
    </div>

    <div
      v-if="settingsVisible"
      class="timer-settings"
    >
      <span class="timer-setting-label">
        分钟
      </span>

      <div class="preset-buttons">
        <button
          v-for="minutes in [15, 20, 25, 40, 60]"
          :key="minutes"
          type="button"
          class="preset-button"
          :class="{
            active:
              selectedMinutes === minutes,
          }"
          @click="selectedMinutes = minutes"
        >
          {{ minutes }}
        </button>
      </div>

      <input
        v-model.number="selectedMinutes"
        class="custom-time-input"
        type="number"
        min="1"
        max="180"
        step="1"
        aria-label="自定义计时分钟数"
      />

      <button
        type="button"
        class="apply-time-button"
        @click="applyMinutes"
      >
        应用
      </button>
    </div>
  </div>
</template>

<style scoped>
.reading-timer {
  position: relative;
}

.timer-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.timer-time {
  min-width: 76px;
  padding: 7px 10px;
  border: 1px solid #d7e0e8;
  border-radius: 8px;
  background: #f8fbfd;
  color: #3f596d;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.timer-time.finished {
  border-color: #d8bcbc;
  background: #fff6f6;
  color: #8a5454;
}

.timer-status {
  color: #7a8790;
  font-size: 12px;
}

.timer-control-button,
.timer-setting-button,
.apply-time-button,
.preset-button {
  border-radius: 7px;
  cursor: pointer;
}

.timer-control-button {
  padding: 7px 11px;
  border: 1px solid #b9d3e6;
  background: #eef7ff;
  color: #35586f;
}

.timer-control-button.secondary,
.timer-setting-button {
  padding: 7px 10px;
  border: 1px solid #dde3e8;
  background: #fff;
  color: #667783;
}

.timer-settings {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 360px;
  padding: 10px 12px;
  border: 1px solid #dfe6eb;
  border-radius: 9px;
  background: #fff;
  box-shadow: 0 6px 20px rgba(48, 70, 86, 0.08);
}

.timer-setting-label {
  color: #687783;
  font-size: 12px;
}

.preset-buttons {
  display: flex;
  gap: 4px;
}

.preset-button {
  min-width: 34px;
  padding: 5px 7px;
  border: 1px solid #dde3e8;
  background: #fff;
  color: #607482;
  font-size: 12px;
}

.preset-button.active {
  border-color: #b9d3e6;
  background: #eef7ff;
}

.custom-time-input {
  width: 58px;
  padding: 5px 6px;
  border: 1px solid #d7e0e8;
  border-radius: 7px;
  box-sizing: border-box;
}

.apply-time-button {
  padding: 6px 10px;
  border: 1px solid #b9d3e6;
  background: #eef7ff;
  color: #35586f;
}

@media (max-width: 900px) {
  .timer-settings {
    position: static;
    flex-wrap: wrap;
    min-width: 0;
    margin-top: 8px;
  }
}
</style>
