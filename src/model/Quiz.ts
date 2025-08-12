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
import { hashLessonContent } from './hash';

export type FileLoader = (fileName: string) => Promise<string>;

export class Quiz {
	words: Word[] = [];
	wordMap: Map<string, string> = new Map();
	askBothWays: number = 1;
	fileName: string | null = null;
	originalTimeStamp: number = 0;
	contentHash: string | null = null;

	constructor(words?: Word[]) {
		if (words) {
			this.words = [...words];
			this.fileName = null;
			this.originalTimeStamp = 0;
			this.contentHash = null;
			for (const word of words) {
				this.wordMap.set(word.answer, word.question);
			}
		}
	}

	getFile(): string | null {
		return this.fileName;
	}

	getContentHash(): string | null {
		return this.contentHash;
	}

	static async loadFromFile(fileName: string, fileLoader?: FileLoader): Promise<Quiz> {
		const loader = new QuizLoader(fileName, fileLoader);
		await loader.processFile();
		return loader.getQuiz();
	}

	getWords(): Word[] {
		return this.words;
	}

	getByAnswer(answer: string): string | undefined {
		return this.wordMap.get(answer);
	}

	modifiedOnDisk(): boolean {
		// In a web environment, we can't easily check file modification times
		// This would need to be implemented based on the specific file system access method
		return false;
	}
}

class QuizLoader {
	private result = new Quiz();
	private question1 = 'What is ""';
	private question2 = 'What is ""';
	encoding = 'UTF-8';
	private questions = new Set<string>();
	private answers = new Set<string>();
	private words: [string, string][] = [];
	private fileLoader: FileLoader;
	private originalContent = '';

	constructor(fileName: string, fileLoader?: FileLoader) {
		this.result.fileName = fileName;
		this.result.originalTimeStamp = Date.now();
		this.fileLoader = fileLoader || this.defaultFetchLoader;
	}

	private defaultFetchLoader: FileLoader = async (fileName: string): Promise<string> => {
		const normalizedFileName = fileName.startsWith('/') ? fileName.substring(1) : fileName; // double slashes trip up serviceWorker
		const response = await fetch(normalizedFileName);
		return response.text();
	};

	async processFile(): Promise<void> {
		try {
			const text = await this.fileLoader(this.result.fileName!);
			this.originalContent = text;
			const lines = text.split('\n');
			
			for (let i = 0; i < lines.length; i++) {
				this.processLine(lines[i], i + 1);
			}
		} catch (error) {
			console.error('Error processing file:', error);
			throw error;
		}
	}

	private processLine(line: string, lineNo: number): void {
		if (line.match(/^\s*$/)) {
			// empty or whitespace, ignore
			return;
		}

		if (line.charAt(0) === '#') {
			const pos = line.indexOf('=');
			if (pos >= 0) {
				const first = line.substring(1, pos).trim();
				const last = line.substring(pos + 1).trim();

				if (first.toLowerCase() === 'question1') {
					this.question1 = last;
				} else if (first.toLowerCase() === 'question2') {
					this.question2 = last;
				} else if (first.toLowerCase() === 'askbothways') {
					this.result.askBothWays = parseInt(last);
				} else if (first.toLowerCase() === 'encoding') {
					this.encoding = last;
				}
			}
		} else {
			const pos = line.indexOf(', ');
			if (pos >= 0) {
				const first = line.substring(0, pos).trim();
				const last = line.substring(pos + 2).trim();

				if (!this.questions.has(first)) {
					this.questions.add(first);
					if (!this.answers.has(last)) {
						this.answers.add(last);
						this.words.push([first, last]);
					} else {
						console.warn(`Duplicate answer "${last}" at line ${lineNo}`);
					}
				} else {
					console.warn(`Duplicate question "${first}" at line ${lineNo}`);
				}
			} else {
				console.warn(`Syntax error at line ${lineNo}`);
			}
		}
	}

	getQuiz(): Quiz {
		let lineNumber = 0;
		for (const [question, answer] of this.words) {
			this.result.words.push({
				question,
				answer,
				side: 0,
				lineNumber,
				template: this.question2
			});
			this.result.wordMap.set(answer, question);

			if (this.result.askBothWays !== 0) {
				this.result.words.push({
					question: answer,
					answer: question,
					side: 1,
					lineNumber,
					template: this.question1
				});
				this.result.wordMap.set(question, answer);
			}
			lineNumber++;
		}

		// Calculate content hash
		this.result.contentHash = hashLessonContent(this.originalContent);

		return this.result;
	}
}