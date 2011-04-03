package nl.helixsoft.higgins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WordState implements Serializable
{
	private static final long serialVersionUID = 2L;

	static final int MAXBINS = 10;

	private final Word w;
	
	public WordState (Word w)
	{
		this.w = w;
	}
	
	/**
	 * The bin the word is in currently - words progress from bin to bin
	 * as they are answered correctly.
	 */
	public int getBin () { return bin; }
	public int getHowSoon () { return howSoon; }
	public int getQuizCount () { return quizCount; }
	Map<String, Integer> getWrongAnswers () { return wrongAnswers; }
	
	public boolean compareAnswer(String anAnswer, int counter, int[] binCount)
	{
		boolean result;
		result = anAnswer.equals(w.getAnswer());
		if (result)
		{
			quizCount++;
			correctCount++;
			if (--left == 0)
			{
				if (bin < MAXBINS - 1)
				{
					binCount[bin]--;
					bin++;
					binCount[bin]++;
				}
				howSoon = -1;
				left = 1;
			}
			else
				howSoon = counter;
		}
		else
		{
			quizCount++;
			howSoon = counter;
			left = 2;
			
			// keep statistics about wrong answers
			int wrongCount = 1;
			if (wrongAnswers.containsKey(anAnswer))
			{
				wrongCount = wrongAnswers.get(anAnswer) + 1;
			}
			wrongAnswers.put (anAnswer, wrongCount);
		}
		return result;
	}
	
	
	private int left = 1;
	private int bin = 0;
	private int howSoon = -1;
	private int quizCount = 0;
	private int correctCount = 0;
	Map<String, Integer> wrongAnswers = new HashMap<String, Integer>();

	public Word getWord() 
	{
		return w;
	}

}
