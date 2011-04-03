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
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ResultDlg extends JDialog implements ActionListener
{
	private JButton btnOk;
	private JTextArea txtResults;
	
	ResultDlg (JFrame frame)
	{
		super (frame, true);
		setTitle (MainFrame.res.getString("QUIZ_RESULT"));
		
		JPanel panel = new JPanel();
		setContentPane(panel);
		panel.setLayout (new FormLayout(
				"3dlu, pref, 3dlu", 
				"3dlu, pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();
		
		txtResults = new JTextArea (20, 40);
		txtResults.setEditable(false);
		panel.add (new JScrollPane(txtResults), cc.xy (2, 2));
		
		btnOk = new JButton(MainFrame.res.getString("OK"));
		panel.add (btnOk, cc.xy (2, 4));
		btnOk.addActionListener(this);
		
		pack();
		setLocationRelativeTo(frame);
	}
	
	QuizSession quiz = null;
	
	public void setQuiz(QuizSession aQuiz)
	{
		quiz = aQuiz;
		
		List<WordState> mostDifficult = quiz.getMostDifficult(20);
		for (WordState w : mostDifficult)
		{
			Map<String, Integer> wrongAnswers = w.getWrongAnswers();
			txtResults.append(w.getWord().getQuestion() + ", " + w.getWord().getAnswer() + "\n");					
			txtResults.append("  " + MainFrame.res.getString("MISTAKES") + "\n");
			if (wrongAnswers.size() > 0)
			{
				for (String key : wrongAnswers.keySet())
				{
					txtResults.append("  \"" + key + "\" x " + wrongAnswers.get(key) + "\n");
				}
			}
		}
	}

	public void actionPerformed(ActionEvent ae) 
	{
		if (ae.getSource() == btnOk)
		{
			setVisible(false);
		}
	}
}
