<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue';

defineProps<{
	question: string,
	disabled?: boolean,
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
		
		// Focus input after submission
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

const focusInput = () => {
	nextTick(() => {
		inputRef.value?.focus();
	});
};

// Auto-focus input when component mounts
onMounted(() => {
	focusInput();
});

// Expose focus method for parent components
defineExpose({
	focusInput
});
</script>

<template>
	<div class="question-input">
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
				:disabled="disabled"
			/>
			<button
				@click="handleSubmit"
				:disabled="!userAnswer.trim() || disabled"
				class="submit-button"
			>
				Submit
			</button>
		</div>
	</div>
</template>

<style scoped>
.question-input {
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
	animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(-10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
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
</style>