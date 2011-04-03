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

import java.awt.Frame;
import java.awt.Point;
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

import javax.swing.JOptionPane;

import nl.helixsoft.util.TypedProperties;

public class Engine 
{
	private final MainFrame mainFrame = new MainFrame(this);
	
	private QuizSession session = null;	
	private CourseModel model = null;
	
	private int startCounter;
	private Date startTime;

	public void run() 
	{
		initPreferences();
		mainFrame.createAndShowGui();
	}

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
	public static final File STATE = new File (APPDATADIR, "higgins.sto");
	private static final File PREFERENCES = new File (APPDATADIR, "higgins.props");

	public static ResourceBundle res = ResourceBundle.getBundle("nl.helixsoft.higgins.Strings");
	
	private TypedProperties<HiggPrefs> prefs;
	public TypedProperties<HiggPrefs> getPrefs() 
	{
		return prefs;
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

	void close()
	{
		if (session != null || model != null)
		{			
			try
			{
				State state = new State();
				state.session = session;
				if (model != null) 
				{
					state.courseFile = model.courseFile;
					model.saveCourse();
				}
								
				STATE.getParentFile().mkdirs();
				ObjectOutputStream oos = new
					ObjectOutputStream(
							new FileOutputStream(
									STATE));
				oos.writeObject(state);
				oos.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			writeLog();
		}

		Point p = mainFrame.getFrame().getLocationOnScreen();

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
		
		mainFrame.getFrame().dispose();		
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
		line += String.format (" #q: %4d ", session.getCounter() - startCounter);
		line += String.format(" dur.: %02d:%02d:%02d", spanHours, spanMinutes, spanSeconds);
		line += " " + session.getQuiz().getFile() + "\n";
		// ignore runs of less than 10.
		if (session.getCounter() - startCounter >= 10)
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
		startCounter = session.getCounter();
		startTime = new Date();
	}
	
	private void nextWord()
	{
		session.nextQuestion();
		mainFrame.refreshBins();
		mainFrame.showQuestion(session.getCounter(), session.getQuestion());
	}

	void checkAnswer(String myAnswer)
	{
		if (session != null && !session.isFinished())
		{
			int counter = session.getCounter();
			boolean correct = session.compareAnswer(myAnswer); 
			mainFrame.showAnswer(
					counter, 
					session.getQuestion(), 
					correct, 
					myAnswer,
					session.getCorrectAnswer(),
					session.getHint());
			
			if (model != null)
			{
				model.record(session.getCurrentWord(), correct);
			}
			
			if (session.isFinished())
			{
				writeLog();
				showResults();
				closeQuiz();
				if (model != null)
				{
					nextCourseSession();
				}
				else
				{
					mainFrame.startupMessage();
				}
			}
			else
			{
				nextWord();
			}
		}
	}
	
	private void showResults()
	{
		ResultDlg dlg = new ResultDlg(mainFrame.getFrame());
		dlg.setQuiz (session);
		dlg.setVisible(true);
	}

	public Frame getFrame() 
	{
		return mainFrame.getFrame();
	}

	public CourseModel getModel() 
	{
		return model;
	}

	public void nextCourseSession()
	{
		Quiz newLesson = model.createNewLesson();
		QuizSession session = new QuizSession(newLesson);
		startQuiz(session);		
	}

	private void closeQuiz()
	{
		if (session != null) 
		{
			writeLog();
			session = null;
			mainFrame.refreshBins();
		}
	}

	/**
	 * start quiz, but without re-initializing.
	 * So this can also be used to continue a stored quiz.
	 * 
	 * If any previously started quiz was not yet closed cleanly, it will be.
	 */
	public void startQuiz(QuizSession newQuiz)
	{
		mainFrame.clearOutput();
		// stop logging current quiz, if it exists
		if (session != null) 
		{
			writeLog();
			session = null;
		}

		session = newQuiz;
		session.setBins(prefs.getInt(HiggPrefs.DEFAULT_BINS));
		mainFrame.refreshBins();
		beginLog();
		nextWord();
	}

	public QuizSession getSession() 
	{
		return session;
	}

	public void newCourse() 
	{
		model = new CourseModel();
		mainFrame.viewCourseAction.setEnabled(true);
	}

	public void restartQuiz()
	{
		if (session != null)
		{
			// start quiz again with same file
			startQuiz (loadQuiz(session.getQuiz().getFile()));
		}
	}

	void loadState()
	{
		if (STATE.exists())
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream (STATE));
				State state = (State)ois.readObject();
				
				if (state.courseFile != null)
				{
					loadCourse(state.courseFile);
					
					if (state.session == null)
					{
						nextCourseSession();
					}
				}

				if (state.session != null)
				{
					QuizSession newSession = state.session;
					
					if (newSession.getQuiz().modifiedOnDisk())
					{
						// frame doesn't exist yet at this moment!
						int result = JOptionPane.showConfirmDialog(null, res.getString("QUIZ_CHANGED"), 
								res.getString("QUIZ_CHANGED_TITLE"), JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (result == JOptionPane.YES_OPTION)
						{
							// reload & re-initialize quiz
							Quiz quiz = Quiz.loadFromFile(newSession.getQuiz().getFile());
							newSession = new QuizSession (quiz);
						}
					}
					startQuiz (newSession);
				} 
				
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
			catch (ClassCastException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public QuizSession loadQuiz (File f)
	{
		try
		{
			return new QuizSession(Quiz.loadFromFile(f));
		}
		catch (IOException ex)
		{
			JOptionPane.showMessageDialog(getFrame(), "File read error",
					"Problem while opening lesson\n" + ex.getMessage(), 
					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return null;
		}
	}

	public void loadCourse(File f) throws IOException, ClassNotFoundException 
	{
		model = CourseModel.loadCourse(f);
		mainFrame.viewCourseAction.setEnabled(true);
	}

}
