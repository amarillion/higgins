package nl.helixsoft.higgins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** State of a Word during a quiz. That means: the box the word is in, 
 * if it has to be repeated because of a wrong answer, 
 * and a list of wrong answers given. 
 */  
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
	
	/**
	 * Compare answer against quiz, taking into account special characters.
	 * An answer containing a single slash means that there are two options that may be swapped.
	 */
	private boolean compareMagically(String a, String b)
	{
		if (a.equals(b)) return true;
		
		// if the answer has slashes in it, it is allowed to change the order
		String[] parts = b.split (" / ");
		if (parts.length == 2)
		{
			if (a.equals (parts[1] + " / " + parts[0]))
				return true;
		}
		
		return false;
	}
	
	public boolean compareAnswer(String anAnswer, int counter, int[] binCount)
	{
		boolean result;
		
		result = compareMagically (anAnswer, w.getAnswer());
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
	private Map<String, Integer> wrongAnswers = new HashMap<String, Integer>();

	public Word getWord() 
	{
		return w;
	}

}
