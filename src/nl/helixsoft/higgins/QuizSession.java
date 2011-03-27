//    This file is part of Dr. Higgins.
//    Copyright 2003-2011 Martijn van Iersel <amarillion@yahoo.com>
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * represents a single lesson, a series of q, a pairs read from a text file.
 */
public class QuizSession implements Serializable 
{
	private Quiz quiz;
	int bins = 4;
	
	private void loadLesson (File newFileName) throws IOException
	{
		quiz = Quiz.loadFromFile(newFileName);
		
		reset();
		
		binCount[0] = quiz.getWordCount();
		Collections.shuffle(quiz.getWords());
		currentWord = 0;
	}
	
	public boolean isFinished()
	{
	    // check if all bins before last are empty
	    boolean result = true;
	    for (int i = 0; i < getBins() - 1; ++i)
		{
			if (getBinCount(i) != 0) result = false;
		}
	    return result;
	}
	
	/**
	 * Find a suitable question to be asked next, according to the following algorithm:
	 * <p>
	 * go through list    
	 * 1. look for word that has highest due. If due > 3, pick it. If due > 2, pick it with 30% chance
	 * 2. if none found yet, pick two words that are not in last bin. Return the one in lowest bin.
	 */
	public void nextQuestion()
	{
		Collections.shuffle (quiz.getWords());

		int i, j;
		currentWord = -1;
		int maxDue = -1;
		for (i = 0; i < quiz.getWords().size(); ++i)
		{
			int due = counter - quiz.getWords().get(i).getHowSoon(); 
			if (quiz.getWords().get(i).getHowSoon() != -1 && 
					due > maxDue)
			{
				maxDue = due;
				currentWord = i;
			}
		}

		int dueWait = 3;
		Random random = new Random();
		if (random.nextInt(100) < 40) dueWait = 2;
	    // if no currentWord found, or there is no word with due higher than 3,
	    // or 30% chance if there is no word higher than 2

		if (currentWord == -1 || maxDue < dueWait)
		{
			i = 0;
			while (i < quiz.getWords().size() && 
					(quiz.getWords().get(i).getBin() >= bins -1 ||
					quiz.getWords().get(i).getHowSoon() != -1)) { i++; }
			j = i;
			if (i < quiz.getWords().size())
			{
				while (j < quiz.getWords().size() && 
						(quiz.getWords().get(j).getBin() >= bins -1 ||
						quiz.getWords().get(j).getHowSoon() != -1)) { j++; }
			}
			if (i < quiz.getWords().size() && j < quiz.getWords().size())
			{
				currentWord = quiz.getWords().get(i).getBin() > quiz.getWords().get(j).getBin() ? j : i;
			}
		}
	}
	
	public void reset()
	{
		totalDuration = 0;
		sessions = 0;
		counter = 1;
		for (int i = 0; i < Word.MAXBINS; ++i) binCount[i] = 0;
		currentWord = -1;
	}
	
	/**
	 * compare given answer to the correct answer.
	 * Returns true if they match.
	 * As a side effect, increases counter and may set hint
	 * so call this only once per q/a.
	 */
	public boolean compareAnswer (String anAnswer)
	{
		boolean result = quiz.getWord(currentWord).compareAnswer(anAnswer, counter, binCount);
		hint = null;
		if (!result)
		{
			String otherWord = quiz.getByAnswer(anAnswer);
			if (otherWord != null)
			{
				hint =  MainFrame.res.getString("YOU_MAY_BE_CONFUSED_WITH") + 
					" \"" +	anAnswer + "\" -> \"" + otherWord + "\"";
			}
		}
		counter++;
		return result;
	}
	
	public String getCorrectAnswer()
	{
		return quiz.getWord(currentWord).getAnswer();
	}
	
	/**
	 * get the current question, 
	 * composed of the question template with the
	 * current word filled in 
	 */
	public String getQuestion()
	{
		assert (currentWord >= 0 && currentWord < quiz.getWords().size());
		String w = quiz.getWord(currentWord).getQuestion();
		String q = quiz.getWord(currentWord).getDir() == 0 ? quiz.getQuestion2() : quiz.getQuestion1();
		int pos = q.indexOf("\"\"");
		if (pos >= 0)
		{
			return q.substring (0, pos + 1) + w + q.substring (pos + 1);
		}
		else
		{
			return q + " " + w;
		}
	}
	
	public boolean hasHint()
	{
		return hint != null;
	}
	
	public String getHint() { return hint; }
	
	public List<Word> getMostDifficult(int amount)
	{
		List<Word> result = new ArrayList<Word>();
		result.addAll(quiz.getWords());
		Collections.sort (result, new Comparator<Word>()
				{
					public int compare(Word o1, Word o2) 
					{
						return o2.getQuizCount() - o1.getQuizCount();
					}
				});
		if (result.size() <= amount)
			return result;
		else
			return result.subList(0, amount);
	}
	
	public int getCounter() { return counter; }
	
	public int getBins() { return bins; }
	
	public int getBinCount(int b) { return binCount[b]; }
	
	public void setBins(int newCount)
	{
		bins = newCount;
	}
		
	public QuizSession(File f) throws IOException
	{
		loadLesson (f);
	}
	
	public Quiz getQuiz() { return quiz; }
	
	
	private int totalDuration;
	private int sessions;
	private int counter;
	private String hint = null;
	
	// TODO Word?
	private int currentWord; 
	
	private int binCount[] = new int[Word.MAXBINS];
}
