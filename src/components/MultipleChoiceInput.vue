<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';

interface Choice {
	text: string,
	isCorrect: boolean,
}

const props = defineProps<{
	question: string,
	choices: Choice[],
	disabled?: boolean,
}>();

const emit = defineEmits<{
	answer: [answer: string],
}>();

const selectedChoice = ref<number | null>(null);
const hoveredChoice = ref<number | null>(null);

const shuffledChoices = computed(() => {
	// Shuffle choices to randomize display order
	const choices = [...(props.choices || [])];
	for (let i = choices.length - 1; i > 0; i--) {
		const j = Math.floor(Math.random() * (i + 1));
		[choices[i], choices[j]] = [choices[j], choices[i]];
	}
	return choices;
});

// Clear selection when question/choices change
watch(() => props.question, () => {
	selectedChoice.value = null;
});

watch(() => props.choices, () => {
	selectedChoice.value = null;
});

const handleChoiceClick = (index: number) => {
	if (props.disabled) return;
	
	selectedChoice.value = index;
	const choice = shuffledChoices.value[index];
	
	if (choice) {
		emit('answer', choice.text);
	}
};

const handleKeydown = (event: KeyboardEvent) => {
	if (props.disabled) return;
	
	const key = event.key;
	if (key >= '1' && key <= '9') {
		const index = parseInt(key) - 1;
		if (index < shuffledChoices.value.length) {
			handleChoiceClick(index);
		}
	}
};

// Add keyboard listener
onMounted(() => {
	document.addEventListener('keydown', handleKeydown);
});

// Clean up keyboard listener
onBeforeUnmount(() => {
	document.removeEventListener('keydown', handleKeydown);
});

// Get the correct choice for parent components
defineExpose({
	getCorrectAnswer: () => {
		const correctChoice = shuffledChoices.value.find(choice => choice.isCorrect);
		return correctChoice?.text || '';
	}
});
</script>

<template>
	<div class="multiple-choice-input">
		<h2>Question</h2>
		
		<div class="question-container">
			<p class="question">{{ question }}</p>
		</div>
		
		<div class="choices-container">
			<div class="choices-instruction">
				<p>Select the correct answer (or press number keys 1-{{ shuffledChoices.length }}):</p>
			</div>
			
			<div class="choices-grid">
				<button
					v-for="(choice, index) in shuffledChoices"
					:key="`${choice.text}-${index}`"
					class="choice-button"
					:class="{
						'selected': selectedChoice === index,
						'disabled': disabled
					}"
					@click="handleChoiceClick(index)"
					@mouseenter="hoveredChoice = index"
					@mouseleave="hoveredChoice = null"
					:disabled="disabled"
				>
					{{ choice.text }}
				</button>
			</div>
		</div>
	</div>
</template>

<style scoped>
.multiple-choice-input {
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

.choices-container {
	margin: 20px 0;
}

.choices-instruction {
	margin-bottom: 15px;
}

.choices-instruction p {
	margin: 0;
	font-size: 14px;
	color: #666;
	text-align: center;
}

.choices-grid {
	display: grid;
	gap: 12px;
	grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
}

.choice-button {
	display: block;
	padding: 15px;
	border: 2px solid #ddd;
	border-radius: 8px;
	background-color: white;
	cursor: pointer;
	transition: all 0.2s ease;
	text-align: left;
	font-size: 16px;
	font-weight: 500;
	line-height: 1.4;
	min-height: 60px;
}

.choice-button:hover:not(.disabled) {
	border-color: #007bff;
	background-color: #f8f9ff;
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
}

.choice-button.selected {
	border-color: #007bff;
	background-color: #e7f3ff;
}

.choice-button.disabled {
	cursor: not-allowed;
	opacity: 0.7;
	background-color: #f8f9fa;
}


/* Responsive design for smaller screens */
@media (max-width: 600px) {
	.choices-grid {
		grid-template-columns: 1fr;
	}
	
	.choice-button {
		min-height: 50px;
		padding: 12px;
	}
}
</style>