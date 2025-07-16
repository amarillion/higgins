<script setup lang="ts">
import LessonPage from './components/LessonPage.vue';
</script>

<template>
  <div class="app">
    <header class="app-header">
      <h1>🎓 Dr. Higgins - Language Learning</h1>
      <p>Master vocabulary with spaced repetition</p>
    </header>
    
    <main class="app-main">
      <LessonPage />
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
}

.app-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.app-header h1 {
  margin: 0 0 10px 0;
  font-size: 2.5em;
  font-weight: bold;
}

.app-header p {
  margin: 0;
  font-size: 1.2em;
  opacity: 0.9;
}

.app-main {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
}
</style>
