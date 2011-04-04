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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of questions, read from a text file.
 */
public class Quiz implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** private, do not allow external instantiation */
	private Quiz()
	{
		// initialize defaults
		question1 = Engine.res.getString("WHAT_IS");
		question2 = Engine.res.getString("WHAT_IS");
		askBothWays = 1;
	}

	public Quiz(List<Word> words)
	{
		this();
		this.words.addAll (words);
		fileName = null;
		originalTimeStamp = 0;
		for (Word w : words)
		{
			wordMap.put (w.getAnswer(), w.getQuestion());
		}
	}
	
	private List<Word> words = new ArrayList<Word>();
	// map of answers, so that we can check for confusion
	private Map<String, String> wordMap = new HashMap<String, String>();
	
	private String question1;
	private String question2;
	
	private int askBothWays;
	private File fileName;
	private long originalTimeStamp;

	/**
	 * The lesson file from which this quiz was loaded
	 */
	public File getFile() { return fileName; }

	/** 
	 * Instantiate a new quiz object, and fill it with data read from file 
	 * @returns new Quiz instance
	 */
	public static Quiz loadFromFile(File newFileName) throws IOException
	{
		QuizLoader ql = new QuizLoader(newFileName);
		ql.processFile();
		return ql.getQuiz();
	}

	/** Question to ask for words on one side of the comma. Default: what is ""? */
	public String getQuestion1() 
	{
		return question1;
	}

	/** Question to ask for words on the other side of the comma. Default: what is ""? */
	public String getQuestion2() 
	{
		return question2;
	}

	/** Get all words, on both sides of the comma */
	public List<Word> getWords() 
	{
		return words;
	}

	/**
	 * Find the question for the given answer. Useful for the "you may be confused with" feature. 
	 * @param anAnswer
	 * @return the question
	 */
	public String getByAnswer(String anAnswer)
	{
		return wordMap.get(anAnswer);
	}

	/**
	 * @return true if the file on disk has changed since it was loaded originally.
	 */
	public boolean modifiedOnDisk()
	{
		return fileName != null && fileName.lastModified() > originalTimeStamp;
	}

	/**
	 * A helper class to load a quiz from a text file.
	 * a.o. checks for duplicate questions or duplicate answers
	 */
	private static class QuizLoader
	{
		Quiz result = new Quiz();
		String encoding;
		Set<String> questions = new HashSet<String>();
		Set<String> answers = new HashSet<String>();
		List <String[]> words = new ArrayList<String[]>(); // String[2], Pairs of q, a 
			
		private void determineEncoding(File f) throws IOException
		{
			FileInputStream fs = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fs);
			LineNumberReader reader = new LineNumberReader(isr);
			
			String line;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith ("#encoding="))
				{
					encoding = line.substring(10).trim();
					break;
				}
			}			
			System.out.println ("Using encoding " + encoding);
		}
		
		public QuizLoader (File f)
		{
			result.fileName = f;
			result.originalTimeStamp = f.lastModified();
			// default encoding: ISO8859-1 or iso-latin-1
			encoding = "ISO8859-1";
		}
		
		private void processFile() throws IOException
		{
			determineEncoding (result.fileName);
			
			FileInputStream fs = new FileInputStream(result.fileName);
			InputStreamReader isr = new InputStreamReader(fs, encoding);
			LineNumberReader reader = new LineNumberReader(isr);
			
			String line;
			//TODO check that /012 and /015 are handled correctly.
			while ((line = reader.readLine()) != null)
			{
				processLine (line, reader.getLineNumber());
			}
		}
		
		private void processLine(String line, int lineNo)
		{
			String first, last;
			if (line.matches("\\s*"))
			{
				// empty or whitespace, ignore
			}
			else if (line.charAt(0) == '#')
			{
				int pos = line.indexOf('=');
				if (pos >= 0)
				{
					first = line.substring(1, pos).trim();
					last = line.substring(pos + 1, line.length()).trim();
					
					if (first.equalsIgnoreCase("question1")) { result.question1 = last; }
					else if (first.equalsIgnoreCase("question2")) { result.question2 = last; }
					else if (first.equalsIgnoreCase("askbothways")) { result.askBothWays = Integer.parseInt(last); }
				}
			}
			else
			{
				int pos = line.indexOf(", ");
				if (pos >= 0)
				{
					first = line.substring(0, pos).trim();
					last = line.substring(pos + 2, line.length()).trim();
					if (!questions.contains(first))
					{
						questions.add(first);
						if (!answers.contains (last))
						{
							answers.add (last);
							String[] pair = new String[] { first , last };
							words.add(pair);
						}
						else
						{
							System.err.println (
									Engine.res.getString("WARN_DUP_ANSWER")
									.replace("%1", last)
									.replace("%2", "" + lineNo)
								);
						}
					}
					else
					{
						System.err.println (
								Engine.res.getString("WARN_DUP_QUESTION")
								.replace("%1", last)
								.replace("%2", "" + lineNo)
							);
					}
				}
				else
				{
					System.err.println (
							Engine.res.getString("WARN_SYNTAX")
							.replace("%1", "" + lineNo)
						);
				}
			}
		}

		public Quiz getQuiz() 
		{
			// create words
			int l = 0;
			for (String[] pair : words)
			{
				result.words.add(new Word (pair[0], pair[1], 0, l));
				result.wordMap.put(pair[1], pair[0]);
				if (result.askBothWays != 0)
				{				
					result.words.add(new Word (pair[1], pair[0], 1, l));
					result.wordMap.put(pair[0], pair[1]);
				}
			}

			return result;
		}
	}
}
