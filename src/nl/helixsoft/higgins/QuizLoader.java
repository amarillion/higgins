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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A helper class to load a quiz from a text file.
 * a.o. checks for duplicate questions or duplicate answers
 */
class QuizLoader
{
	Set<String> questions = new HashSet<String>();
	Set<String> answers = new HashSet<String>();
	List <String[]> words = new ArrayList<String[]>(); // String[2], Pairs of q, a 
	String question1 = MainFrame.res.getString("WHAT_IS");
	String question2 = MainFrame.res.getString("WHAT_IS");
	String description = "";
	// default encoding: ISO8859-1 or iso-latin-1
	String encoding = "ISO8859-1";
	int bins = 4;
	int askBothWays = 1;
	
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
	
	void processFile(File f) throws IOException
	{
		determineEncoding (f);
		
		FileInputStream fs = new FileInputStream(f);
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
				
				if (first.equalsIgnoreCase("question1")) { question1 = last; }
				else if (first.equalsIgnoreCase("question2")) { question2 = last; }
				else if (first.equalsIgnoreCase("description")) { description = last; }
				else if (first.equalsIgnoreCase("bins")) { bins = Integer.parseInt(last); }
				else if (first.equalsIgnoreCase("askbothways")) { askBothWays = Integer.parseInt(last); }
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
						//TODO: translate
						System.err.println ("Warning: duplicate answer '" + last +
								"' in line " + lineNo + ", ignored.");
					}
				}
				else
				{
					//TODO: translate
					System.err.println ("Warning: duplicate question '" + first +
							"' in line " + lineNo + ", ignored.");
				}
			}
			else
			{
				//TODO: translate
				System.err.println ("Warning: syntax error in line "
						+ lineNo + ", comma expected. ignored.");
			}
		}
	}
}