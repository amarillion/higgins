#!/usr/bin/env node

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

import * as fs from 'fs/promises';
import * as path from 'path';
import * as readline from 'readline';
import { fileURLToPath } from 'url';
import { Quiz, QuizSession } from './model';
import type { FileLoader } from './model';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const fsFileLoader: FileLoader = async (fileName: string): Promise<string> => {
	return await fs.readFile(fileName, 'utf-8');
};

async function askQuestion(question: string): Promise<string> {
	const rl = readline.createInterface({
		input: process.stdin,
		output: process.stdout,
	});

	return new Promise((resolve) => {
		rl.question(question, (answer) => {
			rl.close();
			resolve(answer.trim());
		});
	});
}

async function runQuiz(): Promise<void> {
	try {
		console.log('🎓 Dr. Higgins Quiz Session');
		console.log('==========================\n');

		const lessonPath = path.join(__dirname, '../public/lessons/spaans/spaans1.txt');
		console.log(`Loading lesson: ${lessonPath}`);

		const quiz = await Quiz.loadFromFile(lessonPath, fsFileLoader);
		console.log(`✅ Loaded ${quiz.getWords().length} word pairs\n`);

		const session = new QuizSession(quiz);
		let questionCount = 0;

		console.log('Instructions:');
		console.log('- Answer the questions as they appear');
		console.log('- Type "quit" to exit');
		console.log('- Your progress is tracked with a spaced repetition system\n');

		while (!session.isFinished()) {
			session.nextQuestion();
			questionCount++;

			const question = session.getQuestion();
			console.log(`\n📝 Question ${questionCount}:`);
			console.log(`${question}`);

			const userAnswer = await askQuestion('Your answer: ');

			if (userAnswer.toLowerCase() === 'quit') {
				console.log('\n👋 Thanks for studying!');
				break;
			}

			const isCorrect = session.compareAnswer(userAnswer);
			const correctAnswer = session.getCorrectAnswer();

			if (isCorrect) {
				console.log('✅ Correct!');
			} else {
				console.log(`❌ Incorrect. The correct answer is: ${correctAnswer}`);
				const hint = session.getHint();
				if (hint) {
					console.log(`💡 Hint: ${hint}`);
				}
			}

			// Show progress
			const bins = session.getBins();
			const progress = [];
			for (let i = 0; i < bins; i++) {
				progress.push(`Bin ${i + 1}: ${session.getBinCount(i)}`);
			}
			console.log(`📊 Progress: ${progress.join(', ')}`);
		}

		if (session.isFinished()) {
			console.log('\n🎉 Congratulations! You have completed the quiz!');
			console.log('All words have been moved to the final bin.');
		}

		console.log(`\n📈 Final Statistics:`);
		console.log(`Total questions asked: ${questionCount}`);
		console.log(`Current counter: ${session.getCounter()}`);

		const mostDifficult = session.getMostDifficult(5);
		if (mostDifficult.length > 0) {
			console.log('\n🔥 Most challenging words:');
			mostDifficult.forEach((wordState, index) => {
				const word = wordState.getWord();
				console.log(`${index + 1}. "${word.question}" → "${word.answer}" (${wordState.getQuizCount()} times)`);
			});
		}

	} catch (error) {
		console.error('❌ Error running quiz:', error);
		process.exit(1);
	}
}

// Run the CLI
runQuiz();