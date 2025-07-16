import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import pluginVue from 'eslint-plugin-vue';
import { defineConfig } from 'eslint/config';
import stylistic from '@stylistic/eslint-plugin';

export default defineConfig([
	{
		files: ['**/*.{js,ts,vue}'],
		plugins: { js, '@stylistic': stylistic }, extends: ['js/recommended'],
		languageOptions: { globals: globals.browser },
		rules: {
			'eqeqeq': [ 'error', 'always' ],
			'camelcase': [ 'error' ],
			'no-shadow': [ 'error' ],

			// Base ESLint rules
			'no-console': 'off',
			'no-unused-vars': 'off', // Handled by TypeScript ESLint

			// TypeScript specific rules
			'@typescript-eslint/no-unused-vars': ['warn', {
				argsIgnorePattern: '^_',
				varsIgnorePattern: '^_'
			}],
			'@typescript-eslint/no-explicit-any': 'warn',
			'@typescript-eslint/explicit-function-return-type': 'off',
			'@typescript-eslint/no-non-null-assertion': 'off',
			'@typescript-eslint/no-empty-function': 'off',

			// Stylistic rules
			'@stylistic/indent': ['error', 'tab'],
			'@stylistic/no-mixed-spaces-and-tabs': ['error', 'smart-tabs'],
			'@stylistic/quotes': ['error', 'single', { 'avoidEscape': true }],
			'@stylistic/semi': ['error', 'always'],
			'@stylistic/comma-dangle': ['error', 'never'],

			'@stylistic/no-trailing-spaces': [ 'error', {
				skipBlankLines: true
			} ],

			'@stylistic/member-delimiter-style': [ 'error', {
				multiline: {
					delimiter: 'comma',
					requireLast: true
				},

				singleline: {
					delimiter: 'comma'
				}
			} ],

			'@stylistic/padded-blocks': 'off',
			'@stylistic/max-statements-per-line': 'off'
		}
	},
	tseslint.configs.recommended,
	pluginVue.configs['flat/essential'],
	{ files: ['**/*.vue'], languageOptions: { parserOptions: { parser: tseslint.parser } } }
]);
