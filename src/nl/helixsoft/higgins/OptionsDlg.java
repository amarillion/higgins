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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class OptionsDlg extends JDialog implements ActionListener
{
	private JTextField txtBins;
	private JButton btnOk;
	private JButton btnCancel;
	
	public OptionsDlg(JFrame frame)
	{
		super (frame, true);
		setTitle (MainFrame.res.getString("OPTIONS"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);
		panel.setLayout (new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();
		txtBins = new JTextField (10);
		panel.add (new JLabel (MainFrame.res.getString("NUMBER_OF_BINS")), cc.xy (2,2));
		panel.add (txtBins, cc.xy (4,2));
		btnOk = new JButton (MainFrame.res.getString("OK"));
		btnCancel = new JButton (MainFrame.res.getString("CANCEL"));
		panel.add (btnOk, cc.xy (2,4));
		panel.add (btnCancel, cc.xy (4,4));
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
		pack();
		setLocationRelativeTo (frame);
	}

	private boolean isCancelled = false;
	
	public void setBins(int value)
	{
		txtBins.setText("" + value);
	}
	
	public int getBins()
	{
		return Integer.parseInt (txtBins.getText());
		//TODO: validate - integer between 2 and 10
	}
	
	public boolean isCancelled()
	{
		return isCancelled;
	}

	public void actionPerformed(ActionEvent ae) 
	{
		if (ae.getSource() == btnOk)
		{
			isCancelled = false;
			setVisible(false);
		}
		else if (ae.getSource() == btnCancel)
		{
			isCancelled = true;
			setVisible(false);
		}
	}

}