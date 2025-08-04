<script setup lang="ts">

import { ref } from 'vue';
import LessonSelector from './LessonSelector.vue';
import { store } from '../store';
import AppHeader from './AppHeader.vue';
import StreakPage from './StreakPage.vue';

enum Page {
	LESSON_SELECT = 'LESSON_SELECT',
	STREAK = 'STREAK',
}

const currentPage = ref<Page>(Page.LESSON_SELECT);
</script>

<template>
	<AppHeader/>
	<LessonSelector
		v-if="currentPage === Page.LESSON_SELECT"
		@lesson-selected="(lesson) => store.selectLesson(lesson)">
	</LessonSelector>
	<StreakPage v-else-if="currentPage === Page.STREAK" />
	<nav>
		<ul>
			<li><button @click="currentPage = Page.STREAK">Streak</button></li>
			<li><button @click="currentPage = Page.LESSON_SELECT">Select Lesson</button></li>
		</ul>
	</nav>
</template>

<style scoped>
nav {
	position: fixed;
	bottom: 0;
	width: 100%;
}
nav > ul {
	display: flex;
	flex-direction: row;
	justify-content: left;
}
nav > ul > li {
	list-style: none;
}
</style>