/**
 * Get today's date in YYYY-MM-DD format. (Local time zone)
 */
export function getTodayString(): string {
	const localDate = new Date();
	return localDateToString(localDate);
}

/**
 * Convert a local Date object to a string in YYYY-MM-DD format.
 */
export function localDateToString(localDate: Date): string {
	// Do not use toISOString() as it converts to UTC and we want the _local_ date
	const [ year, month, day ] = [ localDate.getFullYear(), localDate.getMonth() + 1, localDate.getDate() ];
	return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}
