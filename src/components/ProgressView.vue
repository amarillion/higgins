<script setup lang="ts">
interface BinData {
	bin: number,
	count: number,
}

interface ProgressData {
	bins: BinData[],
	counter: number,
	isFinished: boolean,
}

const props = defineProps<{
	progress: ProgressData,
}>();

const getBinWidth = (count: number) => {
	const maxCount = Math.max(...props.progress.bins.map(b => b.count));
	return maxCount > 0 ? (count / maxCount) * 100 : 0;
};
</script>
<template>
	<div class="progress-view">
		<h2>Progress</h2>
		
		<div class="stats">
			<div class="stat">
				<span class="label">Questions Asked:</span>
				<span class="value">{{ progress.counter }}</span>
			</div>
			<div class="stat">
				<span class="label">Status:</span>
				<span class="value" :class="{ 'finished': progress.isFinished }">
					{{ progress.isFinished ? 'Completed!' : 'In Progress' }}
				</span>
			</div>
		</div>
		
		<div class="bins">
			<h3>Learning Bins</h3>
			<div class="bins-container">
				<div
					v-for="binData in progress.bins"
					:key="binData.bin"
					class="bin"
					:class="{ 'empty': binData.count === 0 }"
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
		
		<div class="explanation">
			<p><strong>How it works:</strong></p>
			<ul>
				<li><strong>Bin 1:</strong> New words or recently missed words</li>
				<li><strong>Bin 2-3:</strong> Words you're learning</li>
				<li><strong>Bin 4:</strong> Well-learned words (asked less frequently)</li>
			</ul>
			<p>Words move up when answered correctly and back to Bin 1 when answered incorrectly.</p>
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

.stat {
	display: flex;
	flex-direction: column;
	gap: 4px;
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

.bins-container {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
	gap: 10px;
}

.bin {
	background: white;
	border: 1px solid #ddd;
	border-radius: 6px;
	padding: 10px;
	transition: all 0.2s ease;
}

.bin.empty {
	opacity: 0.6;
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

.explanation {
	margin-top: 20px;
	font-size: 14px;
	color: #666;
}

.explanation ul {
	margin: 10px 0;
	padding-left: 20px;
}

.explanation li {
	margin: 5px 0;
}
</style>
