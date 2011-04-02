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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class CourseModel extends AbstractTableModel implements Serializable
{
	List<File> files = new ArrayList<File>(); // ordered list of lessons
	Map<File, List<WordData>> wordData = new HashMap<File, List<WordData>>(); // word data organized per lesson
	
	//TODO: maybe better done as iterator "view" over wordData map
	List<WordData> allWordData = new ArrayList<WordData>(); // all word data
	
	File courseFile;

	int lessonSize; // number of words per time
	float pctErrors; // percentage of lesson that should consist of repeating previous errors
	float pctRepetition; // percentage of lesson that should consist of repeating old words 
	// pctErrors + pctRepetition < 1.0
	
	private static class WordData
	{
		public WordData(Word w)
		{
			this.w = w;
			askedTimes = 0;
			lastAsked = null;
			errorRate = 0;
		}
		
		Word w;
		int askedTimes; // number of times asked
		Date lastAsked; // last asked date

		/** 
		 * weighted running average of errorRate. 
		 * Each correct answer: errorRate = 0.9 * errorRate + 0.1; 
		 * Each wrong answer: errorRate = 0.9 * errorRate;
		 * This means effect of older answers diminishes over time.
		 */
		float errorRate;
	}
	
	/**
	 * Add a file to the course. Read words from the file
	 * @param f file to add
	 */
	public void addFile(File f) throws IOException
	{
		if (wordData.containsKey(f)) return; // ignore, already added.
		Quiz q = Quiz.loadFromFile(f);
		
		files.add(f);
		List<WordData> list = new ArrayList<WordData>();
		wordData.put (f, list);
		for (Word w : q.getWords())
		{
			WordData wd = new WordData(w);
			list.add(wd);
			allWordData.add(wd);
		}
		
		fireTableDataChanged();
	}
	
	/**
	 * Remove a file from the course. Associated word data will be removed as well. 
	 * @param f file to remove
	 */
	public void removeFile(File f)
	{
		if (!wordData.containsKey(f)) return; // ignore, wasn't in there
		
		files.remove(f);
		for (WordData wd : wordData.get(f))
		{
			//TODO: linear lookup suboptimal
			allWordData.remove(wd);
		}
		wordData.remove(f);
		
		fireTableDataChanged();
	}
	
	/**
	 * Re-read quiz data from file.
	 * Use this in case a File was modified on disk since the course was created.
	 * @param f file to refresh
	 */
	public void refreshFile(File f)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Serialize course data to disk, using same file that 
	 * it was loaded from or saved to before.
	 */
	public void saveCourse()
	{
	}
	
	/**
	 * Serialize course data to disk, to a new file.
	 * @param f file to save to.
	 */
	public void saveCourseAs(File f)
	{
	}
	
	/**
	 * load course from file
	 * @param f file to load from
	 */
	public void loadCourse(File f)
	{
	}
	
	private List<Word> getErrorList()
	{
		List<Word> result = new ArrayList<Word>();
		for (WordData i : allWordData)
		{
			if (i.errorRate > 0.3) result.add(i.w);
		}
		return result;
	}
	
	/** returns list of words that have been asked before, oldest first */
	private List<Word> getRepeatList()
	{
		List<WordData> sortedList = new ArrayList<WordData>();
		for (WordData d : allWordData)
		{
			if (d.lastAsked != null)
				sortedList.add(d);
		}
		Collections.sort (sortedList, new Comparator<WordData> ()
				{
					@Override
					public int compare(WordData arg0, WordData arg1) 
					{
						if (arg0.lastAsked == null || arg1.lastAsked == null) throw new NullPointerException();
						return arg0.lastAsked.compareTo(arg1.lastAsked);
					}
				});
		List<Word> result = new ArrayList<Word>();
		for (WordData i : sortedList)
		{
			result.add (i.w);
		}
		return result;
	}
	
	/**
	 * Create a new lesson based on course parameters.
	 * <p>
	 * The new course will consist of
	 * <ul>
	 * <li> (1.0-pctRepeat-pctError)%: words that haven't been asked yet, starting from first File in list.
	 * <li> (pctError)%: pick words that have been asked. Start from words with high error rate
	 * <li> (pctRepeat)%: pick words that have been asked. Start from words with lastAsked long ago.
	 * </ul>	
	 */
	public Quiz createNewLesson()
	{
		List<Word> result = new ArrayList<Word>();
		
		int cErrors = (int)(lessonSize * pctErrors);
		
		List<Word> errorList = getErrorList();
		if (errorList.size() < cErrors) cErrors = errorList.size();
		
		int cRepeats = (int)(lessonSize * pctRepetition);
		List<Word> repeatList = getRepeatList();
		if (repeatList.size() < cRepeats) cRepeats = repeatList.size();
		
		int cRemain = lessonSize - cErrors - cRepeats;
		List<Word> newList = getNewList();
		if (newList.size() < cRemain) { /* TODO */ }
		
		result.addAll(errorList.subList(0, cErrors));
		result.addAll(repeatList.subList(0, cRepeats));
		result.addAll(newList.subList(0, cRemain));
		
		Quiz quiz = new Quiz(result);
		return quiz;
	}

	private List<Word> getNewList() 
	{
		List<Word> result = new ArrayList<Word>();
		for (WordData wd : allWordData)
		{
			if (wd.askedTimes == 0) result.add (wd.w);
		}
		return result;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public int getRowCount() {
		return files.size();
	}

	private int getLessonSize(File f)
	{
		return wordData.get(f).size();
	}

	private float getErrorRate(File f)
	{
		float sum = 0;
		int n = 0;
		
		for (WordData i : wordData.get(f))
		{
			sum += i.errorRate;
			n++;
		}
		
		return (n == 0 ? 0 : sum / n);
	}
	
	private float getAvgAsked(File f)
	{
		int sum = 0;
		int n = 0;

		for (WordData i : wordData.get(f))
		{
			sum += i.askedTimes;
			n++;
		}
		
		return (n == 0 ? 0 : (float)(sum / n));
	}
	
	@Override
	public Object getValueAt(int row, int col) 
	{
		switch (col)
		{
		case 0: // lesson name
			return files.get(row).getName();
		case 1: // lesson size
			return getLessonSize(files.get(row));
		case 2: // average times asked
			return getAvgAsked(files.get(row));
		case 3: // average error rate
			return getErrorRate(files.get(row));
		default:
			throw new IndexOutOfBoundsException("Column " + col + " is out of range");
		}
	}

	public File getLessonByIndex(int row) 
	{
		return files.get(row);
	}
	
}
