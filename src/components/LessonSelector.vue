<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';

interface LessonDirectory {
	[language: string]: string[],
}

interface SelectedLesson {
	language: string,
	lessonPath: string,
	lessonName: string,
}

const emit = defineEmits<{
	lessonSelected: [lesson: SelectedLesson],
}>();

const lessonDirectory = ref<LessonDirectory>({});
const selectedLanguage = ref<string>('');
const selectedLessonPath = ref<string>('');
const loading = ref(true);
const error = ref<string | null>(null);

const availableLanguages = computed(() => Object.keys(lessonDirectory.value));
const availableLessons = computed(() => {
	if (!selectedLanguage.value || !lessonDirectory.value[selectedLanguage.value]) {
		return [];
	}
	return lessonDirectory.value[selectedLanguage.value];
});

const loadLessonDirectory = async () => {
	try {
		loading.value = true;
		error.value = null;
		
		const response = await fetch('/lesson-directory.json');
		if (!response.ok) {
			throw new Error(`Failed to load lesson directory: ${response.statusText}`);
		}
		
		const data = await response.json();
		lessonDirectory.value = data;
		
		// Auto-select first language if available
		const languages = Object.keys(data);
		if (languages.length > 0) {
			selectedLanguage.value = languages[0];
		}
		
	} catch (err) {
		error.value = `Failed to load lesson directory: ${err instanceof Error ? err.message : 'Unknown error'}`;
		console.error('Error loading lesson directory:', err);
	} finally {
		loading.value = false;
	}
};

const extractLessonName = (lessonPath: string): string => {
	// Extract just the filename without extension from the full path
	const filename = lessonPath.split('/').pop() || lessonPath;
	return filename.replace('.txt', '');
};

const onLanguageChange = () => {
	selectedLessonPath.value = '';
};

const startLesson = () => {
	if (!selectedLanguage.value || !selectedLessonPath.value) {
		return;
	}
	
	// Remove 'public/' prefix from lesson path for the Quiz.loadFromFile call
	const cleanPath = selectedLessonPath.value.replace(/^public/, '');
	
	const selectedLesson: SelectedLesson = {
		language: selectedLanguage.value,
		lessonPath: cleanPath,
		lessonName: extractLessonName(selectedLessonPath.value)
	};
	
	emit('lessonSelected', selectedLesson);
};

onMounted(() => {
	loadLessonDirectory();
});
</script>

<template>
	<div class="lesson-selector">
		<h1>Select a Lesson</h1>
		
		<div v-if="loading" class="loading">
			Loading available lessons...
		</div>
		
		<div v-else-if="error" class="error">
			{{ error }}
			<button @click="loadLessonDirectory">Retry</button>
		</div>
		
		<div v-else class="selector-form">
			<div class="form-group">
				<label for="language-select">Language:</label>
				<select
					id="language-select"
					v-model="selectedLanguage"
					@change="onLanguageChange"
					class="form-control"
				>
					<option value="">-- Select Language --</option>
					<option
						v-for="language in availableLanguages"
						:key="language"
						:value="language"
					>
						{{ language }}
					</option>
				</select>
			</div>
			
			<div v-if="selectedLanguage" class="form-group">
				<label for="lesson-select">Lesson:</label>
				<select
					id="lesson-select"
					v-model="selectedLessonPath"
					class="form-control"
				>
					<option value="">-- Select Lesson --</option>
					<option
						v-for="lessonPath in availableLessons"
						:key="lessonPath"
						:value="lessonPath"
					>
						{{ extractLessonName(lessonPath) }}
					</option>
				</select>
			</div>
			
			<button
				v-if="selectedLanguage && selectedLessonPath"
				@click="startLesson"
				class="start-button"
			>
				Start {{ selectedLanguage }} Lesson
			</button>
		</div>
	</div>
</template>

<style scoped>
.lesson-selector {
	max-width: 600px;
	margin: 0 auto;
	padding: 40px 20px;
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

.selector-form {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.form-group {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.form-group label {
	font-weight: 600;
	color: #333;
}

.form-control {
	padding: 12px;
	border: 2px solid #ddd;
	border-radius: 6px;
	font-size: 16px;
	background-color: white;
}

.form-control:focus {
	outline: none;
	border-color: #007bff;
	box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.start-button {
	padding: 15px 30px;
	background-color: #28a745;
	color: white;
	border: none;
	border-radius: 8px;
	font-size: 18px;
	font-weight: 600;
	cursor: pointer;
	transition: background-color 0.2s;
	margin-top: 20px;
}

.start-button:hover {
	background-color: #218838;
}

.start-button:active {
	transform: translateY(1px);
}
</style>