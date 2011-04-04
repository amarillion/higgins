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

import java.io.Serializable;

/**
 * Represents a question-answer pair. This class is immutable.
 */
public class Word implements Serializable
{
	private static final long serialVersionUID = 2L;
		
	private final String question;
	private final String answer;
	private final int dir; 
	private final int pair;

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

	/** Position of this question in the lesson file. 
	 * The reverse of this question has the same index. */
	public int getPair () { return pair; }
}
