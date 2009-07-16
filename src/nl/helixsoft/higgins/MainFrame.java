package nl.helixsoft.higgins;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

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
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
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
	public static ResourceBundle res = ResourceBundle.getBundle("nl.helixsoft.higgins.Strings");
	
	public static final File APPDATADIR;
	public static final File DEFAULT_LESSONS_DIR;
	public static final File INSTALLDIR;
	static
	{
		if (System.getProperty("os.name").startsWith("Win"))
		{
			APPDATADIR = new File (new File (System.getenv("appdata"), "HelixSoft"), "DrHiggins");
		}
		else
		{
			APPDATADIR = new File (System.getProperty("user.home"), ".higgins");
		}
		
		INSTALLDIR = new File (System.getProperty("user.dir"));
		File test = new File (INSTALLDIR, "lessons");
		if (test.exists())
		{
			DEFAULT_LESSONS_DIR = test;
		}
		else
		{
			DEFAULT_LESSONS_DIR = INSTALLDIR;
		}
	}
	
	public static final File LOGFILE = new File (APPDATADIR, "higgins.log");
	public static final File PREFERENCES = new File (APPDATADIR, "higgins.props");
	public static final File STATE = new File (APPDATADIR, "higgins.sto");

	private JFrame frame;
	private JTextField txtInput;
	private JTextArea txtOutput;
	private QuizProgressPanel binPanel;
	private JLabel lblResult;
	private TypedProperties<HiggPrefs> prefs;
	
	private int startCounter;
	private Date startTime;
	private InputMethod im;
	
	public void createAndShowGui()
	{
		initPreferences();

		frame = new JFrame("Dr. Higgins");
		frame.setLayout(new FormLayout(
				"3dlu, pref, 3dlu, pref, 3dlu",
				"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu"));
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
		
		boolean isInternationalInput = prefs.getBoolean(HiggPrefs.INTERNATIONAL_INPUT); 
		im = new InputMethod(txtInput);
		im.setCurrentSection(isInternationalInput ? "west-european" : null);
		final JCheckBox ckInputMethod;
		ckInputMethod = new JCheckBox("Enable international input", 
				prefs.getBoolean(HiggPrefs.INTERNATIONAL_INPUT));
		ckInputMethod.setFocusable(false);
		ckInputMethod.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae) 
			{
				im.setCurrentSection(
						ckInputMethod.isSelected() ?
								"west-european" : null);
				prefs.set(HiggPrefs.INTERNATIONAL_INPUT, 
						ckInputMethod.isSelected());
			}
		});
		
		binPanel = new QuizProgressPanel();
		
		frame.add (lblResult, cc.xy(2,2));
		frame.add (txtOutput, cc.xy(2,4));
		frame.add (ckInputMethod, cc.xy(2,6)); 
		frame.add (txtInput, cc.xyw(2,8,3));
		frame.add (binPanel, cc.xywh(4,2,1,5));
		
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
		
		clearFrame();
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
		JMenu file = new JMenu (res.getString("FILE"));
		file.add(new NewAction());
		file.add(new RestartAction());
		file.addSeparator();
		file.add(new OptionsAction());
		file.addSeparator();
		file.add(new ExitAction());
		JMenu view = new JMenu (res.getString("VIEW"));
		view.add(new StatsAction());
		JMenu help = new JMenu (res.getString("HELP"));
		help.add(new AboutAction());
		help.add(new HelpAction());
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
			putValue (NAME, res.getString("NEW"));
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
				prefs.set(HiggPrefs.LAST_USED_LESSONS_DIR, 
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
	
	/**
	 * start quiz, but without re-initializing.
	 * So this can also be used to continue a stored quiz.
	 * 
	 * If any previously started quiz was not yet closed cleanly, it will be.
	 */
	private void startQuiz(Quiz newQuiz)
	{
		txtOutput.setText("");
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
				LOGFILE.getParentFile().mkdirs();
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
		txtOutput.append (res.getString("QUESTION") + " #" + quiz.getCounter() 
				+ ": " + quiz.getQuestion() + "\n"); 
	}

	private void drawBins()
	{
		binPanel.repaint();
	}

	private void clearFrame()
	{
		lblResult.setText (" ");
		txtOutput.setText ("");
		txtOutput.append (res.getString("NO_CURRENT_LESSON") + "\n");
		txtOutput.append (res.getString("GO_TO_FILE") + "\n");
	}
	
	private void checkAnswer()
	{
		String myAnswer = txtInput.getText();
		txtOutput.setText ("");
		txtOutput.append(res.getString("QUESTION") + " #" + quiz.getCounter() 
				+ ": " + quiz.getQuestion() + "\n");
		if (quiz.compareAnswer(myAnswer))
		{
			lblResult.setText(res.getString("CORRECT"));
			lblResult.setForeground(Color.GREEN);
		}
		else
		{
			lblResult.setText(res.getString("WRONG"));
			lblResult.setForeground(Color.RED);
			txtOutput.append(res.getString("YOU_ANSWERED") + " \"" + myAnswer + "\"\n");
		}
		txtOutput.append(res.getString("THE_ANSWER_WAS") + " \"" + quiz.getCorrectAnswer() + "\"\n");
		if (quiz.hasHint())
		{
			txtOutput.append (quiz.getHint() + "\n");
		}
		txtOutput.append ("\n");
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
			putValue (NAME, res.getString("RESTART"));
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
			putValue (NAME, res.getString("OPTIONS"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			OptionsDlg dlg = new OptionsDlg(frame);
			dlg.setBins(prefs.getInt(HiggPrefs.DEFAULT_BINS));
			dlg.setVisible(true);
			if (!dlg.isCancelled())
			{
				prefs.set(HiggPrefs.DEFAULT_BINS, dlg.getBins()); 
				if (quiz != null) quiz.setBins(dlg.getBins());
			}
		}
	};

	private class ExitAction extends AbstractAction
	{
		ExitAction()
		{
			super();
			putValue (NAME, res.getString("EXIT"));
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
				
				long otherTimeStamp = newQuiz.getFile().lastModified();
				if (otherTimeStamp > newQuiz.getFileTimeStamp())
				{
					// frame doesn't exist yet at this moment!
					int result = JOptionPane.showConfirmDialog(null, res.getString("QUIZ_CHANGED"), 
							res.getString("QUIZ_CHANGED_TITLE"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (result == JOptionPane.YES_OPTION)
					{
						// reload & re-initialize quiz
						newQuiz = new Quiz (newQuiz.getFile());
					}
				}
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
				STATE.getParentFile().mkdirs();
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

		prefs.set(HiggPrefs.WIN_X, p.x);
		prefs.set(HiggPrefs.WIN_Y, p.y);

		try
		{
			PREFERENCES.getParentFile().mkdirs();
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
			putValue (NAME, res.getString("STATISTICS"));
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
			putValue (NAME, res.getString("ABOUT_DR_HIGGINS"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			AboutDlg dlg = new AboutDlg();
			dlg.createAndShowGUI(frame);
		}
	};

	private class HelpAction extends AbstractAction
	{
		HelpAction()
		{
			super();
			putValue (NAME, res.getString("HELP"));
		}
		
		public void actionPerformed(ActionEvent ae) 
		{
			BrowserLauncher bl;
			File helpFile = new File(INSTALLDIR, "doc/index.html");
			String error = null;
			try {
				bl = new BrowserLauncher(null);
				bl.openURLinBrowser(helpFile.toURI().toString());
			} catch (BrowserLaunchingInitializingException e) {
				error = res.getString("COULD_NOT_LAUNCH_BROWSER");
				e.printStackTrace();
			} catch (UnsupportedOperatingSystemException e) {
				error = res.getString("COULD_NOT_LAUNCH_BROWSER");
				e.printStackTrace();
			} catch (BrowserLaunchingExecutionException e) {
				error = res.getString("COULD_NOT_LAUNCH_BROWSER");
				e.printStackTrace();
			}
			if (error != null)
				JOptionPane.showMessageDialog(frame, error);
		}
	}
	

}
