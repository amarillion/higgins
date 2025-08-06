import { describe, it, expect } from 'vitest';
import { StateCompression, type CompactQuizState } from '../src/store/stateCompression';
import { Quiz } from '../src/model/Quiz';
import { QuizSession } from '../src/model/QuizSession';
import type { Word } from '../src/model/types';

// Test constants - keep separate from implementation to avoid tight coupling
const TEST_EXPECTED_VERSION = 4;
const TEST_COMPACT_STATE_LENGTH = 7;
const TEST_MAXBINS = 3;

// Indices for compact state array - for test readability
enum CompactStateIndex {
	VERSION = 0,
	COUNTER = 1,
	BINS = 2,
	CURRENT_WORD = 3,
	SESSION_CORRECT_ANSWERS = 4,
	BIN_COUNT = 5,
	WORD_STATES = 6
}

// Indices for word state arrays - for test readability
enum WordStateIndex {
	BIN = 0,
	HOW_SOON = 1,
	QUIZ_COUNT = 2,
	REMAINING_REPETITIONS = 3,
	LINE_NUMBER = 4,
	SIDE = 5
}

describe('StateCompression', () => {

	describe('serialize', () => {
		it('should serialize fresh session correctly', () => {
			const { session } = createSimpleQuiz();
			const compactState = StateCompression.serialize(session);
			
			expect(compactState).toHaveLength(TEST_COMPACT_STATE_LENGTH);
			expect(compactState[CompactStateIndex.VERSION]).toBe(TEST_EXPECTED_VERSION);
			expect(compactState[CompactStateIndex.COUNTER]).toBe(1); // counter starts at 1
			expect(compactState[CompactStateIndex.BINS]).toBe(TEST_MAXBINS); // bins
			expect(compactState[CompactStateIndex.CURRENT_WORD]).toBe(0); // currentWord starts at 0
			expect(compactState[CompactStateIndex.SESSION_CORRECT_ANSWERS]).toBe(0); // sessionCorrectAnswers starts at 0
			expect(compactState[CompactStateIndex.BIN_COUNT]).toEqual([3, 0, 0]); // binCount: all words in bin 0
			expect(compactState[CompactStateIndex.WORD_STATES]).toHaveLength(3); // 3 word states
		});

		it('should serialize word states correctly', () => {
			const { session } = createSimpleQuiz();

			const compactState = StateCompression.serialize(session);
			const wordStates = compactState[CompactStateIndex.WORD_STATES];
			
			// Note: lineNumber and side will vary based on the test data, so we check the structure
			expect(wordStates[0]).toHaveLength(6); // Should have 6 elements now
			expect(wordStates[0][WordStateIndex.BIN]).toBe(0);
			expect(wordStates[0][WordStateIndex.HOW_SOON]).toBe(-1);
			expect(wordStates[0][WordStateIndex.QUIZ_COUNT]).toBe(0);
			expect(wordStates[0][WordStateIndex.REMAINING_REPETITIONS]).toBe(1);
			expect(typeof wordStates[0][WordStateIndex.LINE_NUMBER]).toBe('number');
			expect(typeof wordStates[0][WordStateIndex.SIDE]).toBe('number');
		});

		it('should serialize modified session state', () => {
			const { session } = createSimpleQuiz();

			// Simulate answering first question correctly
			const result = session.compareAnswer(session.getCorrectAnswer());
			expect(result).toBe(true);
			
			const compactState = StateCompression.serialize(session);
			const wordStates = compactState[CompactStateIndex.WORD_STATES];
			
			// First word should have been moved to bin 1, with howSoon set
			expect(wordStates[0][WordStateIndex.BIN]).toBe(1); // bin = 1
			expect(wordStates[0][WordStateIndex.HOW_SOON]).toBe(-1);
			expect(wordStates[0][WordStateIndex.QUIZ_COUNT]).toBe(1); // quizCount = 1
			expect(wordStates[0][WordStateIndex.REMAINING_REPETITIONS]).toBe(1);
		});
	});

	describe('deserialize', () => {
		it('should deserialize fresh session state', () => {
			const { session } = createSimpleQuiz();

			const compactState = StateCompression.serialize(session);
			const restored = StateCompression.deserialize(compactState);
			
			expect(restored.version).toBe(TEST_EXPECTED_VERSION);
			expect(restored.counter).toBe(1);
			expect(restored.bins).toBe(TEST_MAXBINS);
			expect(restored.currentWord).toBe(0);
			expect(restored.sessionCorrectAnswers).toBe(0);
			expect(restored.binCount).toEqual([3, 0, 0]);
			expect(restored.wordStates).toHaveLength(3);
			
			// Check first word state structure (values will depend on test data)
			const firstWordState = restored.wordStates[0];
			expect(firstWordState.bin).toBe(0);
			expect(firstWordState.howSoon).toBe(-1);
			expect(firstWordState.quizCount).toBe(0);
			expect(firstWordState.remainingRepetitions).toBe(1);
			expect(typeof firstWordState.lineNumber).toBe('number');
			expect(typeof firstWordState.side).toBe('number');
		});

		it('should handle -1 howSoon values correctly', () => {
			const { session } = createSimpleQuiz();

			const compactState = StateCompression.serialize(session);
			const restored = StateCompression.deserialize(compactState);
			
			// All fresh words should have howSoon = -1
			restored.wordStates.forEach(wordState => {
				expect(wordState.howSoon).toBe(-1);
			});
		});

		it('should round-trip serialize/deserialize correctly', () => {
			const { session } = createSimpleQuiz();

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
			const invalidVersion = 999;
			const invalidState = [invalidVersion, 1, 4, 0, 0, [3, 0, 0, 0], []];
			
			expect(() => {
				StateCompression.deserialize(invalidState as CompactQuizState);
			}).toThrow(`Unsupported serialization version: ${invalidVersion}`);
		});
	});

	describe('estimateSize', () => {
		it('should calculate reasonable size estimate', () => {
			const { session } = createSimpleQuiz();

			const compactState = StateCompression.serialize(session);
			const estimatedSize = StateCompression.estimateSize(compactState);
			const actualSize = JSON.stringify(compactState).length;
			
			expect(estimatedSize).toBe(actualSize);
			expect(estimatedSize).toBeGreaterThan(0);
			expect(estimatedSize).toBeLessThan(1000); // Should be quite compact for 3 words
		});

		it('should show space savings vs object format', () => {
			const { session } = createSimpleQuiz();

			const compactState = StateCompression.serialize(session);
			const compactSize = StateCompression.estimateSize(compactState);
			
			// Compare with hypothetical object format (rough estimate)
			const objectFormat = {
				version: 1,
				counter: session.getCounter(),
				bins: session.getBins(),
				currentWord: session.getCurrentWordIndex(),
				binCount: [3, 0, 0],
				wordStates: [
					{ bin: 0, howSoon: -1, quizCount: 0, remainingRepetitions: 1 },
					{ bin: 0, howSoon: -1, quizCount: 0, remainingRepetitions: 1 },
					{ bin: 0, howSoon: -1, quizCount: 0, remainingRepetitions: 1 },
				]
			};
			const objectSize = JSON.stringify(objectFormat).length;
			
			// Array format should be more compact
			expect(compactSize).toBeLessThan(objectSize);
		});
	});

	describe('validateSize', () => {
		it('should validate small state as acceptable', () => {
			const { session } = createSimpleQuiz();
			const compactState = StateCompression.serialize(session);
			expect(StateCompression.validateSize(compactState)).toBe(true);
		});

		it('should reject state exceeding size limit', () => {
			const { session } = createSimpleQuiz();
			const compactState = StateCompression.serialize(session);
			const tinyLimit = 0.01; // 10 bytes - way too small
			expect(StateCompression.validateSize(compactState, tinyLimit)).toBe(false);
		});

		it('should use default 1MB limit', () => {
			const { session } = createSimpleQuiz();
			const compactState = StateCompression.serialize(session);
			expect(StateCompression.validateSize(compactState)).toBe(true);
		});
	});

	describe('session restoration integration', () => {
		it('should restore session state correctly', () => {
			const { session, quiz } = createSimpleQuiz();

			// Modify original session
			session.compareAnswer(session.getCorrectAnswer());
			const originalCounter = session.getCounter();
			const originalBinCount = session.getBinCount(1);
			
			// Serialize
			const compactState = StateCompression.serialize(session);
			
			// Restore state
			const restoredState = StateCompression.deserialize(compactState);
			const newSession = QuizSession.restoreState(quiz, restoredState);
			
			// Verify restoration
			expect(newSession.getCounter()).toBe(originalCounter);
			expect(newSession.getBinCount(1)).toBe(originalBinCount);
		});

		it('should handle lesson subset consistency across serialization', () => {
			const { session: originalSession, quiz: largeQuiz } = createLargeQuiz();

			// Capture the original selected words
			const originalWordStates = originalSession.getWordStates();
			const originalWords = originalWordStates.map(ws => ws.getWord().question);
			
			// Modify session state
			originalSession.compareAnswer(originalSession.getCorrectAnswer());
			
			// Serialize the session
			const compactState = StateCompression.serialize(originalSession);
			
			// Create a new session from the same quiz
			const restoredState = StateCompression.deserialize(compactState);
			const newSession = QuizSession.restoreState(largeQuiz, restoredState);
			
			const restoredWords = new Set(newSession.getWordStates().map(ws => ws.getWord().question));
			
			// Verify that the subsets of words are the same before & after restoration.
			const intersection = new Set([...originalWords].filter(x => restoredWords.has(x)));
			expect(intersection.size).toBe(originalWords.length);
		});

		it('should restore bin counts accurately', () => {
			const { session: originalSession, quiz } = createSimpleQuiz();

			// Modify session state
			originalSession.compareAnswer(originalSession.getCorrectAnswer());
			
			// Serialize the session
			const compactState = StateCompression.serialize(originalSession);
			
			// And deserialize agains
			const restoredState = StateCompression.deserialize(compactState);
			const newSession = QuizSession.restoreState(quiz, restoredState);
			
			// aggregate bin counts of all words
			const actualBinCounts = new Array(newSession.getBins()).fill(0);
			for (const wordState of newSession.getWordStates()) {
				actualBinCounts[wordState.getBin()]++;
			}
			
			// Verify that restored bin counts match actual word distribution
			for (let i = 0; i < newSession.getBins(); i++) {
				expect(newSession.getBinCount(i)).toBe(actualBinCounts[i]);
			}
		});
	
	});

});

function createSimpleQuiz() {
	// Create a small quiz for testing
	const words: Word[] = [
		{ question: 'hello', answer: 'hola', side: 0, lineNumber: 0, template: 'What is' },
		{ question: 'goodbye', answer: 'adiós', side: 0, lineNumber: 1, template: 'What is' },
		{ question: 'cat', answer: 'gato', side: 0, lineNumber: 2, template: 'What is' }
	];
	
	const quiz = new Quiz(words);
	const session = QuizSession.newInstance(quiz);

	return { quiz, session };
}

function createLargeQuiz() {
	// Create a larger quiz with more words than MAX_LESSON_SIZE (20)
	const largeWordSet: Word[] = [];
	for (let i = 0; i < 30; i++) {
		largeWordSet.push({
			question: `word${i}`,
			answer: `answer${i}`,
			side: 0,
			lineNumber: i,
			template: 'What is'
		});
	}
	
	const quiz = new Quiz(largeWordSet);
	const session = QuizSession.newInstance(quiz);
	
	return { quiz, session };
}
