<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';
import LessonPage from './components/lesson/LessonPage.vue';
import LobbyPage from './components/lobby/LobbyPage.vue';

import { store } from './store/index.ts';

// Handle page unload to save session
const handleBeforeUnload = () => {
	if (store.lessonActive && store.selectedLesson && store.currentLessonHash) {
		store.saveSession();
	}
};

onMounted(() => {
	window.addEventListener('beforeunload', handleBeforeUnload);
	
	// Try to restore session on app load
	store.restoreSession().catch(error => {
		console.error('Failed to restore session on app load:', error);
	});
});

onUnmounted(() => {
	window.removeEventListener('beforeunload', handleBeforeUnload);
});</script>

<template>
	<main>
		<LessonPage v-if="store.lessonActive"/>
		<LobbyPage v-else/>
	</main>
</template>

<style scoped>
main {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
}
</style>
