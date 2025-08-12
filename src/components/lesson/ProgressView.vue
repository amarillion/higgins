<script setup lang="ts">
interface BinData {
	bin: number,
	count: number,
}

interface ProgressData {
	bins: BinData[],
	totalWords: number,
	counter: number,
	isFinished: boolean,
}

const props = defineProps<{
	progress: ProgressData,
}>();

const getBinWidth = (count: number) => {
	const maxCount = props.progress.totalWords;
	return maxCount > 0 ? (count / maxCount) * 100 : 0;
};

</script>
<template>
	<div class="progress-view">
		<h3>Progress</h3>
		<div class="stats">
			<div class="stat">
				<span class="label">Questions Asked: </span>
				<span class="value">{{ progress.counter }}</span>
			</div>
		</div>
		
		<div class="bins">
			<div class="bins-container">
				<div
					v-for="binData in progress.bins"
					:key="binData.bin"
					class="bin"
				>
					<div class="bin-header">
						<span class="bin-number">Bin {{ binData.bin }}</span>
						<span class="bin-count">{{ binData.count }}</span>
					</div>
					<div class="bin-bar">
						<div
							class="bin-fill"
							:style="{ width: getBinWidth(binData.count) + '%' }"
						></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<style scoped>
.progress-view {
	margin: 20px 0;
	padding: 20px;
	border: 1px solid #ddd;
	border-radius: 8px;
	background-color: var(--background-soft);
}

.stats {
	display: flex;
	gap: 20px;
	margin-bottom: 20px;
}

.label {
	font-weight: bold;
	color: var(--text-color-subtle);
	font-size: 14px;
}

.value {
	font-size: 16px;
	color: var(--text-color-soft);
}

.value.finished {
	color: #28a745;
	font-weight: bold;
}

.bins h3 {
	margin: 0 0 10px 0;
	color: #333;
}

h3 {
	margin: 0 0 1rem 0;
}

.bins-container {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
	gap: 10px;
}

.bin {
	border: 1px solid var(--background-secondary);
	border-radius: 6px;
	padding: 10px;
	transition: all 0.2s ease;
}

.bin-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8px;
}

.bin-number {
	font-weight: bold;
	color: var(--text-color-soft);
}

.bin-count {
	background: #007bff;
	color: white;
	padding: 2px 8px;
	border-radius: 12px;
	font-size: 12px;
	font-weight: bold;
}

.bin.empty .bin-count {
	background: var(--background-secondary);
}

.bin-bar {
	height: 6px;
	background: var(--background-soft);
	border-radius: 3px;
	overflow: hidden;
}

.bin-fill {
	height: 100%;
	background: linear-gradient(90deg, #007bff, #0056b3);
	transition: width 0.3s ease;
}

.bin.empty .bin-fill {
	background: var(--background-secondary);
}

</style>
