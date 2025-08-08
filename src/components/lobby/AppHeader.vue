<script setup lang="ts">
</script>
<template>
	<header class="app-header">
		<h1>🎓 Dr. Higgins</h1>
		<p>Learn a Language</p>
	</header>
</template>
<style scoped>
.app-header {
	text-align: center;
	padding: 10px;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

@media (min-width: 800px) {
	.app-header {
		border-radius: 10px;
	}
}

.app-header h1 {
	margin: 0 0 10px 0;
	font-size: 2.5em;
	font-weight: bold;
}

.app-header p {
	margin: 0;
	font-size: 1.2em;
	opacity: 0.9;
}

</style>