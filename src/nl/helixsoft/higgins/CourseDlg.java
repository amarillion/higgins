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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CourseDlg extends JDialog implements ActionListener, ChangeListener
{
	private JTable tblLessons; 
	private JButton btnSave;
	private JButton btnCancel;
	private JButton btnAdd;
	private JButton btnRemove;
	
	private JTextField txtLessonSize;
	
	private JSlider slErrors;
	private JSlider slRepeats;
	private JSlider slNew;

	private CourseModel model;
	
	CourseDlg (JFrame frame, CourseModel model)
	{
		super (frame, true);
		setTitle (MainFrame.res.getString("COURSE"));
		
		setLayout(new FormLayout(
				"3dlu, pref:grow, 3dlu, pref, 3dlu", 
				"3dlu, pref:grow, 3dlu, pref, 3dlu, pref, 3dlu"
			));
		CellConstraints cc = new CellConstraints();
		CellConstraints cc2 = new CellConstraints();

		tblLessons = new JTable();
		add (tblLessons, cc.xy(2,2));

		btnAdd= new JButton(MainFrame.res.getString("ADD"));
		btnAdd.addActionListener(this);
		btnRemove = new JButton(MainFrame.res.getString("REMOVE"));
		btnRemove.addActionListener(this);
		ButtonStackBuilder bsb = new ButtonStackBuilder();
		bsb.addButtons(new JButton[] { btnAdd, btnRemove });
		add (bsb.getPanel(), cc.xy(4, 2));

		PanelBuilder builder = new PanelBuilder(new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"
				));
		
		txtLessonSize = new JTextField(5);
		txtLessonSize.addActionListener(this);
		builder.add (
				new JLabel(MainFrame.res.getString("LESSONSIZE")), cc.xy(2,2), 
				txtLessonSize, cc2.xy(4,2)
			);
		slErrors = new JSlider(0, 100, 20);
		slErrors.addChangeListener(this);
		builder.add (
				new JLabel(MainFrame.res.getString("PCTERRORS")), cc.xy(2,4), 
				slErrors, cc2.xy(4,4)
			);
		slRepeats = new JSlider(0, 100, 20);
		slRepeats.addChangeListener(this);
		builder.add (
				new JLabel(MainFrame.res.getString("PCTREPEAT")), cc.xy(2,6),
				slRepeats, cc2.xy(4,6)
			);
		slNew = new JSlider(0, 100, 60);
		slNew.addChangeListener(this);
		builder.add (
				new JLabel(MainFrame.res.getString("PCTNEW")), cc.xy(2,8), 
				slNew, cc2.xy(4, 8)
			);
		add (builder.getPanel(), cc.xy(2, 4));
		
		btnSave = new JButton(MainFrame.res.getString("SAVE"));
		btnSave.addActionListener(this);
		btnCancel = new JButton(MainFrame.res.getString("CANCEL"));
		btnCancel.addActionListener(this);
		JPanel btnPanel = ButtonBarFactory.buildLeftAlignedBar(btnSave, btnCancel);
		add (btnPanel, cc.xy(2, 6));
		
		pack();
		setLocationRelativeTo (frame);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == btnSave)
		{
		}
		else if (e.getSource() == btnCancel)
		{
		}
		else if (e.getSource() == btnAdd)
		{
			JFileChooser jfc = new JFileChooser();
			jfc.setMultiSelectionEnabled(true);
			if (jfc.showOpenDialog(this) == JOptionPane.OK_OPTION)
			{
				for (File f : jfc.getSelectedFiles())
				{
					model.addFile(f);
				}
			}
		}
		else if (e.getSource() == btnRemove)
		{
			List<File> toRemove = new ArrayList<File>();
			for (int row : tblLessons.getSelectedRows())
			{
				toRemove.add (model.getLessonByIndex(row));
			}
			for (File f : toRemove)
			{
				model.removeFile(f);
			}
		}
		else if (e.getSource() == txtLessonSize)
		{
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) 
	{
		JSlider adj;
		JSlider o1;
		JSlider o2;
		
		if 	    (e.getSource() == slNew)     { adj = slNew; o1 = slErrors; o2 = slRepeats; }
		else if (e.getSource() == slErrors)  { adj = slErrors; o1 = slRepeats; o2 = slNew; }
		else if (e.getSource() == slRepeats) { adj = slRepeats; o1 = slNew; o2 = slErrors; }
		else return;

		int tot = slNew.getValue() + slErrors.getValue() + slRepeats.getValue();
		
		// make sure the delta-remainder is evenly distributed
		if (adj.getValue() % 2 == 0) { JSlider temp = o1; o1 = o2; o2 = temp; }
		
		int o1n = o1.getValue() + (100 - tot) / 2;
		if (o1n + adj.getValue() > 100) o1n = 100 - adj.getValue();
		o1.setValue(o1n);
		o2.setValue(100 - adj.getValue() - o1.getValue());
	}
}
