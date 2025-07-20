<script setup lang="ts">
import { ref } from 'vue';
import LessonSelector from './components/LessonSelector.vue';
import LessonPage from './components/LessonPage.vue';

interface SelectedLesson {
	language: string,
	lessonPath: string,
	lessonName: string,
}

const selectedLesson = ref<SelectedLesson | null>(null);

const handleLessonSelected = (lesson: SelectedLesson) => {
	selectedLesson.value = lesson;
};

const goBackToSelection = () => {
	selectedLesson.value = null;
};
</script>

<template>
  <main>
    <LessonSelector
      v-if="!selectedLesson"
      @lesson-selected="handleLessonSelected"
    />
    <LessonPage
      v-else
      :selected-lesson="selectedLesson"
      @go-back="goBackToSelection"
    />
  </main>
</template>

<style scoped>
main {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}
</style>
