//    This file is part of Dr. Higgins.
//    Copyright 2003-2010 Martijn van Iersel <amarillion@yahoo.com>
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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * provides main method
 */
public class Main 
{
	/**
	 * create and show the GUI
	 */
	public static void main(String args[])
	{
		final MainFrame mainFrame = new MainFrame();
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try {
					//System look and feel is ugly for linux
				    if (System.getProperty("os.name").startsWith("Linux"))
				    {
						UIManager.setLookAndFeel(
					        UIManager.getCrossPlatformLookAndFeelClassName());
				    }
				    else
				    {
						UIManager.setLookAndFeel(
						        UIManager.getSystemLookAndFeelClassName());
				    }
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				mainFrame.createAndShowGui();				
			}
		});
	}
}
