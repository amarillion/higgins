package nl.helixsoft.higgins;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

/**
 * Handles keyboard shortcuts for international characters.
 * Intercepts keyboard event on a text field, and 
 * if they match a certain sequence, inserts a character in the text field.
 */
public class InputMethod implements KeyListener
{
	private InputMethod.State currentState;
	private String currentSection = "west-european"; 
	private String saved = "";
	
	public void setCurrentSection (String value)
	{
		if (currentSection != value)
		{
			flush();
			currentSection = value;
			currentState = startStates.get(currentSection);
		}
	}
	
	public String getCurrentSection()
	{
		return currentSection;
	}
	
	public Collection<String> getSections()
	{
		return startStates.keySet();
	}
	
	private static class MyKeyStroke
	{
		private boolean altPressed;
		private Character code;
		
		MyKeyStroke (Character code, boolean altPressed)
		{
			this.altPressed = altPressed;
			this.code = code;
		}
		
		@Override public boolean equals(Object other)
		{
			if (other instanceof MyKeyStroke)
			{
				MyKeyStroke mks = (MyKeyStroke)other;
				return altPressed == mks.altPressed && 
						code == mks.code;
			}
			return false;
		}			
		
		@Override public int hashCode()
		{
			return 5 * code.hashCode() + (altPressed ? 3 : 0);
		}
	}
	
	/**
	 * Instert all saved-up characters and reset to the starting state.
	 */
	public void flush()
	{
		// insert saved characters
		insertAtCursor (saved);
		saved = "";
		// reset state to beginning
		currentState = startStates.get(currentSection);
	}
	
	public void insertAtCursor(String value)
	{
		try {
			txt.getDocument().insertString(
					txt.getCaretPosition(), value, null);
			// experimental, alternative to inserting text directly
//			txt.dispatchEvent(
//					new KeyEvent(ke.getComponent(), 
//							KeyEvent.KEY_TYPED,
//							ke.getWhen(),
//							ke.getModifiersEx(),
//							KeyEvent.VK_UNDEFINED,
//							currentState.result.charAt(0)
//							)
//			);
		} 
		catch (BadLocationException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void keyPressed(KeyEvent ke) 
	{
	}

	public void keyReleased(KeyEvent ke) 
	{		
	}
	
	public void keyTyped(KeyEvent ke) 
	{
		if (currentState == null) return; // input method disabled
		MyKeyStroke currentKeyStroke = 
			new MyKeyStroke (ke.getKeyChar(), (ke.getModifiersEx() & InputEvent.ALT_GRAPH_DOWN_MASK) > 0);
		if (currentState.stateMap.containsKey(currentKeyStroke))
		{
			currentState = currentState.stateMap.get (currentKeyStroke);
			saved += ke.getKeyChar();
			if (currentState.result != null)
			{
				insertAtCursor(currentState.result);
				currentState = startStates.get(currentSection);
				saved = "";
			}
			ke.consume();
		}
		else
		{
			flush();
		}
	};
	
	private static class State
	{
		Map<MyKeyStroke, InputMethod.State> stateMap = new HashMap<MyKeyStroke, InputMethod.State>();
		String result = null;
		
		public void addState(List<Character> codes, String result)
		{
			Character first = codes.get(0);
			MyKeyStroke keyStroke = new MyKeyStroke (first, false);
			
			InputMethod.State next = stateMap.get (keyStroke);
			if (next == null)
			{
				next = new State();
				stateMap.put (keyStroke, next);
			}
			
			if (codes.size() <= 1)
			{
				next.result = result;
			}
			else
			{
				next.addState (codes.subList(1, codes.size()), result);
			}
		}
	}
	
	Map <String, InputMethod.State> startStates = new HashMap<String, InputMethod.State>();			
	
	public void addSequence (String section, List<Character> codes, String result)
	{
		InputMethod.State start = startStates.get(section);
		if (start == null) 
		{
			start = new State();
			startStates.put (section, start);
		}
		start.addState (codes, result);
	}

	public void addSequence (String section, MyKeyStroke keyStroke, String result)
	{
		InputMethod.State start = startStates.get(section);
		if (start == null) 
		{
			start = new State();
			startStates.put (section, start);
		}
		start.result = result;
		State next = new State();
		next.result = result;
		start.stateMap.put(keyStroke, next);
	}

	private final JTextField txt;
	
	InputMethod (JTextField txt)
	{
		this.txt = txt;
		txt.addKeyListener(this);
		
		addSequence ("west-european", Arrays.asList('"', 'a'), "\u00e4");
		addSequence ("west-european", Arrays.asList('"', 'e'), "\u00eb");
		addSequence ("west-european", Arrays.asList('"', 'i'), "\u00ef");
		addSequence ("west-european", Arrays.asList('"', 'o'), "\u00f6");
		addSequence ("west-european", Arrays.asList('"', 'u'), "\u00fc");
		addSequence ("west-european", Arrays.asList('"', 'A'), "\u00c4");
		addSequence ("west-european", Arrays.asList('"', 'E'), "\u00cb");
		addSequence ("west-european", Arrays.asList('"', 'I'), "\u00cf");
		addSequence ("west-european", Arrays.asList('"', 'O'), "\u00d6");
		addSequence ("west-european", Arrays.asList('"', 'U'), "\u00dc");
		addSequence ("west-european", Arrays.asList('"', ' '), "\"");
		addSequence ("west-european", Arrays.asList('^', 'a'), "\u00e2");
		addSequence ("west-european", Arrays.asList('^', 'e'), "\u00ea");
		addSequence ("west-european", Arrays.asList('^', 'i'), "\u00ee");
		addSequence ("west-european", Arrays.asList('^', 'o'), "\u00f4");
		addSequence ("west-european", Arrays.asList('^', 'u'), "\u00fb");
		addSequence ("west-european", Arrays.asList('^', 'A'), "\u00c2");
		addSequence ("west-european", Arrays.asList('^', 'E'), "\u00ca");
		addSequence ("west-european", Arrays.asList('^', 'I'), "\u00ce");
		addSequence ("west-european", Arrays.asList('^', 'O'), "\u00d4");
		addSequence ("west-european", Arrays.asList('^', 'U'), "\u00db");
		addSequence ("west-european", Arrays.asList('^', ' '), "^");
		addSequence ("west-european", Arrays.asList('~', 'n'), "\u00f1");
		addSequence ("west-european", Arrays.asList('~', 'N'), "\u00d1");
		addSequence ("west-european", Arrays.asList('~', ' '), "~");
		addSequence ("west-european", Arrays.asList('`', 'a'), "\u00e0");
		addSequence ("west-european", Arrays.asList('`', 'e'), "\u00e8");
		addSequence ("west-european", Arrays.asList('`', 'i'), "\u00ec");
		addSequence ("west-european", Arrays.asList('`', 'o'), "\u00f2");
		addSequence ("west-european", Arrays.asList('`', 'u'), "\u00f9");
		addSequence ("west-european", Arrays.asList('`', 'A'), "\u00c0");
		addSequence ("west-european", Arrays.asList('`', 'E'), "\u00c8");
		addSequence ("west-european", Arrays.asList('`', 'I'), "\u00cc");
		addSequence ("west-european", Arrays.asList('`', 'O'), "\u00d2");
		addSequence ("west-european", Arrays.asList('`', 'U'), "\u00d9");
		addSequence ("west-european", Arrays.asList('`', ' '), "`");
		addSequence ("west-european", Arrays.asList('\'', 'a'), "\u00e1");
		addSequence ("west-european", Arrays.asList('\'', 'e'), "\u00e9");
		addSequence ("west-european", Arrays.asList('\'', 'i'), "\u00ed");
		addSequence ("west-european", Arrays.asList('\'', 'o'), "\u00f3");
		addSequence ("west-european", Arrays.asList('\'', 'u'), "\u00fa");
		addSequence ("west-european", Arrays.asList('\'', 'A'), "\u00c1");
		addSequence ("west-european", Arrays.asList('\'', 'E'), "\u00c9");
		addSequence ("west-european", Arrays.asList('\'', 'I'), "\u00cd");
		addSequence ("west-european", Arrays.asList('\'', 'O'), "\u00d3");
		addSequence ("west-european", Arrays.asList('\'', 'U'), "\u00da");			
		addSequence ("west-european", Arrays.asList('\'', ' '), "'");
		addSequence ("west-european", Arrays.asList(',', 'c'), "\u00e7");
		addSequence ("west-european", Arrays.asList(',', 'C'), "\u00c7");
		addSequence ("west-european", Arrays.asList(',', ' '), ",");
		addSequence ("west-european", Arrays.asList('/', 'o'), "\u00f8");
		addSequence ("west-european", Arrays.asList('/', 'O'), "\u00d8");
		addSequence ("west-european", Arrays.asList('/', ' '), "/");
		
		addSequence ("west-european", new MyKeyStroke ('?', true), "\u00bf");
		addSequence ("west-european", new MyKeyStroke ('!', true), "\u00a1");

		// polish letters
		addSequence ("west-european", new MyKeyStroke ('a', true), "\u0105");
		addSequence ("west-european", new MyKeyStroke ('e', true), "\u0119");
		addSequence ("west-european", new MyKeyStroke ('c', true), "\u0107");
		addSequence ("west-european", new MyKeyStroke ('l', true), "\u0142");
		addSequence ("west-european", new MyKeyStroke ('n', true), "\u0144");
		addSequence ("west-european", new MyKeyStroke ('o', true), "\u00f3");
		addSequence ("west-european", new MyKeyStroke ('s', true), "\u015b");
		addSequence ("west-european", new MyKeyStroke ('z', true), "\u017c");
		addSequence ("west-european", new MyKeyStroke ('x', true), "\u017a");

		currentState = startStates.get(currentSection);
	}
}