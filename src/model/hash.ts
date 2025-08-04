/**
 * Simple hash function for string content
 * Based on djb2 algorithm - fast and reasonably good distribution
 */
export function hashString(str: string): string {
	let hash = 5381;
	for (let i = 0; i < str.length; i++) {
		hash = ((hash << 5) + hash) + str.charCodeAt(i);
		hash = hash & hash; // Convert to 32-bit integer
	}
	return Math.abs(hash).toString(16);
}

/**
 * Calculate hash for lesson content
 * This preserves line order and includes comments since line numbers matter for state restoration
 */
export function hashLessonContent(content: string): string {
	// Normalize content: only trim whitespace but preserve line order and comments
	const normalizedContent = content
		.split('\n')
		.map(line => line.trim())
		.filter(line => line.length > 0) // Only remove empty lines
		.join('\n');
	
	return hashString(normalizedContent);
}