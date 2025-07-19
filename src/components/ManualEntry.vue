<script setup lang="ts">
import { ref } from 'vue';
import QuestionInput from './QuestionInput.vue';
import QuestionResult from './QuestionResult.vue';

defineProps<{
	question: string,
	feedback: string,
	hint: string,
	isCorrect: boolean | null,
	userAnswer?: string,
	correctAnswer?: string,
}>();

const emit = defineEmits<{
	answer: [answer: string],
}>();

const questionInputRef = ref<InstanceType<typeof QuestionInput>>();

const handleAnswer = (answer: string) => {
	emit('answer', answer);
};

// Expose method to focus input for parent components
defineExpose({
	focusInput: () => {
		questionInputRef.value?.focusInput();
	}
});
</script>
<template>
	<div class="manual-entry">
		<QuestionInput
			ref="questionInputRef"
			:question="question"
			:disabled="isCorrect !== null"
			@answer="handleAnswer"
		/>
		
		<QuestionResult
			:feedback="feedback"
			:hint="hint"
			:is-correct="isCorrect"
			:user-answer="userAnswer"
			:correct-answer="correctAnswer"
		/>
	</div>
</template>

<style scoped>
.manual-entry {
	display: flex;
	flex-direction: column;
	gap: 16px;
}
</style>
