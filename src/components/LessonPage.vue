<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import ManualEntry from './ManualEntry.vue';
import ProgressView from './ProgressView.vue';
import { Quiz, QuizSession } from '../model';

const quiz = ref<Quiz | null>(null);
const session = ref<QuizSession | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);
const currentQuestion = ref<string>('');
const currentAnswer = ref<string>('');
const feedback = ref<string>('');
const hint = ref<string>('');
const isCorrect = ref<boolean | null>(null);

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
		
		// Load the Spanish lesson
		const loadedQuiz = await Quiz.loadFromFile('/lessons/spaans/ciudad_de_bestias_1.txt');
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
};

const handleAnswer = (answer: string) => {
	if (!session.value) return;
	
	const correct = session.value.compareAnswer(answer);
	const correctAnswer = session.value.getCorrectAnswer();
	
	isCorrect.value = correct;
	
	if (correct) {
		feedback.value = 'Correct!';
	} else {
		feedback.value = `Incorrect. The correct answer is: ${correctAnswer}`;
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
</script>
<template>
	<div class="lesson-page">
		<h1>Spanish Lesson Quiz</h1>
		
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
			<button @click="resetQuiz">Start Over</button>
		</div>
		
		<div v-else>
			<ManualEntry
				:question="currentQuestion"
				:feedback="feedback"
				:hint="hint"
				:is-correct="isCorrect"
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
