<script setup lang="ts">
defineProps<{
	feedback: string,
	hint: string,
	isCorrect: boolean | null,
	userAnswer?: string,
	correctAnswer?: string,
}>();
</script>

<template>
	<div v-if="feedback" class="question-result">
		<div
			class="feedback"
			:class="{
				'correct': isCorrect === true,
				'incorrect': isCorrect === false
			}"
		>
			<div class="feedback-text">
				{{ feedback }}
			</div>
			
			<div v-if="userAnswer && correctAnswer && isCorrect === false" class="answer-comparison">
				<div class="answer-row">
					<span class="label">Your answer:</span>
					<span class="user-answer">{{ userAnswer }}</span>
				</div>
				<div class="answer-row">
					<span class="label">Correct answer:</span>
					<span class="correct-answer">{{ correctAnswer }}</span>
				</div>
			</div>
		</div>
		
		<div v-if="hint" class="hint">
			💡 {{ hint }}
		</div>
	</div>
</template>

<style scoped>
.question-result {
	margin: 20px 0;
	animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(20px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.feedback {
	padding: 16px;
	border-radius: 8px;
	font-weight: 600;
	margin-bottom: 12px;
	border: 2px solid transparent;
	transition: all 0.3s ease;
}

.feedback.correct {
	background-color: #d4edda;
	color: #155724;
	border-color: #c3e6cb;
	box-shadow: 0 2px 8px rgba(40, 167, 69, 0.15);
}

.feedback.incorrect {
	background-color: #f8d7da;
	color: #721c24;
	border-color: #f5c6cb;
	box-shadow: 0 2px 8px rgba(220, 53, 69, 0.15);
}

.feedback-text {
	font-size: 16px;
	margin-bottom: 12px;
}

.answer-comparison {
	background-color: rgba(255, 255, 255, 0.3);
	border-radius: 6px;
	padding: 12px;
	margin-top: 8px;
}

.answer-row {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-bottom: 6px;
}

.answer-row:last-child {
	margin-bottom: 0;
}

.label {
	font-weight: 500;
	min-width: 100px;
	font-size: 14px;
	opacity: 0.8;
}

.user-answer {
	font-family: 'Courier New', monospace;
	background-color: rgba(220, 53, 69, 0.1);
	padding: 4px 8px;
	border-radius: 4px;
	color: #721c24;
	font-weight: 500;
}

.correct-answer {
	font-family: 'Courier New', monospace;
	background-color: rgba(40, 167, 69, 0.1);
	padding: 4px 8px;
	border-radius: 4px;
	color: #155724;
	font-weight: 500;
}

.hint {
	background-color: #fff3cd;
	color: #856404;
	border: 2px solid #ffeaa7;
	padding: 12px;
	border-radius: 8px;
	font-size: 14px;
	line-height: 1.4;
	box-shadow: 0 2px 8px rgba(255, 193, 7, 0.15);
	animation: pulse 0.5s ease-in;
}

@keyframes pulse {
	0% {
		transform: scale(1);
	}
	50% {
		transform: scale(1.02);
	}
	100% {
		transform: scale(1);
	}
}

.hint::before {
	content: '💡';
	margin-right: 8px;
	font-size: 16px;
}
</style>