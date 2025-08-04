import { describe, it, expect } from 'vitest';
import { hashString, hashLessonContent } from '../src/model/hash';

describe('hashString', () => {
	it('should return consistent hash for same input', () => {
		const input = 'hello world';
		const hash1 = hashString(input);
		const hash2 = hashString(input);
		
		expect(hash1).toBe(hash2);
		expect(hash1).toBeTypeOf('string');
		expect(hash1.length).toBeGreaterThan(0);
	});

	it('should return different hashes for different inputs', () => {
		const hash1 = hashString('hello');
		const hash2 = hashString('world');
		
		expect(hash1).not.toBe(hash2);
	});

	it('should handle empty string', () => {
		const hash = hashString('');
		expect(hash).toBeTypeOf('string');
		expect(hash.length).toBeGreaterThan(0);
	});

	it('should handle special characters', () => {
		const hash = hashString('café, naïve, résumé');
		expect(hash).toBeTypeOf('string');
		expect(hash.length).toBeGreaterThan(0);
	});

	it('should return hexadecimal string', () => {
		const hash = hashString('test');
		expect(hash).toMatch(/^[0-9a-f]+$/);
	});
});

describe('hashLessonContent', () => {
	it('should hash lesson content consistently', () => {
		const content = `hello, hola
goodbye, adiós
cat, gato`;
		
		const hash1 = hashLessonContent(content);
		const hash2 = hashLessonContent(content);
		
		expect(hash1).toBe(hash2);
	});

	it('should ignore whitespace differences', () => {
		const content1 = `hello, hola
goodbye, adiós
cat, gato`;
		
		const content2 = `  hello, hola  
		goodbye, adiós
cat, gato   `;
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).toBe(hash2);
	});

	it('should ignore empty lines', () => {
		const content1 = `hello, hola
goodbye, adiós
cat, gato`;
		
		const content2 = `hello, hola

goodbye, adiós

cat, gato

`;
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).toBe(hash2);
	});

	it('should include comment lines (preserving line numbers)', () => {
		const content1 = `hello, hola
goodbye, adiós
cat, gato`;
		
		const content2 = `# This is a comment
hello, hola
# Another comment
goodbye, adiós
#question1=What is
cat, gato`;
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).not.toBe(hash2); // Should be different because comments affect line numbers
	});

	it('should be order-dependent (preserving line order)', () => {
		const content1 = `hello, hola
goodbye, adiós
cat, gato`;
		
		const content2 = `cat, gato
hello, hola
goodbye, adiós`;
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).not.toBe(hash2); // Should be different because order affects line numbers
	});

	it('should detect content changes', () => {
		const content1 = `hello, hola
goodbye, adiós
cat, gato`;
		
		const content2 = `hello, hola
goodbye, adiós
dog, perro`;
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).not.toBe(hash2);
	});

	it('should handle empty content', () => {
		const hash = hashLessonContent('');
		expect(hash).toBeTypeOf('string');
		expect(hash.length).toBeGreaterThan(0);
	});

	it('should handle content with only comments', () => {
		const content = `# This is a comment
#question1=What is
# Another comment`;
		
		const hash = hashLessonContent(content);
		expect(hash).toBeTypeOf('string');
		expect(hash.length).toBeGreaterThan(0);
	});

	it('should treat different spacing in content lines as different', () => {
		const content1 = 'hello, hola';
		const content2 = 'hello,hola';
		
		const hash1 = hashLessonContent(content1);
		const hash2 = hashLessonContent(content2);
		
		expect(hash1).not.toBe(hash2);
	});
});