<script setup lang="ts">
// No props needed - this is just informational content
</script>

<template>
	<div class="how-it-works">
		<h3>How the Learning System Works</h3>
		
		<div class="explanation">
			<p><strong>The Spaced Repetition Method:</strong></p>
			<ul>
				<li><strong>Bin 1:</strong> New words or recently missed words (shown as multiple choice)</li>
				<li><strong>Bin 2:</strong> Words you're beginning to learn (shown as multiple choice)</li>
				<li><strong>Bin 3:</strong> Words you know well (shown as text input)</li>
				<li><strong>Bin 4:</strong> Well-mastered words (text input, asked less frequently)</li>
			</ul>
			
			<div class="flow-explanation">
				<p><strong>Word Movement:</strong></p>
				<ul>
					<li>✅ <strong>Correct answers:</strong> Words move up to higher bins</li>
					<li>❌ <strong>Incorrect answers:</strong> Words move back to Bin 1</li>
					<li>🔄 <strong>Review timing:</strong> Higher bins are reviewed less frequently</li>
				</ul>
			</div>
			
			<div class="benefits">
				<p><strong>Benefits:</strong></p>
				<ul>
					<li>Focus more time on difficult words</li>
					<li>Reinforce learning at optimal intervals</li>
					<li>Gradually transition from recognition to recall</li>
					<li>Maximize learning efficiency</li>
				</ul>
			</div>
		</div>
	</div>
</template>

<style scoped>
.how-it-works {
	background: white;
	border: 1px solid #ddd;
	border-radius: 8px;
	padding: 20px;
	margin-top: 15px;
	animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(-10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.how-it-works h3 {
	margin: 0 0 15px 0;
	color: #333;
	border-bottom: 2px solid #007bff;
	padding-bottom: 8px;
}

.explanation {
	font-size: 14px;
	color: #555;
	line-height: 1.5;
}

.explanation p {
	margin: 12px 0 8px 0;
	color: #333;
}

.explanation ul {
	margin: 8px 0 16px 0;
	padding-left: 20px;
}

.explanation li {
	margin: 6px 0;
}

.flow-explanation, .benefits {
	margin-top: 20px;
	padding: 15px;
	background: #f8f9fa;
	border-left: 4px solid #007bff;
	border-radius: 0 6px 6px 0;
}

.benefits {
	border-left-color: #28a745;
}

.flow-explanation p, .benefits p {
	margin-top: 0;
	font-weight: 600;
	color: #333;
}
</style>