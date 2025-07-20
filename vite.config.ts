/* eslint-disable camelcase */
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { VitePWA } from 'vite-plugin-pwa';

// https://vite.dev/config/
export default defineConfig({
	test: {
		environment: 'jsdom'
	},
	base: './', // Use relative paths in index.html, makes our app relocatable.
	plugins: [
		vue(),
		VitePWA({
			registerType: 'autoUpdate',
			manifest: {
				name: 'Dr. Higgins',
				short_name: 'higgins',

				// URL relative to manifest file
				start_url: './',

				display: 'standalone',
				background_color: '#ffffbb',
				theme_color: '#7744ff',
				icons: [ {
					// relative to manifest file
					src: './logo192.png',
					sizes: '192x192',
					type: 'image/png'
				}, {
					// relative to manifest file
					src: './logo512.png',
					sizes: '512x512',
					type: 'image/png'
				} ]
			}
		})
	]
});
