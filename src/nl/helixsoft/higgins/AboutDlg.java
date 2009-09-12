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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialog in help->about
 */
public class AboutDlg 
{
	public static final String getVersion()
	{
		Properties props = new Properties();
		try
		{
			props.load(AboutDlg.class.getResourceAsStream("version.properties"));
		}
		catch (IOException ex) { return "ERROR: Unable to read version number"; }
		return props.getProperty("version");
	}

	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI(JFrame parent)
	{
		final JFrame aboutDlg = new JFrame();
		
		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");
		
		JLabel versionLabel = new JLabel ("Dr. Higgins version " + getVersion());
		JLabel label = new JLabel();
		//TODO: Translate
		label.setText(
			"<html><h2>Dr. Higgins</h2>" +
			"(c) copyright 2003-2009<br>\n" +
			"M.P. van Iersel <amarillion@yahoo.com>\n" +
			"<h2>Contributors and Translators</h2>\n" +
			"Olivia Guerra Santin (Spanish)<br>\n" +
			"Adem Bilican (French)<br>\n" +
			"Magdalena S\u0142upska (Polish)<br>\n" +
			"<br>\n" +
			"This program is free and open source<br>\n" +
			"Licensed as GPL 3 or higher,<br>\n" +
			"see COPYING.txt for details<br>\n" +
			"<p>\n" +
			"Visit the website at http://www.helixsoft.nl</html>");
		
		CellConstraints cc = new CellConstraints();
		
		JPanel dialogBox = new JPanel();
		dialogBox.setLayout (layout);
		dialogBox .add (label, cc.xy(2,2));
		
		JButton btnOk = new JButton();
		btnOk.setText(MainFrame.res.getString("OK"));
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});
		
		dialogBox.add (versionLabel, cc.xy(2, 4));
		dialogBox.add (btnOk, cc.xy (2, 6));	
		
		aboutDlg.setResizable(false);
		aboutDlg.setTitle(MainFrame.res.getString("ABOUT_DR_HIGGINS"));
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setLocationRelativeTo(parent);
		aboutDlg.setVisible(true);
	}

}
