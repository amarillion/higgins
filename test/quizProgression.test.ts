import { describe, it, expect, beforeEach } from 'vitest';
import { Quiz } from '../src/model/Quiz';
import { QuizSession } from '../src/model/QuizSession';
import { WordState } from '../src/model/WordState';
import type { Word } from '../src/model/types';

describe('Quiz Progression', () => {
	let quiz: Quiz;
	let session: QuizSession;

	beforeEach(() => {
		// Create a small lesson with just a few words for testing
		const words: Word[] = [
			{ question: 'hello', answer: 'hola', side: 0, lineNumber: 0, template: 'What is' },
			{ question: 'goodbye', answer: 'adiós', side: 0, lineNumber: 1, template: 'What is' },
			{ question: 'cat', answer: 'gato', side: 0, lineNumber: 2, template: 'What is' }
		];
		
		quiz = new Quiz(words);
		session = QuizSession.newInstance(quiz);
	});

	it('should not select words from the highest bin for questioning', () => {
		const wordStates = session.getWordStates();
		
		// Move all but one word to highest bin and make them "due" for questioning
		for (let wordIndex = 0; wordIndex < 2; wordIndex++) {
			const wordState = wordStates[wordIndex];
			
			// Move to highest bin
			while (wordState.getBin() < WordState.MAXBINS - 1) {
				const correctAnswer = wordState.getWord().answer;
				wordState.compareAnswer(correctAnswer, session.getCounter(), session['binCount']);
			}
			
			// Make this word "due" by setting howSoon to a past counter value
			// eslint-disable-next-line @typescript-eslint/no-explicit-any
			(wordState as any).howSoon = -5;
		}
		
		// Verify setup: first two words are in highest bin, third word in bin 0
		expect(wordStates[0].getBin()).toBe(WordState.MAXBINS - 1);
		expect(wordStates[1].getBin()).toBe(WordState.MAXBINS - 1);
		expect(wordStates[2].getBin()).toBe(0);
		
		// Now call nextQuestion multiple times to verify it never selects from highest bin
		const selectedBins: number[] = [];
		for (let i = 0; i < 10; i++) {
			session.nextQuestion();
			const currentWordIndex = session.getCurrentWordIndex();
			if (currentWordIndex >= 0) {
				// Get the actual word from the session's internal word array
				const internalWords = session.getWordStates();
				const actualCurrentWord = internalWords[currentWordIndex];
				const selectedBin = actualCurrentWord.getBin();
				selectedBins.push(selectedBin);
			}
		}
		
		// Words from highest bin should NEVER be selected
		for (const bin of selectedBins) {
			expect(bin).toBeLessThan(WordState.MAXBINS - 1);
		}
	});

	it('should consider session finished when all words are in highest bin', () => {
		// Move all words to highest bin
		const wordStates = session.getWordStates();
		for (const wordState of wordStates) {
			while (wordState.getBin() < WordState.MAXBINS - 1) {
				const correctAnswer = wordState.getWord().answer;
				wordState.compareAnswer(correctAnswer, 1, session['binCount']);
			}
		}
		
		// Session should be considered finished when all words are in highest bin
		expect(session.isFinished()).toBe(true);
		
		// Verify all words are indeed in highest bin
		for (const wordState of wordStates) {
			expect(wordState.getBin()).toBe(WordState.MAXBINS - 1);
		}
		
		// The bin count for the highest bin should equal total number of words
		const highestBinIndex = WordState.MAXBINS - 1;
		expect(session.getBinCount(highestBinIndex)).toBe(wordStates.length);
		
		// All other bins should be empty
		for (let i = 0; i < highestBinIndex; i++) {
			expect(session.getBinCount(i)).toBe(0);
		}
	});
});