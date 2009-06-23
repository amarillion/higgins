//    This file is part of Dr. Higgins.
//    Copyright 2003-2009 Martijn van Iersel <amarillion@yahoo.com>
//
//    Dr. Higgins is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Dr. Higgins is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Dr. Higgins.  If not, see <http://www.gnu.org/licenses/>.
package nl.helixsoft.higgins;

import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a question-answer pair.
 */
public class Word {

	static final int MAXBINS = 10;

	Word (String q, String a, int dir, int pair)
	{
		question = q;
		answer = a;
		this.dir = dir;
		this.pair = pair;
	}
	
	public String getQuestion() { return question; }
	public String getAnswer() { return answer; }
	
	/** 
	 * the direction of this question - if 0,
	 * this question was the part before the comma in the lesson.
	 * if 1, the question was the part after the comma.
	 * e.g. if all pairs are of the form english, spanish,
	 * then 0 means the translation for the english word is asked.
	 */
	public int getDir () { return dir; }
	
	/**
	 * The bin the word is in currently - words progress from bin to bin
	 * as they are answered correctly.
	 */
	public int getBin () { return bin; }
	public int getHowSoon () { return howSoon; }
	public int getQuizCount () { return quizCount; }
	Map<String, Integer> getWrongAnswers () { return wrongAnswers; }
	
	public void serialize (Writer writer) 
	{
		// TODO
	}
	
	public void deserialize (Reader reader) 
	{ 
		// TODO 
	}
	
	public boolean compareAnswer(String anAnswer, int counter, int[] binCount)
	{
		boolean result;
		result = anAnswer.equals(answer);
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
	private String question;
	private String answer;
	Map<String, Integer> wrongAnswers = new HashMap<String, Integer>();
	int dir; /* direction of this question 1->0 or 0->1 */
	int pair; /* position in lesson file */
}
