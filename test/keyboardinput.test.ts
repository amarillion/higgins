import { mount } from '@vue/test-utils';
import KeyboardInput from '../src/components/lesson/KeyboardInput.vue';
import { describe, expect, test } from 'vitest';

describe('KeyboardInput Component Test', () => {
	test('renders correctly', () => {
		const wrapper = mount(KeyboardInput, { props: { question: 'What is "Hello" in Dutch?', lang: 'nl' } });
		expect(wrapper.text()).toContain('What is "Hello" in Dutch?');
	});
});
