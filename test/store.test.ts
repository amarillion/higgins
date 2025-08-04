import { describe, it, expect, beforeEach, vi } from 'vitest';
import { store } from '../src/store/index';
import { SessionStorage } from '../src/store/sessionStorage';
import type { SelectedLesson } from '../src/store/sessionStorage';

// Mock the Quiz.loadFromFile method since we're testing store logic, not Quiz loading
vi.mock('../src/model/Quiz', () => ({
	Quiz: {
		loadFromFile: vi.fn().mockResolvedValue({
			getContentHash: () => 'mock-hash-123',
			getWords: () => [
				{ question: 'hello', answer: 'hola', side: 0, lineNumber: 0, template: 'What is' }
			]
		})
	}
}));

describe('store', () => {
	const mockLesson: SelectedLesson = {
		language: 'spanish',
		lessonPath: 'lessons/spanish/test.txt',
		lessonName: 'Test Lesson'
	};

	beforeEach(() => {
		// Clear localStorage between tests
		localStorage.clear();
		
		// Reset store state
		store.lessonActive = false;
		store.selectedLesson = null;
		store.currentLessonHash = null;
	});

	describe('selectLesson', () => {
		it('should activate lesson and save session', async () => {
			await store.selectLesson(mockLesson);

			expect(store.lessonActive).toBe(true);
			expect(store.selectedLesson).toEqual(mockLesson);
			expect(store.currentLessonHash).toBe('mock-hash-123');
		});

		it('should save session data to localStorage', async () => {
			await store.selectLesson(mockLesson);

			const sessionData = SessionStorage.load();
			expect(sessionData).not.toBeNull();
			expect(sessionData!.selectedLesson).toEqual(mockLesson);
			expect(sessionData!.lessonHash).toBe('mock-hash-123');
			expect(sessionData!.timestamp).toBeTypeOf('number');
		});
	});

	describe('closeLesson', () => {
		it('should clear lesson state', async () => {
			// Setup: select a lesson first
			await store.selectLesson(mockLesson);
			
			// Act: close the lesson
			store.closeLesson();

			// Assert: state is cleared
			expect(store.lessonActive).toBe(false);
			expect(store.selectedLesson).toBeNull();
			expect(store.currentLessonHash).toBeNull();
		});

		it('should clear localStorage', async () => {
			// Setup: select a lesson to create session data
			await store.selectLesson(mockLesson);
			expect(SessionStorage.load()).not.toBeNull();
			
			// Act: close the lesson
			store.closeLesson();

			// Assert: localStorage is cleared
			expect(SessionStorage.load()).toBeNull();
		});
	});

	describe('restoreSession', () => {
		it('should restore session when hash matches', async () => {
			// Setup: create session data
			const sessionData = {
				selectedLesson: mockLesson,
				lessonHash: 'mock-hash-123',
				timestamp: Date.now()
			};
			SessionStorage.save(sessionData);

			// Act: restore session
			const result = await store.restoreSession();

			// Assert: session restored successfully
			expect(result).toBe(true);
			expect(store.lessonActive).toBe(true);
			expect(store.selectedLesson).toEqual(mockLesson);
			expect(store.currentLessonHash).toBe('mock-hash-123');
		});

		it('should not restore session when hash differs', async () => {
			// Setup: create session data with different hash
			const sessionData = {
				selectedLesson: mockLesson,
				lessonHash: 'old-hash-456',
				timestamp: Date.now()
			};
			SessionStorage.save(sessionData);

			// Act: restore session
			const result = await store.restoreSession();

			// Assert: session not restored
			expect(result).toBe(false);
			expect(store.lessonActive).toBe(false);
			expect(store.selectedLesson).toBeNull();
			expect(store.currentLessonHash).toBeNull();
		});

		it('should clear localStorage when hash differs', async () => {
			// Setup: create session data with different hash
			const sessionData = {
				selectedLesson: mockLesson,
				lessonHash: 'old-hash-456',
				timestamp: Date.now()
			};
			SessionStorage.save(sessionData);

			// Act: restore session
			await store.restoreSession();

			// Assert: localStorage cleared due to hash mismatch
			expect(SessionStorage.load()).toBeNull();
		});

		it('should return false when no session data exists', async () => {
			const result = await store.restoreSession();

			expect(result).toBe(false);
			expect(store.lessonActive).toBe(false);
		});
	});

	describe('saveSession', () => {
		it('should not save when no lesson is selected', () => {
			store.saveSession();
			expect(SessionStorage.load()).toBeNull();
		});

		it('should not save when no hash is available', () => {
			store.selectedLesson = mockLesson;
			store.currentLessonHash = null;
			
			store.saveSession();
			expect(SessionStorage.load()).toBeNull();
		});

		it('should save when both lesson and hash are available', () => {
			store.selectedLesson = mockLesson;
			store.currentLessonHash = 'test-hash';
			
			store.saveSession();
			
			const sessionData = SessionStorage.load();
			expect(sessionData).not.toBeNull();
			expect(sessionData!.selectedLesson).toEqual(mockLesson);
			expect(sessionData!.lessonHash).toBe('test-hash');
		});
	});
});