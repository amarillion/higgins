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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StatsDlg extends JDialog implements ActionListener
{
	private JTable table;
	private JButton btnOk;
	
	public StatsDlg(JFrame frame)
	{
		super (frame, true);
		setTitle(MainFrame.res.getString("STATISTICS"));
		JPanel panel = new JPanel();
		setContentPane(panel);
		panel.setLayout (new FormLayout(
				"3dlu, pref:grow, 3dlu",
				"3dlu, pref:grow, 3dlu, pref, 3dlu"));
		
		CellConstraints cc = new CellConstraints();
		
		table = new JTable();
		panel.add (new JScrollPane(table), cc.xy(2,2));
		
		btnOk = new JButton(MainFrame.res.getString("OK"));
		btnOk.addActionListener(this);
		panel.add (btnOk, cc.xy(2,4));
		
		pack();
		setLocationRelativeTo(frame);
	}
	
	void loadLogFile()
	{
		DefaultTableModel tm = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int col)
			{
				return false;
			}
		};
		
		tm.setColumnCount(4);
		String[] columns = new String[] {
				MainFrame.res.getString("DATE"),
				MainFrame.res.getString("QUESTIONS"),
				MainFrame.res.getString("DURATION"),
				MainFrame.res.getString("LESSON") };
		tm.setColumnIdentifiers(columns);
		try 
		{
			BufferedReader reader = new BufferedReader (new FileReader (MainFrame.LOGFILE));
			String line;
			while ((line = reader.readLine()) != null)
			{
				//TODO: use tab as separator
				String date = line.substring(0, 24);
				String numQuestions = line.substring(28, 34);
				String duration = line.substring(40, 49);
				String lesson = line.substring(49);
				tm.addRow(new String[] {date, numQuestions, duration, lesson} );
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table.setModel(tm);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//TODO: calculate optimal sizes
		table.getColumn(columns[0]).setPreferredWidth(200);
		table.getColumn(columns[1]).setPreferredWidth(40);
		table.getColumn(columns[2]).setPreferredWidth(80);
		table.getColumn(columns[3]).setPreferredWidth(400);
		table.doLayout();
		pack();
	}
	
	public void actionPerformed(ActionEvent ae) 
	{
		if (ae.getSource() == btnOk)
		{
			setVisible (false);
		}
	}
}
