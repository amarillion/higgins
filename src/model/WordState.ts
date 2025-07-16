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

import type { Word } from './types';

export class WordState {
	static readonly MAXBINS = 10;

	private word: Word;
	private bin: number = 0;
	private howSoon: number = -1;
	private quizCount: number = 0;
	private correctInARow: number = 0;

	constructor(word: Word) {
		this.word = word;
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

	getCorrectInARow(): number {
		return this.correctInARow;
	}

	compareAnswer(answer: string, counter: number, binCount: number[]): boolean {
		const correct = this.word.answer.toLowerCase() === answer.toLowerCase();
		this.quizCount++;

		if (correct) {
			this.correctInARow++;
			// Move to next bin if answered correctly enough times
			if (this.bin < WordState.MAXBINS - 1) {
				binCount[this.bin]--;
				this.bin++;
				binCount[this.bin]++;
			}
			// Set when this word should be asked again
			this.howSoon = counter + Math.pow(2, this.bin);
		} else {
			this.correctInARow = 0;
			// Move back to first bin on wrong answer
			if (this.bin > 0) {
				binCount[this.bin]--;
				this.bin = 0;
				binCount[this.bin]++;
			}
			this.howSoon = -1; // Ask again soon
		}

		return correct;
	}
}