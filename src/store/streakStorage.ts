import { getTodayString, localDateToString } from '../util/localDate';

export interface StreakData {
	currentStreak: number,
	checkedDays: string[], // Array of date strings in YYYY-MM-DD format for past 28 days
	dailyLessonsCompleted: Record<string, number>, // Track completed lessons per day
	lastUpdated: number, // Timestamp of last update
}

const STREAK_STORAGE_KEY = 'higgins2-streak';
const REQUIRED_DAILY_LESSONS = 1;
const STREAK_HISTORY_DAYS = 28;

export class StreakStorage {
	static save(data: StreakData): void {
		try {
			localStorage.setItem(STREAK_STORAGE_KEY, JSON.stringify(data));
		} catch (error) {
			console.warn('Failed to save streak data to localStorage:', error);
		}
	}

	static load(): StreakData | null {
		try {
			const stored = localStorage.getItem(STREAK_STORAGE_KEY);
			if (!stored) return null;
			
			const data = JSON.parse(stored);
			
			// Validate the structure
			if (typeof data.currentStreak !== 'number' ||
				!Array.isArray(data.checkedDays) ||
				typeof data.dailyLessonsCompleted !== 'object' ||
				typeof data.lastUpdated !== 'number') {
				console.warn('Invalid streak data structure, clearing');
				StreakStorage.clear();
				return null;
			}
			
			return data;
		} catch {
			console.warn('Failed to load streak data from localStorage');
			StreakStorage.clear();
			return null;
		}
	}

	static clear(): void {
		try {
			localStorage.removeItem(STREAK_STORAGE_KEY);
		} catch (error) {
			console.warn('Failed to clear streak data from localStorage:', error);
		}
	}

	static getDefaultData(): StreakData {
		return {
			currentStreak: 0,
			checkedDays: [],
			dailyLessonsCompleted: {},
			lastUpdated: Date.now()
		};
	}

	/**
	 * Get array of the past N days in YYYY-MM-DD format
	 */
	static getPastDays(days: number): string[] {
		const result: string[] = [];
		const today = new Date();
		
		for (let i = 0; i < days; i++) {
			const date = new Date(today);
			date.setDate(today.getDate() - i);
			result.push(localDateToString(date));
		}
		
		return result.reverse(); // Return oldest to newest
	}

	/**
	 * Clean up old data beyond the tracking period
	 */
	static cleanupOldData(data: StreakData): StreakData {
		const cutoffDays = StreakStorage.getPastDays(STREAK_HISTORY_DAYS);
		const cutoffSet = new Set(cutoffDays);
		
		// Filter checked days to only include recent days
		const filteredCheckedDays = data.checkedDays.filter(day => cutoffSet.has(day));
		
		// Filter daily completed lessons to only include recent days
		const filteredDailyLessons: Record<string, number> = {};
		for (const day of cutoffDays) {
			if (data.dailyLessonsCompleted[day] !== undefined) {
				filteredDailyLessons[day] = data.dailyLessonsCompleted[day];
			}
		}
		
		return {
			...data,
			checkedDays: filteredCheckedDays,
			dailyLessonsCompleted: filteredDailyLessons
		};
	}

	/**
	 * Add completed lesson for today and check if day should be marked as complete
	 */
	static addCompletedLesson(data: StreakData): StreakData {
		const today = getTodayString();
		const currentCount = data.dailyLessonsCompleted[today] || 0;
		const newCount = currentCount + 1;
		
		const updatedData = {
			...data,
			dailyLessonsCompleted: {
				...data.dailyLessonsCompleted,
				[today]: newCount
			},
			lastUpdated: Date.now()
		};

		// Check if today should be marked as complete
		if (newCount >= REQUIRED_DAILY_LESSONS && !data.checkedDays.includes(today)) {
			updatedData.checkedDays = [...data.checkedDays, today];
			updatedData.currentStreak = StreakStorage.calculateStreak(updatedData.checkedDays);
		}

		return StreakStorage.cleanupOldData(updatedData);
	}

	/**
	 * Calculate current streak from checked days array
	 */
	private static calculateStreak(checkedDays: string[]): number {
		if (checkedDays.length === 0) return 0;
		
		// Sort days in descending order (newest first)
		const sortedDays = [...checkedDays].sort().reverse();
		const today = getTodayString();
		
		let streak = 0;
		let currentDay = new Date(today);
		
		const todayStr = today;
		const yesterdayStr = localDateToString(new Date(currentDay.getTime() - 24 * 60 * 60 * 1000));
		
		let startDate: Date;
		if (sortedDays.includes(todayStr)) {
			startDate = new Date(todayStr);
		} else if (sortedDays.includes(yesterdayStr)) {
			startDate = new Date(yesterdayStr);
		} else {
			return 0; // No recent activity
		}
		
		// Count consecutive days backwards from start date
		currentDay = new Date(startDate);
		while (true) {
			const dayStr = localDateToString(currentDay);
			if (sortedDays.includes(dayStr)) {
				streak++;
				currentDay.setDate(currentDay.getDate() - 1);
			} else {
				break;
			}
		}
		
		return streak;
	}

	/**
	 * Get streak info for display
	 */
	static getStreakInfo(data: StreakData): {
		currentStreak: number,
		todayProgress: number,
		todayComplete: boolean,
		past28Days: Array<{
			date: string,
			isChecked: boolean,
			lessonsCompleted: number,
		}>,
	} {
		const today = getTodayString();
		const todayLessons = data.dailyLessonsCompleted[today] || 0;
		const past28Days = StreakStorage.getPastDays(STREAK_HISTORY_DAYS);
		
		return {
			currentStreak: data.currentStreak,
			todayProgress: todayLessons,
			todayComplete: data.checkedDays.includes(today),
			past28Days: past28Days.map(date => ({
				date,
				isChecked: data.checkedDays.includes(date),
				lessonsCompleted: data.dailyLessonsCompleted[date] || 0
			}))
		};
	}

	static getRequiredDailyLessons(): number {
		return REQUIRED_DAILY_LESSONS;
	}
}