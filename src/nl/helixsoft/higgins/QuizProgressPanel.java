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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Custom panel for viewing progress of quiz
 */
public class QuizProgressPanel extends JPanel
{
	private final Engine engine;
	
	public QuizProgressPanel(Engine engine)
	{
		this.engine = engine;
		setBorder (BorderFactory.createLineBorder(Color.BLACK));
		setBackground(Color.BLUE);
		setForeground(Color.YELLOW);
	}

	public Dimension getPreferredSize()
	{
		return new Dimension (250, 200);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent (g);
		
		QuizSession session = engine.getSession();
		if (session != null)
		{
			for (int i = 0; i < session.getBins(); ++i)
			{
				g.drawString (MainFrame.res.getString("BIN") + 
						" " + (i + 1) + ": " + session.getBinCount(i), 
						10, 20 * (i + 1));
			}
		}
	}
}
