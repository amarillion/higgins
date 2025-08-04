// simple store modeled after https://vuejs.org/guide/scaling-up/state-management.html

import { reactive } from 'vue'

export interface SelectedLesson {
	language: string,
	lessonPath: string,
	lessonName: string,
}

export const store = reactive({
	lessonActive: false,
	selectedLesson: null as SelectedLesson | null,

	closeLesson() {
		this.lessonActive = false;
		this.selectedLesson = null;
	},

	selectLesson(lesson: SelectedLesson) {
		this.selectedLesson = lesson;
		this.lessonActive = true;
	},
});