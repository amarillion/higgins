import { describe, it, expect } from 'vitest';
import { compareMagically } from '../src/model/WordState';

describe('compareMagically', () => {
	
	it('should return true for exact matches', () => {
		const result = compareMagically('hello', 'hello');
		expect(result).toBe(true);
	});

	it('should return true for case-insensitive matches', () => {
		const result = compareMagically('Hello', 'hello');
		expect(result).toBe(true);
	});

	it('should return true for whitespace mismatches', () => {
		const result = compareMagically(' hello ', 'hello');
		expect(result).toBe(true);
	});

	it('should return true for swapped options with slashes', () => {
		const result = compareMagically('option1 / option2', 'option2 / option1');
		expect(result).toBe(true);
	});

});