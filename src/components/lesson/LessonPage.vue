<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import QuestionEntry from './QuestionEntry.vue';
import ProgressView from './ProgressView.vue';
import { Quiz, QuizSession } from '../../model';

import { store } from '../../store';
import { notNull } from '../../util/assert';

const quiz = ref<Quiz | null>(null);
const session = ref<QuizSession | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const currentQuestion = ref<string>('');
const currentAnswer = ref<string>('');
const feedback = ref<string>('');
const hint = ref<string>('');
const isCorrect = ref<boolean | null>(null);
const userAnswer = ref<string>('');
const correctAnswer = ref<string>('');
const inputType = ref<'text' | 'multiple-choice'>('text');
const choices = ref<Array<{ text: string, isCorrect: boolean }>>([]);

const progressData = computed(() => {
	if (!session.value) return null;
	
	const bins = session.value.getBins();
	const binData = [];
	
	for (let i = 0; i < bins; i++) {
		binData.push({
			bin: i + 1,
			count: session.value.getBinCount(i)
		});
	}
	
	return {
		bins: binData,
		counter: session.value.getCounter(),
		isFinished: session.value.isFinished()
	};
});

const loadQuiz = async () => {
	try {
		loading.value = true;
		error.value = null;
		
		const selectedLesson = notNull(store.selectedLesson);
		
		// Load the selected lesson
		const loadedQuiz = await Quiz.loadFromFile(selectedLesson.lessonPath);
		quiz.value = loadedQuiz;
		
		// Create a new session
		session.value = new QuizSession(loadedQuiz);
		
		// Get first question
		nextQuestion();
		
	} catch (err) {
		error.value = `Failed to load quiz: ${err instanceof Error ? err.message : 'Unknown error'}`;
		console.error('Error loading quiz:', err);
	} finally {
		loading.value = false;
	}
};

const nextQuestion = () => {
	if (!session.value) return;
	
	session.value.nextQuestion();
	currentQuestion.value = session.value.getQuestion();
	currentAnswer.value = '';
	feedback.value = '';
	hint.value = '';
	isCorrect.value = null;
	userAnswer.value = '';
	correctAnswer.value = '';
	
	// Determine input type based on current word's bin level
	const currentBin = session.value.getCurrentWordBin();
	const correct = session.value.getCorrectAnswer();
	
	if (currentBin === 0) {
		// Use multiple choice for bins 0-1 (newer/harder words)
		inputType.value = 'multiple-choice';
		
		// Get 3-7 random incorrect answers
		const numIncorrect = Math.floor(Math.random() * 5) + 3; // 3-7 incorrect choices
		const incorrectAnswers = session.value.getRandomIncorrectAnswers(correct, numIncorrect);
		
		// Create choices array with correct answer and incorrect answers
		const allChoices = [
			{ text: correct, isCorrect: true },
			...incorrectAnswers.map(answer => ({ text: answer, isCorrect: false }))
		];
		
		choices.value = allChoices;
	} else {
		// Use text input for bins 1+ (more learned words)
		inputType.value = 'text';
		choices.value = [];
	}
};

const handleAnswer = (answer: string) => {
	if (!session.value) return;
	
	const correct = session.value.compareAnswer(answer);
	const sessionCorrectAnswer = session.value.getCorrectAnswer();
	
	// Store the user's answer and correct answer
	userAnswer.value = answer;
	correctAnswer.value = sessionCorrectAnswer;
	isCorrect.value = correct;
	
	if (correct) {
		feedback.value = 'Correct!';
	} else {
		feedback.value = 'Incorrect.';
		const sessionHint = session.value.getHint();
		if (sessionHint) {
			hint.value = sessionHint;
		}
	}
	
	// Move to next question after a short delay
	setTimeout(() => {
		if (!session.value?.isFinished()) {
			nextQuestion();
		}
	}, 2000);
};

const resetQuiz = () => {
	loadQuiz();
};

onMounted(() => {
	loadQuiz();
});

function stopLesson() {
	// TODO: confirm dialog
	store.closeLesson();
};

</script>
<template>
	<div class="lesson-page">
		<div class="lesson-header">
			<button @click="stopLesson" class="back-button">← Back to Lessons</button>
			<h3>{{ store.selectedLesson?.language }} - {{ store.selectedLesson?.lessonName }}</h3>
		</div>
		
		<div v-if="loading" class="loading">
			Loading quiz...
		</div>
		
		<div v-else-if="error" class="error">
			{{ error }}
			<button @click="resetQuiz">Try Again</button>
		</div>
		
		<div v-else-if="progressData?.isFinished" class="finished">
			<h2>🎉 Congratulations!</h2>
			<p>You have completed the quiz!</p>
			<button @click="store.closeLesson">Back to Lobby</button>
		</div>
		
		<div v-else>
			<QuestionEntry
				:question="currentQuestion"
				:feedback="feedback"
				:hint="hint"
				:is-correct="isCorrect"
				:user-answer="userAnswer"
				:correct-answer="correctAnswer"
				:input-type="inputType"
				:choices="choices"
				@answer="handleAnswer"
			/>
			
			<ProgressView
				v-if="progressData"
				:progress="progressData"
			/>
		</div>
	</div>
</template>

<style scoped>
.lesson-page {
	max-width: 800px;
	margin: 0 auto;
}

.lesson-header {
	display: flex;
	align-items: center;
	gap: 15px;
	margin-bottom: 20px;
}

.back-button {
	padding: 8px 12px;
	background-color: #6c757d;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 14px;
	transition: background-color 0.2s;
}

.back-button:hover {
	background-color: #5a6268;
}

.lesson-header h1 {
	margin: 0;
	flex: 1;
}

.loading {
	text-align: center;
	padding: 40px;
	font-size: 18px;
	color: #666;
}

.error {
	text-align: center;
	padding: 40px;
	color: #dc3545;
	background-color: #f8d7da;
	border: 1px solid #f5c6cb;
	border-radius: 8px;
}

.error button {
	margin-top: 10px;
	padding: 8px 16px;
	background-color: #dc3545;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
}

.error button:hover {
	background-color: #c82333;
}

.finished {
	text-align: center;
	padding: 40px;
	background-color: #d4edda;
	border: 1px solid #c3e6cb;
	border-radius: 8px;
	color: #155724;
}

.finished button {
	margin-top: 15px;
	padding: 12px 24px;
	background-color: #28a745;
	color: white;
	border: none;
	border-radius: 6px;
	cursor: pointer;
	font-size: 16px;
}

.finished button:hover {
	background-color: #218838;
}
</style>
