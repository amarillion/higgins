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

export interface Word {
	question: string,
	answer: string,
	side: number,
	lineNumber: number,
	template: string,
}

export interface WordState {
	word: Word,
	bin: number,
	howSoon: number,
	quizCount: number,
	correctInARow: number,
	compareAnswer(answer: string, counter: number, binCount: number[]): boolean,
}

export enum SessionEventType {
	QUESTION_CHANGED = 'QUESTION_CHANGED',
	BINS_CHANGED = 'BINS_CHANGED'
}

export interface SessionListener {
	sessionChanged(type: SessionEventType): void,
}