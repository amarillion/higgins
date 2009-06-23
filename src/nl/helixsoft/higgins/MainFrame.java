package nl.helixsoft.higgins;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import nl.helixsoft.util.TypedProperties;

/**
 * The main window where the Quiz takes place
 */
public class MainFrame 
{
	//TODO: not hard-coded
	public static final File DEFAULT_LESSONS_DIR = new File ("/home/martijn/prg/jHiggins/lessons");
	public static final File LOGFILE = new File ("/home/martijn/higginslog.txt");
	public static final File PREFERENCES = new File ("/home/martijn/.higgins.props");
	public static final File STATE = new File ("/home/martijn/.higgins.sto");

	private JFrame frame;
	private JTextField txtInput;
	private JTextArea txtOutput;
	private QuizProgressPanel binPanel;
	private JLabel lblResult;
	private TypedProperties<HiggPrefs> prefs;
	
	private int startCounter;
	private Date startTime;
	
	public void createAndShowGui()
	{
		frame = new JFrame("Dr. Higgins");
		frame.setLayout(new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
		CellConstraints cc = new CellConstraints();
		
		JMenuBar bar = new JMenuBar();
		createMenu(bar);
		frame.setJMenuBar(bar);
				
		lblResult = new JLabel (" ");
		lblResult.setHorizontalAlignment(JLabel.CENTER);
		Font bigFont = lblResult.getFont().deriveFont(20.0f).deriveFont(Font.BOLD);
		lblResult.setFont(bigFont);
		
		txtInput = new JTextField (40);
		txtOutput = new JTextArea (8, 40);
		txtOutput.setEditable(false);
		txtOutput.setFocusable(false);
		
		Font medFont = txtInput.getFont().deriveFont(16.0f);
		txtInput.setFont(medFont);
		txtOutput.setFont(medFont);
		
		clearFrame();
		binPanel = new QuizProgressPanel();
		
		frame.add (lblResult, cc.xy(2,2));
		frame.add (txtOutput, cc.xy(2,4));
		frame.add (txtInput, cc.xyw(2,6,3));
		frame.add (binPanel, cc.xywh(4,2,1,3));
		
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
						closeQuiz();
					}
					else
					{
						nextWord();
					}
				}
			}
		});
		
		frame.pack();
		txtInput.requestFocusInWindow();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent we)
			{
				// make sure close get's called when clicking "x"
				// or ALT+F4
				close();
			}			
		});
		
		initPreferences();
		loadState();
		frame.setLocation(
				prefs.getInt(HiggPrefs.WIN_X),
				prefs.getInt(HiggPrefs.WIN_Y)
				);
		frame.setVisible(true);
	}

	private void initPreferences()
	{		
		prefs = new TypedProperties<HiggPrefs>(HiggPrefs.values());
		if (PREFERENCES.exists())
		{
			try
			{
				FileInputStream fs = new FileInputStream(PREFERENCES);
				prefs.load(fs);
				fs.close();
			} 
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
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
			chooser.setCurrentDirectory(
					prefs.getFile(HiggPrefs.LAST_USED_LESSONS_DIR));
			if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
			{
				prefs.setFile(HiggPrefs.LAST_USED_LESSONS_DIR, 
						chooser.getCurrentDirectory());
				File f = chooser.getSelectedFile();
				startQuiz (loadQuiz(f));
			}
		}
	};

	private Quiz loadQuiz (File f)
	{
		try
		{
			return new Quiz(f);
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(frame, "File read error",
					"Problem while opening lesson\n" + ex.getMessage(), 
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return null;
		}
	}
	
	private void closeQuiz()
	{
		if (quiz != null) 
		{
			writeLog();
			quiz = null;
			binPanel.setQuiz(null);
		}
	}
	
	private void startQuiz(Quiz newQuiz)
	{
		// stop logging current quiz, if it exists
		if (quiz != null) 
		{
			writeLog();
			quiz = null;
		}

		quiz = newQuiz;
		//TODO: this overrides bin option in lesson itself
		quiz.setBins(prefs.getInt(HiggPrefs.DEFAULT_BINS));
		binPanel.setQuiz (quiz);
		beginLog();
		nextWord();
	}
	
	private void writeLog()
	{
		Date end = new Date();
		
		long span = (end.getTime() - startTime.getTime()) / 1000L;
		long spanHours = span / 3600L;
		long spanMinutes = (span % 3600L) / 60L;
		long spanSeconds = (span % 60L);
		
		DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		String line = format.format(startTime);
		line += String.format (" #q: %4d ", quiz.getCounter() - startCounter);
		line += String.format(" dur.: %02d:%02d:%02d", spanHours, spanMinutes, spanSeconds);
		line += " " + quiz.getFile() + "\n";
		// ignore runs of less than 10.
		if (quiz.getCounter() - startCounter >= 10)
		{
			try
			{
				FileWriter writer = new FileWriter(LOGFILE, true);
				writer.write (line);
				writer.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	private void beginLog()
	{
		startCounter = quiz.getCounter();
		startTime = new Date();
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
		binPanel.repaint();
	}

	private void clearFrame()
	{
		txtOutput.setText ("");
		txtOutput.append ("No current Lesson.\n");
		txtOutput.append ("Go to file->new to start a new lesson.\n");
	}
	
	String inputMethod (String src)
	{
		String s = src;
		s = s.replaceAll("\\^e", "ê"); //TODO: define with \ uNNNN constants
		s = s.replaceAll("~n", "ñ");
		
		s = s.replaceAll("\"a", "ä");
		s = s.replaceAll("\"e", "ë");
		s = s.replaceAll("\"i", "ï");
		s = s.replaceAll("\"o", "ö");
		s = s.replaceAll("\"u", "ü");
		
		s = s.replaceAll("\"A", "Ä");
		s = s.replaceAll("\"E", "Ë");
		s = s.replaceAll("\"I", "Ï");
		s = s.replaceAll("\"O", "Ö");
		s = s.replaceAll("\"U", "Ü");
		
		s = s.replaceAll("`a", "à");
		s = s.replaceAll("`e", "è");
		s = s.replaceAll("`i", "ì");
		s = s.replaceAll("`o", "ò");
		s = s.replaceAll("`u", "ù");
		
		s = s.replaceAll("'a", "á");
		s = s.replaceAll("'e", "é");
		s = s.replaceAll("'i", "í");
		s = s.replaceAll("'o", "ó");
		s = s.replaceAll("'u", "ú");
		
		s = s.replaceAll("^\\?", "¿");
		s = s.replaceAll("^!", "¡");	    
	    
		return s;
	}
	
	private void checkAnswer()
	{
		//TODO: make use of input methods provided by java
		String myAnswer = inputMethod (txtInput.getText());
		txtOutput.setText ("");
		if (quiz.compareAnswer(myAnswer))
		{
			lblResult.setText("Correct");
			lblResult.setForeground(Color.GREEN);
			txtOutput.append("The answer was :\"" + quiz.getCorrectAnswer() + "\"");
		}
		else
		{
			lblResult.setText("Wrong");
			lblResult.setForeground(Color.RED);
			txtOutput.append("You answered :\"" + myAnswer + "\"\n");
			txtOutput.append("The answer was :\"" + quiz.getCorrectAnswer() + "\"");
		}
		if (quiz.hasHint())
		{
			txtOutput.append ("Hint: " + quiz.getHint());
		}
	}
	
	private void showResults()
	{
		ResultDlg dlg = new ResultDlg(frame);
		dlg.setQuiz (quiz);
		dlg.setVisible(true);
	}
	
	private class RestartAction extends AbstractAction
	{
		RestartAction()
		{
			super();
			putValue (NAME, "Restart");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			if (quiz != null)
			{
				// start quiz again with same file
				startQuiz (loadQuiz(quiz.getFile()));
			}
		}
	};

	private class OptionsAction extends AbstractAction
	{
		OptionsAction()
		{
			super();
			putValue (NAME, "Options");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			OptionsDlg dlg = new OptionsDlg(frame);
			dlg.setBins(prefs.getInt(HiggPrefs.DEFAULT_BINS));
			dlg.setVisible(true);
			if (!dlg.isCancelled())
			{
				prefs.setInt(HiggPrefs.DEFAULT_BINS, dlg.getBins()); 
				if (quiz != null) quiz.setBins(dlg.getBins());
			}
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
			close();
		}
	};

	private void loadState()
	{
		if (STATE.exists())
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream (STATE));
				Quiz newQuiz = (Quiz)ois.readObject();
				startQuiz (newQuiz);
				STATE.delete();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	private void close()
	{
		if (quiz != null)
		{
			try
			{
				ObjectOutputStream oos = new
					ObjectOutputStream(
							new FileOutputStream(
									STATE));
				oos.writeObject(quiz);
				oos.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			writeLog();
		}

		//TODO: for some reason always returns 0,0
		Point p = frame.getLocationOnScreen();

		prefs.setInt(HiggPrefs.WIN_X, p.x);
		prefs.setInt(HiggPrefs.WIN_Y, p.y);

		try
		{
			FileOutputStream fs = new FileOutputStream(PREFERENCES);
			prefs.store(fs, "Dr. Higgins preferences");
			fs.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		
		frame.dispose();		
	}
	
	private class StatsAction extends AbstractAction
	{
		StatsAction()
		{
			super();
			putValue (NAME, "Statistics");
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			StatsDlg dlg = new StatsDlg(frame);
			dlg.loadLogFile();
			dlg.setVisible(true);
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
