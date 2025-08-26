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

import { randomIntBetween } from '../util/random';
import type { Word } from './types';

/**
 * Compare answer against quiz, taking into account special characters.
 * An answer containing a single slash means that there are two options that may be swapped.
 */
export function compareMagically(observed: string, expected: string) {
	const a = observed.toLowerCase().trim();
	const b = expected.toLowerCase().trim();
	if (a === b) return true;
	
	// if the answer has slashes in it, it is allowed to change the order
	const parts = b.split (' / ');
	if ((parts.length === 2) && (a === `${parts[1]} / ${parts[0]}`)) {
		return true;
	}
	
	// if the answer has a pipe in it, then only one of the two has to match
	const options = b.split (' | ');
	if ((options.length === 2) && (a === options[0] || a === options[1])) {
		return true;
	}
	
	return false;
}

export class WordState {
	static readonly MAXBINS = 3;

	private word: Word;
	private bin: number = 0;

	/* howSoon is used to insert repetitions into the upcoming queue
	 it is either -1, or a counter value indicating when it's going to be overdue */
	private howSoon: number = -1;

	private quizCount: number = 0;
	
	/**
	 * Words start off with only 1 required repetition for advancement.
	 * Mistakes must be repeated two times before they can advance to the next bin.
	 * After a mistake is cleared, the required repetitions go back to 1.
	 */
	private remainingRepetitions: number = 1;

	private constructor(word: Word) {
		this.word = word;
	}

	static newInstance(word: Word) {
		return new WordState(word);
	}

	getWord(): Word {
		return this.word;
	}

	getBin(): number {
		return this.bin;
	}

	getHowSoon(): number {
		return this.howSoon;
	}

	getQuizCount(): number {
		return this.quizCount;
	}

	getRemainingRepetitions(): number {
		return this.remainingRepetitions;
	}

	compareAnswer(answer: string, counter: number, binCount: number[]): boolean {
		const correct = compareMagically(answer, this.word.answer);
		this.quizCount++;

		if (correct) {
			// Move to next bin if answered correctly enough times
			if (--this.remainingRepetitions === 0) {
				if (this.bin < WordState.MAXBINS - 1) {
					binCount[this.bin]--;
					this.bin++;
					binCount[this.bin]++;
				}
				this.howSoon = -1;
				this.remainingRepetitions = 1;
			}
			else {
				// Still more correct answers needed to move to next bin, so
				// set to be repeated soon, but not immediately
				this.howSoon = counter + randomIntBetween(2, 3);
			}
		} else {
			// Incorrect answer, repeat two times
			this.remainingRepetitions = 2;
			this.howSoon = counter + randomIntBetween(2, 3);

			// TODO: keep statistics on wrong answers here.
		}

		return correct;
	}

	// State restoration support
	static restoreState(word: Word, state: { bin: number, howSoon: number, quizCount: number, remainingRepetitions: number }) {
		const result = new WordState(word);
		result.bin = state.bin;
		result.howSoon = state.howSoon;
		result.quizCount = state.quizCount;
		result.remainingRepetitions = state.remainingRepetitions;
		return result;
	}

}