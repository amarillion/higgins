export interface StreakData {
	currentStreak: number,
	checkedDays: string[], // Array of date strings in YYYY-MM-DD format for past 28 days
	dailyCorrectAnswers: Record<string, number>, // Track correct answers per day
	lastUpdated: number, // Timestamp of last update
}

const STREAK_STORAGE_KEY = 'higgins2-streak';
const REQUIRED_DAILY_CORRECT = 20;
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
				typeof data.dailyCorrectAnswers !== 'object' ||
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
			dailyCorrectAnswers: {},
			lastUpdated: Date.now()
		};
	}

	/**
	 * Get today's date in YYYY-MM-DD format
	 */
	static getTodayString(): string {
		return new Date().toISOString().split('T')[0];
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
			result.push(date.toISOString().split('T')[0]);
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
		
		// Filter daily correct answers to only include recent days
		const filteredDailyCorrect: Record<string, number> = {};
		for (const day of cutoffDays) {
			if (data.dailyCorrectAnswers[day] !== undefined) {
				filteredDailyCorrect[day] = data.dailyCorrectAnswers[day];
			}
		}
		
		return {
			...data,
			checkedDays: filteredCheckedDays,
			dailyCorrectAnswers: filteredDailyCorrect
		};
	}

	/**
	 * Add correct answers for today and check if day should be marked as complete
	 */
	static addCorrectAnswers(data: StreakData, count: number): StreakData {
		const today = StreakStorage.getTodayString();
		const currentCount = data.dailyCorrectAnswers[today] || 0;
		const newCount = currentCount + count;
		
		const updatedData = {
			...data,
			dailyCorrectAnswers: {
				...data.dailyCorrectAnswers,
				[today]: newCount
			},
			lastUpdated: Date.now()
		};

		// Check if today should be marked as complete
		if (newCount >= REQUIRED_DAILY_CORRECT && !data.checkedDays.includes(today)) {
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
		const today = StreakStorage.getTodayString();
		
		let streak = 0;
		let currentDay = new Date(today);
		
		// Check if today or yesterday is checked (allow for timezone differences)
		const todayStr = today;
		const yesterdayStr = new Date(currentDay.getTime() - 24 * 60 * 60 * 1000).toISOString().split('T')[0];
		
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
			const dayStr = currentDay.toISOString().split('T')[0];
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
			correctAnswers: number,
		}>,
	} {
		const today = StreakStorage.getTodayString();
		const todayCorrect = data.dailyCorrectAnswers[today] || 0;
		const past28Days = StreakStorage.getPastDays(STREAK_HISTORY_DAYS);
		
		return {
			currentStreak: data.currentStreak,
			todayProgress: todayCorrect,
			todayComplete: data.checkedDays.includes(today),
			past28Days: past28Days.map(date => ({
				date,
				isChecked: data.checkedDays.includes(date),
				correctAnswers: data.dailyCorrectAnswers[date] || 0
			}))
		};
	}

	static getRequiredDailyCorrect(): number {
		return REQUIRED_DAILY_CORRECT;
	}
}