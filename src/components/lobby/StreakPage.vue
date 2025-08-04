<template>
	<div class="streak-page">
		<h1>Learning Streak</h1>
		
		<div v-if="streakInfo" class="streak-content">
			<!-- Current Streak Display -->
			<div class="streak-summary">
				<div class="streak-number">
					<span class="streak-count">{{ streakInfo.currentStreak }}</span>
					<span class="streak-label">day{{ streakInfo.currentStreak !== 1 ? 's' : '' }}</span>
				</div>
				<p class="streak-description">Current streak</p>
			</div>

			<!-- Today's Progress -->
			<div class="today-progress">
				<h3>Today's Progress</h3>
				<div class="progress-bar">
					<div
						class="progress-fill"
						:style="{ width: progressPercentage + '%' }"
					></div>
				</div>
				<p class="progress-text">
					{{ streakInfo.todayProgress }} / {{ requiredDaily }} lessons completed
					<span v-if="streakInfo.todayComplete" class="completed">✓ Complete!</span>
				</p>
			</div>

			<!-- 28-Day Calendar -->
			<div class="calendar-section">
				<h3>Past 28 Days</h3>
				<div class="calendar-grid">
					<div
						v-for="day in streakInfo.past28Days"
						:key="day.date"
						class="calendar-day"
						:class="{
							'completed': day.isChecked,
							'partial': day.lessonsCompleted > 0 && !day.isChecked,
							'today': isToday(day.date)
						}"
						:title="getDayTooltip(day)"
					>
						<div class="day-number">{{ getDayNumber(day.date) }}</div>
						<div class="day-month" v-if="isFirstOfMonth(day.date)">{{ getMonthName(day.date) }}</div>
					</div>
				</div>
			</div>

			<!-- Legend -->
			<div class="legend">
				<div class="legend-item">
					<div class="legend-dot completed"></div>
					<span>Complete ({{ requiredDaily }}+ lessons)</span>
				</div>
				<div class="legend-item">
					<div class="legend-dot partial"></div>
					<span>Some progress</span>
				</div>
				<div class="legend-item">
					<div class="legend-dot empty"></div>
					<span>No activity</span>
				</div>
			</div>
		</div>

		<div v-else class="loading">
			<p>Loading streak data...</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { store } from '../../store';
import { StreakStorage } from '../../store/streakStorage';

const streakInfo = ref(store.getStreakInfo());
const requiredDaily = StreakStorage.getRequiredDailyLessons();

const progressPercentage = computed(() => {
	if (!streakInfo.value) return 0;
	return Math.min((streakInfo.value.todayProgress / requiredDaily) * 100, 100);
});

function isToday(dateStr: string): boolean {
	const today = new Date().toISOString().split('T')[0];
	return dateStr === today;
}

function getDayNumber(dateStr: string): number {
	return new Date(dateStr).getDate();
}

function isFirstOfMonth(dateStr: string): boolean {
	return new Date(dateStr).getDate() === 1;
}

function getMonthName(dateStr: string): string {
	return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' });
}

function getDayTooltip(day: { date: string, isChecked: boolean, lessonsCompleted: number }): string {
	const date = new Date(day.date).toLocaleDateString();
	if (day.isChecked) {
		return `${date}: Complete! (${day.lessonsCompleted} lessons completed)`;
	} else if (day.lessonsCompleted > 0) {
		return `${date}: ${day.lessonsCompleted} lessons completed (need ${requiredDaily})`;
	} else {
		return `${date}: No activity`;
	}
}

// Refresh streak info when component mounts
onMounted(() => {
	store.initializeStreakData();
	streakInfo.value = store.getStreakInfo();
});
</script>

<style scoped>
.streak-page {
	padding: 2rem;
	max-width: 800px;
	margin: 0 auto;
	text-align: center;
}

h1 {
	color: #333;
	margin-bottom: 2rem;
	font-size: 2rem;
}

h3 {
	color: #555;
	margin-bottom: 1rem;
	font-size: 1.2rem;
}

.streak-content {
	display: flex;
	flex-direction: column;
	gap: 2rem;
}

/* Streak Summary */
.streak-summary {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	padding: 2rem;
	border-radius: 12px;
	box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.streak-number {
	display: flex;
	align-items: baseline;
	justify-content: center;
	gap: 0.5rem;
	margin-bottom: 0.5rem;
}

.streak-count {
	font-size: 4rem;
	font-weight: bold;
	line-height: 1;
}

.streak-label {
	font-size: 1.5rem;
	opacity: 0.9;
}

.streak-description {
	font-size: 1.1rem;
	opacity: 0.9;
	margin: 0;
}

/* Today's Progress */
.today-progress {
	background: #f8f9fa;
	padding: 1.5rem;
	border-radius: 8px;
	border: 1px solid #e9ecef;
}

.progress-bar {
	width: 100%;
	height: 20px;
	background-color: #e9ecef;
	border-radius: 10px;
	overflow: hidden;
	margin: 1rem 0;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #28a745, #20c997);
	border-radius: 10px;
	transition: width 0.3s ease;
}

.progress-text {
	margin: 0;
	color: #555;
}

.completed {
	color: #28a745;
	font-weight: bold;
	margin-left: 0.5rem;
}

/* Calendar */
.calendar-section {
	background: #f8f9fa;
	padding: 1.5rem;
	border-radius: 8px;
	border: 1px solid #e9ecef;
}

.calendar-grid {
	display: grid;
	grid-template-columns: repeat(7, 1fr);
	gap: 4px;
	max-width: 400px;
	margin: 0 auto;
}

.calendar-day {
	aspect-ratio: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	border-radius: 4px;
	cursor: pointer;
	transition: all 0.2s ease;
	position: relative;
	min-height: 40px;
}

.calendar-day.completed {
	background-color: #28a745;
	color: white;
}

.calendar-day.partial {
	background-color: #ffc107;
	color: #333;
}

.calendar-day:not(.completed):not(.partial) {
	background-color: #e9ecef;
	color: #6c757d;
}

.calendar-day.today {
	box-shadow: 0 0 0 2px #007bff;
}

.calendar-day:hover {
	transform: scale(1.1);
}

.day-number {
	font-size: 0.9rem;
	font-weight: bold;
}

.day-month {
	font-size: 0.6rem;
	position: absolute;
	top: 2px;
	left: 2px;
	opacity: 0.8;
}

/* Legend */
.legend {
	display: flex;
	justify-content: center;
	gap: 2rem;
	flex-wrap: wrap;
}

.legend-item {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	font-size: 0.9rem;
	color: #666;
}

.legend-dot {
	width: 16px;
	height: 16px;
	border-radius: 2px;
}

.legend-dot.completed {
	background-color: #28a745;
}

.legend-dot.partial {
	background-color: #ffc107;
}

.legend-dot.empty {
	background-color: #e9ecef;
}

.loading {
	padding: 2rem;
	color: #666;
}

@media (max-width: 600px) {
	.streak-page {
		padding: 1rem;
	}
	
	.streak-count {
		font-size: 3rem;
	}
	
	.legend {
		flex-direction: column;
		align-items: center;
		gap: 1rem;
	}
	
	.calendar-grid {
		gap: 2px;
	}
}
</style>