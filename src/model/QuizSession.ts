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

import { shuffle } from '../util/random';
import { Quiz } from './Quiz';
import { WordState } from './WordState';
import { SessionEventType } from './types';
import type { SessionListener, Word } from './types';

export class QuizSession {
	private static readonly MAX_LESSON_SIZE = 15;

	private quiz: Quiz;
	private words: WordState[] = [];
	private bins: number = 4;
	private counter: number = 1;
	private hint: string | null = null;
	private currentWord: number = -1;
	private binCount: number[] = new Array(WordState.MAXBINS).fill(0);
	private listeners: SessionListener[] = [];
	private sessionCorrectAnswers: number = 0;

	constructor(quiz: Quiz) {
		this.quiz = quiz;
		
		// Get all available words
		const allWords = quiz.getWords();
		
		// Randomly select up to MAX_LESSON_SIZE words
		const selectedWords = this.selectRandomWords(allWords, QuizSession.MAX_LESSON_SIZE);
		
		for (const word of selectedWords) {
			this.words.push(new WordState(word));
		}
		
		this.binCount[0] = this.words.length;
		shuffle(this.words);
		this.currentWord = 0;
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
		let maxDue = -1;
		this.currentWord = -1;

		// Look for word with highest due, but exclude words from highest bin
		for (let i = 0; i < this.words.length; i++) {
			const due = this.counter - this.words[i].getHowSoon();
			if (this.words[i].getHowSoon() !== -1 &&
				due > maxDue &&
				this.words[i].getBin() < this.bins - 1) {
				maxDue = due;
				this.currentWord = i;
			}
		}

		const dueWait = Math.random() < 0.4 ? 2 : 3;

		// If no current word found, or there is no word with due higher than threshold
		if (this.currentWord === -1 || maxDue < dueWait) {
			// Reset current word to ensure we don't keep a highest-bin word from previous selection
			this.currentWord = -1;
			
			let i = 0;
			while (i < this.quiz.getWords().length &&
				   (this.words[i].getBin() >= this.bins - 1 || this.words[i].getHowSoon() !== -1)) {
				i++;
			}
			
			let j = i;
			if (i < this.quiz.getWords().length) {
				while (j < this.quiz.getWords().length &&
					   (this.words[j].getBin() >= this.bins - 1 || this.words[j].getHowSoon() !== -1)) {
					j++;
				}
			}
			
			if (i < this.quiz.getWords().length && j < this.quiz.getWords().length) {
				this.currentWord = this.words[i].getBin() > this.words[j].getBin() ? j : i;
			}
		}

		this.fireSessionChangedEvent(SessionEventType.QUESTION_CHANGED);
	}

	compareAnswer(answer: string): boolean {
		const result = this.words[this.currentWord].compareAnswer(answer, this.counter, this.binCount);
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
		return this.words[this.currentWord].getWord().answer;
	}

	getQuestion(): string {
		if (this.currentWord < 0 || this.currentWord >= this.quiz.getWords().length) {
			throw new Error('Invalid current word index');
		}
		
		const word = this.words[this.currentWord].getWord().question;
		const template = this.words[this.currentWord].getWord().template;
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
		return this.words[this.currentWord].getWord().question;
	}

	getCurrentWordBin(): number {
		if (this.currentWord < 0 || this.currentWord >= this.words.length) {
			return 0; // Default to lowest bin if no current word
		}
		return this.words[this.currentWord].getBin();
	}

	getRandomIncorrectAnswers(correctAnswer: string, count: number = 3): string[] {
		const allAnswers = this.quiz.getWords()
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
		return this.currentWord;
	}

	getSessionCorrectAnswers(): number {
		return this.sessionCorrectAnswers;
	}

	// Restore state from serialized data
	restoreState(state: {
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
	}): void {
		this.counter = state.counter;
		this.bins = state.bins;
		this.currentWord = state.currentWord;
		this.binCount = [...state.binCount];
		this.sessionCorrectAnswers = state.sessionCorrectAnswers || 0;
		
		// Restore word states by matching lineNumber and side
		for (const savedWordState of state.wordStates) {
			const matchingWordState = this.words.find(ws =>
				ws.getWord().lineNumber === savedWordState.lineNumber &&
				ws.getWord().side === savedWordState.side
			);
			
			if (matchingWordState) {
				matchingWordState.restoreState(savedWordState);
			}
		}
		
		this.fireSessionChangedEvent(SessionEventType.QUESTION_CHANGED);
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