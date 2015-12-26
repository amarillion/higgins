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

import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/** 
 * Data model for a course program. This class maintains a list of Files that are the individual lessons, 
 * and tracks for each question in each quiz when it was last asked, how often it was asked and how 
 * often it was answered wrong.
 * <p>
 * This class can be used directly as tableModel.
 */
public class CourseModel extends AbstractTableModel implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	List<FileData> fileData = new ArrayList<FileData>();
	
	private static class FileData implements Serializable
	{
		public File file;
		public long timestamp;
	}
	
	Map<File, List<WordHistory>> wordData = new HashMap<File, List<WordHistory>>(); // word data organized per lesson
	
	
	// not serialized...
	transient List<WordHistory> allWordDataView = new ArrayList<WordHistory>(); // all word data
	
	private File courseFile = null;

	int lessonSize = 100; // number of words per time
	float pctErrors = 0.2f; // percentage of lesson that should consist of repeating previous errors
	float pctRepetition = 0.2f; // percentage of lesson that should consist of repeating old words 
	// pctErrors + pctRepetition < 1.0
	
	/** Extra data about a question in one of the quizzes of this course */
	private static class WordHistory implements Serializable
	{
		private static final long serialVersionUID = 3L;
		
		public WordHistory(Word w)
		{
			this.w = w;
			askedTimes = 0;
			lastAsked = null;
			errorRate = 0;
		}
		
		final Word w;
		
		/** number of times asked */
		int askedTimes;
		
		/** moment that this was last asked*/
		Date lastAsked;

		/** 
		 * Weighted running average of errorRate. 
		 * Each correct answer: errorRate = 0.75 * errorRate + 0.25; 
		 * Each wrong answer: errorRate = 0.75 * errorRate;
		 * This means effect of older answers diminishes over time.
		 */
		double errorRate;
	}
	
	/**
	 * Add a file to the course. Read words from the file
	 * @param f file to add
	 */
	public void addFile(File f) throws IOException
	{
		if (wordData.containsKey(f)) return; // ignore, already added.
		Quiz q = Quiz.loadFromFile(f);
		
		FileData fd = new FileData();
		fd.file = f;
		fd.timestamp = f.lastModified();
		fileData.add(fd);
		
		List<WordHistory> list = new ArrayList<WordHistory>();
		wordData.put (f, list);
		for (Word w : q.getWords())
		{
			WordHistory wd = new WordHistory(w);
			list.add(wd);
			allWordDataView.add(wd);
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
		
		removeFileData(f);
		wordData.remove(f);
		refresh();
		
		fireTableDataChanged();
	}

	/**
	 * Remove a given file from the FileData list.
	 */
	private void removeFileData(File f) 
	{
		Iterator<FileData> it = fileData.iterator();
		while (it.hasNext())
		{
			FileData fd = it.next();
			if (fd.file.equals(f))
			{
				it.remove();
			}
		}
	}
	
	/**
	 * Re-read quiz data from file.
	 * Use this in case a File was modified on disk since the course was created.
	 * @param f file to refresh
	 * @throws IOException 
	 */
	public void refreshFile(FileData fd) throws IOException
	{
		File f = fd.file;
		fd.timestamp = f.lastModified();
		
		List<WordHistory> oldWordHistory = wordData.get(f);
		wordData.remove(f);
		
		List<WordHistory> newWordHistory = new ArrayList<WordHistory>();
		wordData.put (f, newWordHistory);
		
		// re-read lesson
		Quiz q = Quiz.loadFromFile(f);
		
		for (Word w : q.getWords())
		{
			WordHistory wh = popWord (oldWordHistory, w);
			if (wh == null)
			{
				wh = new WordHistory(w);
				// this is a new word;
				System.out.println ("Word added: " + w);
			}
			
			newWordHistory.add(wh);
		}
		
		// remaining in oldHwordHistory
		for (WordHistory wh : oldWordHistory)
		{
			System.out.println ("Word removed: " + wh.w);
		}

		refresh();
		fireTableDataChanged();
	}
	
	private static WordHistory popWord (List<WordHistory> oldHistory, Word w)
	{
		WordHistory result = null;
		
		//TODO: linear lookup suboptimal
		Iterator<WordHistory> it = oldHistory.iterator();
		while (it.hasNext())
		{
			WordHistory wh = it.next();
			if (wh.w.getQuestion().equals(w.getQuestion()) &&
				wh.w.getAnswer().equals(w.getAnswer()))
			{
				it.remove();
				result = wh;
				break;
			}
		}
		
		return result;
	}

	/**
	 * Serialize course data to disk, using same file that 
	 * it was loaded from or saved to before.
	 * @throws IOException 
	 */
	public void saveCourse() throws IOException
	{
		if (getCourseFile() == null) throw new IllegalStateException("Must set file first");
		ObjectOutputStream oos = new
		ObjectOutputStream(
				new FileOutputStream(
						getCourseFile()));
		oos.writeObject(this);
		oos.close();
	}
	
	/**
	 * load course from file
	 * @param f file to load from
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static CourseModel loadCourse(File f) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream (f));
		CourseModel newModel = (CourseModel)ois.readObject();
		
		// copy any old files for backwards compatibility
		if (newModel.fileData == null) newModel.fileData = new ArrayList<FileData>();
		for (File i : newModel.files)
		{
			FileData fd = new FileData();
			fd.file = i;
			fd.timestamp = i.lastModified();
			newModel.fileData.add(fd);
		}
		
		// clear deprecated data fields
		newModel.files.clear();
		newModel.allWordData.clear();
		
		// create the word data view
		newModel.refresh();
		
		ois.close();
		
		return newModel;
	}

	public int refreshLessons(Frame frame) throws IOException 
	{
		// check timestamps for out-of-date lessons
		
		//TODO translate...
		String sep = "<html>Course contains some lessons that are out-of-date.<br>The following lessons have been refreshed: ";
		
		StringBuilder msg = new StringBuilder ();
		int refreshCount = 0;
		for (FileData fd : fileData)
		{
			if (fd.file.lastModified() > fd.timestamp)
			{
				refreshCount++;
				msg.append(sep);
				msg.append(fd.file.getName());
				sep = ", ";
				refreshFile(fd);
			}
		}

		if (refreshCount > 0)
		{
			JOptionPane.showMessageDialog(frame, msg);
		}
		
		return refreshCount;
	}
		
	// refresh the allWordData view by copying from the file data.
	private void refresh()
	{
		if (allWordDataView == null) allWordDataView = new ArrayList<WordHistory>();
		allWordDataView.clear();
		for (Map.Entry<File, List<WordHistory>> e : wordData.entrySet())
		{
			allWordDataView.addAll(e.getValue());
		}
	}
	
	/** 
	 * @return list of questions that have been answered wrong, ordered 
	 * by their errrorRate (high errorRate first)
	 */
	private List<Word> getErrorList()
	{
		List<WordHistory> sortedList = new ArrayList<WordHistory>();
		sortedList.addAll(allWordDataView);
		Collections.sort(sortedList, new Comparator<WordHistory> () 
				{
					@Override
					public int compare(WordHistory o1, WordHistory o2) 
					{
						return Double.compare(o2.errorRate, o1.errorRate);
					}
				});
		
		List<Word> result = new ArrayList<Word>();
		for (WordHistory i : sortedList)
		{
			if (i.errorRate < 0.1) break; 
			result.add(i.w);
		}
		
		return result;
	}
	
	/** returns list of words that have been asked before, oldest first */
	private List<Word> getRepeatList()
	{
		List<WordHistory> sortedList = new ArrayList<WordHistory>();
		for (WordHistory d : allWordDataView)
		{
			if (d.lastAsked != null)
				sortedList.add(d);
		}
		Collections.sort (sortedList, new Comparator<WordHistory> ()
				{
					@Override
					public int compare(WordHistory arg0, WordHistory arg1) 
					{
						if (arg0.lastAsked == null || arg1.lastAsked == null) throw new NullPointerException();
						return arg0.lastAsked.compareTo(arg1.lastAsked);
					}
				});
		List<Word> result = new ArrayList<Word>();
		for (WordHistory i : sortedList)
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
	@SuppressWarnings("unchecked")
	public Quiz createNewLesson()
	{
		Set<Word> result = new HashSet<Word>();
		
		float [] weights = new float[] { pctErrors, pctRepetition, 1.0f - pctErrors - pctRepetition };
		List<Word>[] lists = new List[] { getErrorList(), getRepeatList(), getNewList() } ;
		
		// sort lists by size, biggest last
		// bubble-sort: for only three items this is acceptable.
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < i; ++j)
			{
				if (lists[i].size() < lists[j].size())
				{
					float tempf = weights[i]; weights[i] = weights[j]; weights[j] = tempf;
					List templ = lists[i]; lists[i] = lists[j];	lists[j] = templ;					
				}
			}
		
		// add the smallest list
		float remain = lessonSize;
		int target = (int)(remain * weights[0]);
		result.addAll (lists[0].subList(0, Math.min (target, lists[0].size())));
		
		// two remaining weights must add up to 1.0
		float sum = weights[1] + weights[2];
		weights[1] /= sum;
		weights[2] /= sum;
		
		// add middle list
		remain = lessonSize - result.size();
		target = (int)(remain * weights[1]);
		result.addAll (lists[1].subList(0, Math.min(target, lists[1].size())));
		
		// add final list
		target = lessonSize - result.size();
		result.addAll (lists[2].subList(0, Math.min(target, lists[2].size())));
		
		Quiz quiz = new Quiz(result);
		return quiz;
	}

	/**
	 * Get list of questions that have not been asked before, in the order that they occur in the file list.
	 */
	private List<Word> getNewList() 
	{
		List<Word> result = new ArrayList<Word>();
		for (WordHistory wd : allWordDataView)
		{
			if (wd.askedTimes == 0) result.add (wd.w);
		}
		return result;
	}

	@Override
	/** This table has 4 columns */
	public int getColumnCount() {
		return 4;
	}

	@Override
	/** Number of rows is equal to the number of lesson files */
	public int getRowCount() {
		return fileData.size();
	}

	/** get number of questions in a lesson */
	private int getLessonSize(File f)
	{
		return wordData.get(f).size();
	}

	/** get average error rate for a given lesson */
	private float getErrorRate(File f)
	{
		float sum = 0;
		int n = 0;
		
		for (WordHistory i : wordData.get(f))
		{
			sum += i.errorRate;
			n++;
		}
		
		return (n == 0 ? 0 : sum / n);
	}
	
	/** get average number of times each question in a lesson was asked already */
	private float getAvgAsked(File f)
	{
		int sum = 0;
		int n = 0;

		for (WordHistory i : wordData.get(f))
		{
			sum += i.askedTimes;
			n++;
		}
		
		return (n == 0 ? 0 : ((float)sum / (float)n));
	}
	
	@Override
	/** Table cell value. Each row is one of the lesson files. 
	 * There are 4 columns, one for the lesson name, the lesson size, average times asked 
	 * and average error rate.  
	 */
	public Object getValueAt(int row, int col) 
	{
		switch (col)
		{
		case 0: // lesson name
			return fileData.get(row).file.getName();
		case 1: // lesson size
			return getLessonSize(fileData.get(row).file);
		case 2: // average times asked
			return getAvgAsked(fileData.get(row).file);
		case 3: // average error rate
			return getErrorRate(fileData.get(row).file);
		default:
			throw new IndexOutOfBoundsException("Column " + col + " is out of range");
		}
	}

	@Override
	/** returns column headers */
	public String getColumnName(int col)
	{
		switch (col)
		{
		case 0: return Engine.res.getString("LESSON");
		case 1: return Engine.res.getString("QUESTIONS");
		case 2: return Engine.res.getString("TIMES_ASKED");
		case 3: return Engine.res.getString("ERROR_RATE");
		default: throw new IndexOutOfBoundsException("Column " + col + " is out of range");
		}
	}
	
	/** look up a lesson file by its index in the list */
	public File getLessonByIndex(int row) 
	{
		return fileData.get(row).file;
	}

	/** Call this after a question has been answered. Record the question results. */
	public void record(String question, boolean correct) 
	{
		//TODO: linear search not optimal
		WordHistory found = null;
		for (WordHistory wd : allWordDataView)
		{
			if (wd.w.getQuestion().equals (question))
			{
				found = wd;
				break;
			}
		}
		if (found != null)
		{
			found.lastAsked = new Date();
			System.out.print (found.w + " -  errorRate: " + found.errorRate);
			found.errorRate = (found.errorRate * 0.75) + (correct ? 0.0 : 0.25);
			System.out.println (" -> " + found.errorRate);
			found.askedTimes++;
		}
		else
		{
			//TODO translate
			System.err.println ("Unknown question '" + question + "'");
		}
	}

	public void setCourseFile(File courseFile) 
	{
		this.courseFile = courseFile;
	}

	public File getCourseFile() 
	{
		return courseFile;
	}
	
	@Deprecated // unused, kept for backwards compatibility of serializable interface
	List<File> files = new ArrayList<File>(); // ordered list of lessons

	@Deprecated // kept for backwards compatibility
	List<WordHistory> allWordData = new ArrayList<WordHistory>(); // all word data
}
