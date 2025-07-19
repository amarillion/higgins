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

import { Quiz } from './Quiz';
import { WordState } from './WordState';
import { SessionEventType } from './types';
import type { SessionListener } from './types';

export class QuizSession {
	private quiz: Quiz;
	private words: WordState[] = [];
	private bins: number = 4;
	private counter: number = 1;
	private hint: string | null = null;
	private currentWord: number = -1;
	private binCount: number[] = new Array(WordState.MAXBINS).fill(0);
	private listeners: SessionListener[] = [];

	constructor(quiz: Quiz) {
		this.quiz = quiz;
		
		for (const word of quiz.getWords()) {
			this.words.push(new WordState(word));
		}
		
		this.binCount[0] = this.words.length;
		this.shuffleWords();
		this.currentWord = 0;
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
		this.shuffleWords();

		let maxDue = -1;
		this.currentWord = -1;

		// Look for word with highest due
		for (let i = 0; i < this.words.length; i++) {
			const due = this.counter - this.words[i].getHowSoon();
			if (this.words[i].getHowSoon() !== -1 && due > maxDue) {
				maxDue = due;
				this.currentWord = i;
			}
		}

		const dueWait = Math.random() < 0.4 ? 2 : 3;

		// If no current word found, or there is no word with due higher than threshold
		if (this.currentWord === -1 || maxDue < dueWait) {
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
		for (let i = shuffled.length - 1; i > 0; i--) {
			const j = Math.floor(Math.random() * (i + 1));
			[shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
		}
		
		return shuffled.slice(0, count);
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

	private shuffleWords(): void {
		for (let i = this.words.length - 1; i > 0; i--) {
			const j = Math.floor(Math.random() * (i + 1));
			[this.words[i], this.words[j]] = [this.words[j], this.words[i]];
		}
	}

	private fireSessionChangedEvent(type: SessionEventType): void {
		for (const listener of this.listeners) {
			listener.sessionChanged(type);
		}
	}
}