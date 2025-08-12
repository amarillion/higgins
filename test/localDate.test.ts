import { describe, expect, it } from 'vitest';
import { localDateToString } from '../src/util/localDate';

describe('localDateStr', () => {

	it('Environment should be configured with an explicit time zone', () => {
		// since Date objects work with local time,
		// to make tests indpendent of the test environment,
		// you must set the time zone explicitly.
		expect (process.env.TZ).toBe('America/Mexico_City');
	});

	it('Should return local date, not UTC date', () => {

		const localDate = new Date('2024-10-01T22:00:00'); // in local time zone (Mexico)

		// in UTC, this timestamp is a day ahead.
		expect(localDate.toISOString().split('T')[0]).toBe('2024-10-02');

		// the local date isn't though.
		expect(localDateToString(localDate)).toBe('2024-10-01');
	});

});