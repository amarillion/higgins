package nl.helixsoft.higgins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * The main window where the Quiz takes place
 */
public class MainFrame 
{
	private JFrame frame;
	private TextField txtInput;
	private TextArea txtOutput;
	private JPanel binPanel;
	private JLabel lblResult1;
	private JLabel lblResult2;
	
	public void createAndShowGui()
	{
		frame = new JFrame("Dr. Higgins");
		frame.setLayout(new BorderLayout());
		
		JMenuBar bar = new JMenuBar();
		createMenu(bar);
		frame.setJMenuBar(bar);
		
		JPanel resultPanel = new JPanel();
		lblResult1 = new JLabel ("Correct");
		lblResult2 = new JLabel ("explanation...");
		resultPanel.add (lblResult1);
		resultPanel.add (lblResult2);
		
		txtInput = new TextField (60);
		txtOutput = new TextArea (10, 60);
		txtOutput.setEditable(false);
		txtOutput.append ("No current Lesson.\n");
		txtOutput.append ("Go to file->new to start a new lesson.\n");
		binPanel = new JPanel();
		
		frame.add (resultPanel, BorderLayout.NORTH);
		frame.add (txtOutput, BorderLayout.CENTER);
		frame.add (txtInput, BorderLayout.SOUTH);
		frame.add (binPanel, BorderLayout.EAST);
		
		txtInput.requestFocus();
		txtInput.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0) 
			{
				if (quiz != null && !quiz.isFinished())
				{
					checkAnswer();
					txtInput.setText("");
					
					if (quiz.isFinished())
					{
						writeLog();
						showResults();
						clearFrame();
					}
					else
					{
						nextWord();
					}
				}
			}
		});
		
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public void createMenu(JMenuBar bar)
	{
		JMenu file = new JMenu ("file");
		file.add(new NewAction());
		file.add(new RestartAction());
		file.addSeparator();
		file.add(new OptionsAction());
		file.addSeparator();
		file.add(new ExitAction());
		JMenu view = new JMenu ("view");
		view.add(new StatsAction());
		JMenu help = new JMenu ("help");
		help.add(new AboutAction());
		bar.add (file);
		bar.add (view);
		bar.add (help);
	}
	
	private Quiz quiz = null;
	
	private class NewAction extends AbstractAction
	{
		NewAction()
		{
			super();
			putValue (NAME, "New");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			JFileChooser chooser = new JFileChooser();
			//TODO: make flexibile
			chooser.setCurrentDirectory(new File ("/home/martijn/prg/jHiggins/lessons"));
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				File f = chooser.getSelectedFile();
				try
				{
					// stop logging current quiz, if it exists
					if (quiz != null) 
					{
						writeLog();
						quiz = null;
					}
					quiz = new Quiz(f);
					beginLog();
					nextWord();
				}
				catch (IOException ex)
				{
					JOptionPane.showMessageDialog(frame, "File read error",
							"Problem while opening lesson\n" + ex.getMessage(), 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	};

	private void writeLog()
	{
		//TODO
	}

	private void beginLog()
	{
		//TODO
	}
	
	private void nextWord()
	{
		quiz.nextQuestion();
		drawBins();
		txtOutput.append ("\nQuestion #" + quiz.getCounter() 
				+ ": " + quiz.getQuestion() + "\n"); 
	}

	private void drawBins()
	{
		//TODO
	}

	private void clearFrame()
	{
		//TODO
	}
	
	private void checkAnswer()
	{
		String myAnswer = txtInput.getText();
		txtOutput.setText ("");
		if (quiz.compareAnswer(myAnswer))
		{
			lblResult1.setText("Correct");
			lblResult1.setForeground(Color.GREEN);
			txtOutput.append("The answer was :\"" + quiz.getCorrectAnswer() + "\"");
		}
		else
		{
			lblResult1.setText("Wrong");
			lblResult1.setForeground(Color.RED);
			txtOutput.append("You answered :\"" + myAnswer + "\"");
			txtOutput.append("The answer was :\"" + quiz.getCorrectAnswer() + "\"");
		}
		if (quiz.hasHint())
		{
			txtOutput.append ("Hint: " + quiz.getHint());
		}
	}
	
	private void showResults()
	{
		//TODO
	}
	
	private static class RestartAction extends AbstractAction
	{
		RestartAction()
		{
			super();
			putValue (NAME, "Restart");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			// TODO Auto-generated method stub
		}
	};

	private static class OptionsAction extends AbstractAction
	{
		OptionsAction()
		{
			super();
			putValue (NAME, "Options");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			// TODO Auto-generated method stub
		}
	};

	private class ExitAction extends AbstractAction
	{
		ExitAction()
		{
			super();
			putValue (NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			frame.setVisible(false);
		}
	};

	private static class StatsAction extends AbstractAction
	{
		StatsAction()
		{
			super();
			putValue (NAME, "Statistics");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			// TODO Auto-generated method stub
		}
	};

	private class AboutAction extends AbstractAction
	{
		AboutAction()
		{
			super();
			putValue (NAME, "About Dr. Higgins");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			AboutDlg dlg = new AboutDlg();
			dlg.createAndShowGUI(frame);
		}
	};

}
