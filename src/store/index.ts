// simple store modeled after https://vuejs.org/guide/scaling-up/state-management.html

import { reactive } from 'vue';
import { SessionStorage, type SessionData, type SelectedLesson } from './sessionStorage';
import { StreakStorage, type StreakData } from './streakStorage';
import { StateCompression } from './stateCompression';
import { Quiz } from '../model/Quiz';
import { QuizSession } from '../model/QuizSession';
import { SessionEventType } from '../model/types';

export type { SelectedLesson };

export const store = reactive({
	lessonActive: false,
	selectedLesson: null as SelectedLesson | null,
	currentLessonHash: null as string | null,
	currentQuizSession: null as QuizSession | null,
	streakData: null as StreakData | null,

	closeLesson() {
		// Save session and streak data before closing
		if (this.selectedLesson && this.currentLessonHash) {
			this.saveSession();
		}
		this.saveStreakData();
		
		// Remove quiz session listener
		if (this.currentQuizSession) {
			this.currentQuizSession.removeListener(this);
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
		
		// Initialize streak data if not already loaded
		this.initializeStreakData();
		
		// Load lesson to get hash and create session
		try {
			const quiz = await Quiz.loadFromFile(lesson.lessonPath);
			this.currentLessonHash = quiz.getContentHash();
			this.currentQuizSession = QuizSession.newInstance(quiz);
			
			// Add listener for correct answers
			this.currentQuizSession.addListener(this);
			
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
			
			this.lessonActive = true;
			this.initializeStreakData();
			
			// Restore quiz state if available
			if (sessionData.quizState) {
				try {
					const restoredState = StateCompression.deserialize(sessionData.quizState);
					this.currentQuizSession = QuizSession.restoreState(quiz, restoredState);
					console.log('Session and quiz state restored successfully');
				} catch (error) {
					this.currentQuizSession = QuizSession.newInstance(quiz);
					console.error('Failed to restore quiz state, starting fresh session:', error);
				}
			} else {
				this.currentQuizSession = QuizSession.newInstance(quiz);
				console.log('Session restored successfully (no quiz state saved)');
			}

			this.currentQuizSession.addListener(this);
			return true;
		} catch (error) {
			console.error('Failed to restore session:', error);
			SessionStorage.clear();
			return false;
		}
	},

	// SessionListener implementation
	sessionChanged(_type: SessionEventType): void {
		// Check if lesson is completed after any change
		if (this.currentQuizSession && this.currentQuizSession.isFinished()) {
			this.handleLessonCompletion();
		}
	},

	// Streak tracking methods
	initializeStreakData() {
		if (!this.streakData) {
			this.streakData = StreakStorage.load() || StreakStorage.getDefaultData();
		}
	},

	handleLessonCompletion() {
		if (!this.streakData) {
			this.initializeStreakData();
		}
		
		if (this.streakData) {
			this.streakData = StreakStorage.addCompletedLesson(this.streakData);
			this.saveStreakData();
		}
	},

	saveStreakData() {
		if (this.streakData) {
			StreakStorage.save(this.streakData);
		}
	},

	getStreakInfo() {
		if (!this.streakData) {
			this.initializeStreakData();
		}
		
		return this.streakData ? StreakStorage.getStreakInfo(this.streakData) : null;
	},
});