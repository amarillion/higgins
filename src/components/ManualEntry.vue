<script setup lang="ts">
import { ref, nextTick } from 'vue';

defineProps<{
	question: string,
	feedback: string,
	hint: string,
	isCorrect: boolean | null,
}>();

const emit = defineEmits<{
	answer: [answer: string],
}>();

const userAnswer = ref('');
const inputRef = ref<HTMLInputElement>();

const handleSubmit = () => {
	if (userAnswer.value.trim()) {
		emit('answer', userAnswer.value.trim());
		userAnswer.value = '';
		
		// Focus input after feedback is shown
		nextTick(() => {
			inputRef.value?.focus();
		});
	}
};

const handleKeydown = (event: KeyboardEvent) => {
	if (event.key === 'Enter') {
		handleSubmit();
	}
};
</script>
<template>
	<div class="manual-entry">
		<h2>Question</h2>
		
		<div class="question-container">
			<p class="question">{{ question }}</p>
		</div>
		
		<div class="input-container">
			<input
				ref="inputRef"
				v-model="userAnswer"
				type="text"
				placeholder="Type your answer here..."
				class="answer-input"
				@keydown="handleKeydown"
				:disabled="isCorrect !== null"
			/>
			<button
				@click="handleSubmit"
				:disabled="!userAnswer.trim() || isCorrect !== null"
				class="submit-button"
			>
				Submit
			</button>
		</div>
		
		<div v-if="feedback" class="feedback-container">
			<div
				class="feedback"
				:class="{
					'correct': isCorrect === true,
					'incorrect': isCorrect === false
				}"
			>
				{{ feedback }}
			</div>
			
			<div v-if="hint" class="hint">
				💡 {{ hint }}
			</div>
		</div>
	</div>
</template>

<style scoped>
.manual-entry {
	margin: 20px 0;
	padding: 20px;
	border: 1px solid #ddd;
	border-radius: 8px;
	background-color: #fff;
}

.question-container {
	margin: 20px 0;
}

.question {
	font-size: 18px;
	font-weight: bold;
	color: #333;
	margin: 0;
	padding: 15px;
	background-color: #f8f9fa;
	border: 1px solid #e9ecef;
	border-radius: 6px;
}

.input-container {
	display: flex;
	gap: 10px;
	margin: 20px 0;
}

.answer-input {
	flex: 1;
	padding: 12px;
	font-size: 16px;
	border: 2px solid #ddd;
	border-radius: 6px;
	outline: none;
	transition: border-color 0.2s ease;
}

.answer-input:focus {
	border-color: #007bff;
}

.answer-input:disabled {
	background-color: #f8f9fa;
	cursor: not-allowed;
}

.submit-button {
	padding: 12px 24px;
	font-size: 16px;
	background-color: #007bff;
	color: white;
	border: none;
	border-radius: 6px;
	cursor: pointer;
	transition: background-color 0.2s ease;
}

.submit-button:hover:not(:disabled) {
	background-color: #0056b3;
}

.submit-button:disabled {
	background-color: #6c757d;
	cursor: not-allowed;
}

.feedback-container {
	margin-top: 20px;
}

.feedback {
	padding: 12px;
	border-radius: 6px;
	font-weight: bold;
	margin-bottom: 10px;
}

.feedback.correct {
	background-color: #d4edda;
	color: #155724;
	border: 1px solid #c3e6cb;
}

.feedback.incorrect {
	background-color: #f8d7da;
	color: #721c24;
	border: 1px solid #f5c6cb;
}

.hint {
	background-color: #fff3cd;
	color: #856404;
	border: 1px solid #ffeaa7;
	padding: 10px;
	border-radius: 6px;
	font-size: 14px;
}
</style>
