import { describe, it, expect, beforeEach } from 'vitest';
import { StateCompression, type CompactQuizState } from '../src/store/stateCompression';
import { Quiz } from '../src/model/Quiz';
import { QuizSession } from '../src/model/QuizSession';
import type { Word } from '../src/model/types';

describe('StateCompression', () => {
	let quiz: Quiz;
	let session: QuizSession;

	beforeEach(() => {
		// Create a small quiz for testing
		const words: Word[] = [
			{ question: 'hello', answer: 'hola', side: 0, lineNumber: 0, template: 'What is' },
			{ question: 'goodbye', answer: 'adiós', side: 0, lineNumber: 1, template: 'What is' },
			{ question: 'cat', answer: 'gato', side: 0, lineNumber: 2, template: 'What is' }
		];
		
		quiz = new Quiz(words);
		session = new QuizSession(quiz);
	});

	describe('serialize', () => {
		it('should serialize fresh session correctly', () => {
			const compactState = StateCompression.serialize(session);
			
			expect(compactState).toHaveLength(7); // [version, counter, bins, currentWord, sessionCorrectAnswers, binCount, wordStates]
			expect(compactState[0]).toBe(2); // version
			expect(compactState[1]).toBe(1); // counter starts at 1
			expect(compactState[2]).toBe(4); // bins
			expect(compactState[3]).toBe(0); // currentWord starts at 0
			expect(compactState[4]).toBe(0); // sessionCorrectAnswers starts at 0
			expect(compactState[5]).toEqual([3, 0, 0, 0]); // binCount: all words in bin 0
			expect(compactState[6]).toHaveLength(3); // 3 word states
		});

		it('should serialize word states correctly', () => {
			const compactState = StateCompression.serialize(session);
			const wordStates = compactState[6];
			
			// Fresh word state should be [bin=0, howSoon=-1, quizCount=0, correctInARow=0]
			expect(wordStates[0]).toEqual([0, -1, 0, 0]);
			expect(wordStates[1]).toEqual([0, -1, 0, 0]);
			expect(wordStates[2]).toEqual([0, -1, 0, 0]);
		});

		it('should serialize modified session state', () => {
			// Simulate answering first question correctly
			const result = session.compareAnswer(session.getCorrectAnswer());
			expect(result).toBe(true);
			
			const compactState = StateCompression.serialize(session);
			const wordStates = compactState[6];
			
			// First word should have been moved to bin 1, with howSoon set
			expect(wordStates[0][0]).toBe(1); // bin = 1
			expect(wordStates[0][1]).toBe(3); // howSoon = counter + 2^bin = 1 + 2^1 = 3
			expect(wordStates[0][2]).toBe(1); // quizCount = 1
			expect(wordStates[0][3]).toBe(1); // correctInARow = 1
		});
	});

	describe('deserialize', () => {
		it('should deserialize fresh session state', () => {
			const compactState = StateCompression.serialize(session);
			const restored = StateCompression.deserialize(compactState);
			
			expect(restored.version).toBe(2);
			expect(restored.counter).toBe(1);
			expect(restored.bins).toBe(4);
			expect(restored.currentWord).toBe(0);
			expect(restored.sessionCorrectAnswers).toBe(0);
			expect(restored.binCount).toEqual([3, 0, 0, 0]);
			expect(restored.wordStates).toHaveLength(3);
			
			// Check first word state
			expect(restored.wordStates[0]).toEqual({
				bin: 0,
				howSoon: -1,
				quizCount: 0,
				correctInARow: 0
			});
		});

		it('should handle -1 howSoon values correctly', () => {
			const compactState = StateCompression.serialize(session);
			const restored = StateCompression.deserialize(compactState);
			
			// All fresh words should have howSoon = -1
			restored.wordStates.forEach(wordState => {
				expect(wordState.howSoon).toBe(-1);
			});
		});

		it('should round-trip serialize/deserialize correctly', () => {
			// Modify session state
			session.compareAnswer(session.getCorrectAnswer());
			session.compareAnswer('wrong');
			
			const original = StateCompression.serialize(session);
			const restored = StateCompression.deserialize(original);
			
			// Should be identical after round-trip
			expect(restored.counter).toBe(session.getCounter());
			expect(restored.bins).toBe(session.getBins());
		});

		it('should throw error for unsupported version', () => {
			const invalidState = [999, 1, 4, 0, 0, [3, 0, 0, 0], []]; // version 999
			
			expect(() => {
				StateCompression.deserialize(invalidState as CompactQuizState);
			}).toThrow('Unsupported serialization version: 999');
		});
	});

	describe('estimateSize', () => {
		it('should calculate reasonable size estimate', () => {
			const compactState = StateCompression.serialize(session);
			const estimatedSize = StateCompression.estimateSize(compactState);
			const actualSize = JSON.stringify(compactState).length;
			
			expect(estimatedSize).toBe(actualSize);
			expect(estimatedSize).toBeGreaterThan(0);
			expect(estimatedSize).toBeLessThan(1000); // Should be quite compact for 3 words
		});

		it('should show space savings vs object format', () => {
			const compactState = StateCompression.serialize(session);
			const compactSize = StateCompression.estimateSize(compactState);
			
			// Compare with hypothetical object format (rough estimate)
			const objectFormat = {
				version: 1,
				counter: session.getCounter(),
				bins: session.getBins(),
				currentWord: session.getCurrentWordIndex(),
				binCount: [3, 0, 0, 0],
				wordStates: [
					{ bin: 0, howSoon: -1, quizCount: 0, correctInARow: 0 },
					{ bin: 0, howSoon: -1, quizCount: 0, correctInARow: 0 },
					{ bin: 0, howSoon: -1, quizCount: 0, correctInARow: 0 }
				]
			};
			const objectSize = JSON.stringify(objectFormat).length;
			
			// Array format should be more compact
			expect(compactSize).toBeLessThan(objectSize);
		});
	});

	describe('validateSize', () => {
		it('should validate small state as acceptable', () => {
			const compactState = StateCompression.serialize(session);
			expect(StateCompression.validateSize(compactState)).toBe(true);
		});

		it('should reject state exceeding size limit', () => {
			const compactState = StateCompression.serialize(session);
			const tinyLimit = 0.01; // 10 bytes - way too small
			expect(StateCompression.validateSize(compactState, tinyLimit)).toBe(false);
		});

		it('should use default 1MB limit', () => {
			const compactState = StateCompression.serialize(session);
			expect(StateCompression.validateSize(compactState)).toBe(true);
		});
	});

	describe('session restoration integration', () => {
		it('should restore session state correctly', () => {
			// Modify original session
			session.compareAnswer(session.getCorrectAnswer());
			const originalCounter = session.getCounter();
			const originalBinCount = session.getBinCount(1);
			
			// Serialize and create new session
			const compactState = StateCompression.serialize(session);
			const newSession = new QuizSession(quiz);
			
			// Restore state
			const restoredState = StateCompression.deserialize(compactState);
			newSession.restoreState(restoredState);
			
			// Verify restoration
			expect(newSession.getCounter()).toBe(originalCounter);
			expect(newSession.getBinCount(1)).toBe(originalBinCount);
		});
	});
});