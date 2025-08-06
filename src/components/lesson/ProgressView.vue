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
	background-color: #f9f9f9;
}

.stats {
	display: flex;
	gap: 20px;
	margin-bottom: 20px;
}

.label {
	font-weight: bold;
	color: #666;
	font-size: 14px;
}

.value {
	font-size: 16px;
	color: #333;
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
	background: white;
	border: 1px solid #ddd;
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
	color: #555;
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
	background: #ccc;
}

.bin-bar {
	height: 6px;
	background: #e9ecef;
	border-radius: 3px;
	overflow: hidden;
}

.bin-fill {
	height: 100%;
	background: linear-gradient(90deg, #007bff, #0056b3);
	transition: width 0.3s ease;
}

.bin.empty .bin-fill {
	background: #ccc;
}

.help-section {
	margin-top: 20px;
}

.help-button {
	padding: 8px 16px;
	background: #f8f9fa;
	border: 2px solid #dee2e6;
	border-radius: 6px;
	color: #495057;
	font-size: 14px;
	font-weight: 500;
	cursor: pointer;
	transition: all 0.2s ease;
	display: flex;
	align-items: center;
	gap: 6px;
}

.help-button:hover {
	background: #e9ecef;
	border-color: #adb5bd;
	color: #343a40;
}

.help-button.active {
	background: #007bff;
	border-color: #007bff;
	color: white;
}

.help-button.active:hover {
	background: #0056b3;
	border-color: #0056b3;
}
</style>
