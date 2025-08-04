<script setup lang="ts">
import LessonPage from './components/lesson/LessonPage.vue';
import LobbyPage from './components/lobby/LobbyPage.vue';

import { store } from './store/index.ts';
</script>

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
