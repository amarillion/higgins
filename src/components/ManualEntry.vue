<script setup lang="ts">
import { ref } from 'vue';
import QuestionInput from './QuestionInput.vue';
import MultipleChoiceInput from './MultipleChoiceInput.vue';
import QuestionResult from './QuestionResult.vue';

interface Choice {
	text: string,
	isCorrect: boolean,
}

const props = defineProps<{
	question: string,
	feedback: string,
	hint: string,
	isCorrect: boolean | null,
	userAnswer?: string,
	correctAnswer?: string,
	inputType?: 'text' | 'multiple-choice',
	choices?: Choice[],
}>();

const emit = defineEmits<{
	answer: [answer: string],
}>();

const questionInputRef = ref<InstanceType<typeof QuestionInput>>();
const multipleChoiceRef = ref<InstanceType<typeof MultipleChoiceInput>>();

const handleAnswer = (answer: string) => {
	emit('answer', answer);
};

// Expose method to focus input for parent components
defineExpose({
	focusInput: () => {
		if (props.inputType === 'text') {
			questionInputRef.value?.focusInput();
		}
		// Multiple choice doesn't need focus since it uses mouse/keyboard shortcuts
	}
});
</script>
<template>
	<div class="manual-entry">
		<QuestionInput
			v-if="inputType === 'text' || !inputType"
			ref="questionInputRef"
			:question="question"
			:disabled="isCorrect !== null"
			@answer="handleAnswer"
		/>
		
		<MultipleChoiceInput
			v-else-if="inputType === 'multiple-choice'"
			ref="multipleChoiceRef"
			:question="question"
			:choices="choices || []"
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
