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
	define: {
		__VERSION__: JSON.stringify(process.env.npm_package_version || '0.0.0'),
		__BUILD_DATE__: JSON.stringify(new Date().toDateString()),
	},
	plugins: [
		vue(),
		VitePWA({
			registerType: 'autoUpdate',
			// cache all the imports
			workbox: {
				globPatterns: ['**/*']
			},
			// cache all the static assets in the public folder
			includeAssets: [
				'**/*'
			],
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
