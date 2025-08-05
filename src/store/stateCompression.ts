import type { QuizSession } from '../model/QuizSession';

/**
 * Compact serialization format for QuizSession state
 *
 * Format: [version, counter, bins, currentWord, sessionCorrectAnswers, binCount[], wordStates[]]
 * WordState format: [bin, howSoon, quizCount, remainingRepetitions, lineNumber, side]
 *
 * Space optimizations:
 * - Array format eliminates JSON key repetition
 * - Small integers for bounded values (bin 0-9, etc.)
 * - Version allows future format changes
 */

const SERIALIZATION_VERSION = 4;

export type CompactQuizState = [
	number, // version
	number, // counter
	number, // bins
	number, // currentWord
	number, // sessionCorrectAnswers
	number[], // binCount
	number[][], // wordStates as [bin, howSoon, quizCount, left, lineNumber, side][]
];

export class StateCompression {
	/**
	 * Serialize QuizSession to compact array format
	 */
	static serialize(session: QuizSession): CompactQuizState {
		const wordStates: number[][] = [];
		
		// Get all word states using the new public method
		const words = session.getWordStates();
		
		for (const wordState of words) {
			const bin = wordState.getBin();
			const howSoon = wordState.getHowSoon();
			const quizCount = wordState.getQuizCount();
			const correctInARow = wordState.getRemainingRepetitions();
			const word = wordState.getWord();
			
			wordStates.push([bin, howSoon, quizCount, correctInARow, word.lineNumber, word.side]);
		}
		
		const binCount: number[] = [];
		for (let i = 0; i < session.getBins(); i++) {
			binCount.push(session.getBinCount(i));
		}
		
		return [
			SERIALIZATION_VERSION,
			session.getCounter(),
			session.getBins(),
			session.getCurrentWordIndex(),
			session.getSessionCorrectAnswers(),
			binCount,
			wordStates
		];
	}
	
	/**
	 * Calculate estimated size in localStorage (rough estimate)
	 */
	static estimateSize(state: CompactQuizState): number {
		// JSON.stringify adds some overhead, but arrays are more compact than objects
		return JSON.stringify(state).length;
	}
	
	/**
	 * Deserialize compact state back to session data
	 * Note: This returns the raw data, QuizSession reconstruction happens elsewhere
	 */
	static deserialize(compactState: CompactQuizState): {
		version: number,
		counter: number,
		bins: number,
		currentWord: number,
		sessionCorrectAnswers: number,
		binCount: number[],
		wordStates: Array<{
			bin: number,
			howSoon: number,
			quizCount: number,
			remainingRepetitions: number,
			lineNumber: number,
			side: number,
		}>,
	} {
		const [version, counter, bins, currentWord, sessionCorrectAnswers, binCount, rawWordStates] = compactState;
		
		if (version !== SERIALIZATION_VERSION) {
			throw new Error(`Unsupported serialization version: ${version}`);
		}
		
		const wordStates = rawWordStates.map(([bin, howSoon, quizCount, remainingRepetitions, lineNumber, side]) => ({
			bin,
			howSoon,
			quizCount,
			remainingRepetitions,
			lineNumber,
			side
		}));
		
		return {
			version,
			counter,
			bins,
			currentWord,
			sessionCorrectAnswers,
			binCount,
			wordStates
		};
	}
	
	/**
	 * Validate that state is within localStorage limits
	 * Most browsers allow 5-10MB per origin, but we should be conservative
	 */
	static validateSize(state: CompactQuizState, maxSizeKB: number = 1024): boolean {
		const sizeBytes = this.estimateSize(state);
		return sizeBytes <= maxSizeKB * 1024;
	}
}