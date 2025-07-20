import { mount } from '@vue/test-utils';
import HelloWorld from '../src/components/HelloWorld.vue';
import { describe, expect, test } from 'vitest';

describe('HelloWorld Component Test', () => {
	test('renders with correct message', () => {
		const wrapper = mount(HelloWorld, { props: { msg: 'Test Message'} });
		expect(wrapper.text()).toContain('Test Message');
	});
});
