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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import nl.helixsoft.util.Browser;

/**
 * The main window, where the Quiz takes place
 */
public class MainFrame 
{
	private final Engine parent;
	
	public MainFrame(Engine engine)
	{
		parent = engine;
	}
	
	/** Utility function to load font as resource from the classpath. */ 
	private Font loadFont()
	{
		String fName = "ttf-japanese-gothic.ttf";
		Font font;
		try 
		{
			InputStream is = Main.class.getResourceAsStream(fName);
			font = Font.createFont(Font.TRUETYPE_FONT, is);
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			System.err.println(fName + " not loaded.  Using serif font.");
			font = new Font("serif", Font.PLAIN, 24);
		}
		return font;	
	}

	private JFrame frame;

	private JTextField txtInput;
	private JTextArea txtOutput;
	private QuizProgressPanel binPanel;
	
	private JLabel lblResult;
	
	//TODO: make private
	ViewCourseAction viewCourseAction;
	
	private InputMethod im;
	
	public void createAndShowGui()
	{
		Font font = loadFont();
		
		frame = new JFrame("Dr. Higgins");
		getFrame().setLayout(new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();
		
		JMenuBar bar = new JMenuBar();
		createMenu(bar);
		getFrame().setJMenuBar(bar);
				
		lblResult = new JLabel (" ");
		lblResult.setHorizontalAlignment(JLabel.CENTER);
		Font bigFont = lblResult.getFont().deriveFont(20.0f).deriveFont(Font.BOLD);
		lblResult.setFont(bigFont);
		
		txtInput = new JTextField (40);
		txtOutput = new JTextArea (8, 40);
		txtOutput.setEditable(false);
		txtOutput.setFocusable(false);
		
		Font medFont = font.deriveFont(20.0f);
		txtInput.setFont(medFont);
		txtOutput.setFont(medFont);
		
		boolean isInternationalInput = parent.getPrefs().getBoolean(HiggPrefs.INTERNATIONAL_INPUT); 
		im = new InputMethod(txtInput);
		im.setCurrentSection(isInternationalInput ? "west-european" : null);
		final JCheckBox ckInputMethod;
		ckInputMethod = new JCheckBox("Enable international input", 
				parent.getPrefs().getBoolean(HiggPrefs.INTERNATIONAL_INPUT));
		ckInputMethod.setFocusable(false);
		ckInputMethod.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae) 
			{
				im.setCurrentSection(
						ckInputMethod.isSelected() ?
								"west-european" : null);
				parent.getPrefs().set(HiggPrefs.INTERNATIONAL_INPUT, 
						ckInputMethod.isSelected());
			}
		});
		
		binPanel = new QuizProgressPanel(parent);
		
		getFrame().add (lblResult, cc.xy(2,2));
		getFrame().add (txtOutput, cc.xy(2,4));
		getFrame().add (ckInputMethod, cc.xy(2,6)); 
		getFrame().add (txtInput, cc.xyw(2,8,3));
		getFrame().add (binPanel, cc.xywh(4,2,1,5));
		
		txtInput.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				String myAnswer = txtInput.getText().trim();
				parent.checkAnswer(myAnswer);
			}
		});
		
		getFrame().pack();
		txtInput.requestFocusInWindow();
		getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		getFrame().addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				// make sure close get's called when clicking "x"
				// or ALT+F4
				parent.close();
			}			
		});
		
		startupMessage();
		
		//TODO: find a way to move loadState to Engine.run();
		parent.loadState();

		frame.setLocation(
				parent.getPrefs().getInt(HiggPrefs.WIN_X),
				parent.getPrefs().getInt(HiggPrefs.WIN_Y)
				);
		frame.setVisible(true);
	}

	public void createMenu(JMenuBar bar)
	{
		JMenu file = new JMenu (Engine.res.getString("FILE"));
		file.add(new NewAction());
		file.add(new RestartAction());
		file.addSeparator();
		file.add(new NewCourseAction());
		file.add(new OpenCourseAction());
		viewCourseAction = new ViewCourseAction();
		file.add(viewCourseAction);
		file.addSeparator();
		file.add(new OptionsAction());
		file.addSeparator();
		file.add(new ExitAction());
		JMenu view = new JMenu (Engine.res.getString("VIEW"));
		view.add(new StatsAction());
		JMenu help = new JMenu (Engine.res.getString("HELP"));
		help.add(new AboutAction());
		help.add(new HelpAction());
		bar.add (file);
		bar.add (view);
		bar.add (help);
	}
	
	void startupMessage()
	{
		lblResult.setText (" ");
		txtOutput.setText ("");
		txtOutput.append (Engine.res.getString("NO_CURRENT_LESSON") + "\n");
		txtOutput.append (Engine.res.getString("GO_TO_FILE") + "\n");
	}

	public void clearOutput()
	{
		txtOutput.setText ("");		
	}
	
	private class NewAction extends AbstractAction
	{
		NewAction()
		{
			super();
			putValue (NAME, Engine.res.getString("NEW"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(
					parent.getPrefs().getFile(HiggPrefs.LAST_USED_LESSONS_DIR));
			if (chooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				parent.getPrefs().set(HiggPrefs.LAST_USED_LESSONS_DIR, 
						chooser.getCurrentDirectory());
				File f = chooser.getSelectedFile();
				parent.startQuiz (parent.loadQuiz(f));
			}
		}
	};

	private class NewCourseAction extends AbstractAction
	{
		NewCourseAction()
		{
			super();
			putValue (NAME, Engine.res.getString("NEW_COURSE"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			parent.newCourse();
			CourseDlg dlg = new CourseDlg(parent);
			dlg.setVisible(true);			
		}
	};

	private class OpenCourseAction extends AbstractAction
	{
		OpenCourseAction()
		{
			super();
			putValue (NAME, Engine.res.getString("OPEN_COURSE"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(
					parent.getPrefs().getFile(HiggPrefs.LAST_USED_COURSE_DIR));
			if (chooser.showOpenDialog(getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				parent.getPrefs().set(HiggPrefs.LAST_USED_COURSE_DIR, 
						chooser.getCurrentDirectory());
				File f = chooser.getSelectedFile();
				try {
					parent.loadCourse(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	//TODO: make private
	public class ViewCourseAction extends AbstractAction
	{
		ViewCourseAction()
		{
			super();
			putValue (NAME, Engine.res.getString("VIEW_COURSE"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
			setEnabled(parent.getModel() != null);
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			if (parent.getModel() == null) return; // should not be able to get here. 
			CourseDlg dlg = new CourseDlg(parent);
			dlg.setVisible(true);
		}
	};

	private class RestartAction extends AbstractAction
	{
		RestartAction()
		{
			super();
			putValue (NAME, Engine.res.getString("RESTART"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			parent.restartQuiz ();
		}
	};

	private class OptionsAction extends AbstractAction
	{
		OptionsAction()
		{
			super();
			putValue (NAME, Engine.res.getString("OPTIONS"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			OptionsDlg dlg = new OptionsDlg(getFrame());
			dlg.setBins(parent.getPrefs().getInt(HiggPrefs.DEFAULT_BINS));
			dlg.setVisible(true);
			if (!dlg.isCancelled())
			{
				parent.getPrefs().set(HiggPrefs.DEFAULT_BINS, dlg.getBins()); 
				if (parent.getSession() != null) parent.getSession().setBins(dlg.getBins());
			}
		}
	};

	private class ExitAction extends AbstractAction
	{
		ExitAction()
		{
			super();
			putValue (NAME, Engine.res.getString("EXIT"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			parent.close();
		}
	};

		
	private class StatsAction extends AbstractAction
	{
		StatsAction()
		{
			super();
			putValue (NAME, Engine.res.getString("STATISTICS"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			StatsDlg dlg = new StatsDlg(getFrame());
			dlg.loadLogFile();
			dlg.setVisible(true);
		}
	};

	private class AboutAction extends AbstractAction
	{
		AboutAction()
		{
			super();
			putValue (NAME, Engine.res.getString("ABOUT_DR_HIGGINS"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			AboutDlg dlg = new AboutDlg();
			dlg.createAndShowGUI(getFrame());
		}
	};

	private class HelpAction extends AbstractAction
	{
		HelpAction()
		{
			super();
			putValue (NAME, Engine.res.getString("HELP"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			File helpFile = new File(Engine.INSTALLDIR, "doc/index.html");
			Browser.launch(getFrame(), helpFile.toURI().toString());
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void showAnswer(int counter, String question, boolean correct, String myAnswer, String correctAnswer, String hint) 
	{
		txtOutput.setText ("");
		txtOutput.append(Engine.res.getString("QUESTION") + " #" + counter 
				+ ": " + question + "\n");
		
		if (correct)
		{
			lblResult.setText(Engine.res.getString("CORRECT"));
			lblResult.setForeground(Color.GREEN);
		}
		else
		{
			lblResult.setText(Engine.res.getString("WRONG"));
			lblResult.setForeground(Color.RED);
			txtOutput.append(Engine.res.getString("YOU_ANSWERED") + " \"" + myAnswer + "\"\n");
		}
		
		txtOutput.append(Engine.res.getString("THE_ANSWER_WAS") + " \"" + correctAnswer + "\"\n");
		
		if (hint != null)
		{
			txtOutput.append (hint + "\n");
		}

		txtOutput.append ("\n");
		txtInput.setText("");
	}

	public void showQuestion(int counter, String question) 
	{
		txtOutput.append (Engine.res.getString("QUESTION") + " #" + counter 
				+ ": " + question + "\n"); 
	}

	public void refreshBins()
	{
		binPanel.repaint();
	}
	

}
