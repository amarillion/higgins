<template>
	<div class="streak-page">
		<div v-if="streakInfo" class="streak-content">
			<h1>Current Streak</h1>

			<!-- Current Streak Display -->
			<div class="streak-summary">
				<div class="streak-number">
					<span class="streak-count">{{ streakInfo.currentStreak }}</span>
					<span class="streak-label">day{{ streakInfo.currentStreak !== 1 ? 's' : '' }}</span>
				</div>
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
							'today': isToday(day.date)
						}"
						:title="getDayTooltip(day)"
					>
						<div class="day-number">{{ getDayNumber(day.date) }}</div>
						<div class="day-month" v-if="isFirstOfMonth(day.date)">{{ getMonthName(day.date) }}</div>
					</div>
				</div>
			</div>

		</div>

		<div v-else class="loading">
			<p>Loading streak data...</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { store } from '../../store';
import { getTodayString } from '../../util/localDate';

const streakInfo = ref(store.getStreakInfo());

function isToday(dateStr: string): boolean {
	const today = getTodayString();
	return dateStr === today;
}

function getDayNumber(dateStr: string): number {
	// avoid converting to Date() because then timezones get involved again.
	return parseInt(dateStr.split('-')[2]);
}

function isFirstOfMonth(dateStr: string): boolean {
	return new Date(dateStr).getDate() === 1;
}

function getMonthName(dateStr: string): string {
	return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' });
}

function getDayTooltip(day: { date: string, isChecked: boolean, lessonsCompleted: number }): string {
	if (day.isChecked) {
		return `${day.date}: Checked! (${day.lessonsCompleted} lessons completed)`;
	} else {
		return `${day.date}: No activity`;
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
	max-width: 800px;
	margin: 0 auto;
	text-align: center;
}

h1 {
	color: var(--text-color-soft);
	font-size: 2rem;
}

h3 {
	color: var(--text-color-subtle);
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
	margin: 0 2rem;
	padding: 2rem;
	border: 1px solid #eee;
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

.completed {
	color: #28a745;
	font-weight: bold;
}

/* Calendar */
.calendar-section {
	background: var(--background-soft);
	padding: 1.5rem;
	border-radius: 8px;
	border: 1px solid var(--background-border);
}

.calendar-grid {
	display: grid;
	grid-template-columns: repeat(7, 1fr);
	max-width: 400px;
	margin: 0 auto;
	border-right: 1px dashed var(--background-border);
	border-bottom: 1px dashed var(--background-border);
}

.calendar-day {
	aspect-ratio: 1; /* makes them square */
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	position: relative;
	min-height: 40px;
	border-left: 1px dashed var(--background-border);
	border-top: 1px dashed var(--background-border);
}

.calendar-day.completed {
	background-color: #28a745;
	color: white;
}

.calendar-day:not(.completed) {
	background-color:var(--background-secondary);
	color: var(--text-color-soft);
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

.loading {
	padding: 2rem;
	color: #666;
}

@media (max-width: 800px) {

	.streak-count {
		font-size: 3rem;
	}

	.calendar-section {
		border-radius: 0;
	}
}

</style>