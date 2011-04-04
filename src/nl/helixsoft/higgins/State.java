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
import java.io.Serializable;

/** simple class for the purpose of serializing session state. Used temporarily when saving a 
 * session or loading, 
 * but not during the running of the program. */ 
public class State implements Serializable 
{
	private static final long serialVersionUID = 1L;

	public QuizSession session = null;
	public File courseFile = null;
}
