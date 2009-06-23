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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Dialog in help->about
 */
public class AboutDlg 
{
	/**
	 * call this to open the dialog
	 */
	public void createAndShowGUI(JFrame parent)
	{
		final JFrame aboutDlg = new JFrame();
		
		FormLayout layout = new FormLayout(
				"4dlu, pref, 4dlu, pref, 4dlu",
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 4dlu");
		
		JLabel versionLabel = new JLabel ("Dr. Higgins " + Main.VERSION_STRING);
		JTextArea label = new JTextArea();
		label.setEditable(false);
		label.setText("(c) copyright 2003-2009\nM.P. van Iersel <amarillion@yahoo.com>\nThis program is licensend under GPL 3 or higher,\nsee COPYING.txt for details");
		
		CellConstraints cc = new CellConstraints();
		
		JPanel dialogBox = new JPanel();
		dialogBox.setLayout (layout);
		dialogBox .add (label, cc.xy(4,2));
		
		JButton btnOk = new JButton();
		btnOk.setText("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				aboutDlg.setVisible (false);
				aboutDlg.dispose();
			}
		});
		
		dialogBox.add (versionLabel, cc.xy(2, 4));
		dialogBox.add (btnOk, cc.xyw (2, 6, 3, "center, top"));			
		
		aboutDlg.setResizable(false);
		aboutDlg.setTitle("About Dr. Higgins " + Main.VERSION_STRING);
		aboutDlg.add (dialogBox);
		aboutDlg.pack();
		aboutDlg.setLocationRelativeTo(parent);
		aboutDlg.setVisible(true);
	}

}
