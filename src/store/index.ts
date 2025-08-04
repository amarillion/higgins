// simple store modeled after https://vuejs.org/guide/scaling-up/state-management.html

import { reactive } from 'vue';
import { SessionStorage, type SessionData, type SelectedLesson } from './sessionStorage';
import { StateCompression } from './stateCompression';
import { Quiz } from '../model/Quiz';
import { QuizSession } from '../model/QuizSession';

export type { SelectedLesson };

export const store = reactive({
	lessonActive: false,
	selectedLesson: null as SelectedLesson | null,
	currentLessonHash: null as string | null,
	currentQuizSession: null as QuizSession | null,

	closeLesson() {
		// Save session before closing
		if (this.selectedLesson && this.currentLessonHash) {
			this.saveSession();
		}
		
		this.lessonActive = false;
		this.selectedLesson = null;
		this.currentLessonHash = null;
		this.currentQuizSession = null;
		
		// Clear session after closing
		SessionStorage.clear();
	},

	async selectLesson(lesson: SelectedLesson) {
		this.selectedLesson = lesson;
		this.lessonActive = true;
		
		// Load lesson to get hash and create session
		try {
			const quiz = await Quiz.loadFromFile(lesson.lessonPath);
			this.currentLessonHash = quiz.getContentHash();
			this.currentQuizSession = new QuizSession(quiz);
			
			// Save session with current hash
			this.saveSession();
		} catch (error) {
			console.error('Failed to load lesson for session persistence:', error);
		}
	},

	saveSession() {
		if (!this.selectedLesson || !this.currentLessonHash) return;
		
		const sessionData: SessionData = {
			selectedLesson: this.selectedLesson,
			lessonHash: this.currentLessonHash,
			timestamp: Date.now()
		};
		
		// Add quiz state if session exists
		if (this.currentQuizSession) {
			try {
				const compactState = StateCompression.serialize(this.currentQuizSession);
				
				// Check size before saving
				if (StateCompression.validateSize(compactState)) {
					sessionData.quizState = compactState;
				} else {
					console.warn('Quiz state too large for localStorage, saving without state');
				}
			} catch (error) {
				console.error('Failed to serialize quiz state:', error);
			}
		}
		
		SessionStorage.save(sessionData);
	},

	async restoreSession(): Promise<boolean> {
		const sessionData = SessionStorage.load();
		if (!sessionData) return false;
		
		try {
			// Load the lesson to check if hash has changed
			const quiz = await Quiz.loadFromFile(sessionData.selectedLesson.lessonPath);
			const currentHash = quiz.getContentHash();
			
			if (currentHash !== sessionData.lessonHash) {
				console.log('Lesson content has changed, invalidating session');
				SessionStorage.clear();
				return false;
			}
			
			// Hash matches, restore session
			this.selectedLesson = sessionData.selectedLesson;
			this.currentLessonHash = sessionData.lessonHash;
			this.currentQuizSession = new QuizSession(quiz);
			this.lessonActive = true;
			
			// Restore quiz state if available
			if (sessionData.quizState) {
				try {
					const restoredState = StateCompression.deserialize(sessionData.quizState);
					this.currentQuizSession.restoreState(restoredState);
					console.log('Session and quiz state restored successfully');
				} catch (error) {
					console.error('Failed to restore quiz state, starting fresh session:', error);
				}
			} else {
				console.log('Session restored successfully (no quiz state saved)');
			}
			
			return true;
		} catch (error) {
			console.error('Failed to restore session:', error);
			SessionStorage.clear();
			return false;
		}
	},
});