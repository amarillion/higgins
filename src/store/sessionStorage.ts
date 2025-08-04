import type { CompactQuizState } from './stateCompression';

export interface SelectedLesson {
	language: string,
	lessonPath: string,
	lessonName: string,
}

export interface SessionData {
	selectedLesson: SelectedLesson,
	lessonHash: string,
	quizState?: CompactQuizState, // Compact quiz session state
	timestamp: number,
}

const SESSION_STORAGE_KEY = 'higgins2-session';

export class SessionStorage {
	static save(data: SessionData): void {
		try {
			localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(data));
		} catch (error) {
			console.warn('Failed to save session to localStorage:', error);
		}
	}

	static load(): SessionData | null {
		try {
			const stored = localStorage.getItem(SESSION_STORAGE_KEY);
			if (!stored) return null;
			
			const data = JSON.parse(stored);
			
			// Validate the structure
			if (!data.selectedLesson || !data.lessonHash || !data.timestamp) {
				console.warn('Invalid session data structure, clearing');
				SessionStorage.clear();
				return null;
			}
			
			return data;
		} catch {
			console.warn('Failed to load session from localStorage');
			SessionStorage.clear();
			return null;
		}
	}

	static clear(): void {
		try {
			localStorage.removeItem(SESSION_STORAGE_KEY);
		} catch (error) {
			console.warn('Failed to clear session from localStorage:', error);
		}
	}

	static exists(): boolean {
		try {
			return localStorage.getItem(SESSION_STORAGE_KEY) !== null;
		} catch {
			return false;
		}
	}
}