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
 * This normalizes the content by removing whitespace variations
 * and calculates a hash that will change if the lesson content changes
 */
export function hashLessonContent(content: string): string {
	// Normalize content: trim lines, remove empty lines, sort for consistent hashing
	const normalizedContent = content
		.split('\n')
		.map(line => line.trim())
		.filter(line => line.length > 0 && !line.startsWith('#')) // Remove comments for content hash
		.sort() // Sort to handle reordering
		.join('\n');
	
	return hashString(normalizedContent);
}