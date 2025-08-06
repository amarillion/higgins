//    This file is part of Dr. Higgins.
//    Copyright 2003-2011 Martijn van Iersel <amarillion@yahoo.com>
//
//    Dr. Higgins is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Dr. Higgins is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Dr. Higgins.  If not, see <http://www.gnu.org/licenses/>.

import { assert } from '../util/assert';
import { pickOne, shuffle } from '../util/random';
import { Quiz } from './Quiz';
import { WordState } from './WordState';
import { SessionEventType } from './types';
import type { SessionListener, Word } from './types';

export class QuizSession {
	private static readonly MAX_LESSON_SIZE = 20;

	private quiz: Quiz;
	private words: WordState[] = [];
	private bins: number = WordState.MAXBINS;
	private counter: number = 1;
	private hint: string | null = null;
	private currentWordIdx: number = -1;
	private binCount: number[] = new Array(WordState.MAXBINS).fill(0);
	private listeners: SessionListener[] = [];
	private sessionCorrectAnswers: number = 0;

	static newInstance(quiz: Quiz) {
		return new QuizSession(quiz);
	}

	private constructor(quiz: Quiz) {
		this.quiz = quiz;
		
		// Get all available words
		const allWords = quiz.getWords();
		
		// Randomly select up to MAX_LESSON_SIZE words
		const selectedWords = this.selectRandomWords(allWords, QuizSession.MAX_LESSON_SIZE);
		
		this.words = selectedWords.map(word => WordState.newInstance(word));
		
		this.binCount[0] = this.words.length;
		shuffle(this.words);
		this.currentWordIdx = 0;
	}

	/**
	 * Randomly select up to maxCount words from the given array
	 */
	private selectRandomWords(words: Word[], maxCount: number): Word[] {
		if (words.length <= maxCount) {
			return [...words]; // Return all words if we have fewer than maxCount
		}
		
		const shuffled = [...words];
		shuffle(shuffled);
		
		return shuffled.slice(0, maxCount);
	}

	isFinished(): boolean {
		// Check if all bins before last are empty
		for (let i = 0; i < this.getBins() - 1; i++) {
			if (this.getBinCount(i) !== 0) {
				return false;
			}
		}
		return true;
	}

	nextQuestion(): void {
		shuffle(this.words);

		const available = this.words.filter(word => word.getBin() < this.bins - 1);

		let selectedWord: WordState | null = null;

		// Look for word with highest due
		const [ firstDue ] = available
			.filter(word => word.getHowSoon() !== -1) // discard words that are not scheduled at all
			.filter(word => this.counter - word.getHowSoon() >= 0) // discard words that are not yet due
			.sort((a, b) => b.getHowSoon() - a.getHowSoon()); // highest due first
		
		if (firstDue) {
			selectedWord = firstDue;
		}

		if (!selectedWord) {
			const notDue = available.filter(word => word.getHowSoon() === -1);
			if (notDue.length > 0) {
				// do two random samples, and pick the one in the lowest bin.
				const word1 = pickOne(notDue);
				const word2 = pickOne(notDue);
				selectedWord = word1.getBin() > word2.getBin() ? word2 : word1;
			}
		}

		if (!selectedWord) {
			selectedWord = pickOne(available);
		}
		
		this.currentWordIdx = this.words.indexOf(selectedWord);
		this.fireSessionChangedEvent(SessionEventType.QUESTION_CHANGED);
	}

	compareAnswer(answer: string): boolean {
		const result = this.words[this.currentWordIdx].compareAnswer(answer, this.counter, this.binCount);
		this.hint = null;
		
		if (!result) {
			const otherWord = this.quiz.getByAnswer(answer);
			if (otherWord) {
				this.hint = `You may be confused with "${answer}" -> "${otherWord}"`;
			}
		} else {
			// Track correct answers for streak tracking
			this.sessionCorrectAnswers++;
			this.fireSessionChangedEvent(SessionEventType.ANSWER_CORRECT);
		}
		
		this.counter++;
		return result;
	}

	getCorrectAnswer(): string {
		return this.words[this.currentWordIdx].getWord().answer;
	}

	getQuestion(): string {
		if (this.currentWordIdx < 0 || this.currentWordIdx >= this.quiz.getWords().length) {
			throw new Error('Invalid current word index');
		}
		
		const word = this.words[this.currentWordIdx].getWord().question;
		const template = this.words[this.currentWordIdx].getWord().template;
		const pos = template.indexOf('""');
		
		if (pos >= 0) {
			return template.substring(0, pos + 1) + word + template.substring(pos + 1);
		} else {
			return template + ' ' + word;
		}
	}

	getHint(): string | null {
		return this.hint;
	}

	getMostDifficult(amount: number): WordState[] {
		const result = [...this.words];
		result.sort((a, b) => b.getQuizCount() - a.getQuizCount());
		return result.slice(0, amount);
	}

	getCounter(): number {
		return this.counter;
	}

	getBins(): number {
		return this.bins;
	}

	getBinCount(bin: number): number {
		return this.binCount[bin];
	}

	setBins(newCount: number): void {
		this.bins = newCount;
		this.fireSessionChangedEvent(SessionEventType.BINS_CHANGED);
	}

	getQuiz(): Quiz {
		return this.quiz;
	}

	getCurrentWord(): string {
		return this.words[this.currentWordIdx].getWord().question;
	}

	getCurrentWordBin(): number {
		if (this.currentWordIdx < 0 || this.currentWordIdx >= this.words.length) {
			return 0; // Default to lowest bin if no current word
		}
		return this.words[this.currentWordIdx].getBin();
	}

	getRandomIncorrectAnswers(correctAnswer: string, count: number = 3): string[] {
		// get side of the current word
		const currentSide = this.words[this.currentWordIdx].getWord().side;

		const allAnswers = this.quiz.getWords()
			.filter(word => word.side === currentSide)
			.map(word => word.answer)
			.filter(answer => answer.toLowerCase().trim() !== correctAnswer.toLowerCase().trim());
		
		// Shuffle array using Fisher-Yates algorithm
		const shuffled = [...allAnswers];
		shuffle(shuffled);
		
		return shuffled.slice(0, count);
	}

	// State serialization support
	getWordStates(): WordState[] {
		return [...this.words];
	}

	getCurrentWordIndex(): number {
		return this.currentWordIdx;
	}

	getSessionCorrectAnswers(): number {
		return this.sessionCorrectAnswers;
	}

	// Restore state from serialized data
	static restoreState(quiz: Quiz, state: {
		counter: number,
		bins: number,
		currentWord: number,
		binCount: number[],
		wordStates: Array<{
			bin: number,
			howSoon: number,
			quizCount: number,
			remainingRepetitions: number,
			lineNumber: number,
			side: number,
		}>,
		sessionCorrectAnswers?: number,
	}) {
		const qs = new QuizSession(quiz);
		qs.counter = state.counter;
		qs.bins = state.bins;
		qs.currentWordIdx = state.currentWord;
		qs.binCount = [...state.binCount];
		qs.sessionCorrectAnswers = state.sessionCorrectAnswers || 0;

		// Get all available words
		const allWords = quiz.getWords();

		qs.words = state.wordStates.map(ws => {
			const matchingWord = allWords.find(w =>
				w.lineNumber === ws.lineNumber && w.side === ws.side
			);
			//TODO: graceful degradation instead of assert
			assert(matchingWord, `Word not found for lineNumber ${ws.lineNumber} and side ${ws.side}`);
			
			return WordState.restoreState(matchingWord, ws);
		});
		
		qs.fireSessionChangedEvent(SessionEventType.QUESTION_CHANGED);
		return qs;
	}

	addListener(listener: SessionListener): void {
		this.listeners.push(listener);
	}

	removeListener(listener: SessionListener): void {
		const index = this.listeners.indexOf(listener);
		if (index > -1) {
			this.listeners.splice(index, 1);
		}
	}

	private fireSessionChangedEvent(type: SessionEventType): void {
		for (const listener of this.listeners) {
			listener.sessionChanged(type);
		}
	}
}